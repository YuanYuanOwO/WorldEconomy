package me.blvckbytes.world_economy.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

public class PlayerMessagesSection extends AConfigSection {

  public @Nullable BukkitEvaluable pluginReloadedSuccess;
  public @Nullable BukkitEvaluable pluginReloadedError;

  public @Nullable BukkitEvaluable missingPermissionReloadCommand;

  public PlayerMessagesSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
