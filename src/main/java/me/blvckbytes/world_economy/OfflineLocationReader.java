package me.blvckbytes.world_economy;

import de.tr7zw.nbtapi.NBTFile;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OfflineLocationReader implements Listener {

  private final WorldGroupRegistry worldGroupRegistry;
  private final Logger logger;
  private final File mainWorldPlayerDataFolder;

  private final Map<UUID, WorldGroup> worldGroupByUuidCache;

  public OfflineLocationReader(
    WorldGroupRegistry worldGroupRegistry,
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
    this.worldGroupRegistry = worldGroupRegistry;
    this.logger = logger;
    this.worldGroupByUuidCache = new HashMap<>();

    config.registerReloadListener(worldGroupByUuidCache::clear);
  }

  public @Nullable WorldGroup getLastLocationWorldGroup(OfflinePlayer player) {
    var playerId = player.getUniqueId();

    if (player instanceof Player onlinePlayer) {
      worldGroupByUuidCache.remove(playerId);
      return worldGroupRegistry.getWorldGroupByMemberNameIgnoreCase(onlinePlayer.getWorld().getName());
    }

    WorldGroup result;

    if ((result = worldGroupByUuidCache.get(playerId)) != null)
      return result;

    var worldName = getWorldNameLowerFromPlayerDataFile(playerId);

    if (worldName == null)
      return null;

    result = worldGroupRegistry.getWorldGroupByMemberNameIgnoreCase(worldName);

    if (result == null) {
      logger.severe("World \"" + worldName + "\" of player \"" + playerId + "\" could not be corresponded to an existing world-group");
      return null;
    }

    worldGroupByUuidCache.put(playerId, result);
    return result;
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
