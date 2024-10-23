package me.blvckbytes.world_economy.commands;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.world_economy.*;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class BalancesCommand implements CommandExecutor, TabCompleter {

  private final EconomyDataRegistry dataRegistry;
  private final WorldEconomyProvider economyProvider;
  private final WorldGroupRegistry worldGroupRegistry;
  private final OfflinePlayerCache offlinePlayerCache;
  private final ConfigKeeper<MainSection> config;

  public BalancesCommand(
    EconomyDataRegistry dataRegistry,
    WorldEconomyProvider economyProvider,
    WorldGroupRegistry worldGroupRegistry,
    OfflinePlayerCache offlinePlayerCache,
    ConfigKeeper<MainSection> config
  ) {
    this.dataRegistry = dataRegistry;
    this.economyProvider = economyProvider;
    this.worldGroupRegistry = worldGroupRegistry;
    this.offlinePlayerCache = offlinePlayerCache;
    this.config = config;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    var canViewOthers = PluginPermission.COMMAND_BALANCES_OTHER.has(sender);

    if (!PluginPermission.COMMAND_BALANCES.has(sender)) {
      sender.sendMessage(config.rootSection.playerMessages.missingPermissionBalancesSelfCommand.stringify(
        config.rootSection.builtBaseEnvironment
      ));

      return true;
    }

    EconomyAccountRegistry targetRegistry;

    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        sender.sendMessage(config.rootSection.playerMessages.playerOnlyBalancesSelfCommand.stringify(
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

    else if (args.length == 1) {
      var target = offlinePlayerCache.getByName(args[0]);

      if (target != sender && !canViewOthers) {
        sender.sendMessage(config.rootSection.playerMessages.missingPermissionBalancesOtherCommand.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }

      targetRegistry = dataRegistry.getAccountRegistry(target);

      if (targetRegistry == null) {
        sender.sendMessage(config.rootSection.playerMessages.couldNotLoadAccountOther.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("name", target.getName())
            .build()
        ));

        return true;
      }
    }

    else {
      BukkitEvaluable message;

      if (canViewOthers)
        message = config.rootSection.playerMessages.usageBalancesCommandOther;
      else
        message = config.rootSection.playerMessages.usageBalancesCommandSelf;

      sender.sendMessage(message.stringify(
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("label", label)
          .build()
      ));

      return true;
    }

    config.rootSection.playerMessages.balancesScreen.asList(
      ScalarType.STRING,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("holder", targetRegistry.getHolder().getName())
        .withStaticVariable("balances", makeBalancesLines(targetRegistry))
        .build()
    ).forEach(sender::sendMessage);

    return true;
  }

  private Set<Map.Entry<String, String>> makeBalancesLines(EconomyAccountRegistry accountRegistry) {
    var result = new LinkedHashMap<String, String>();

    for (var worldGroup : worldGroupRegistry.getWorldGroups()) {
      var displayName = worldGroup.displayName().stringify(GPEEE.EMPTY_ENVIRONMENT);
      var accountBalance = accountRegistry.getAccount(worldGroup).getBalance();
      result.put(displayName, economyProvider.format(accountBalance));
    }

    return result.entrySet();
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1 && PluginPermission.COMMAND_BALANCES_OTHER.has(sender))
      return offlinePlayerCache.createSuggestions(args[0]);

    return List.of();
  }
}
