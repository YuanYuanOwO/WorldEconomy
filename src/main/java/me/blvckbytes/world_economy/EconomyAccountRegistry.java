package me.blvckbytes.world_economy;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EconomyAccountRegistry {

  private final WorldGroup worldGroup;
  private final Map<UUID, EconomyAccount> accountById;

  private final Map<UUID, Integer> topListIndexById;
  private final List<EconomyAccount> topList;

  private final ConfigKeeper<MainSection> config;
  private final BalanceConstraint balanceConstraint;

  public EconomyAccountRegistry(
    WorldGroup worldGroup,
    ConfigKeeper<MainSection> config,
    BalanceConstraint balanceConstraint
  ) {
    this.worldGroup = worldGroup;

    this.accountById = new HashMap<>();
    this.topListIndexById = new HashMap<>();
    this.topList = new ArrayList<>();

    this.config = config;
    this.balanceConstraint = balanceConstraint;
  }

  public Collection<EconomyAccount> getTopAccounts(int limit) {
    var result = new ArrayList<EconomyAccount>();

    for (var i = topList.size() - 1; i >= 0; --i) {
      result.add(topList.get(i));

      if (result.size() == limit)
        break;
    }

    return result;
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
    synchronized (topList) {
      var playerId = account.holder.getUniqueId();
      var existingIndex = topListIndexById.remove(playerId);

      if (existingIndex != null)
        topList.remove((int) existingIndex);

      var newIndex = Collections.binarySearch(topList, account, Comparator.comparing(EconomyAccount::getBalance));

      if (newIndex < 0)
        newIndex = -newIndex - 1;

      topList.add(newIndex, account);
      topListIndexById.put(playerId, newIndex);
    }
  }
}
