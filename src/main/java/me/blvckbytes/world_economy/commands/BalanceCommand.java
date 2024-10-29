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

public class BalanceCommand implements CommandExecutor, TabCompleter {

  private final EconomyDataRegistry economyDataRegistry;
  private final WorldEconomyProvider economyProvider;
  private final WorldGroupRegistry worldGroupRegistry;
  private final OfflineLocationReader offlineLocationReader;
  private final OfflinePlayerCache offlinePlayerCache;
  private final ConfigKeeper<MainSection> config;

  public BalanceCommand(
    EconomyDataRegistry economyDataRegistry,
    WorldEconomyProvider economyProvider,
    WorldGroupRegistry worldGroupRegistry,
    OfflineLocationReader offlineLocationReader,
    OfflinePlayerCache offlinePlayerCache,
    ConfigKeeper<MainSection> config
  ) {
    this.economyDataRegistry = economyDataRegistry;
    this.economyProvider = economyProvider;
    this.worldGroupRegistry = worldGroupRegistry;
    this.offlineLocationReader = offlineLocationReader;
    this.offlinePlayerCache = offlinePlayerCache;
    this.config = config;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    BukkitEvaluable message;

    if (!PluginPermission.COMMAND_BALANCE.has(sender)) {
      if ((message = config.rootSection.playerMessages.missingPermissionBalanceSelfCommand) != null)
        message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

      return true;
    }

    var canViewOthers = PluginPermission.COMMAND_BALANCE_OTHER.has(sender);

    OfflinePlayer targetPlayer;
    WorldGroup targetWorldGroup;

    if (args.length == 0 || args.length == 1) {
      if (!(sender instanceof Player player)) {
        if ((message = config.rootSection.playerMessages.playerOnlyBalanceSelfCommand) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

        return true;
      }

      targetPlayer = player;
    }

    else if (args.length == 2) {
      targetPlayer = offlinePlayerCache.getByName(args[1]);

      if (targetPlayer != sender && !canViewOthers) {
        if ((message = config.rootSection.playerMessages.missingPermissionBalanceOtherCommand) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

        return true;
      }
    }

    else {
      if (!canViewOthers) {
        if ((message = config.rootSection.playerMessages.usageBalanceCommandSelf) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

        return true;
      }

      if ((message = config.rootSection.playerMessages.usageBalanceCommandOther) != null) {
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

    if (args.length == 0) {
      targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(targetPlayer);

      if (targetWorldGroup == null) {
        if ((message = config.rootSection.playerMessages.notInAnyWorldGroupSelf) != null)
          message.sendMessage(sender, config.rootSection.builtBaseEnvironment);

        return true;
      }
    }

    else {
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
    }

    var accountRegistry = economyDataRegistry.getAccountRegistry(targetWorldGroup);
    var targetAccount = accountRegistry.getAccount(targetPlayer);
    var isSelf = targetPlayer == sender;

    if (targetAccount == null) {
      message =
      (
        isSelf
        ? config.rootSection.playerMessages.couldNotLoadAccountSelf
        : config.rootSection.playerMessages.couldNotLoadAccountOther
      );

      if (message != null) {
        message.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("name", targetPlayer.getName())
            .build()
        );
      }

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
    if (!PluginPermission.COMMAND_BALANCE.has(sender))
      return List.of();

    if (args.length == 1)
      return worldGroupRegistry.createSuggestions(args[0]);

    if (args.length == 2 && PluginPermission.COMMAND_BALANCE_OTHER.has(sender))
      return offlinePlayerCache.createSuggestions(args[1]);

    return List.of();
  }
}
