# WorldEconomy

A highly simplistic, Vault-based economy provider, supporting per-world balances and world-groups. Find out more on its [Documentation-Site](https://blvckbytes.github.io/docs-world-economy/).

## TODO-List

- Extensively test all commands
  - Test all permissions
  - Test all arg combinations
  - Test all possible cases
  - Test all aliases
- Extract all messages into en_us.txt
- Ensure that all variable-comments of the config are up-to-date

Write about how offline player last-loc resolving is important for when the EconomyProvider tries to
withdraw to an offline-player without a given world-string. For commands, behavior is configurable.