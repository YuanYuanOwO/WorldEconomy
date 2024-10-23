package me.blvckbytes.world_economy.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class EconomySection extends AConfigSection {

  public String nameSingular;
  public String namePlural;
  public double startingBalance;
  public @Nullable Double maxMoney;
  public @Nullable Double minMoney;
  public @Nullable Double minPayAmount;

  @CSAlways
  public NumberFormatSection currencyFormat;

  public @Nullable String currencyFormatPrefix;
  public @Nullable String currencyFormatSuffix;

  public EconomySection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.nameSingular = "undefined";
    this.namePlural = "undefined";
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (startingBalance < 0)
      throw new MappingError("Starting-balance cannot be less than zero");

    if (minMoney != null && maxMoney != null && minMoney > maxMoney)
      throw new MappingError("The minimum amount of money cannot be larger than the maximum amount of money");

    if (minPayAmount != null && minPayAmount < 0 )
      throw new MappingError("The minimum pay-amount cannot be less than zero");
  }
}
