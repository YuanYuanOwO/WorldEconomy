package me.blvckbytes.world_economy.commands;

import me.blvckbytes.world_economy.*;
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

  public MoneyCommand(
    OfflinePlayerCache offlinePlayerCache,
    EconomyDataRegistry economyDataRegistry,
    OfflineLocationReader offlineLocationReader,
    WorldGroupRegistry worldGroupRegistry,
    WorldEconomyProvider economyProvider
  ) {
    this.offlinePlayerCache = offlinePlayerCache;
    this.economyDataRegistry = economyDataRegistry;
    this.offlineLocationReader = offlineLocationReader;
    this.worldGroupRegistry = worldGroupRegistry;
    this.economyProvider = economyProvider;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player && !PluginPermission.COMMAND_MONEY.has(player)) {
      player.sendMessage("§cNo permission to use the money command");
      return true;
    }

    MoneyAction action;
    EconomyAccountRegistry accountRegistry;
    double value;
    WorldGroup targetWorldGroup;

    if (args.length == 3 || args.length == 4) {
      if ((action = MoneyAction.getByName(args[0])) == null) {
        sender.sendMessage("§cUsage: /" + label + " <set/remove/add> <player> <value> [world-group]");
        return true;
      }

      var targetPlayer = offlinePlayerCache.getByName(args[1]);
      accountRegistry = economyDataRegistry.getEconomyData(targetPlayer);

      if (accountRegistry == null) {
        sender.sendMessage("§cPlayer " + targetPlayer.getName() + " is not known on this server");
        return true;
      }

      try {
        value = Double.parseDouble(args[2]);
      } catch (NumberFormatException e) {
        sender.sendMessage("§cValue is not a valid number: §4" + args[2]);
        return true;
      }

      if (value < 0) {
        sender.sendMessage("§cValue cannot be less than zero: §4" + value);
        return true;
      }

      if (args.length == 3) {
        if (!(sender instanceof Player player)) {
          sender.sendMessage("§cOnly available to players");
          return false;
        }

        targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

        if (targetWorldGroup == null) {
          sender.sendMessage("§cYou're currently not in any known world-group");
          return true;
        }
      }

      else {
        targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[3]);

        if (targetWorldGroup == null) {
          sender.sendMessage("§cWorld-group with name " + args[3] + " not found");
          return true;
        }
      }
    }

    else {
      sender.sendMessage("§cUsage: /" + label + " <set/remove/add> <player> <value> [world-group]");
      return true;
    }

    var targetAccount = accountRegistry.getAccount(targetWorldGroup);

    // TODO: Yes, this is hella repetitive; will clear it up once messages are added to config.

    switch (action) {
      case Add -> {
        if (!targetAccount.deposit(value)) {
          sender.sendMessage("§cMaximum money exceeded");
          return true;
        }

        var formattedValue = economyProvider.format(value);

        sender.sendMessage("§aGave " + accountRegistry.getHolder().getName() + " " + formattedValue);

        Player targetPlayer;

        if ((targetPlayer = accountRegistry.getHolder().getPlayer()) != null) {
          if (sender instanceof Player executor)
            targetPlayer.sendMessage("§aYou have been given " + formattedValue + " §aby " + executor.getName());
          else
            targetPlayer.sendMessage("§aYou have been given " + formattedValue + " §aby Console");
        }

        return true;
      }

      case Remove -> {
        if (!targetAccount.withdraw(value)) {
          sender.sendMessage("§cMinimum money exceeded");
          return true;
        }

        var formattedValue = economyProvider.format(value);

        sender.sendMessage("§aRemoved " + formattedValue + " from " + accountRegistry.getHolder().getName());

        Player targetPlayer;

        if ((targetPlayer = accountRegistry.getHolder().getPlayer()) != null) {
          if (sender instanceof Player executor)
            targetPlayer.sendMessage("§a" + executor.getName() + " has removed " + formattedValue + " §afrom your account");
          else
            targetPlayer.sendMessage("§aConsole has removed " + formattedValue + " §afrom your account");
        }

        return true;
      }

      case Set -> {
        if (!targetAccount.set(value)) {
          sender.sendMessage("§cValue not within range of min and max");
          return true;
        }

        var formattedValue = economyProvider.format(value);

        sender.sendMessage("§aSet balance of " + accountRegistry.getHolder().getName() + " to " + formattedValue);

        Player targetPlayer;

        if ((targetPlayer = accountRegistry.getHolder().getPlayer()) != null) {
          if (sender instanceof Player executor)
            targetPlayer.sendMessage("§a" + executor.getName() + " has set your balanace to " + formattedValue);
          else
            targetPlayer.sendMessage("§aConsole has set your balanace to " + formattedValue);
        }

        return true;
      }
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player && !PluginPermission.COMMAND_MONEY.has(player))
      return List.of();

    if (args.length == 1)
      return MoneyAction.names;

    if (args.length == 2)
      return offlinePlayerCache.createSuggestions(args[1]);

    if (args.length == 3)
      return worldGroupRegistry.createSuggestions(args[2]);

    return List.of();
  }
}
