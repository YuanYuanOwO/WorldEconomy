package me.blvckbytes.world_economy;

public class EconomyAccount {

  private double balance;
  private boolean dirty;

  private final BalanceConstraint balanceConstraint;

  public EconomyAccount(
    double balance,
    BalanceConstraint balanceConstraint
  ) {
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
      dirty = true;
      return true;
    }
  }

  public boolean deposit(double value) {
    synchronized (this) {
      if (!balanceConstraint.isWithinRange(this, balance + value))
        return false;

      balance += value;
      dirty = true;
      return true;
    }
  }

  public boolean set(double value) {
    synchronized (this) {
      if (!balanceConstraint.isWithinRange(this, value))
        return false;

      balance = value;
      dirty = true;
      return true;
    }
  }

  public boolean isDirty() {
    return dirty;
  }

  public void clearDirty() {
    this.dirty = false;
  }
}
