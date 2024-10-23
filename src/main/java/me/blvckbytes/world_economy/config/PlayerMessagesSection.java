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
