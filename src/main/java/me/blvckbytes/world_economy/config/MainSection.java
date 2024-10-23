package me.blvckbytes.world_economy.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bbconfigmapper.sections.CSInlined;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

@CSAlways
public class MainSection extends AConfigSection {

  public PlayerMessagesSection playerMessages;
  public CommandsSection commands;
  public EconomySection economy;

  @CSInlined
  public WorldGroupsSection worldGroups;

  private @Nullable String fallbackOfflinePlayerWorld;

  @CSIgnore
  public @Nullable String fallbackOfflinePlayerWorldLower;

  public MainSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (fallbackOfflinePlayerWorld != null)
      this.fallbackOfflinePlayerWorldLower = fallbackOfflinePlayerWorld.toLowerCase();
  }
}
