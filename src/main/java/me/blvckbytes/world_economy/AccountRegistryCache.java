package me.blvckbytes.world_economy;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccountRegistryCache {

  private final Plugin plugin;
  private final Function<OfflinePlayer, EconomyAccountRegistry> computer;
  private final Consumer<EconomyAccountRegistry> removalHandler;
  private final ConfigKeeper<MainSection> config;

  private final Map<UUID, EconomyAccountRegistry> accountRegistryByPlayerId;
  private final Map<UUID, BukkitTask> deletionTaskByPlayerId;

  public AccountRegistryCache(
    Plugin plugin,
    Function<OfflinePlayer, EconomyAccountRegistry> computer,
    Consumer<EconomyAccountRegistry> removalHandler,
    ConfigKeeper<MainSection> config
  ) {
    this.plugin = plugin;
    this.computer = computer;
    this.removalHandler = removalHandler;
    this.config = config;

    this.accountRegistryByPlayerId = new HashMap<>();
    this.deletionTaskByPlayerId = new HashMap<>();
  }

  public @Nullable EconomyAccountRegistry remove(OfflinePlayer player) {
    synchronized (this) {
      var playerId = player.getUniqueId();
      var result = accountRegistryByPlayerId.remove(playerId);

      if (result != null)
        this.removalHandler.accept(result);

      var existingTask = deletionTaskByPlayerId.remove(playerId);

      if (existingTask != null)
        existingTask.cancel();

      return result;
    }
  }

  public EconomyAccountRegistry retrieveOrCompute(OfflinePlayer player) {
    synchronized (this) {
      var playerId = player.getUniqueId();
      var playerData = accountRegistryByPlayerId.get(playerId);

      if (playerData == null) {
        playerData = computer.apply(player);

        if (playerData != null)
          accountRegistryByPlayerId.put(playerId, playerData);
      }

      touchData(player);
      return playerData;
    }
  }

  public Collection<EconomyAccountRegistry> values() {
    synchronized (this) {
      return Collections.unmodifiableCollection(accountRegistryByPlayerId.values());
    }
  }

  public int size() {
    synchronized (this) {
      return accountRegistryByPlayerId.size();
    }
  }

  public void clear() {
    synchronized (this) {
      this.accountRegistryByPlayerId.clear();

      for (var taskIterator = deletionTaskByPlayerId.entrySet().iterator(); taskIterator.hasNext(); ) {
        taskIterator.next().getValue().cancel();
        taskIterator.remove();
      }
    }
  }

  private void touchData(OfflinePlayer player) {
    var playerId = player.getUniqueId();
    var existingTask = deletionTaskByPlayerId.remove(playerId);

    if (existingTask != null)
      existingTask.cancel();

    // Store online players until they quit, not based on a timeout.
    // But do remove their task if they just joined, by executing the above.
    if (player instanceof Player)
      return;

    deletionTaskByPlayerId.put(
      playerId,
      Bukkit.getScheduler().runTaskLater(
        plugin,
        () -> {
          if (!player.isOnline())
            this.remove(player);
        },
        config.rootSection.economy.offlinePlayerCacheSeconds * 20
      )
    );
  }
}
