package me.blvckbytes.world_economy.commands;

import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.world_economy.OfflineLocationReader;
import me.blvckbytes.world_economy.PluginPermission;
import me.blvckbytes.world_economy.WorldGroup;
import me.blvckbytes.world_economy.WorldGroupRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class BalanceTopCommand implements CommandExecutor, TabCompleter {

  private final OfflineLocationReader offlineLocationReader;
  private final WorldGroupRegistry worldGroupRegistry;

  public BalanceTopCommand(
    OfflineLocationReader offlineLocationReader,
    WorldGroupRegistry worldGroupRegistry
  ) {
    this.offlineLocationReader = offlineLocationReader;
    this.worldGroupRegistry = worldGroupRegistry;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player && !PluginPermission.COMMAND_BALTOP.has(player)) {
      player.sendMessage("§cNo permission to use baltop");
      return true;
    }

    WorldGroup targetWorldGroup;

    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        sender.sendMessage("§cOnly for players");
        return true;
      }

      targetWorldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

      if (targetWorldGroup == null) {
        sender.sendMessage("§cYour current location is not within any of the known groups");
        return true;
      }
    }

    else if (args.length == 1) {
      if (sender instanceof Player player && !PluginPermission.COMMAND_BALTOP_GROUP.has(player)) {
        player.sendMessage("§cNo permission to show top of other worlds");
        return true;
      }

      targetWorldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(args[0]);

      if (targetWorldGroup == null) {
        sender.sendMessage("World-Group " + args[0] + " could not be located");
        return true;
      }
    }

    else {
      sender.sendMessage("§cUsage: /" + label + " [world-group]");
      return true;
    }

    var groupDisplayName = targetWorldGroup.displayName().stringify(GPEEE.EMPTY_ENVIRONMENT);

    sender.sendMessage("§aTodo: Display top players for " + groupDisplayName);
    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1) {
      if (sender instanceof Player player && !PluginPermission.COMMAND_BALTOP_GROUP.has(player))
        return List.of();

      return worldGroupRegistry.createSuggestions(args[0]);
    }

    return List.of();
  }
}
