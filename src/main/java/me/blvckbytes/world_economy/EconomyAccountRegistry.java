package me.blvckbytes.world_economy;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class EconomyAccountRegistry {

  private final OfflinePlayer holder;
  private final Map<WorldGroup, EconomyAccount> accountByWorldGroup;
  private final ConfigKeeper<MainSection> config;
  private final BalanceConstraint balanceConstraint;

  public EconomyAccountRegistry(
    OfflinePlayer holder,
    Map<WorldGroup, EconomyAccount> accountByWorldGroup,
    ConfigKeeper<MainSection> config,
    BalanceConstraint balanceConstraint
  ) {
    this.holder = holder;
    this.accountByWorldGroup = accountByWorldGroup;
    this.config = config;
    this.balanceConstraint = balanceConstraint;
  }

  public OfflinePlayer getHolder() {
    return holder;
  }

  public EconomyAccount getAccount(WorldGroup worldGroup) {
    return accountByWorldGroup.computeIfAbsent(worldGroup, key -> (
      new EconomyAccount(config.rootSection.economy.startingBalance, balanceConstraint)
    ));
  }

  public Map<String, Double> toScalarMap() {
    var result = new HashMap<String, Double>();

    for (var entry : accountByWorldGroup.entrySet()) {
      var worldGroupIdentifier = entry.getKey().identifierNameLower();
      var accountBalance = entry.getValue().getBalance();
      result.put(worldGroupIdentifier, accountBalance);
    }

    return result;
  }
}
