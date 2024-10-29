package me.blvckbytes.world_economy;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

public class EconomyAccount {

  private double balance;
  private boolean dirty;

  public @Nullable Runnable afterBalanceUpdate;

  public final OfflinePlayer holder;
  public final WorldGroup worldGroup;

  private final BalanceConstraint balanceConstraint;

  public EconomyAccount(
    OfflinePlayer holder,
    WorldGroup worldGroup,
    double balance,
    BalanceConstraint balanceConstraint
  ) {
    this.holder = holder;
    this.worldGroup = worldGroup;
    this.balance = balance;
    this.balanceConstraint = balanceConstraint;
  }

  public double getBalance() {
    synchronized (this) {
      return balance;
    }
  }

  public boolean hasBalance(double value) {
    synchronized (this) {
      return balanceConstraint.isWithinRange(this, balance - value);
    }
  }

  public boolean withdraw(double value) {
    synchronized (this) {
      if (!balanceConstraint.isWithinRange(this, balance - value))
        return false;

      balance -= value;
      markDirty();
      return true;
    }
  }

  public boolean deposit(double value) {
    synchronized (this) {
      if (!balanceConstraint.isWithinRange(this, balance + value))
        return false;

      balance += value;
      markDirty();
      return true;
    }
  }

  public boolean set(double value) {
    synchronized (this) {
      if (!balanceConstraint.isWithinRange(this, value))
        return false;

      balance = value;
      markDirty();
      return true;
    }
  }

  public boolean isDirty() {
    synchronized (this) {
      return dirty;
    }
  }

  public void clearDirty() {
    synchronized (this) {
      this.dirty = false;
    }
  }

  private void markDirty() {
    this.dirty = true;

    if (this.afterBalanceUpdate != null)
      this.afterBalanceUpdate.run();
  }
}
