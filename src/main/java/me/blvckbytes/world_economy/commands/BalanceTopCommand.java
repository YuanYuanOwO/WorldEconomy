package me.blvckbytes.world_economy.commands;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
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

public class BalanceTopCommand implements CommandExecutor, TabCompleter {

  private final OfflineLocationReader offlineLocationReader;
  private final EconomyDataRegistry economyDataRegistry;
  private final WorldGroupRegistry worldGroupRegistry;
  private final WorldEconomyProvider economyProvider;
  private final ConfigKeeper<MainSection> config;

  public BalanceTopCommand(
    OfflineLocationReader offlineLocationReader,
    EconomyDataRegistry economyDataRegistry,
    WorldGroupRegistry worldGroupRegistry,
    WorldEconomyProvider economyProvider,
    ConfigKeeper<MainSection> config
  ) {
    this.offlineLocationReader = offlineLocationReader;
    this.economyDataRegistry = economyDataRegistry;
    this.worldGroupRegistry = worldGroupRegistry;
    this.economyProvider = economyProvider;
    this.config = config;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    BukkitEvaluable message;

    if (!PluginPermission.COMMAND_BALTOP.has(sender)) {
      if ((message = config.rootSection.playerMessages.missingPermissionCommandBalTop) != null)
        message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

      return true;
    }

    var canSpecifyGroup = PluginPermission.COMMAND_BALTOP_GROUP.has(sender);
    WorldGroup targetWorldGroup;

    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        if ((message = config.rootSection.playerMessages.playerOnlyBalTopCommandNoWorldGroup) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

        return true;
      }

      targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

      if (targetWorldGroup == null) {
        // TODO: Add name of current world to evaluation-environment on all uses
        if ((message = config.rootSection.playerMessages.notInAnyWorldGroupSelf) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

        return true;
      }
    } else if (args.length == 1) {
      if (!canSpecifyGroup) {
        if ((message = config.rootSection.playerMessages.missingPermissionCommandBalTopOtherGroups) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

        return true;
      }

      targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[0]);

      if (targetWorldGroup == null) {
        if ((message = config.rootSection.playerMessages.unknownWorldGroup) != null) {
          message.sendMessage(
            sender,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("name", args[0])
              .build()
          );
        }

        return true;
      }
    } else {
      if (canSpecifyGroup) {
        if ((message = config.rootSection.playerMessages.usageBalTopCommandOtherGroups) != null) {
          message.sendMessage(
            sender,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("label", label)
              .withStaticVariable("group_names", worldGroupRegistry.createSuggestions(null))
              .build()
          );
        }
      }

      if ((message = config.rootSection.playerMessages.usageBalTopCommand) != null) {
        message.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("label", label)
            .build()
        );
      }

      return true;
    }

    var accountRegistry = economyDataRegistry.getAccountRegistry(targetWorldGroup);
    var entries = new LinkedHashMap<String, String>();

    for (var account : accountRegistry.getTopAccounts(config.rootSection.economy.topListSize))
      entries.put(account.holder.getName(), economyProvider.format(account.getBalance()));

    if ((message = config.rootSection.playerMessages.balTopScreen) != null) {
      message.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("group", targetWorldGroup.displayName().asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment))
          .withStaticVariable("entries", entries)
          .build()
      );
    }

    return true;
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
