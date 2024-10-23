package me.blvckbytes.world_economy.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class PlayerMessagesSection extends AConfigSection {

  public BukkitEvaluable playerOnlyBalancesSelfCommand;
  public BukkitEvaluable missingPermissionBalancesSelfCommand;
  public BukkitEvaluable missingPermissionBalancesOtherCommand;
  public BukkitEvaluable usageBalancesCommandSelf;
  public BukkitEvaluable usageBalancesCommandOther;
  public BukkitEvaluable balancesScreen;

  public BukkitEvaluable playerOnlyBalanceSelfCommand;
  public BukkitEvaluable missingPermissionBalanceSelfCommand;
  public BukkitEvaluable missingPermissionBalanceOtherCommand;
  public BukkitEvaluable usageBalanceCommandSelf;
  public BukkitEvaluable usageBalanceCommandOther;
  public BukkitEvaluable balanceMessageSelf;
  public BukkitEvaluable balanceMessageOther;

  public BukkitEvaluable unknownWorldGroup;
  public BukkitEvaluable notInAnyWorldGroupSelf;
  public BukkitEvaluable notInAnyWorldGroupOther;
  public BukkitEvaluable couldNotLoadAccountSelf;
  public BukkitEvaluable couldNotLoadAccountOther;

  public BukkitEvaluable pluginReloadedSuccess;
  public BukkitEvaluable pluginReloadedError;

  public BukkitEvaluable missingPermissionReloadCommand;

  public PlayerMessagesSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    registerFieldDefault(BukkitEvaluable.class, () -> BukkitEvaluable.UNDEFINED_STRING);
  }
}
