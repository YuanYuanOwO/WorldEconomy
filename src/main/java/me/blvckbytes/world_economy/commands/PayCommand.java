package me.blvckbytes.world_economy.commands;

import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.world_economy.OfflineLocationReader;
import me.blvckbytes.world_economy.WorldGroupRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PayCommand implements CommandExecutor {

  /*
    # Whether pay only works within the same world, or allows cross-world transactions
    sameWorld: true
   */

  private final WorldGroupRegistry worldGroupRegistry;
  private final OfflineLocationReader offlineLocationReader;

  public PayCommand(WorldGroupRegistry worldGroupRegistry, OfflineLocationReader offlineLocationReader) {
    this.worldGroupRegistry = worldGroupRegistry;
    this.offlineLocationReader = offlineLocationReader;
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

      if (args[0].equalsIgnoreCase("world")) {
        var result = offlineLocationReader.getLocationWorldName(Bukkit.getOfflinePlayer(args[1]));
        sender.sendMessage("Result: " + (result == null ? "null" : result));
        return true;
      }
    }

    sender.sendMessage("§aPay command");
    return true;
  }
}
