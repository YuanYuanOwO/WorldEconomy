package me.blvckbytes.world_economy.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.List;

public class WorldGroupSection extends AConfigSection {

  public BukkitEvaluable displayName;
  public List<String> members;

  public WorldGroupSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.members = List.of();
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (displayName == null)
      throw new MappingError("Key \"displayName\" cannot be absent");

    for (var member : members) {
      if (member.contains(" "))
        throw new MappingError("Illegal member \"" + member + "\"; members cannot contain spaces");
    }
  }
}
