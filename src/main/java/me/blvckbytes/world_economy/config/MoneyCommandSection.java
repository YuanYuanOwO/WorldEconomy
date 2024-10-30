package me.blvckbytes.world_economy.config;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class MoneyCommandSection extends ACommandSection {

  public static final String INITIAL_NAME = "money";

  public boolean resolveTargetLastWorldGroup;

  public MoneyCommandSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(INITIAL_NAME, baseEnvironment);

    this.resolveTargetLastWorldGroup = false;
  }
}
