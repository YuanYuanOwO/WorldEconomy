package me.blvckbytes.world_economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.logging.Logger;

public class LoggingEconomyWrapper implements Economy {

  public final Economy handle;
  private final Logger logger;

  public LoggingEconomyWrapper(Economy handle, Logger logger) {
    this.handle = handle;
    this.logger = logger;
  }

  // ================================================================================
  // Provider
  // ================================================================================

  @Override
  public boolean isEnabled() {
    var result = handle.isEnabled();
    logger.info("Economy::isEnabled() -> " + result);
    return result;
  }

  @Override
  public String getName() {
    var result = handle.getName();
    logger.info("Economy::getName() -> " + result);
    return result;
  }

  // ================================================================================
  // Currency
  // ================================================================================

  @Override
  public String format(double value) {
    var result = handle.format(value);
    logger.info("Economy::format(" + value + ") -> " + result);
    return result;
  }

  @Override
  public String currencyNamePlural() {
    var result = handle.currencyNamePlural();
    logger.info("Economy::currencyNamePlural() -> " + result);
    return result;
  }

  @Override
  public String currencyNameSingular() {
    var result = handle.currencyNameSingular();
    logger.info("Economy::currencyNameSingular() -> " + result);
    return result;
  }

  @Override
  public int fractionalDigits() {
    var result = handle.fractionalDigits();
    logger.info("Economy::fractionalDigits() -> " + result);
    return result;
  }

  // ================================================================================
  // Account
  // ================================================================================

  @Override
  @SuppressWarnings("deprecation")
  public boolean hasAccount(String playerName) {
    var result = handle.hasAccount(playerName);
    logger.info("Economy::hasAccount(" + playerName + ") -> " + result);
    return result;
  }

  @Override
  public boolean hasAccount(OfflinePlayer player) {
    var result = handle.hasAccount(player);
    logger.info("Economy::hasAccount(" + player.getUniqueId() + ") -> " + result);
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean hasAccount(String playerName, String worldName) {
    var result = handle.hasAccount(playerName, worldName);
    logger.info("Economy::hasAccount(" + playerName + ", " + worldName + ") -> " + result);
    return result;
  }

  @Override
  public boolean hasAccount(OfflinePlayer player, String worldName) {
    var result = handle.hasAccount(player, worldName);
    logger.info("Economy::hasAccount(" + player.getUniqueId() + ", " + worldName + ") -> " + result);
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean createPlayerAccount(String playerName) {
    var result = handle.createPlayerAccount(playerName);
    logger.info("Economy::createPlayerAccount(" + playerName + ") -> " + result);
    return result;
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player) {
    var result = handle.createPlayerAccount(player);
    logger.info("Economy::createPlayerAccount(" + player.getUniqueId() + ") -> " + result);
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean createPlayerAccount(String playerName, String worldName) {
    var result = handle.createPlayerAccount(playerName, worldName);
    logger.info("Economy::createPlayerAccount(" + playerName + ", " + worldName + ") -> " + result);
    return result;
  }

  @Override
  public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
    var result = handle.createPlayerAccount(player, worldName);
    logger.info("Economy::createPlayerAccount(" + player.getUniqueId() + ", " + worldName + ") -> " + result);
    return result;
  }

  // ================================================================================
  // Balance
  // ================================================================================

  @Override
  @SuppressWarnings("deprecation")
  public double getBalance(String playerName) {
    var result = handle.getBalance(playerName);
    logger.info("Economy::getBalance(" + playerName + ") -> " + result);
    return result;
  }

  @Override
  public double getBalance(OfflinePlayer player) {
    var result = handle.getBalance(player);
    logger.info("Economy::getBalance(" + player.getUniqueId() + ") -> " + result);
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public double getBalance(String playerName, String worldName) {
    var result = handle.getBalance(playerName, worldName);
    logger.info("Economy::getBalance(" + playerName + ", " + worldName + ") -> " + result);
    return result;
  }

  @Override
  public double getBalance(OfflinePlayer player, String worldName) {
    var result = handle.getBalance(player);
    logger.info("Economy::getBalance(" + player.getUniqueId() + ", " + worldName + ") -> " + result);
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean has(String playerName, double value) {
    var result = handle.has(playerName, value);
    logger.info("Economy::has(" + playerName + ", " + value + ") -> " + result);
    return result;
  }

  @Override
  public boolean has(OfflinePlayer player, double value) {
    var result = handle.has(player, value);
    logger.info("Economy::has(" + player.getUniqueId() + ", " + value + ") -> " + result);
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public boolean has(String playerName, String worldName, double value) {
    var result = handle.has(playerName, worldName, value);
    logger.info("Economy::has(" + playerName + ", " + worldName + ", " + value + ") -> " + result);
    return result;
  }

  @Override
  public boolean has(OfflinePlayer player, String worldName, double value) {
    var result = handle.has(player, worldName, value);
    logger.info("Economy::has(" + player.getUniqueId() + ", " + worldName + ", " + value + ") -> " + result);
    return result;
  }

  // ================================================================================
  // Withdrawing
  // ================================================================================

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse withdrawPlayer(String playerName, double value) {
    var result = handle.withdrawPlayer(playerName, value);
    logger.info("Economy::withdrawPlayer(" + playerName + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, double value) {
    var result = handle.withdrawPlayer(player, value);
    logger.info("Economy::withdrawPlayer(" + player.getUniqueId() + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse withdrawPlayer(String playerName, String worldName, double value) {
    var result = handle.withdrawPlayer(playerName, value);
    logger.info("Economy::withdrawPlayer(" + playerName + ", " + worldName + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double value) {
    var result = handle.withdrawPlayer(player, value);
    logger.info("Economy::withdrawPlayer(" + player.getUniqueId() + ", " + worldName + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  // ================================================================================
  // Depositing
  // ================================================================================

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse depositPlayer(String playerName, double value) {
    var result = handle.depositPlayer(playerName, value);
    logger.info("Economy::depositPlayer(" + playerName + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, double value) {
    var result = handle.depositPlayer(player, value);
    logger.info("Economy::depositPlayer(" + player.getUniqueId() + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse depositPlayer(String playerName, String worldName, double value) {
    var result = handle.depositPlayer(playerName, value);
    logger.info("Economy::depositPlayer(" + playerName + ", " + worldName + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double value) {
    var result = handle.depositPlayer(player, value);
    logger.info("Economy::depositPlayer(" + player.getUniqueId() + ", " + worldName + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  // ================================================================================
  // Bank
  // ================================================================================

  @Override
  public boolean hasBankSupport() {
    var result = handle.hasBankSupport();
    logger.info("Economy::hasBankSupport() -> " + result);
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse createBank(String bankName, String playerName) {
    var result = handle.createBank(bankName, playerName);
    logger.info("Economy::createBank(" + bankName + ", " + playerName + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse createBank(String bankName, OfflinePlayer player) {
    var result = handle.createBank(bankName, player);
    logger.info("Economy::createBank(" + bankName + ", " + player.getUniqueId() + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse deleteBank(String bankName) {
    var result = handle.deleteBank(bankName);
    logger.info("Economy::deleteBank(" + bankName + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse bankBalance(String bankName) {
    var result = handle.bankBalance(bankName);
    logger.info("Economy::bankBalance(" + bankName + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse bankHas(String bankName, double value) {
    var result = handle.bankHas(bankName, value);
    logger.info("Economy::bankHas(" + bankName + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse bankWithdraw(String bankName, double value) {
    var result = handle.bankWithdraw(bankName, value);
    logger.info("Economy::bankWithdraw(" + bankName + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse bankDeposit(String bankName, double value) {
    var result = handle.bankDeposit(bankName, value);
    logger.info("Economy::bankDeposit(" + bankName + ", " + value + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse isBankOwner(String bankName, String playerName) {
    var result = handle.isBankOwner(bankName, playerName);
    logger.info("Economy::isBankOwner(" + bankName + ", " + playerName + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse isBankOwner(String bankName, OfflinePlayer player) {
    var result = handle.isBankOwner(bankName, player);
    logger.info("Economy::isBankOwner(" + bankName + ", " + player.getUniqueId() + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  @SuppressWarnings("deprecation")
  public EconomyResponse isBankMember(String bankName, String playerName) {
    var result = handle.isBankMember(bankName, playerName);
    logger.info("Economy::isBankMember(" + bankName + ", " + playerName + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public EconomyResponse isBankMember(String bankName, OfflinePlayer player) {
    var result = handle.isBankMember(bankName, player);
    logger.info("Economy::isBankMember(" + bankName + ", " + player.getUniqueId() + ") -> " + stringifyResponse(result));
    return result;
  }

  @Override
  public List<String> getBanks() {
    var result = handle.getBanks();
    logger.info("Economy::getBanks() -> " + String.join(", ", result));
    return result;
  }

  private String stringifyResponse(EconomyResponse response) {
    return "EconomyResponse("
      + "amount=" + response.amount + ", "
      + "balance=" + response.balance + ", "
      + "type=" + response.type.name() + ", "
      + "errorMessage=" + response.errorMessage +
    ")";
  }
}
