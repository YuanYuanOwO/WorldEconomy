# WorldEconomy

A highly simplistic, Vault-based economy provider, supporting per-world balances and world-groups.

## Persistence

### File Structure

The balance of any given player is stored in a JSON-file with it's name being equal to the player's UUID in a folder called `players`, within the plugin's directory; this implies that the server has to be in `online-mode`, as to guarantee unique and persistent IDs.

### File Contents

Said JSON-file contains a map, relating world-group-names to balances; this implies that renaming groups at a later point in time will result in a loss of money for the players within that group, as the plugin is not aware of this action, and can thereby not automatically migrate.

Example:

```json
{
    "skyblock": 1023.21,
    "oneblock": 541.98
}
```

## Miscellaneous Features

### World Groups

A group may contain multiple worlds, which are presented to the player as a single world when executing commands ingame. One possible use-case for groups would be the fact that each world is composed of the overworld, nether, and end; but various other, custom cases are obviously supported too.

Example:

```yaml
groups:
  skyblock:
    - 'SkyblockHUB'
    - 'bskyblock_world'
    - 'bskyblock_world_the_end'
    - 'bskyblock_world_nether'
  oneblock:
    - 'OneblockHUB'
    - 'oneblock_world'
    - 'oneblock_world_nether'
    - 'oneblock_world_the_end'
```

### Negative Balances

Negative balances are strictly not supported; all transactions trying to withdraw more money than the player actually has will result in a failure.

### Vault support

#### Banks

The creation, deletion and manipulation of banks is strictly not supported, and will thereby result in `hasBankSupport()` returning false; all bank-related operations will result in a no-op.

#### Currencies

This plugin only supports a single, configurable currency name and format, common to all worlds.

#### Per-World Balances

If there is no world specified on the API-call, the world in which the player is or has played before logging out will be assumed; unidentifyable world-names will result in a transaction-failure.

## Commands

### Check Balance

Description: Checks the current balance of any given player in any given world.\
Usage: `/bal [player] [world]`

- The base-command `/bal` requires permission `worldeconomy.command.bal`
- The argument `[player]` requires permission `worldeconomy.command.bal.other`
- The argument `[world]` requires permission `worldeconomy.command.bal.other.world`

Each of these executions yields a message containing the current amount of oneself or the mentioned player, within either the world the executor is currently in, or the world mentioned.

### Pay Money

Description: Transfers a positive amount of money from the executor to the player mentioned in `<player>`, within the same world.\
Usage: `/pay <player> <amount>`

If the recipient is online, and not within the same world as the sender, the command should be denied; if the recipient is offline, and has not been within the same world before logging out, the command should also be denied (configurable). Players cannot pay themselves.

- Requires the permission `worldeconomy.command.pay`

### Manage Money

Description: An administrative tool to update the balance of any given player.\
Usage: `/money <set/add/remove> <player> <amount> [world]`

If the target is online, they will be notified of their balance having been affected. If the target is offline, they have to have played on the server before (meaning that a corresponding JSON-file has to exist). Without specifying the `[world]`-argument, the world of the executor will be taken as an input.

- This command requires the permission `worldeconomy.command.money`

### Top List

Description: Prints the first `N` (configurable) players for any given world, owning the highest balances, to the chat.\
Usage: `/baltop [world]`