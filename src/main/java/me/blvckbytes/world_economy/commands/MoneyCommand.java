package me.blvckbytes.world_economy.commands;

import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.*;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class MoneyCommand implements CommandExecutor, TabCompleter {

  private final OfflinePlayerCache offlinePlayerCache;
  private final EconomyDataRegistry economyDataRegistry;
  private final OfflineLocationReader offlineLocationReader;
  private final WorldGroupRegistry worldGroupRegistry;
  private final WorldEconomyProvider economyProvider;
  private final ConfigKeeper<MainSection> config;

  public MoneyCommand(
    OfflinePlayerCache offlinePlayerCache,
    EconomyDataRegistry economyDataRegistry,
    OfflineLocationReader offlineLocationReader,
    WorldGroupRegistry worldGroupRegistry,
    WorldEconomyProvider economyProvider,
    ConfigKeeper<MainSection> config
  ) {
    this.offlinePlayerCache = offlinePlayerCache;
    this.economyDataRegistry = economyDataRegistry;
    this.offlineLocationReader = offlineLocationReader;
    this.worldGroupRegistry = worldGroupRegistry;
    this.economyProvider = economyProvider;
    this.config = config;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!PluginPermission.COMMAND_MONEY.has(sender)) {
      sender.sendMessage(config.rootSection.playerMessages.missingPermissionMoneyCommand.stringify(
        config.rootSection.builtBaseEnvironment
      ));

      return true;
    }

    MoneyAction action;
    EconomyAccountRegistry targetAccountRegistry;
    double amount;
    WorldGroup targetWorldGroup;

    if (args.length == 3 || args.length == 4) {
      if ((action = MoneyAction.getByName(args[0])) == null) {
        sender.sendMessage(config.rootSection.playerMessages.unknownMoneyCommandAction.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("input", args[0])
            .withStaticVariable("actions", MoneyAction.names)
            .build()
        ));

        return true;
      }

      var targetPlayer = offlinePlayerCache.getByName(args[1]);
      targetAccountRegistry = economyDataRegistry.getAccountRegistry(targetPlayer);

      if (targetAccountRegistry == null) {
        sender.sendMessage(config.rootSection.playerMessages.couldNotLoadAccountOther.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("name", targetPlayer.getName())
            .build()
        ));

        return true;
      }

      try {
        amount = Double.parseDouble(args[2]);
      } catch (NumberFormatException e) {
        sender.sendMessage(config.rootSection.playerMessages.argumentIsNotADouble.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("value", args[2])
            .build()
        ));

        return true;
      }

      if (amount <= 0) {
        sender.sendMessage(config.rootSection.playerMessages.argumentIsNotStrictlyPositive.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("value", args[2])
            .build()
        ));

        return true;
      }

      if (args.length == 3) {
        if (!(sender instanceof Player player)) {
          sender.sendMessage(config.rootSection.playerMessages.playerOnlyMoneyCommandNoWorldGroup.stringify(
            config.rootSection.builtBaseEnvironment
          ));

          return false;
        }

        targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

        if (targetWorldGroup == null) {
          sender.sendMessage(config.rootSection.playerMessages.notInAnyWorldGroupSelf.stringify(
            config.rootSection.builtBaseEnvironment
          ));

          return true;
        }
      }

      else {
        targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[3]);

        if (targetWorldGroup == null) {
          sender.sendMessage(config.rootSection.playerMessages.unknownWorldGroup.stringify(
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("name", args[3])
              .build()
          ));

          return true;
        }
      }
    }

    else {
      sender.sendMessage(config.rootSection.playerMessages.usageMoneyCommand.stringify(
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("label", label)
          .withStaticVariable("actions", MoneyAction.names)
          .withStaticVariable("group_names", worldGroupRegistry.createSuggestions(null))
          .build()
      ));

      return true;
    }

    var targetAccount = targetAccountRegistry.getAccount(targetWorldGroup);

    String executorName;

    if (sender instanceof Player player)
      executorName = player.getName();
    else {
      executorName = config.rootSection.playerMessages.moneyCommandConsoleName.stringify(
        config.rootSection.builtBaseEnvironment
      );
    }

    var actionEnvironmentBase = config.rootSection.getBaseEnvironment()
      .withStaticVariable("target_old_balance", economyProvider.format(targetAccount.getBalance()))
      .withStaticVariable("amount", economyProvider.format(amount))
      .withStaticVariable("group", targetWorldGroup.displayName().stringify(config.rootSection.builtBaseEnvironment))
      .withStaticVariable("target_name", targetAccountRegistry.getHolder().getName())
      .withStaticVariable("executor_name", executorName)
      .withStaticVariable("executor_name", executorName)
      .withStaticVariable("balance_max", economyProvider.format(config.rootSection.economy.maxMoney))
      .withStaticVariable("balance_min", economyProvider.format(config.rootSection.economy.minMoney));

    Player targetPlayer;

    switch (action) {
      case Add -> {
        if (!targetAccount.deposit(amount)) {
          sender.sendMessage(config.rootSection.playerMessages.moneyAddExceedsReceiversBalance.stringify(
            actionEnvironmentBase.build()
          ));

          return true;
        }
      }

      case Remove -> {
        if (!targetAccount.withdraw(amount)) {
          sender.sendMessage(config.rootSection.playerMessages.moneyRemoveExceedsReceiversBalance.stringify(
            actionEnvironmentBase.build()
          ));

          return true;
        }
      }

      case Set -> {
        if (!targetAccount.set(amount)) {
          sender.sendMessage(config.rootSection.playerMessages.moneySetExceedsReceiversBalance.stringify(
            actionEnvironmentBase.build()
          ));

          return true;
        }
      }
    }

    var actionEnvironment = actionEnvironmentBase
      .withStaticVariable("target_new_balance", economyProvider.format(targetAccount.getBalance()))
      .build();

    BukkitEvaluable executorMessage;
    BukkitEvaluable targetMessage;

    switch (action) {
      case Add -> {
        executorMessage = config.rootSection.playerMessages.moneyCommandAddExecutor;
        targetMessage = config.rootSection.playerMessages.moneyCommandAddTarget;
      }

      case Remove -> {
        executorMessage = config.rootSection.playerMessages.moneyCommandRemoveExecutor;
        targetMessage = config.rootSection.playerMessages.moneyCommandRemoveTarget;
      }

      case Set -> {
        executorMessage = config.rootSection.playerMessages.moneyCommandSetExecutor;
        targetMessage = config.rootSection.playerMessages.moneyCommandSetTarget;
      }

      default -> {
        return true;
      }
    }

    sender.sendMessage(executorMessage.stringify(actionEnvironment));

    if ((targetPlayer = targetAccountRegistry.getHolder().getPlayer()) != null)
      targetPlayer.sendMessage(targetMessage.stringify(actionEnvironment));

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (!PluginPermission.COMMAND_MONEY.has(sender))
      return List.of();

    if (args.length == 1)
      return MoneyAction.names;

    if (args.length == 2)
      return offlinePlayerCache.createSuggestions(args[1]);

    if (args.length == 4)
      return worldGroupRegistry.createSuggestions(args[3]);

    return List.of();
  }
}
