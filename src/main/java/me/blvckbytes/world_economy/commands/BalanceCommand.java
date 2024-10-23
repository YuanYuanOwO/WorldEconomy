package me.blvckbytes.world_economy.commands;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.world_economy.*;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class BalanceCommand implements CommandExecutor, TabCompleter {

  private final EconomyDataRegistry dataRegistry;
  private final WorldEconomyProvider economyProvider;
  private final WorldGroupRegistry worldGroupRegistry;
  private final OfflineLocationReader offlineLocationReader;
  private final OfflinePlayerCache offlinePlayerCache;
  private final ConfigKeeper<MainSection> config;

  public BalanceCommand(
    EconomyDataRegistry dataRegistry,
    WorldEconomyProvider economyProvider,
    WorldGroupRegistry worldGroupRegistry,
    OfflineLocationReader offlineLocationReader,
    OfflinePlayerCache offlinePlayerCache,
    ConfigKeeper<MainSection> config
  ) {
    this.dataRegistry = dataRegistry;
    this.economyProvider = economyProvider;
    this.worldGroupRegistry = worldGroupRegistry;
    this.offlineLocationReader = offlineLocationReader;
    this.offlinePlayerCache = offlinePlayerCache;
    this.config = config;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!PluginPermission.COMMAND_BALANCE.has(sender)) {
      sender.sendMessage(config.rootSection.playerMessages.missingPermissionBalanceSelfCommand.stringify(
        config.rootSection.builtBaseEnvironment
      ));

      return true;
    }

    var canViewOthers = PluginPermission.COMMAND_BALANCE_OTHER.has(sender);

    EconomyAccountRegistry targetRegistry;
    WorldGroup targetWorldGroup;

    if (args.length == 0 || args.length == 1) {
      if (!(sender instanceof Player player)) {
        sender.sendMessage(config.rootSection.playerMessages.playerOnlyBalanceSelfCommand.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }

      targetRegistry = dataRegistry.getAccountRegistry(player);

      if (targetRegistry == null) {
        sender.sendMessage(config.rootSection.playerMessages.couldNotLoadAccountSelf.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }
    }

    else if (args.length == 2) {
      var targetPlayer = offlinePlayerCache.getByName(args[1]);

      if (targetPlayer != sender && !canViewOthers) {
        sender.sendMessage(config.rootSection.playerMessages.missingPermissionBalanceOtherCommand.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }

      targetRegistry = dataRegistry.getAccountRegistry(targetPlayer);

      if (targetRegistry == null) {
        sender.sendMessage(config.rootSection.playerMessages.couldNotLoadAccountOther.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("name", targetPlayer.getName())
            .build()
        ));

        return true;
      }
    }

    else {
      if (!canViewOthers) {
        sender.sendMessage(config.rootSection.playerMessages.usageBalanceCommandSelf.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }

      sender.sendMessage(config.rootSection.playerMessages.usageBalanceCommandOther.stringify(
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("label", label)
          .withStaticVariable("group_names", worldGroupRegistry.createSuggestions(null))
          .build()
      ));

      return true;
    }

    if (args.length == 0) {
      targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(targetRegistry.getHolder());

      if (targetWorldGroup == null) {
        sender.sendMessage(config.rootSection.playerMessages.notInAnyWorldGroupSelf.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }
    }

    else {
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

    var message = (
      targetRegistry.getHolder() == sender
      ? config.rootSection.playerMessages.balanceMessageSelf
      : config.rootSection.playerMessages.balanceMessageOther
    );

    sender.sendMessage(message.stringify(
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("holder", targetRegistry.getHolder().getName())
        .withStaticVariable("balance", economyProvider.format(targetRegistry.getAccount(targetWorldGroup).getBalance()))
        .withStaticVariable("group", targetWorldGroup.displayName().stringify(GPEEE.EMPTY_ENVIRONMENT))
        .build()
    ));

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (!PluginPermission.COMMAND_BALANCE.has(sender))
      return List.of();

    if (args.length == 1)
      return worldGroupRegistry.createSuggestions(args[0]);

    if (args.length == 2 && PluginPermission.COMMAND_BALANCE_OTHER.has(sender))
      return offlinePlayerCache.createSuggestions(args[1]);

    return List.of();
  }
}
