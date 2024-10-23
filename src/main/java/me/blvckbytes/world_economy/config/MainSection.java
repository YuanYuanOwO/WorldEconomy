package me.blvckbytes.world_economy.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.CSInlined;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

@CSAlways
public class MainSection extends AConfigSection {

  public PlayerMessagesSection playerMessages;
  public CommandsSection commands;
  public EconomySection economy;

  @CSInlined
  public WorldGroupsSection worldGroups;

  public MainSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
