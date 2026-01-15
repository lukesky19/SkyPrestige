/*
    SkyPrestige allows players to prestige or reset their Island to unlock rewards after obtaining the required prestige points.
    Copyright (C) 2025 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyPrestige.commands.arguments;

import com.github.lukesky19.skyPrestige.commands.util.IslandArgumentType;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;

/**
 * This class creates the level command argument for the skyprestige command.
 */
public class PrestigeLevelCommand {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PrestigeLevelCommand(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.localeManager = localeManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the level command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the level command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("level")
            .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.level"))
            .then(Commands.literal("set")
                .then(Commands.argument("island_id", new IslandArgumentType(plugin, hookManager))
                    .then(Commands.argument("level", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            Locale locale = localeManager.getConfiguration();
                            CommandSender sender = ctx.getSource().getSender();
                            Island island = ctx.getArgument("island_id", Island.class);
                            String islandId = island.getUniqueId();
                            int prestigeLevel = ctx.getArgument("level", int.class);

                            IslandData islandData = islandDataManager.getData(islandId);
                            if(islandData == null) {
                                logger.error(AdventureUtil.deserialize("No island data found for the island " + islandId + "."));

                                if(sender instanceof Player player) {
                                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
                                } else {
                                    logger.error(AdventureUtil.deserialize(locale.islandDataNotFound()));
                                }

                                return 0;
                            }

                            islandData.setPrestigeLevel(prestigeLevel);

                            List<TagResolver.Single> placeholders = List.of(
                                    Placeholder.parsed("island_id", islandId),
                                    Placeholder.parsed("prestige_level", String.valueOf(islandData.getPrestigeLevel())));

                            if(sender instanceof Player player) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandPrestigeLevelUpdated(), placeholders));
                            } else {
                                logger.error(AdventureUtil.deserialize(locale.islandPrestigeLevelUpdated(), placeholders));
                            }

                            return 1;
                        })))).build();
    }
}
