package me.blvckbytes.world_economy.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class WorldGroupsSection extends AConfigSection {

  public Map<String, WorldGroupSection> worldGroups;

  public WorldGroupsSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.worldGroups = Map.of();
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    var encounteredMemberNamesLower = new HashSet<>();

    for (var worldGroupEntry : worldGroups.entrySet()) {
      var worldGroupIdentifier = worldGroupEntry.getKey();

      if (worldGroupIdentifier.contains(" "))
        throw new MappingError("Illegal identifier \"" + worldGroupIdentifier + "\"; identifiers cannot contain spaces");

      for (var memberName : worldGroupEntry.getValue().members) {
        if (!encounteredMemberNamesLower.add(memberName.toLowerCase()))
          throw new MappingError("Encountered duplicate memberName of \"" + memberName + "\" in group \"" + worldGroupIdentifier + "\"");
      }
    }
  }
}
