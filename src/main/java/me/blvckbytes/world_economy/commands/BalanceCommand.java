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

public class BalanceCommand extends EconomyCommandBase implements CommandExecutor, TabCompleter {

  private final EconomyDataRegistry economyDataRegistry;
  private final WorldEconomyProvider economyProvider;
  private final WorldGroupRegistry worldGroupRegistry;
  private final OfflineLocationReader offlineLocationReader;
  private final OfflinePlayerCache offlinePlayerCache;

  public BalanceCommand(
    EconomyDataRegistry economyDataRegistry,
    WorldEconomyProvider economyProvider,
    WorldGroupRegistry worldGroupRegistry,
    OfflineLocationReader offlineLocationReader,
    OfflinePlayerCache offlinePlayerCache,
    ConfigKeeper<MainSection> config
  ) {
    super(config);

    this.economyDataRegistry = economyDataRegistry;
    this.economyProvider = economyProvider;
    this.worldGroupRegistry = worldGroupRegistry;
    this.offlineLocationReader = offlineLocationReader;
    this.offlinePlayerCache = offlinePlayerCache;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    var isBalanceGroupCommand = config.rootSection.commands.balanceGroup.isLabel(label);

    BukkitEvaluable message;

    if (missingCommandPermission(sender, isBalanceGroupCommand)) {
      if ((message = config.rootSection.playerMessages.missingPermissionBalanceSelfCommand) != null)
        message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

      return true;
    }

    var canViewOthers = canViewOthers(sender, isBalanceGroupCommand);

    OfflinePlayer targetPlayer;
    WorldGroup targetWorldGroup;

    if (args.length == 0) {
      if (isBalanceGroupCommand) {
        sendUsageMessage(sender, label, true, canViewOthers);
        return true;
      }

      if (!(sender instanceof Player player)) {
        if ((message = config.rootSection.playerMessages.playerOnlyBalanceSelfCommand) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

        return true;
      }

      targetPlayer = player;

      var targetLastLocation = offlineLocationReader.getLastLocation(targetPlayer);
      targetWorldGroup = targetLastLocation.worldGroup();

      if (targetWorldGroup == null) {
        sendUnknownWorldGroupMessage(targetLastLocation, targetPlayer, sender);
        return true;
      }
    }

    else if (args.length == 1 || args.length == 2) {
      if (isBalanceGroupCommand) {
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

        if (args.length == 2) {
          targetPlayer = offlinePlayerCache.getByName(args[1]);
        }

        else {
          if (!(sender instanceof Player player)) {
            if ((message = config.rootSection.playerMessages.playerOnlyBalanceGroupSelfCommand) != null)
              message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

            return true;
          }

          targetPlayer = player;
        }
      }

      else {
        if (args.length == 2) {
          sendUsageMessage(sender, label, false, canViewOthers);
          return true;
        }

        if (!canViewOthers) {
          if ((message = config.rootSection.playerMessages.missingPermissionBalanceOtherCommand) != null)
            message.sendMessage(sender, config.rootSection.builtBaseEnvironment);
          return true;
        }

        targetPlayer = offlinePlayerCache.getByName(args[0]);

        var targetLastLocation = offlineLocationReader.getLastLocation(targetPlayer);
        targetWorldGroup = targetLastLocation.worldGroup();

        if (targetWorldGroup == null) {
          sendUnknownWorldGroupMessage(targetLastLocation, targetPlayer, sender);
          return true;
        }
      }
    }

    else {
      sendUsageMessage(sender, label, isBalanceGroupCommand, canViewOthers);
      return true;
    }

    var accountRegistry = economyDataRegistry.getAccountRegistry(targetWorldGroup);
    var targetAccount = accountRegistry.getAccount(targetPlayer);
    var isSelf = targetPlayer == sender;

    if (targetAccount == null) {
      sendUnknownAccountMessage(targetWorldGroup, targetPlayer, sender);
      return true;
    }

    message = (
      isSelf
        ? config.rootSection.playerMessages.balanceMessageSelf
        : config.rootSection.playerMessages.balanceMessageOther
    );

    if (message != null) {
      message.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("holder", targetPlayer.getName())
          .withStaticVariable("balance", economyProvider.format(targetAccount.getBalance()))
          .withStaticVariable("group", targetWorldGroup.displayName().asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment))
          .build()
      );
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    var isBalanceGroupCommand = config.rootSection.commands.balanceGroup.isLabel(label);

    if (missingCommandPermission(sender, isBalanceGroupCommand))
      return List.of();

    var canViewOthers = canViewOthers(sender, isBalanceGroupCommand);

    if (args.length == 1) {
      if (isBalanceGroupCommand)
        return worldGroupRegistry.createSuggestions(args[0]);

      if (!canViewOthers)
        return List.of();

      return offlinePlayerCache.createSuggestions(args[0]);
    }

    if (args.length == 2 && isBalanceGroupCommand && canViewOthers)
      return offlinePlayerCache.createSuggestions(args[1]);

    return List.of();
  }

  private boolean canViewOthers(CommandSender sender, boolean isBalanceGroupCommand) {
    var commandPermission = (
      isBalanceGroupCommand
        ? PluginPermission.COMMAND_BALANCEGROUP_OTHER
        : PluginPermission.COMMAND_BALANCE_OTHER
    );

    return commandPermission.has(sender);
  }

  private boolean missingCommandPermission(CommandSender sender, boolean isBalanceGroupCommand) {
    var commandPermission = (
      isBalanceGroupCommand
        ? PluginPermission.COMMAND_BALANCEGROUP
        : PluginPermission.COMMAND_BALANCE
    );

    return !commandPermission.has(sender);
  }

  private void sendUsageMessage(CommandSender sender, String label, boolean supportsGroups, boolean canViewOthers) {
    BukkitEvaluable message;

    if (supportsGroups) {
      message = (
        canViewOthers
          ? config.rootSection.playerMessages.usageBalanceGroupCommandOther
          : config.rootSection.playerMessages.usageBalanceGroupCommandSelf
      );
    } else {
      message = (
        canViewOthers
          ? config.rootSection.playerMessages.usageBalanceCommandOther
          : config.rootSection.playerMessages.usageBalanceCommandSelf
      );
    }

    if (message != null) {
      message.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("group_names", worldGroupRegistry.createSuggestions(null))
          .withStaticVariable("label", label)
          .build()
      );
    }
  }
}
