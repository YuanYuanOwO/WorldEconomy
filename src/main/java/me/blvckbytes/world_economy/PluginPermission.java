package me.blvckbytes.world_economy;

import org.bukkit.entity.Player;

public enum PluginPermission {
  COMMAND_RELOAD("command.reload"),
  COMMAND_BALANCES("command.balances"),
  COMMAND_BALANCES_OTHER("command.balances.other"),
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
