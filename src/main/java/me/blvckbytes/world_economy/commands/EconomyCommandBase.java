package me.blvckbytes.world_economy.commands;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.world_economy.OfflineLocationReader;
import me.blvckbytes.world_economy.WorldGroup;
import me.blvckbytes.world_economy.config.MainSection;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public abstract class EconomyCommandBase {

  protected ConfigKeeper<MainSection> config;

  protected EconomyCommandBase(ConfigKeeper<MainSection> config) {
    this.config = config;
  }

  public void sendNotADoubleMessage(CommandSender sender, String value) {
    BukkitEvaluable message;

    if ((message = config.rootSection.playerMessages.argumentIsNotADouble) != null) {
      message.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("value", value)
          .build()
      );
    }
  }

  public void sendNotStrictlyPositiveMessage(CommandSender sender, String value) {
    BukkitEvaluable message;

    if ((message = config.rootSection.playerMessages.argumentIsNotStrictlyPositive) != null) {
      message.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("value", value)
          .build()
      );
    }
  }

  public void sendUnknownWorldGroupMessage(
    OfflineLocationReader.LastLocation lastLocation,
    OfflinePlayer targetPlayer,
    CommandSender sender
  ) {
    BukkitEvaluable message;

    if (targetPlayer == sender) {
      if (lastLocation.worldName() == null)
        message = config.rootSection.playerMessages.couldNotLoadAccountOther;
      else
        message = config.rootSection.playerMessages.notInAnyWorldGroupOther;
    } else {
      if (lastLocation.worldName() == null)
        message = config.rootSection.playerMessages.couldNotLoadAccountSelf;
      else
        message = config.rootSection.playerMessages.notInAnyWorldGroupSelf;
    }

    if (message != null) {
      message.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("name", targetPlayer.getName())
          .withStaticVariable("current_world", lastLocation.worldName())
          .build()
      );
    }
  }

  public void sendUnknownAccountMessage(
    WorldGroup worldGroup,
    OfflinePlayer targetPlayer,
    CommandSender sender
  ) {
    BukkitEvaluable message;

    if (targetPlayer == sender)
      message = config.rootSection.playerMessages.couldNotLoadAccountSelf;
    else
      message = config.rootSection.playerMessages.couldNotLoadAccountOther;

    if (message != null) {
      var groupName = worldGroup.displayName().asScalar(
        ScalarType.STRING, config.rootSection.builtBaseEnvironment
      );

      message.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("name", targetPlayer.getName())
          .withStaticVariable("group", groupName)
          .build()
      );
    }
  }
}
