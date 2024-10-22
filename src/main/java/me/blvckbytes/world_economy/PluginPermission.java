package me.blvckbytes.world_economy;

import org.bukkit.entity.Player;

public enum PluginPermission {
  RELOAD_COMMAND("command.reload"),
  ;

  private static final String PREFIX = "worldeconomy";
  private final String node;

  PluginPermission(String node) {
    this.node = PREFIX + "." + node;
  }

  public boolean has(Player player) {
    return player.hasPermission(node);
  }
}
