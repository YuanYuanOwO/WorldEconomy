package me.blvckbytes.world_economy.commands;

import me.blvckbytes.world_economy.*;
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

  public PayCommand(
    OfflinePlayerCache offlinePlayerCache,
    OfflineLocationReader offlineLocationReader,
    EconomyDataRegistry economyDataRegistry,
    WorldGroupRegistry worldGroupRegistry,
    WorldEconomyProvider economyProvider
  ) {
    this.offlinePlayerCache = offlinePlayerCache;
    this.offlineLocationReader = offlineLocationReader;
    this.economyDataRegistry = economyDataRegistry;
    this.worldGroupRegistry = worldGroupRegistry;
    this.economyProvider = economyProvider;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage("§cOnly available to players");
      return true;
    }

    if (!PluginPermission.COMMAND_PAY.has(player)) {
      player.sendMessage("§cNo permission to use the pay command");
      return true;
    }

    var sourceAccountRegistry = economyDataRegistry.getAccountRegistry(player);

    if (sourceAccountRegistry == null) {
      player.sendMessage("§cCould not access your economy-data");
      return true;
    }

    EconomyAccountRegistry targetAccountRegistry;
    double value;
    WorldGroup targetWorldGroup;
    WorldGroup sourceWorldGroup;

    if (args.length == 2 || args.length == 3 || args.length == 4) {
      var targetPlayer = offlinePlayerCache.getByName(args[0]);
      targetAccountRegistry = economyDataRegistry.getAccountRegistry(targetPlayer);

      if (targetAccountRegistry == null) {
        player.sendMessage("§cThe player " + targetPlayer.getName() + " is not known on this server");
        return true;
      }

      try {
        value = Double.parseDouble(args[1]);
      } catch (NumberFormatException e) {
        sender.sendMessage("§cValue is not a valid number: §4" + args[1]);
        return true;
      }

      if (value < 0) {
        sender.sendMessage("§cValue cannot be less than zero: §4" + value);
        return true;
      }

      if (args.length >= 3) {
        if (!PluginPermission.COMMAND_PAY_TARGET.has(player)) {
          player.sendMessage("§cNo permission to specify custom target world-groups");
          return true;
        }

        targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[2]);

        if (targetWorldGroup == null) {
          player.sendMessage("§cUnknown target world-group: §4" + args[2]);
          return true;
        }
      }

      else {
        targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(targetAccountRegistry.getHolder());

        if (targetWorldGroup == null) {
          player.sendMessage("§cTarget is not currently in any known world-group");
          return true;
        }
      }

      if (args.length == 4) {
        if (!PluginPermission.COMMAND_PAY_SOURCE.has(player)) {
          player.sendMessage("§cNo permission to specify custom source world-groups");
          return true;
        }

        sourceWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[3]);

        if (sourceWorldGroup == null) {
          player.sendMessage("§cUnknown source world-group: §4" + args[2]);
          return true;
        }
      }

      else {
        sourceWorldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

        if (sourceWorldGroup == null) {
          player.sendMessage("§cYou're not currently in any known world-group");
          return true;
        }
      }
    }

    else {
      player.sendMessage("§cUsage: /" + label + " <name> <amount> [target world-group] [source world-group]");
      return true;
    }

    if (!PluginPermission.COMMAND_PAY_CROSS.has(player) && !sourceWorldGroup.equals(targetWorldGroup)) {
      player.sendMessage("§cNo permission to send money across world-groups");
      return true;
    }

    var sourceAccount = sourceAccountRegistry.getAccount(sourceWorldGroup);
    var targetAccount = targetAccountRegistry.getAccount(targetWorldGroup);

    // ========== Transaction Begin ==========

    if (!sourceAccount.withdraw(value)) {
      player.sendMessage("§cYou do not have enough money to make this payment.");
      return true;
    }

    if (!targetAccount.deposit(value)) {
      player.sendMessage("§cThis payment would exceed the maximum money of the other party.");
      sourceAccount.deposit(value); // Rollback previous withdrawal
      return true;
    }

    // ========== Transaction End ==========

    String formattedValue = economyProvider.format(value);
    Player targetPlayer;

    if ((targetPlayer = targetAccountRegistry.getHolder().getPlayer()) != null) {
      targetPlayer.sendMessage("§aYou have received " + formattedValue + " §a from " + player.getName());
    }

    player.sendMessage("§aYou've sent " + formattedValue + " §ato " + targetAccountRegistry.getHolder().getName());
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

    if (args.length == 3 || args.length == 4)
      return worldGroupRegistry.createSuggestions(args[args.length - 1]);

    return List.of();
  }
}
