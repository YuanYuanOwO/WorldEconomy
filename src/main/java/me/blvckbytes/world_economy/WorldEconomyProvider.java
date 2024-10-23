package me.blvckbytes.world_economy;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.config.MainSection;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WorldEconomyProvider implements Economy {

  private static final EconomyResponse UNSUPPORTED_BANKS_RESPONSE = new EconomyResponse(
    0, 0, EconomyResponse.ResponseType.FAILURE, "Banks are not supported by this implementation"
  );

  private static final EconomyResponse NO_SUCH_ACCOUNT_RESPONSE = new EconomyResponse(
    0, 0, EconomyResponse.ResponseType.FAILURE, "Could not locate the requested account"
  );

  private final Plugin plugin;
  private final ConfigKeeper<MainSection> config;
  private final EconomyDataRegistry accountRegistry;

  public WorldEconomyProvider(
    Plugin plugin,
    ConfigKeeper<MainSection> config,
    EconomyDataRegistry accountRegistry
  ) {
    this.plugin = plugin;
    this.config = config;
    this.accountRegistry = accountRegistry;
  }

  // ================================================================================
  // Provider
  // ================================================================================

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String getName() {
    return plugin.getName() + " Economy Provider";
  }

  // ================================================================================
  // Currency
  // ================================================================================

  @Override
  public String format(double value) {
    var formattedValue = config.rootSection.economy.currencyFormat.getFormat().format(value);

    String prefix = config.rootSection.economy.currencyFormatPrefix;
    String suffix = config.rootSection.economy.currencyFormatSuffix;

    if (prefix != null && suffix != null)
      return prefix + formattedValue + suffix;

    if (prefix != null)
      return prefix + formattedValue;

    if (suffix != null)
      return formattedValue + suffix;

    return formattedValue;
  }

  @Override
  public String currencyNamePlural() {
    return config.rootSection.economy.namePlural;
  }

  @Override
  public String currencyNameSingular() {
    return config.rootSection.economy.nameSingular;
  }

  @Override
  public int fractionalDigits() {
    return config.rootSection.economy.currencyFormat.getNumberOfDecimalDigits();
  }

  // ================================================================================
  // Account
  // ================================================================================

  @Override
  @SuppressWarnings("deprecation")
  public boolean hasAccount(String name) {
    return hasAccount(Bukkit.getOfflinePlayer(name));
  }

  @Override
  public boolean hasAccount(OfflinePlayer player) {
    return accountRegistry.getForLastWorld(player) != null;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean hasAccount(String playerName, String worldName) {
    return hasAccount(Bukkit.getOfflinePlayer(playerName), worldName);
  }

  @Override
  public boolean hasAccount(OfflinePlayer player, String worldName) {
    return accountRegistry.getForWorldName(player, worldName) != null;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean createPlayerAccount(String playerName) {
    return createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player) {
    return accountRegistry.createForLastWorld(player);
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean createPlayerAccount(String playerName, String worldName) {
    return createPlayerAccount(Bukkit.getOfflinePlayer(playerName), worldName);
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
    return accountRegistry.createForWorldName(player, worldName);
  }

  // ================================================================================
  // Balance
  // ================================================================================

  @Override
  @SuppressWarnings("deprecation")
  public double getBalance(String playerName) {
    return getBalance(Bukkit.getOfflinePlayer(playerName));
  }

  @Override
  public double getBalance(OfflinePlayer player) {
    var account = accountRegistry.getForLastWorld(player);
    return account == null ? 0 : account.getBalance();
  }

  @Override
  @SuppressWarnings("deprecation")
  public double getBalance(String playerName, String worldName) {
    return getBalance(Bukkit.getOfflinePlayer(playerName), worldName);
  }

  @Override
  public double getBalance(OfflinePlayer player, String worldName) {
    var account = accountRegistry.getForWorldName(player, worldName);
    return account == null ? 0 : account.getBalance();
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean has(String playerName, double value) {
    return has(Bukkit.getOfflinePlayer(playerName), value);
  }

  @Override
  public boolean has(OfflinePlayer player, double value) {
    var account = accountRegistry.getForLastWorld(player);
    return account != null && account.hasBalance(value);
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean has(String playerName, String worldName, double value) {
    return has(Bukkit.getOfflinePlayer(playerName), worldName, value);
  }

  @Override
  public boolean has(OfflinePlayer player, String worldName, double value) {
    var account = accountRegistry.getForWorldName(player, worldName);
    return account != null && account.hasBalance(value);
  }

  // ================================================================================
  // Withdrawing
  // ================================================================================

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse withdrawPlayer(String playerName, double value) {
    return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), value);
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, double value) {
    return handleWithdraw(accountRegistry.getForLastWorld(player), value);
  }

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse withdrawPlayer(String playerName, String worldName, double value) {
    return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), worldName, value);
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double value) {
    return handleWithdraw(accountRegistry.getForWorldName(player, worldName), value);
  }

  private EconomyResponse handleWithdraw(@Nullable EconomyAccount account, double value) {
    if (account == null)
      return NO_SUCH_ACCOUNT_RESPONSE;

    if (!account.withdraw(value))
      return new EconomyResponse(0, account.getBalance(), EconomyResponse.ResponseType.FAILURE, "Insufficient balance");

    return new EconomyResponse(value, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
  }

  // ================================================================================
  // Depositing
  // ================================================================================

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse depositPlayer(String playerName, double value) {
    return depositPlayer(Bukkit.getOfflinePlayer(playerName), value);
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, double value) {
    return handleDeposit(accountRegistry.getForLastWorld(player), value);
  }

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse depositPlayer(String playerName, String worldName, double value) {
    return depositPlayer(Bukkit.getOfflinePlayer(playerName), worldName, value);
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double value) {
    return handleDeposit(accountRegistry.getForWorldName(player, worldName), value);
  }

  private EconomyResponse handleDeposit(@Nullable EconomyAccount account, double value) {
    if (account == null)
      return NO_SUCH_ACCOUNT_RESPONSE;

    if (!account.deposit(value))
      return new EconomyResponse(0, account.getBalance(), EconomyResponse.ResponseType.FAILURE, "Exceeded maximum balance");

    return new EconomyResponse(value, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
  }

  // ================================================================================
  // Bank
  // ================================================================================

  @Override
  public boolean hasBankSupport() {
    return false;
  }

  @Override
  public EconomyResponse createBank(String s, String s1) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse deleteBank(String s) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse bankBalance(String s) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse bankHas(String s, double v) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse bankWithdraw(String s, double v) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse bankDeposit(String s, double v) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse isBankOwner(String s, String s1) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse isBankMember(String s, String s1) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
    return UNSUPPORTED_BANKS_RESPONSE;
  }

  @Override
  public List<String> getBanks() {
    return List.of();
  }
}
