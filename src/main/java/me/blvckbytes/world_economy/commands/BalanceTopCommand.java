package me.blvckbytes.world_economy.commands;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.*;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BalanceTopCommand implements CommandExecutor, TabCompleter {

  private final OfflineLocationReader offlineLocationReader;
  private final WorldGroupRegistry worldGroupRegistry;
  private final TopListRegistry topListRegistry;
  private final WorldEconomyProvider economyProvider;
  private final ConfigKeeper<MainSection> config;

  public BalanceTopCommand(
    OfflineLocationReader offlineLocationReader,
    WorldGroupRegistry worldGroupRegistry,
    TopListRegistry topListRegistry,
    WorldEconomyProvider economyProvider,
    ConfigKeeper<MainSection> config
  ) {
    this.offlineLocationReader = offlineLocationReader;
    this.worldGroupRegistry = worldGroupRegistry;
    this.topListRegistry = topListRegistry;
    this.economyProvider = economyProvider;
    this.config = config;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!PluginPermission.COMMAND_BALTOP.has(sender)) {
      sender.sendMessage(config.rootSection.playerMessages.missingPermissionCommandBalTop.stringify(
        config.rootSection.builtBaseEnvironment
      ));

      return true;
    }

    var canSpecifyGroup = PluginPermission.COMMAND_BALTOP_GROUP.has(sender);
    WorldGroup targetWorldGroup;

    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        sender.sendMessage(config.rootSection.playerMessages.playerOnlyBalTopCommandNoWorldGroup.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }

      targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

      if (targetWorldGroup == null) {
        // TODO: Add name of current world to evaluation-environment on all uses
        sender.sendMessage(config.rootSection.playerMessages.notInAnyWorldGroupSelf.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }
    }

    else if (args.length == 1) {
      if (!canSpecifyGroup) {
        sender.sendMessage(config.rootSection.playerMessages.missingPermissionCommandBalTopOtherGroups.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }

      targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[0]);

      if (targetWorldGroup == null) {
        sender.sendMessage(config.rootSection.playerMessages.unknownWorldGroup.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("name", args[0])
            .build()
        ));

        return true;
      }
    }

    else {
      if (canSpecifyGroup) {
        sender.sendMessage(config.rootSection.playerMessages.usageBalTopCommandOtherGroups.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("label", label)
            .withStaticVariable("group_names", worldGroupRegistry.createSuggestions(null))
            .build()
        ));
      }

      sender.sendMessage(config.rootSection.playerMessages.usageBalTopCommand.stringify(
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("label", label)
          .build()
      ));

      return true;
    }

    var topListEntries = topListRegistry.getTopList(targetWorldGroup);

    config.rootSection.playerMessages.balTopScreen.asList(
      ScalarType.STRING,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("group", targetWorldGroup.displayName().stringify(config.rootSection.builtBaseEnvironment))
        .withStaticVariable("entries", makeTopListMap(topListEntries))
        .build()
    ).forEach(sender::sendMessage);

    return true;
  }

  private Map<String, String> makeTopListMap(List<TopListEntry> entries) {
    var result = new LinkedHashMap<String, String>();

    for (var entry : entries)
      result.put(entry.holder().getName(), economyProvider.format(entry.balance()));

    return result;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1) {
      if (sender instanceof Player player && !PluginPermission.COMMAND_BALTOP_GROUP.has(player))
        return List.of();

      return worldGroupRegistry.createSuggestions(args[0]);
    }

    return List.of();
  }
}
