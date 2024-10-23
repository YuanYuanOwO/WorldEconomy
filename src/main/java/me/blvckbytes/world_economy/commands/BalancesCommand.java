package me.blvckbytes.world_economy.commands;

import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.world_economy.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class BalancesCommand implements CommandExecutor, TabCompleter {

  private final EconomyDataRegistry dataRegistry;
  private final WorldEconomyProvider economyProvider;
  private final WorldGroupRegistry worldGroupRegistry;
  private final OfflinePlayerCache offlinePlayerCache;

  public BalancesCommand(
    EconomyDataRegistry dataRegistry,
    WorldEconomyProvider economyProvider,
    WorldGroupRegistry worldGroupRegistry,
    OfflinePlayerCache offlinePlayerCache
  ) {
    this.dataRegistry = dataRegistry;
    this.economyProvider = economyProvider;
    this.worldGroupRegistry = worldGroupRegistry;
    this.offlinePlayerCache = offlinePlayerCache;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player && !PluginPermission.COMMAND_BALANCES.has(player)) {
      player.sendMessage("§cNo permission for command");
      return true;
    }

    OfflinePlayer target;

    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        sender.sendMessage("§cCommand for players only");
        return true;
      }

      target = player;
    }

    else if (args.length == 1) {
      target = offlinePlayerCache.getByName(args[0]);

      if (target != sender && sender instanceof Player player && !PluginPermission.COMMAND_BALANCES_OTHER.has(player)) {
        player.sendMessage("§cNo permission for other");
        return true;
      }
    }

    else {
      sender.sendMessage("§cUsage: /" + label + " [player]");
      return true;
    }

    var accountRegistry = dataRegistry.getAccountRegistry(target);

    if (accountRegistry == null) {
      sender.sendMessage("§cAccount does not exist: " + target.getName());
      return true;
    }

    sender.sendMessage("§7Balances of §e" + target.getName() + "§7:");

    for (var worldGroup : worldGroupRegistry.getWorldGroups()) {
      var displayName = worldGroup.displayName().stringify(GPEEE.EMPTY_ENVIRONMENT);
      var accountBalance = accountRegistry.getAccount(worldGroup).getBalance();
      sender.sendMessage(displayName + "§8: §e" + economyProvider.format(accountBalance));
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player && !PluginPermission.COMMAND_BALANCES_OTHER.has(player))
      return List.of();

    if (args.length == 1)
      return offlinePlayerCache.createSuggestions(args[0]);

    return List.of();
  }
}
