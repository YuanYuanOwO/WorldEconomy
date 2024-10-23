package me.blvckbytes.world_economy;

@FunctionalInterface
public interface BalanceConstraint {

  boolean isWithinRange(EconomyAccount account, double balance);

}
