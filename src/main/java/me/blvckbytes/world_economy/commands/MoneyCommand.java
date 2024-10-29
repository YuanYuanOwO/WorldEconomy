package me.blvckbytes.world_economy.commands;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.*;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.OfflinePlayer;
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
    BukkitEvaluable message;

    if (!PluginPermission.COMMAND_MONEY.has(sender)) {
      if ((message = config.rootSection.playerMessages.missingPermissionMoneyCommand) != null)
        message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

      return true;
    }

    MoneyAction action;
    OfflinePlayer targetPlayer;
    double amount;
    WorldGroup targetWorldGroup;

    if (args.length == 3 || args.length == 4) {
      if ((action = MoneyAction.getByName(args[0])) == null) {
        if ((message = config.rootSection.playerMessages.unknownMoneyCommandAction) != null) {
          message.sendMessage(
            sender,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("input", args[0])
              .withStaticVariable("actions", MoneyAction.names)
              .build()
          );
        }

        return true;
      }

      targetPlayer = offlinePlayerCache.getByName(args[1]);

      try {
        amount = Double.parseDouble(args[2]);
      } catch (NumberFormatException e) {
        if ((message = config.rootSection.playerMessages.argumentIsNotADouble) != null) {
          message.sendMessage(
            sender,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("value", args[2])
              .build()
          );
        }

        return true;
      }

      if (amount <= 0) {
        if ((message = config.rootSection.playerMessages.argumentIsNotStrictlyPositive) != null) {
          message.sendMessage(
            sender,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("value", args[2])
              .build()
          );
        }

        return true;
      }

      if (args.length == 3) {
        if (!(sender instanceof Player player)) {
          if ((message = config.rootSection.playerMessages.playerOnlyMoneyCommandNoWorldGroup) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

          return false;
        }

        targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

        if (targetWorldGroup == null) {
          if ((message = config.rootSection.playerMessages.notInAnyWorldGroupSelf) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

          return true;
        }
      }

      else {
        targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[3]);

        if (targetWorldGroup == null) {
          if ((message = config.rootSection.playerMessages.unknownWorldGroup) != null) {
            message.sendMessage(
              sender,
              config.rootSection.getBaseEnvironment()
                .withStaticVariable("name", args[3])
                .build()
            );
          }

          return true;
        }
      }
    }

    else {
      if ((message = config.rootSection.playerMessages.usageMoneyCommand) != null) {
        message.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("label", label)
            .withStaticVariable("actions", MoneyAction.names)
            .withStaticVariable("group_names", worldGroupRegistry.createSuggestions(null))
            .build()
        );
      }

      return true;
    }

    var accountRegistry = economyDataRegistry.getAccountRegistry(targetWorldGroup);
    var targetAccount = accountRegistry.getAccount(targetPlayer);

    if (targetAccount == null) {
      if ((message = config.rootSection.playerMessages.couldNotLoadAccountOther) != null) {
        message.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("name", targetPlayer.getName())
            .build()
        );
      }

      return true;
    }

    String executorName;

    if (sender instanceof Player player)
      executorName = player.getName();
    else {
      if ((message = config.rootSection.playerMessages.moneyCommandConsoleName) != null)
        executorName = message.asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment);
      else
        executorName = "Console";
    }

    var actionEnvironmentBase = config.rootSection.getBaseEnvironment()
      .withStaticVariable("target_old_balance", economyProvider.format(targetAccount.getBalance()))
      .withStaticVariable("amount", economyProvider.format(amount))
      .withStaticVariable("group", targetWorldGroup.displayName().asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment))
      .withStaticVariable("target_name", targetPlayer.getName())
      .withStaticVariable("executor_name", executorName)
      .withStaticVariable("executor_name", executorName)
      .withStaticVariable("balance_max", economyProvider.format(config.rootSection.economy.maxMoney))
      .withStaticVariable("balance_min", economyProvider.format(config.rootSection.economy.minMoney));

    switch (action) {
      case Add -> {
        if (!targetAccount.deposit(amount)) {
          if ((message = config.rootSection.playerMessages.moneyAddExceedsReceiversBalance) != null)
            message.sendMessage(sender, actionEnvironmentBase.build());

          return true;
        }
      }

      case Remove -> {
        if (!targetAccount.withdraw(amount)) {
          if ((message = config.rootSection.playerMessages.moneyRemoveExceedsReceiversBalance) != null)
            message.sendMessage(sender, actionEnvironmentBase.build());

          return true;
        }
      }

      case Set -> {
        if (!targetAccount.set(amount)) {
          if ((message = config.rootSection.playerMessages.moneySetExceedsReceiversBalance) != null)
            message.sendMessage(sender, actionEnvironmentBase.build());

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

    if (executorMessage != null)
      executorMessage.sendMessage(sender, actionEnvironment);

    Player messageReceiver;

    if (targetPlayer != sender && (messageReceiver = targetPlayer.getPlayer()) != null && targetMessage != null)
      targetMessage.sendMessage(messageReceiver, actionEnvironment);

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
