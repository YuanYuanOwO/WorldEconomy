package me.blvckbytes.world_economy.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

public class PlayerMessagesSection extends AConfigSection {

  public @Nullable BukkitEvaluable playerOnlyBalancesSelfCommand;
  public @Nullable BukkitEvaluable missingPermissionBalancesSelfCommand;
  public @Nullable BukkitEvaluable missingPermissionBalancesOtherCommand;
  public @Nullable BukkitEvaluable usageBalancesCommandSelf;
  public @Nullable BukkitEvaluable usageBalancesCommandOther;
  public @Nullable BukkitEvaluable balancesScreen;

  public @Nullable BukkitEvaluable playerOnlyBalanceSelfCommand;
  public @Nullable BukkitEvaluable missingPermissionBalanceSelfCommand;
  public @Nullable BukkitEvaluable missingPermissionBalanceOtherCommand;
  public @Nullable BukkitEvaluable usageBalanceCommandSelf;
  public @Nullable BukkitEvaluable usageBalanceCommandOther;
  public @Nullable BukkitEvaluable balanceMessageSelf;
  public @Nullable BukkitEvaluable balanceMessageOther;

  public @Nullable BukkitEvaluable playerOnlyPayCommand;
  public @Nullable BukkitEvaluable missingPermissionPayCommand;
  public @Nullable BukkitEvaluable cannotPaySelf;
  public @Nullable BukkitEvaluable cannotPayCrossWorldGroups;
  public @Nullable BukkitEvaluable usagePayCommand;
  public @Nullable BukkitEvaluable usagePayTargetCommand;
  public @Nullable BukkitEvaluable usagePaySourceCommand;
  public @Nullable BukkitEvaluable missingPermissionCommandPayTarget;
  public @Nullable BukkitEvaluable missingPermissionCommandPaySource;

  public @Nullable BukkitEvaluable unknownWorldGroup;
  public @Nullable BukkitEvaluable notInAnyWorldGroupSelf;
  public @Nullable BukkitEvaluable notInAnyWorldGroupOther;
  public @Nullable BukkitEvaluable couldNotLoadAccountSelf;
  public @Nullable BukkitEvaluable couldNotLoadAccountOther;
  public @Nullable BukkitEvaluable argumentIsNotADouble;
  public @Nullable BukkitEvaluable argumentIsNotStrictlyPositive;
  public @Nullable BukkitEvaluable notEnoughMoneyToPay;
  public @Nullable BukkitEvaluable paymentExceedsReceiversBalance;
  public @Nullable BukkitEvaluable paymentSentToPlayer;
  public @Nullable BukkitEvaluable paymentReceivedFromPlayer;

  public @Nullable BukkitEvaluable missingPermissionMoneyCommand;
  public @Nullable BukkitEvaluable unknownMoneyCommandAction;
  public @Nullable BukkitEvaluable usageMoneyCommand;
  public @Nullable BukkitEvaluable playerOnlyMoneyCommandNoWorldGroup;
  public @Nullable BukkitEvaluable moneyCommandConsoleName;
  public @Nullable BukkitEvaluable moneyAddExceedsReceiversBalance;
  public @Nullable BukkitEvaluable moneyRemoveExceedsReceiversBalance;
  public @Nullable BukkitEvaluable moneySetExceedsReceiversBalance;
  public @Nullable BukkitEvaluable moneyCommandAddExecutor;
  public @Nullable BukkitEvaluable moneyCommandRemoveExecutor;
  public @Nullable BukkitEvaluable moneyCommandSetExecutor;
  public @Nullable BukkitEvaluable moneyCommandAddTarget;
  public @Nullable BukkitEvaluable moneyCommandRemoveTarget;
  public @Nullable BukkitEvaluable moneyCommandSetTarget;

  public @Nullable BukkitEvaluable missingPermissionCommandBalTop;
  public @Nullable BukkitEvaluable playerOnlyBalTopCommandNoWorldGroup;
  public @Nullable BukkitEvaluable missingPermissionCommandBalTopOtherGroups;
  public @Nullable BukkitEvaluable usageBalTopCommand;
  public @Nullable BukkitEvaluable usageBalTopCommandOtherGroups;
  public @Nullable BukkitEvaluable balTopScreen;

  public @Nullable BukkitEvaluable pluginReloadedSuccess;
  public @Nullable BukkitEvaluable pluginReloadedError;
  public @Nullable BukkitEvaluable missingPermissionReloadCommand;

  public PlayerMessagesSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
