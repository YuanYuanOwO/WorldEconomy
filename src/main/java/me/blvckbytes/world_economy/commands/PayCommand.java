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
    BukkitEvaluable message;

    if (!(sender instanceof Player player)) {
      if ((message = config.rootSection.playerMessages.playerOnlyPayCommand) != null)
        message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

      return true;
    }

    if (!PluginPermission.COMMAND_PAY.has(player)) {
      if ((message = config.rootSection.playerMessages.missingPermissionPayCommand) != null)
        message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

      return true;
    }

    OfflinePlayer targetPlayer;
    double amount;
    WorldGroup targetWorldGroup;
    WorldGroup sourceWorldGroup;

    if (args.length == 2 || args.length == 3 || args.length == 4) {
      targetPlayer = offlinePlayerCache.getByName(args[0]);

      if (targetPlayer == sender) {
        if ((message = config.rootSection.playerMessages.cannotPaySelf) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

        return true;
      }

      try {
        amount = Double.parseDouble(args[1]);
      } catch (NumberFormatException e) {
        if ((message = config.rootSection.playerMessages.argumentIsNotADouble) != null) {
          message.sendMessage(
            sender,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("value", args[1])
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
              .withStaticVariable("value", args[1])
              .build()
          );
        }

        return true;
      }

      if (args.length >= 3) {
        if (!PluginPermission.COMMAND_PAY_TARGET.has(player)) {
          if ((message = config.rootSection.playerMessages.missingPermissionCommandPayTarget) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

          return true;
        }

        targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[2]);

        if (targetWorldGroup == null) {
          if ((message = config.rootSection.playerMessages.unknownWorldGroup) != null) {
            message.sendMessage(
              sender,
              config.rootSection.getBaseEnvironment()
                .withStaticVariable("name", args[2])
                .build()
            );
          }

          return true;
        }
      }

      else {
        targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(targetPlayer);

        if (targetWorldGroup == null) {
          if ((message = config.rootSection.playerMessages.notInAnyWorldGroupOther) != null) {
            message.sendMessage(
              sender,
              config.rootSection.getBaseEnvironment()
                .withStaticVariable("name", targetPlayer.getName())
                .build()
            );
          }

          return true;
        }
      }

      if (args.length == 4) {
        if (!PluginPermission.COMMAND_PAY_SOURCE.has(player)) {
          if ((message = config.rootSection.playerMessages.missingPermissionCommandPaySource) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

          return true;
        }

        sourceWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[3]);

        if (sourceWorldGroup == null) {
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

      else {
        sourceWorldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

        if (sourceWorldGroup == null) {
          if ((message = config.rootSection.playerMessages.notInAnyWorldGroupSelf) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

          return true;
        }
      }
    }

    else {
      // TODO: Have separate usages based on permissions
      if ((message = config.rootSection.playerMessages.usagePayCommand) != null) {
        message.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("label", label)
            .withStaticVariable("group_names", worldGroupRegistry.createSuggestions(null))
            .build()
        );
      }

      return true;
    }

    var targetAccountRegistry = economyDataRegistry.getAccountRegistry(targetWorldGroup);
    var targetAccount = targetAccountRegistry.getAccount(targetPlayer);

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

    if (!sourceWorldGroup.equals(targetWorldGroup) && !PluginPermission.COMMAND_PAY_CROSS.has(player)) {
      if ((message = config.rootSection.playerMessages.cannotPayCrossWorldGroups) != null)
        message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

      return true;
    }

    var sourceAccountRegistry = economyDataRegistry.getAccountRegistry(sourceWorldGroup);
    var sourceAccount = sourceAccountRegistry.getAccount(player);

    if (sourceAccount == null) {
      if ((message = config.rootSection.playerMessages.couldNotLoadAccountSelf) != null)
        message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

      return true;
    }

    var sourceOldBalance = sourceAccount.getBalance();
    var targetOldBalance = targetAccount.getBalance();

    // ========== Transaction Begin ==========

    if (!sourceAccount.withdraw(amount)) {
      if ((message = config.rootSection.playerMessages.notEnoughMoneyToPay) != null) {
        message.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("balance", economyProvider.format(sourceAccount.getBalance()))
            .withStaticVariable("amount", economyProvider.format(amount))
            .withStaticVariable("group", sourceWorldGroup.displayName().asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment))
            .build()
        );
      }

      return true;
    }

    if (!targetAccount.deposit(amount)) {
      sourceAccount.deposit(amount); // Rollback previous withdrawal

      if ((message = config.rootSection.playerMessages.paymentExceedsReceiversBalance) != null) {
        message.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("balance", economyProvider.format(targetAccount.getBalance()))
            .withStaticVariable("amount", economyProvider.format(amount))
            .withStaticVariable("group", targetWorldGroup.displayName().asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment))
            .withStaticVariable("name", targetPlayer.getName())
            .build()
        );
      }

      return true;
    }

    // ========== Transaction End ==========

    var transactionEnvironment = config.rootSection.getBaseEnvironment()
      .withStaticVariable("source_old_balance", economyProvider.format(sourceOldBalance))
      .withStaticVariable("target_old_balance", economyProvider.format(targetOldBalance))
      .withStaticVariable("source_new_balance", economyProvider.format(sourceAccount.getBalance()))
      .withStaticVariable("target_new_balance", economyProvider.format(targetAccount.getBalance()))
      .withStaticVariable("amount", economyProvider.format(amount))
      .withStaticVariable("target_group", targetWorldGroup.displayName().asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment))
      .withStaticVariable("source_group", sourceWorldGroup.displayName().asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment))
      .withStaticVariable("sender_name", player.getName())
      .withStaticVariable("receiver_name", targetPlayer.getName())
      .build();

    if ((message = config.rootSection.playerMessages.paymentSentToPlayer) != null)
      message.sendMessage(sender, transactionEnvironment);

    Player messageReceiver;

    if ((messageReceiver = targetPlayer.getPlayer()) != null && (message = config.rootSection.playerMessages.paymentReceivedFromPlayer) != null)
      message.sendMessage(messageReceiver, transactionEnvironment);

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
