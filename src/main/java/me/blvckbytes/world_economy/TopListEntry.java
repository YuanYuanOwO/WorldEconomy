package me.blvckbytes.world_economy;

import org.bukkit.OfflinePlayer;

import java.util.Objects;

public record TopListEntry(
  OfflinePlayer holder,
  double balance
) {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TopListEntry that)) return false;
    return Objects.equals(holder, that.holder);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(holder);
  }
}
