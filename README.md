# SkyPrestige
## Description
* SkyPrestige allows players to prestige or reset their Island to unlock rewards after obtaining the required prestige points.

## Features
* Players can prestige or reset their island obtaining the required amount of prestige points.
* Required prestige points are scaled to the number of island members.
* Rewards can be given after the island has been prestiged to one or all island members.
* Handles processing offline island members when they log on after their island was prestiged.
* Teleports both online and offline players when the log on when the island they are on was prestiged.
* A vault to transfer items across prestiges. Slots are unlocked at specific prestige levels.
* Leaderboard placeholders with a way to exempt islands.
* A protection orb to protect specific items from being removed on prestige.
* A multiplier that can be applied to prestige points earned.
* A multiplier event that can be scheduled weekly.

## Dependencies
* BentoBox
* [SkyLib](https://github.com/lukesky19/SkyLib)

## Soft Dependencies
* MagicCobblestoneGenerator addon
* PlaceholderAPI
* PlayerAuctions
* RoseStacker
* SkyPlayTime
* SkySellWands
* Vault

## Commands
* /skyprestige - The base command and the command to prestige an island.
  * Alias: /prestige
* /skyprestige exchange - Exchange prestige points.
* /skyprestige exempt <island_id> - Mark an island as exempt from prestige leaderboards.
* /skyprestige help - View the plugin's help message.
* /skyprestige info - Open the info GUI.
* /skyprestige leaderboard - View the prestige leaderboard.
* /skyprestige level set <island_id> <level>
* /skyprestige multiplier event
* /skyprestige multiplier <add | remove | set> <multiplier>
* /skyprestige multiplier get [additional | event | total]
* /skyprestige opt-in - Opts the player's island in of prestige.
* /skyprestige opt-out - Opts the player's island out of prestige.
* /skyprestige points <add | remove | set> <island_id> <prestige_points>
* /skyprestige points get <island_id>
* /skyprestige progress - View the requirements for the next prestige level.
* /skyprestige reload - Reloads the plugin.
* /skyprestige requirements <level> - View the required prestige points for a particular prestige level.
* /skyprestige rewards - View the rewards for the next prestige level.
* /skyprestige unexempt - Mark an island as not exempt from prestige leaderboards.
* /skyprestige values - Open the values GUI.
* /skyprestige vault - Open the island vault.

## Permissions
* `skyprestige.commands.skyprestige` - Base Command Permission
* `skyprestige.commands.skyprestige.exchange` - The permission to open the GUI that allows players to exchange prestige points.
* `skyprestige.commands.skyprestige.exempt` - The permission to mark an island as exempt from the prestige leaderboard.
* `skyprestige.commands.skyprestige.help` - The permission to view the plugin's help message.
* `skyprestige.commands.skyprestige.info` - The permission to open the info GUI.
* `skyprestige.commands.skyprestige.leaderboard` - The permission to view the prestige leaderboard.
* `skyprestige.commands.skyprestige.level` - The permission to set an island's prestige level.
* `skyprestige.commands.skyprestige.multiplier` - The permission to manage and view information related to the prestige points' multiplier.
* `skyprestige.commands.skyprestige.multiplier.event` - The permission to view either the remaining event time, the time until the next event, or if the event is disabled.
* `skyprestige.commands.skyprestige.multiplier.add` - The permission to add to the current additional multiplier.
* `skyprestige.commands.skyprestige.multiplier.remove` - The permission to remove from the current additional multiplier.
* `skyprestige.commands.skyprestige.multiplier.set` - The permission to set the current additional multiplier.
* `skyprestige.commands.skyprestige.multiplier.get` - The permission to get the current multipliers.
* `skyprestige.commands.skyprestige.multiplier.get.additional` - The permission to get the current additional multiplier.
* `skyprestige.commands.skyprestige.multiplier.get.event` - The permission to get the current event multiplier.
* `skyprestige.commands.skyprestige.multiplier.get.total` - The permission to get the current total multiplier.
* `skyprestige.commands.skyprestige.points` - The permission to use the `/skyprestige points` command.
* `skyprestige.commands.skyprestige.points.add` - The permission to use the `/skyprestige points add` command.
* `skyprestige.commands.skyprestige.points.remove` - The permission to use the `/skyprestige points remove` command.
* `skyprestige.commands.skyprestige.points.set` - The permission to use the `/skyprestige points set` command.
* `skyprestige.commands.skyprestige.points.get` - The permission to use the `/skyprestige points get` command.
* `skyprestige.commands.skyprestige.progress` - The permission to open the GUI that displays the progress for the next prestige level.
* `skyprestige.commands.skyprestige.reload` - The permission to reload the plugin.
* `skyprestige.commands.skyprestige.requirements` - The permission to view the required prestige points for a particular prestige level.
* `skyprestige.commands.skyprestige.rewards` - The permission to open the GUI that displays the rewards for the next prestige level.
* `skyprestige.commands.skyprestige.unexempt` - The permission to mark an island as not exempt from the prestige leaderboard.
* `skyprestige.commands.skyprestige.values` - The permission to open the values GUI.
* `skyprestige.commands.skyprestige.vault` - The permission to open the GUI for the island's vault.

## FAQ
Q: What versions does this plugin support?

A: 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, and 1.21.11.

Q: Are there any plans to support any other versions?

A: I will always do my best to support the latest versions of the game. I will sometimes support other versions until I no longer use them.

Q: Does this work on Spigot and Paper?

A: Only Paper is supported. There are no plans to support any other server software (i.e., Spigot, Folia).

## Issues, Bugs, or Suggestions
* Please create a new [GitHub Issue](https://github.com/lukesky19/SkyPrestige/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to SkyPrestige and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.

## For Server Admins/Owners
* Download the plugin [SkyLib](https://github.com/lukesky19/SkyLib/releases).
* Download the plugin from the releases tab and add it to your server.

## Building
* Go to [SkyLib](https://github.com/lukesky19/SkyLib) and follow the "For Developers" instructions.
* Then run:
  ```./gradlew build```

### Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
