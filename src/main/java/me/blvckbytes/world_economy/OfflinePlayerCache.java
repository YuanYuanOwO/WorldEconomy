package me.blvckbytes.world_economy;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.List;

public class OfflinePlayerCache {

  // TODO: Implement

  public OfflinePlayer getByName(String name) {
    return Bukkit.getOfflinePlayer(name);
  }

  public List<String> createSuggestions(String input) {
    return Arrays
      .stream(Bukkit.getOfflinePlayers())
      .map(OfflinePlayer::getName)
      .filter(name -> StringUtils.containsIgnoreCase(name, input))
      .limit(20)
      .toList();
  }
}
