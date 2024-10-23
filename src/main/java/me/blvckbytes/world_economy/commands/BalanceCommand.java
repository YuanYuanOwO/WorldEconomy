package me.blvckbytes.world_economy.commands;

import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.world_economy.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

  private final EconomyDataRegistry accountRegistry;
  private final WorldEconomyProvider economyProvider;
  private final WorldGroupRegistry worldGroupRegistry;
  private final OfflineLocationReader offlineLocationReader;

  public BalanceCommand(
    EconomyDataRegistry accountRegistry,
    WorldEconomyProvider economyProvider,
    WorldGroupRegistry worldGroupRegistry,
    OfflineLocationReader offlineLocationReader
  ) {
    this.accountRegistry = accountRegistry;
    this.economyProvider = economyProvider;
    this.worldGroupRegistry = worldGroupRegistry;
    this.offlineLocationReader = offlineLocationReader;
  }

  // /bal [player] [world-group-identifier]

  @Override
  @SuppressWarnings("deprecation")
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player && !PluginPermission.COMMAND_BALANCE.has(player)) {
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

    else if (args.length == 1 || args.length == 2) {
      target = Bukkit.getOfflinePlayer(args[0]);

      if (target != sender && sender instanceof Player player && !PluginPermission.COMMAND_BALANCE_OTHER.has(player)) {
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

    WorldGroup targetWorldGroup;
    boolean isLastLocation;

    if (args.length == 2) {
      targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[1]);
      isLastLocation = false;
    }

    else {
      targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(target);
      isLastLocation = true;
    }

    if (targetWorldGroup == null) {
      if (isLastLocation) {
        if (target instanceof Player) {
          sender.sendMessage("§cCurrent location of " + target.getName() + " is not within any of the known groups");
        } else {
          sender.sendMessage("§cLast location of " + target.getName() + " was not within any of the known groups");
        }
      } else
        sender.sendMessage("World-Group " + args[1] + " could not be located");

      return true;
    }

    var targetAccount = economyData.getAccount(targetWorldGroup);

    sender.sendMessage(
      "§7Balance of §e" + target.getName() + "§7 in world-group §e" +
      targetWorldGroup.displayName().stringify(GPEEE.EMPTY_ENVIRONMENT) +
      "§7 is: §e" + economyProvider.format(targetAccount.getBalance())
    );

    return true;
  }
}
