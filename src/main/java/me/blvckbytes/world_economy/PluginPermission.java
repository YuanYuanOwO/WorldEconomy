package me.blvckbytes.world_economy;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum PluginPermission {
  COMMAND_RELOAD("command.reload"),
  COMMAND_BALANCES("command.balances"),
  COMMAND_BALANCES_OTHER("command.balances.other"),
  COMMAND_BALANCE("command.balance"),
  COMMAND_BALANCE_OTHER("command.balance.other"),
  COMMAND_BALANCEGROUP("command.balancegroup"),
  COMMAND_BALANCEGROUP_OTHER("command.balancegroup.other"),
  COMMAND_BALANCETOP("command.baltop"),
  COMMAND_BALANCETOP_GROUP("command.baltop.group"),
  COMMAND_MONEY("command.money"),
  COMMAND_PAY("command.pay"),
  COMMAND_PAY_CROSS("command.pay.cross"),
  COMMAND_PAYGROUP("command.paygroup"),
  COMMAND_PAYGROUP_SOURCE("command.paygroup.source"),
  COMMAND_PAYGROUP_CROSS("command.paygroup.cross"),
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
