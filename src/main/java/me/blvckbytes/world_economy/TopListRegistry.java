package me.blvckbytes.world_economy;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.logging.Logger;

public class TopListRegistry {

  private final EconomyDataRegistry economyDataRegistry;
  private final OfflinePlayerCache offlinePlayerCache;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;
  private final Map<WorldGroup, TopList> topListByWorldGroup;

  public TopListRegistry(
    EconomyDataRegistry economyDataRegistry,
    OfflinePlayerCache offlinePlayerCache,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.economyDataRegistry = economyDataRegistry;
    this.offlinePlayerCache = offlinePlayerCache;
    this.config = config;
    this.logger = logger;

    this.topListByWorldGroup = new LinkedHashMap<>();
    this.config.registerReloadListener(this::initializeTopList);
    this.initializeTopList();
  }

  public List<TopListEntry> getTopList(WorldGroup worldGroup) {
    synchronized (this) {
      return getOrCreateTopList(worldGroup).getEntries();
    }
  }

  public void afterBalanceChange(OfflinePlayer holder, EconomyAccount account, WorldGroup worldGroup) {
    synchronized (this) {
      getOrCreateTopList(worldGroup).addOrUpdate(holder, account.getBalance());
    }
  }

  private void initializeTopList() {
    logger.info("Initializing top-list with all global values; this may take a while...");

    this.topListByWorldGroup.clear();

    economyDataRegistry.forEachPlayerDataFile(playerFile -> {
      var fileName = playerFile.getName();
      var dotIndex = fileName.lastIndexOf('.');

      if (dotIndex < 0)
        return;

      var fileNameWithoutExtension = fileName.substring(0, dotIndex);

      UUID playerId;

      try {
        playerId = UUID.fromString(fileNameWithoutExtension);
      } catch (Exception e) {
        return;
      }

      var offlinePlayer = offlinePlayerCache.getById(playerId);
      var playerData = economyDataRegistry.loadPlayerFile(playerFile);

      if (playerData == null)
        return;

      for (var dataEntry : playerData.entrySet())
        getOrCreateTopList(dataEntry.getKey()).addOrUpdate(offlinePlayer, dataEntry.getValue().getBalance());
    });

    var topListSize = config.rootSection.economy.topListSize;
    logger.info("Top-list initialization complete, with up to " + topListSize + " entries per world-group");
  }

  private TopList getOrCreateTopList(WorldGroup worldGroup) {
    return topListByWorldGroup.computeIfAbsent(worldGroup, key -> new TopList(config.rootSection.economy.topListSize));
  }
}
