package me.blvckbytes.world_economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class EconomyDataCache {

  // TODO: Test cache duration functionality

  private static final long OFFLINE_PLAYER_CACHE_DURATION_TICKS = 20 * 60 * 5;

  private final Plugin plugin;
  private final Function<OfflinePlayer, EconomyAccountRegistry> computer;

  private final Map<UUID, EconomyAccountRegistry> playerDataByPlayerId;
  private final Map<UUID, BukkitTask> deletionTaskByPlayerId;

  public EconomyDataCache(Plugin plugin, Function<OfflinePlayer, EconomyAccountRegistry> computer) {
    this.plugin = plugin;
    this.computer = computer;

    this.playerDataByPlayerId = new HashMap<>();
    this.deletionTaskByPlayerId = new HashMap<>();
  }

  public @Nullable EconomyAccountRegistry remove(OfflinePlayer player) {
    synchronized (this) {
      var playerId = player.getUniqueId();
      var result = playerDataByPlayerId.remove(playerId);

      var existingTask = deletionTaskByPlayerId.remove(playerId);

      if (existingTask != null)
        existingTask.cancel();

      return result;
    }
  }

  public EconomyAccountRegistry retrieveOrCompute(OfflinePlayer player) {
    synchronized (this) {
      var playerId = player.getUniqueId();
      var playerData = playerDataByPlayerId.get(playerId);

      if (playerData == null) {
        playerData = computer.apply(player);

        if (playerData != null)
          playerDataByPlayerId.put(playerId, playerData);
      }

      touchData(player);
      return playerData;
    }
  }

  public Set<Map.Entry<UUID, EconomyAccountRegistry>> entries() {
    return Collections.unmodifiableSet(playerDataByPlayerId.entrySet());
  }

  public int size() {
    return playerDataByPlayerId.size();
  }

  public void clear() {
    this.playerDataByPlayerId.clear();

    for (var taskIterator = deletionTaskByPlayerId.entrySet().iterator(); taskIterator.hasNext();) {
      taskIterator.next().getValue().cancel();
      taskIterator.remove();
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
        OFFLINE_PLAYER_CACHE_DURATION_TICKS
      )
    );
  }
}
