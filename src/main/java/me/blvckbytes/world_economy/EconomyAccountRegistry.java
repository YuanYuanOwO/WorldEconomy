package me.blvckbytes.world_economy;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EconomyAccountRegistry {

  private final WorldGroup worldGroup;
  private final Map<UUID, EconomyAccount> accountById;

  private final ConfigKeeper<MainSection> config;
  private final BalanceConstraint balanceConstraint;

  public EconomyAccountRegistry(
    WorldGroup worldGroup,
    ConfigKeeper<MainSection> config,
    BalanceConstraint balanceConstraint
  ) {
    this.worldGroup = worldGroup;
    this.accountById = new HashMap<>();
    this.config = config;
    this.balanceConstraint = balanceConstraint;
  }

  public Collection<EconomyAccount> getTopAccounts(int limit) {
    // TODO: Implement
    return Collections.unmodifiableCollection(accountById.values());
  }

  public Collection<EconomyAccount> getAccounts() {
    return Collections.unmodifiableCollection(accountById.values());
  }

  public void registerAccount(OfflinePlayer holder, EconomyAccount account) {
    this.accountById.put(holder.getUniqueId(), account);

    account.afterBalanceUpdate = () -> {
      updateTopListPosition(account);
    };

    updateTopListPosition(account);
  }

  public @Nullable EconomyAccount getAccount(OfflinePlayer holder) {
    var holderId = holder.getUniqueId();
    var account = accountById.get(holderId);

    if (account != null)
      return account;

    if (!holder.hasPlayedBefore())
      return null;

    account = new EconomyAccount(holder, worldGroup, config.rootSection.economy.startingBalance, balanceConstraint);
    registerAccount(holder, account);
    return account;
  }

  private void updateTopListPosition(EconomyAccount account) {
    // TODO: Implement
  }
}
