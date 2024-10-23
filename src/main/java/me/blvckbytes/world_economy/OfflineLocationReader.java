package me.blvckbytes.world_economy;

import de.tr7zw.nbtapi.NBTFile;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OfflineLocationReader implements Listener {

  private final ConfigKeeper<MainSection> config;
  private final Logger logger;
  private final File mainWorldPlayerDataFolder;

  private final Map<UUID, String> worldNameByUuidCache;
  private final Set<String> knownWorldNamesLower;

  public OfflineLocationReader(
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    // NBT of OfflinePlayer-s is always stored within the playerdata-folder of the main world
    var mainWorld = Bukkit.getServer().getWorld("world");

    if (mainWorld == null)
      throw new IllegalStateException("Could not get a reference to the main-world");

    var mainWorldFolder = mainWorld.getWorldFolder();

    if (mainWorldFolder == null)
      throw new IllegalStateException("Could not get a reference to the main-world's world-folder");

    this.mainWorldPlayerDataFolder = new File(mainWorldFolder, "playerdata");
    this.config = config;
    this.logger = logger;
    this.worldNameByUuidCache = new HashMap<>();
    this.knownWorldNamesLower = new HashSet<>();

    for (var world : Bukkit.getServer().getWorlds())
      knownWorldNamesLower.add(world.getName().toLowerCase());

    // Could have re-configured the offline-player world-name fallback, which could be cached
    config.registerReloadListener(worldNameByUuidCache::clear);
  }

  public @Nullable String getLocationWorldName(OfflinePlayer player) {
    var playerId = player.getUniqueId();

    if (player instanceof Player onlinePlayer) {
      worldNameByUuidCache.remove(playerId);
      return onlinePlayer.getWorld().getName();
    }

    var worldNameLower = worldNameByUuidCache.computeIfAbsent(playerId, this::getWorldNameLowerFromPlayerDataFile);

    if (worldNameLower == null)
      return null;

    if (!knownWorldNamesLower.contains(worldNameLower)) {
      String unknownWarningMessage = "World \"" + worldNameLower + "\" of player \"" + playerId + "\" was unknown";

      String fallback;

      if ((fallback = config.rootSection.fallbackOfflinePlayerWorldLower) != null) {
        logger.warning(unknownWarningMessage + "; used fallback \"" + fallback + "\"");
        return fallback;
      }

      logger.severe(unknownWarningMessage);
      return null;
    }

    return worldNameLower;
  }

  @EventHandler
  public void onWorldLoad(WorldLoadEvent event) {
    knownWorldNamesLower.add(event.getWorld().getName().toLowerCase());
  }

  @EventHandler
  public void onWorldUnload(WorldUnloadEvent event) {
    knownWorldNamesLower.remove(event.getWorld().getName().toLowerCase());
  }

  private @Nullable String getWorldNameLowerFromPlayerDataFile(UUID uuid) {
    var playerDataFile = new File(mainWorldPlayerDataFolder, uuid.toString() + ".dat");

    if (!playerDataFile.isFile())
      return null;

    try {
      var nbtFile = new NBTFile(playerDataFile);
      var dimensionValue = nbtFile.getString("Dimension");

      int keyDelimiter;

      if ((keyDelimiter = dimensionValue.indexOf(':')) < 0) {
        logger.severe("Expected separator-colon within \"Dimension\" in " + playerDataFile);
        return null;
      }

      var worldNameLower = dimensionValue.substring(keyDelimiter + 1).toLowerCase();

      if (worldNameLower.isBlank()) {
        logger.severe("Expected non-blank name for \"Dimension\" in " + playerDataFile);
        return null;
      }

      // https://minecraftology.fandom.com/wiki/List_of_Dimensions
      return switch (worldNameLower) {
        case "overworld" -> "world";
        case "the_nether" -> "world_nether";
        case "the_end" -> "world_the_end";
        default -> worldNameLower;
      };
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not access playerdata-file " + playerDataFile, e);
      return null;
    }
  }
}
