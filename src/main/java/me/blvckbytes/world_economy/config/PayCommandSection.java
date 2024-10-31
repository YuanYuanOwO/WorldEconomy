package me.blvckbytes.world_economy.config;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class PayCommandSection extends ACommandSection {

  public static final String INITIAL_NAME = "pay";

  public PayCommandSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(INITIAL_NAME, baseEnvironment);
  }
}
