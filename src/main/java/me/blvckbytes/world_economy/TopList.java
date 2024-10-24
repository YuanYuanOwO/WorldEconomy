package me.blvckbytes.world_economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class TopList {

  private final int length;

  TopList(int length) {
    this.length = length;
    // Keep only <length> items at a time, in order, and replace for same <holder>
  }

  @SuppressWarnings("deprecation")
  public List<TopListEntry> getEntries() {
    return List.of(
      new TopListEntry(Bukkit.getOfflinePlayer("Gronkh"), 12800.0),
      new TopListEntry(Bukkit.getOfflinePlayer("Sarazar"), 2912.12),
      new TopListEntry(Bukkit.getOfflinePlayer("Paluten"), 128.13),
      new TopListEntry(Bukkit.getOfflinePlayer("Dner"), 45.31),
      new TopListEntry(Bukkit.getOfflinePlayer("PixelWolf"), 12.55)
    );
  }

  public void addOrUpdate(OfflinePlayer holder, double balance) {
  }
}

