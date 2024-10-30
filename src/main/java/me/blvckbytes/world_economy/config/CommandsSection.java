package me.blvckbytes.world_economy.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

@CSAlways
public class CommandsSection extends AConfigSection {

  public BalanceCommandSection balance;
  public BalanceGroupCommandSection balanceGroup;
  public BalancesCommandSection balances;
  public MoneyCommandSection money;
  public PayCommandSection pay;
  public PayGroupCommandSection payGroup;
  public BalanceTopCommandSection balanceTop;
  public ReloadCommandSection reload;

  public CommandsSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
