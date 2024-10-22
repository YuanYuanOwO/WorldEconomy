package me.blvckbytes.world_economy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PayCommand implements CommandExecutor {

  /*
    # Whether pay only works within the same world, or allows cross-world transactions
    sameWorld: true
   */

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    sender.sendMessage("§aPay command");
    return true;
  }
}
