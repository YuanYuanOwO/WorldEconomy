package me.blvckbytes.world_economy.commands;

import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.world_economy.WorldGroupRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PayCommand implements CommandExecutor {

  /*
    # Whether pay only works within the same world, or allows cross-world transactions
    sameWorld: true
   */

  private final WorldGroupRegistry worldGroupRegistry;

  public PayCommand(WorldGroupRegistry worldGroupRegistry) {
    this.worldGroupRegistry = worldGroupRegistry;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    // Dev-only debug utility
    if (args.length == 2) {
      if (args[0].equalsIgnoreCase("member")) {
        var result = worldGroupRegistry.getWorldGroupByMemberNameIgnoreCase(args[1]);
        sender.sendMessage("Result: " + (result == null ? "null" : result.displayName().stringify(GPEEE.EMPTY_ENVIRONMENT)));
        return true;
      }

      if (args[0].equalsIgnoreCase("identifier")) {
        var result = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[1]);
        sender.sendMessage("Result: " + (result == null ? "null" : result.displayName().stringify(GPEEE.EMPTY_ENVIRONMENT)));
        return true;
      }
    }

    sender.sendMessage("§aPay command");
    return true;
  }
}
