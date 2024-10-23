package me.blvckbytes.world_economy;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class EconomyAccountRegistry implements BalanceConstraint {

  // TODO: Extensively log on WARNING-level when encountering unexpected state
  // TODO: Log API-calls on DEBUG-level
  // TODO: Add a doClamp boolean to the config which decides whether to clamp balance to [min;max] on load

  private final OfflineLocationReader offlineLocationReader;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  public EconomyAccountRegistry(
    OfflineLocationReader offlineLocationReader,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.offlineLocationReader = offlineLocationReader;
    this.config = config;
    this.logger = logger;
  }

  public @Nullable EconomyAccount getForLastWorld(OfflinePlayer player) {
    var worldName = offlineLocationReader.getLocationWorldName(player);
    return worldName == null ? null : getForWorldName(player, worldName);
  }

  public boolean createForLastWorld(OfflinePlayer player) {
    var worldName = offlineLocationReader.getLocationWorldName(player);
    return worldName != null && createForWorldName(player, worldName);
  }

  public @Nullable EconomyAccount getForWorldName(OfflinePlayer player, String worldName) {
    // TODO: Implement
    return new EconomyAccount(player, 0, this);
  }

  public boolean createForWorldName(OfflinePlayer player, String worldName) {
    // TODO: Implement
    return true;
  }

  @Override
  public boolean isWithinRange(EconomyAccount account, double balance) {
    Double constraint;

    if ((constraint = config.rootSection.economy.minMoney) != null) {
      if (balance < constraint)
        return false;
    }

    if ((constraint = config.rootSection.economy.maxMoney) != null) {
      if (balance > constraint)
        return false;
    }

    return true;
  }
}
