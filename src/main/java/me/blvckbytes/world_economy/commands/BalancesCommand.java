package me.blvckbytes.world_economy.commands;

import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.world_economy.EconomyDataRegistry;
import me.blvckbytes.world_economy.PluginPermission;
import me.blvckbytes.world_economy.WorldEconomyProvider;
import me.blvckbytes.world_economy.WorldGroupRegistry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalancesCommand implements CommandExecutor {

  private final EconomyDataRegistry accountRegistry;
  private final WorldEconomyProvider economyProvider;
  private final WorldGroupRegistry worldGroupRegistry;

  public BalancesCommand(
    EconomyDataRegistry accountRegistry,
    WorldEconomyProvider economyProvider,
    WorldGroupRegistry worldGroupRegistry
  ) {
    this.accountRegistry = accountRegistry;
    this.economyProvider = economyProvider;
    this.worldGroupRegistry = worldGroupRegistry;
  }

  @Override
  @SuppressWarnings("deprecation")
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
      target = Bukkit.getOfflinePlayer(args[0]);

      if (target != sender && sender instanceof Player player && !PluginPermission.COMMAND_BALANCES_OTHER.has(player)) {
        player.sendMessage("§cNo permission for other");
        return true;
      }
    }

    else {
      sender.sendMessage("§cUsage: /" + label + " [player]");
      return true;
    }

    var economyData = accountRegistry.getEconomyData(target);

    if (economyData == null) {
      sender.sendMessage("§cAccount does not exist: " + target.getName());
      return true;
    }

    sender.sendMessage("§7Balances of §e" + target.getName() + "§7:");

    for (var worldGroup : worldGroupRegistry.getWorldGroups()) {
      var displayName = worldGroup.displayName().stringify(GPEEE.EMPTY_ENVIRONMENT);
      var accountBalance = economyData.getAccount(worldGroup).getBalance();
      sender.sendMessage(displayName + "§8: §e" + economyProvider.format(accountBalance));
    }

    return true;
  }
}
