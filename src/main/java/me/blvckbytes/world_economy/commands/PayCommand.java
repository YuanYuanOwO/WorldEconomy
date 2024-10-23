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

public class PayCommand implements CommandExecutor, TabCompleter {

  private final OfflinePlayerCache offlinePlayerCache;
  private final OfflineLocationReader offlineLocationReader;
  private final EconomyDataRegistry economyDataRegistry;
  private final WorldGroupRegistry worldGroupRegistry;
  private final WorldEconomyProvider economyProvider;
  private final ConfigKeeper<MainSection> config;

  public PayCommand(
    OfflinePlayerCache offlinePlayerCache,
    OfflineLocationReader offlineLocationReader,
    EconomyDataRegistry economyDataRegistry,
    WorldGroupRegistry worldGroupRegistry,
    WorldEconomyProvider economyProvider,
    ConfigKeeper<MainSection> config
  ) {
    this.offlinePlayerCache = offlinePlayerCache;
    this.offlineLocationReader = offlineLocationReader;
    this.economyDataRegistry = economyDataRegistry;
    this.worldGroupRegistry = worldGroupRegistry;
    this.economyProvider = economyProvider;
    this.config = config;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(config.rootSection.playerMessages.playerOnlyPayCommand.stringify(
        config.rootSection.builtBaseEnvironment
      ));

      return true;
    }

    if (!PluginPermission.COMMAND_PAY.has(player)) {
      sender.sendMessage(config.rootSection.playerMessages.missingPermissionPayCommand.stringify(
        config.rootSection.builtBaseEnvironment
      ));

      return true;
    }

    var sourceAccountRegistry = economyDataRegistry.getAccountRegistry(player);

    if (sourceAccountRegistry == null) {
      sender.sendMessage(config.rootSection.playerMessages.couldNotLoadAccountSelf.stringify(
        config.rootSection.builtBaseEnvironment
      ));

      return true;
    }

    EconomyAccountRegistry targetAccountRegistry;
    double amount;
    WorldGroup targetWorldGroup;
    WorldGroup sourceWorldGroup;

    if (args.length == 2 || args.length == 3 || args.length == 4) {
      var targetPlayer = offlinePlayerCache.getByName(args[0]);
      targetAccountRegistry = economyDataRegistry.getAccountRegistry(targetPlayer);

      if (targetPlayer == sender) {
        sender.sendMessage(config.rootSection.playerMessages.cannotPaySelf.stringify(
          config.rootSection.builtBaseEnvironment
        ));

        return true;
      }

      if (targetAccountRegistry == null) {
        sender.sendMessage(config.rootSection.playerMessages.couldNotLoadAccountOther.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("name", targetPlayer.getName())
            .build()
        ));

        return true;
      }

      try {
        amount = Double.parseDouble(args[1]);
      } catch (NumberFormatException e) {
        sender.sendMessage(config.rootSection.playerMessages.argumentIsNotADouble.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("value", args[1])
            .build()
        ));

        return true;
      }

      if (amount <= 0) {
        sender.sendMessage(config.rootSection.playerMessages.argumentIsNotStrictlyPositive.stringify(
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("value", args[1])
            .build()
        ));

        return true;
      }

      if (args.length >= 3) {
        if (!PluginPermission.COMMAND_PAY_TARGET.has(player)) {
          sender.sendMessage(config.rootSection.playerMessages.missingPermissionCommandPayTarget.stringify(
            config.rootSection.builtBaseEnvironment
          ));

          return true;
        }

        targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[2]);

        if (targetWorldGroup == null) {
          sender.sendMessage(config.rootSection.playerMessages.unknownWorldGroup.stringify(
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("name", args[2])
              .build()
          ));

          return true;
        }
      }

      else {
        targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(targetAccountRegistry.getHolder());

        if (targetWorldGroup == null) {
          sender.sendMessage(config.rootSection.playerMessages.notInAnyWorldGroupOther.stringify(
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("name", targetPlayer.getName())
              .build()
          ));

          return true;
        }
      }

      if (args.length == 4) {
        if (!PluginPermission.COMMAND_PAY_SOURCE.has(player)) {
          sender.sendMessage(config.rootSection.playerMessages.missingPermissionCommandPaySource.stringify(
            config.rootSection.builtBaseEnvironment
          ));

          return true;
        }

        sourceWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[3]);

        if (sourceWorldGroup == null) {
          sender.sendMessage(config.rootSection.playerMessages.unknownWorldGroup.stringify(
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("name", args[3])
              .build()
          ));

          return true;
        }
      }

      else {
        sourceWorldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

        if (sourceWorldGroup == null) {
          sender.sendMessage(config.rootSection.playerMessages.notInAnyWorldGroupSelf.stringify(
            config.rootSection.builtBaseEnvironment
          ));

          return true;
        }
      }
    }

    else {
      // TODO: Have separate usages based on permissions
      sender.sendMessage(config.rootSection.playerMessages.usagePayCommand.stringify(
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("label", label)
          .withStaticVariable("group_names", worldGroupRegistry.createSuggestions(null))
          .build()
      ));

      return true;
    }

    if (!sourceWorldGroup.equals(targetWorldGroup) && !PluginPermission.COMMAND_PAY_CROSS.has(player)) {
      sender.sendMessage(config.rootSection.playerMessages.cannotPayCrossWorldGroups.stringify(
        config.rootSection.builtBaseEnvironment
      ));

      return true;
    }

    var sourceAccount = sourceAccountRegistry.getAccount(sourceWorldGroup);
    var targetAccount = targetAccountRegistry.getAccount(targetWorldGroup);

    var sourceOldBalance = sourceAccount.getBalance();
    var targetOldBalance = targetAccount.getBalance();

    // ========== Transaction Begin ==========

    if (!sourceAccount.withdraw(amount)) {
      sender.sendMessage(config.rootSection.playerMessages.notEnoughMoneyToPay.stringify(
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("balance", economyProvider.format(sourceAccount.getBalance()))
          .withStaticVariable("amount", economyProvider.format(amount))
          .withStaticVariable("group", sourceWorldGroup.displayName().stringify(config.rootSection.builtBaseEnvironment))
          .build()
      ));

      return true;
    }

    if (!targetAccount.deposit(amount)) {
      sourceAccount.deposit(amount); // Rollback previous withdrawal

      sender.sendMessage(config.rootSection.playerMessages.paymentExceedsReceiversBalance.stringify(
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("balance", economyProvider.format(targetAccount.getBalance()))
          .withStaticVariable("amount", economyProvider.format(amount))
          .withStaticVariable("group", targetWorldGroup.displayName().stringify(config.rootSection.builtBaseEnvironment))
          .withStaticVariable("name", targetAccountRegistry.getHolder().getName())
          .build()
      ));

      return true;
    }

    // ========== Transaction End ==========

    var transactionEnvironment = config.rootSection.getBaseEnvironment()
      .withStaticVariable("source_old_balance", economyProvider.format(sourceOldBalance))
      .withStaticVariable("target_old_balance", economyProvider.format(targetOldBalance))
      .withStaticVariable("source_new_balance", economyProvider.format(sourceAccount.getBalance()))
      .withStaticVariable("target_new_balance", economyProvider.format(targetAccount.getBalance()))
      .withStaticVariable("amount", economyProvider.format(amount))
      .withStaticVariable("target_group", targetWorldGroup.displayName().stringify(config.rootSection.builtBaseEnvironment))
      .withStaticVariable("source_group", sourceWorldGroup.displayName().stringify(config.rootSection.builtBaseEnvironment))
      .withStaticVariable("sender_name", player.getName())
      .withStaticVariable("receiver_name", targetAccountRegistry.getHolder().getName())
      .build();

    sender.sendMessage(config.rootSection.playerMessages.paymentSentToPlayer.stringify(transactionEnvironment));

    Player targetPlayer;

    if ((targetPlayer = targetAccountRegistry.getHolder().getPlayer()) != null)
      targetPlayer.sendMessage(config.rootSection.playerMessages.paymentReceivedFromPlayer.stringify(transactionEnvironment));

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player))
      return List.of();

    if (!PluginPermission.COMMAND_PAY.has(player))
      return List.of();

    if (args.length == 1)
      return offlinePlayerCache.createSuggestions(args[0]);

    if (args.length == 3 && PluginPermission.COMMAND_PAY_TARGET.has(sender))
      return worldGroupRegistry.createSuggestions(args[2]);

    if (args.length == 4 && PluginPermission.COMMAND_PAY_SOURCE.has(sender))
      return worldGroupRegistry.createSuggestions(args[3]);

    return List.of();
  }
}
