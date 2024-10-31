package me.blvckbytes.world_economy.commands;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MoneyAction {
  Set,
  Remove,
  Add
  ;

  private static final List<String> names = Arrays.stream(values()).map(Enum::name).toList();

  private static final Map<String, MoneyAction> moneyActionByNameLower;

  static {
    moneyActionByNameLower = new HashMap<>();

    for (var value : values())
      moneyActionByNameLower.put(value.name().toLowerCase(), value);
  }

  public static @Nullable MoneyAction getByName(String name) {
    return moneyActionByNameLower.get(name.toLowerCase());
  }

  public static List<String> createSuggestions(@Nullable String input) {
    if (input == null)
      return names;

    return names
      .stream()
      .filter(name -> StringUtils.containsIgnoreCase(name, input))
      .toList();
  }
}
