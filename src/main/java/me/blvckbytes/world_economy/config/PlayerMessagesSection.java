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
  public @Nullable BukkitEvaluable playerOnlyBalanceGroupSelfCommand;
  public @Nullable BukkitEvaluable missingPermissionBalanceSelfCommand;
  public @Nullable BukkitEvaluable missingPermissionBalanceOtherCommand;
  public @Nullable BukkitEvaluable usageBalanceCommandSelf;
  public @Nullable BukkitEvaluable usageBalanceCommandOther;
  public @Nullable BukkitEvaluable usageBalanceGroupCommandSelf;
  public @Nullable BukkitEvaluable usageBalanceGroupCommandOther;
  public @Nullable BukkitEvaluable balanceMessageSelf;
  public @Nullable BukkitEvaluable balanceMessageOther;

  public @Nullable BukkitEvaluable playerOnlyPayCommand;
  public @Nullable BukkitEvaluable missingPermissionPayCommand;
  public @Nullable BukkitEvaluable cannotPaySelf;
  public @Nullable BukkitEvaluable cannotPayCrossWorldGroups;
  public @Nullable BukkitEvaluable usagePayCommand;
  public @Nullable BukkitEvaluable usagePayGroupCommand;
  public @Nullable BukkitEvaluable missingPermissionCommandPayGroupSource;

  public @Nullable BukkitEvaluable unknownWorldGroup;
  public @Nullable BukkitEvaluable notInAnyWorldGroupSelf;
  public @Nullable BukkitEvaluable notInAnyWorldGroupOther;
  public @Nullable BukkitEvaluable couldNotLoadAccountSelf;
  public @Nullable BukkitEvaluable couldNotLoadAccountOther;
  public @Nullable BukkitEvaluable valueIsNotADouble;
  public @Nullable BukkitEvaluable valueIsNotStrictlyPositive;
  public @Nullable BukkitEvaluable valueIsNotAMultipleOfTransactionStepSize;
  public @Nullable BukkitEvaluable notEnoughMoneyToPay;
  public @Nullable BukkitEvaluable paymentExceedsReceiversBalance;
  public @Nullable BukkitEvaluable paymentSentToPlayerSameGroup;
  public @Nullable BukkitEvaluable paymentSentToPlayerDifferentGroup;
  public @Nullable BukkitEvaluable paymentReceivedFromPlayerSameGroup;
  public @Nullable BukkitEvaluable paymentReceivedFromPlayerDifferentGroup;

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
