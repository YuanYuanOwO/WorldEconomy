package me.blvckbytes.world_economy;

import com.google.gson.*;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EconomyDataRegistry implements BalanceConstraint, Listener {

  private static final Gson GSON_INSTANCE = new GsonBuilder()
    .setPrettyPrinting()
    .registerTypeAdapter(
      EconomyAccount.class,
      (JsonSerializer<EconomyAccount>) (account, type, jsonSerializationContext) -> (
        new JsonPrimitive(account.getBalance())
      )
    )
    .create();

  private final Plugin plugin;
  private final File playersFolder;
  private final OfflineLocationReader offlineLocationReader;
  private final WorldGroupRegistry worldGroupRegistry;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;
  private final AccountRegistryCache accountRegistryCache;

  public EconomyDataRegistry(
    Plugin plugin,
    OfflineLocationReader offlineLocationReader,
    WorldGroupRegistry worldGroupRegistry,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.playersFolder = new File(plugin.getDataFolder(), "players");

    if (playersFolder.exists()) {
      if (!playersFolder.isDirectory())
        throw new IllegalStateException("Expected " + playersFolder + " to be a directory");
    } else {
      if (!playersFolder.mkdirs())
        throw new IllegalStateException("Could not create directories for path " + playersFolder);
    }

    this.plugin = plugin;
    this.offlineLocationReader = offlineLocationReader;
    this.worldGroupRegistry = worldGroupRegistry;
    this.config = config;
    this.logger = logger;

    this.accountRegistryCache = new AccountRegistryCache(plugin, this::loadPlayerFile, this::storePlayerFile);

    // Could have re-configured the min/max/doClamp values, so clamping could be necessary
    // Same holds true for the world-groups, which would have to be re-evaluated when loading
    config.registerReloadListener(this::writeAndClearCache);

    invokeNextWritePeriod();
  }

  private void invokeNextWritePeriod() {
    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
      for (var accountRegistry : accountRegistryCache.values())
        storePlayerFile(accountRegistry);

      invokeNextWritePeriod();
    }, config.rootSection.economy.cacheWritePeriodSeconds * 20);
  }

  // ================================================================================
  // Public API
  // ================================================================================

  public @Nullable EconomyAccountRegistry getAccountRegistry(OfflinePlayer player) {
    if (!player.hasPlayedBefore())
      return null;

    return accountRegistryCache.retrieveOrCompute(player);
  }

  public @Nullable EconomyAccount getForLastWorld(OfflinePlayer player) {
    if (!player.hasPlayedBefore())
      return null;

    synchronized (this) {
      var worldGroup = offlineLocationReader.getLastLocationWorldGroup(player);

      if (worldGroup == null)
        return null;

      return accountRegistryCache.retrieveOrCompute(player).getAccount(worldGroup);
    }
  }

  public @Nullable EconomyAccount getForWorldName(OfflinePlayer player, String worldName) {
    if (!player.hasPlayedBefore())
      return null;

    synchronized (this) {
      var worldGroup = worldGroupRegistry.getWorldGroupByMemberNameIgnoreCase(worldName);

      if (worldGroup == null)
        return null;

      return accountRegistryCache.retrieveOrCompute(player).getAccount(worldGroup);
    }
  }

  public boolean createForLastWorld(OfflinePlayer player) {
    return getForLastWorld(player) != null;
  }

  public boolean createForWorldName(OfflinePlayer player, String worldName) {
    return getForWorldName(player, worldName) != null;
  }

  @Override
  public boolean isWithinRange(EconomyAccount account, double balance) {
    Double constraint;

    // While it would be elegant to do a return clampValue(balance) == balance, I'm a bit afraid of
    // double comparing precision issues, and thereby prefer the verbose alternative - for now, at least.

    if ((constraint = config.rootSection.economy.minMoney) != null) {
      if (balance < constraint)
        return false;
    }

    if ((constraint = config.rootSection.economy.maxMoney) != null) {
      return !(balance > constraint);
    }

    return true;
  }

  public void writeAndClearCache() {
    logger.info("Storing " + accountRegistryCache.size() + " player-data entries from cache");

    for (var accountRegistry : accountRegistryCache.values())
      storePlayerFile(accountRegistry);

    accountRegistryCache.clear();
    logger.info("Write-process complete");
  }

  // ================================================================================
  // Event-Handlers
  // ================================================================================

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    var accountRegistry = accountRegistryCache.remove(event.getPlayer());

    if (accountRegistry != null)
      storePlayerFile(accountRegistry);
  }

  // ================================================================================
  // File-Persistence
  // ================================================================================

  private void storePlayerFile(EconomyAccountRegistry accountRegistry) {
    if (!accountRegistry.isDirty())
      return;

    var playerId = accountRegistry.getHolder().getUniqueId();
    var playerFile = new File(playersFolder, playerId + ".json");

    if (!playerFile.exists()) {
      try {
        if (!playerFile.createNewFile())
          throw new IllegalStateException("Create was unsuccessful for unknown reasons");
      } catch (Exception creationException) {
        logger.log(Level.SEVERE, "Could not create player-file " + playerFile, creationException);
        return;
      }
    }

    try {
      var fileContents = GSON_INSTANCE.toJson(accountRegistry.toScalarMap());

      try (
        var fileWriter = new FileWriter(playerFile, false)
      ) {
        fileWriter.write(fileContents);
        accountRegistry.clearDirty();
      }
    } catch (Exception storeException) {
      logger.log(Level.SEVERE, "Could not store player-data to player-file " + playerFile, storeException);
    }
  }

  private EconomyAccountRegistry loadPlayerFile(OfflinePlayer player) {
    var playerId = player.getUniqueId();
    var playerFile = new File(playersFolder, playerId + ".json");

    if (!playerFile.isFile())
      return new EconomyAccountRegistry(player, new HashMap<>(), config, this);

    try (
      var fileReader = new FileReader(playerFile)
    ) {
      var playerFileObject = GSON_INSTANCE.fromJson(fileReader, JsonObject.class);
      var result = new HashMap<WorldGroup, EconomyAccount>();

      for (var objectEntry : playerFileObject.entrySet()) {
        var worldGroupName = objectEntry.getKey();

        if (!(
          objectEntry.getValue() instanceof JsonPrimitive value && value.isNumber()
        )) {
          logger.log(Level.SEVERE, "Value of world-group \"" + worldGroupName + "\" for player \"" + playerId + "\" is not a number; skipping");
          continue;
        }

        var worldGroup = worldGroupRegistry.getWorldGroupByIdentifierNameIgnoreCase(worldGroupName);

        if (worldGroup == null) {
          logger.log(Level.WARNING, "World-group \"" + worldGroupName + "\" for player \"" + playerId + "\" could not be matched with an existing world-group; skipping");
          continue;
        }

        var balance = value.getAsDouble();

        if (config.rootSection.economy.doClampOnLoad)
          balance = clampValue(balance);

        result.put(worldGroup, new EconomyAccount(balance, this));
      }

      return new EconomyAccountRegistry(player, result, config, this);
    } catch (Exception loadException) {
      logger.log(Level.SEVERE, "Encountered corrupted player-file " + playerFile + "; copying to .bak and deleting", loadException);

      try {
        Files.copy(
          playerFile.toPath(),
          new File(playersFolder, playerId + ".json.bak").toPath(),
          StandardCopyOption.REPLACE_EXISTING
        );
      } catch (Exception copyException) {
        logger.log(Level.SEVERE, "Could not make a backup of " + playerFile + " by copying to .bak and starting anew", copyException);
      }

      return new EconomyAccountRegistry(player, new HashMap<>(), config, this);
    }
  }

  private double clampValue(double value) {
    Double constraint;

    if ((constraint = config.rootSection.economy.minMoney) != null) {
      if (value < constraint)
        value = constraint;
    }

    if ((constraint = config.rootSection.economy.maxMoney) != null) {
      if (value > constraint)
        value = constraint;
    }

    return value;
  }
}
