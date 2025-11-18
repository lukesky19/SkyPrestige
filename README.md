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

## Dependencies
* BentoBox
* [SkyLib](https://github.com/lukesky19/SkyLib)

## Soft Dependencies
* BSkyBlock addon.
* PlaceholderAPI
* RoseStacker
* SkyPlayTime
* Vault

## Commands
* /skyprestige - The base command and the command to prestige an island.
  * Alias: /prestige
* /skyprestige help - View the plugin's help message.
* /skyprestige reload - Reloads the plugin.
* /skyprestige requirements <level> - View the required prestige points for a particular prestige level.
* /skyprestige progress - View the requirements for the next prestige level.
* /skyprestige rewards - View the rewards for the next prestige level.
* /skyprestige exchange - Exchange prestige points.
* /skyprestige vault - Open the island vault.
* /skyprestige level set <island_id> <level>
* /skyprestige points add <island_id> <prestige_points>
* /skyprestige points remove <island_id> <prestige_points>
* /skyprestige points set <island_id> <prestige_points>
* /skyprestige points get <island_id>

## Permissions
* `skyprestige.command.skyprestige` - Base Command Permission
* `skyprestige.command.skyprestige.help` - The permission to view the plugin's help message.
* `skyprestige.command.skyprestige.requirements` - The permission to view the required prestige points for a particular prestige level.
* `skyprestige.command.skyprestige.progress` - The permission to open the GUI that displays the progress for the next prestige level.
* `skyprestige.command.skyprestige.rewards` - The permission to open the GUI that displays the rewards for the next prestige level.
* `skyprestige.command.skyprestige.exchange` - The permission to open the GUI that allows players to exchange prestige points.
* `skyprestige.command.skyprestige.vault` - The permission to open the GUI opens the island's vault.
* `skyprestige.command.skyprestige.level` - The permission to set an island's prestige level.
* `skyprestige.command.skyprestige.points` - The permission to use the `/skyprestige points` command.
* `skyprestige.command.skyprestige.points.add` - The permission to use the `/skyprestige points add` command.
* `skyprestige.command.skyprestige.points.remove` - The permission to use the `/skyprestige points remove` command.
* `skyprestige.command.skyprestige.points.set` - The permission to use the `/skyprestige points set` command.
* `skyprestige.command.skyprestige.points.get` - The permission to use the `/skyprestige points get` command.

## FAQ
Q: What versions does this plugin support?

A: 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, and 1.21.10

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
