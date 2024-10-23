package me.blvckbytes.world_economy;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class EconomyAccountRegistry {

  // TODO: Extensively log on WARNING-level when encountering unexpected state
  // TODO: Log API-calls on DEBUG-level

  private final OfflineLocationReader offlineLocationReader;
  private final Logger logger;

  public EconomyAccountRegistry(OfflineLocationReader offlineLocationReader, Logger logger) {
    this.offlineLocationReader = offlineLocationReader;
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
    return new EconomyAccount();
  }

  public boolean createForWorldName(OfflinePlayer player, String worldName) {
    // TODO: Implement
    return true;
  }
}
