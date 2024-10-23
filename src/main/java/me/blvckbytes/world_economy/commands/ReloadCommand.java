package me.blvckbytes.world_economy.commands;

import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.PluginPermission;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ReloadCommand implements CommandExecutor {

  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  public ReloadCommand(ConfigKeeper<MainSection> config, Logger logger) {
    this.config = config;
    this.logger = logger;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    BukkitEvaluable message;

    if (sender instanceof Player player && !PluginPermission.COMMAND_RELOAD.has(player)) {
      if ((message = config.rootSection.playerMessages.missingPermissionReloadCommand) != null)
        player.sendMessage(message.stringify(config.rootSection.getBaseEnvironment().build()));

      return true;
    }

    try {
      this.config.reload();

      if ((message = config.rootSection.playerMessages.pluginReloadedSuccess) != null)
        sender.sendMessage(message.stringify(config.rootSection.getBaseEnvironment().build()));
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);

      if ((message = config.rootSection.playerMessages.pluginReloadedError) != null)
        sender.sendMessage(message.stringify(config.rootSection.getBaseEnvironment().build()));
    }

    return true;
  }
}
