package me.blvckbytes.world_economy;

import org.bukkit.entity.Player;

public enum PluginPermission {
  COMMAND_RELOAD("command.reload"),
  COMMAND_BALANCES("command.balances"),
  COMMAND_BALANCES_OTHER("command.balances.other"),
  COMMAND_BALANCE("command.balance"),
  COMMAND_BALANCE_OTHER("command.balance.other"),
  COMMAND_BALTOP("command.baltop"),
  COMMAND_BALTOP_GROUP("command.baltop.group"),
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
