package me.blvckbytes.world_economy;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum PluginPermission {
  COMMAND_RELOAD("command.reload"),
  COMMAND_BALANCES("command.balances"),
  COMMAND_BALANCES_OTHER("command.balances.other"),
  COMMAND_BALANCE("command.balance"),
  COMMAND_BALANCE_OTHER("command.balance.other"),
  COMMAND_BALTOP("command.baltop"),
  COMMAND_BALTOP_GROUP("command.baltop.group"),
  COMMAND_MONEY("command.money"),
  COMMAND_PAY("command.pay"),
  COMMAND_PAY_TARGET("command.pay.target"),
  COMMAND_PAY_SOURCE("command.pay.source"),
  COMMAND_PAY_CROSS("command.pay.cross"),
  ;

  private static final String PREFIX = "worldeconomy";
  private final String node;

  PluginPermission(String node) {
    this.node = PREFIX + "." + node;
  }

  public boolean has(Player player) {
    return player.hasPermission(node);
  }

  public boolean has(CommandSender sender) {
    if (sender instanceof Player player)
      return has(player);

    return true;
  }
}
