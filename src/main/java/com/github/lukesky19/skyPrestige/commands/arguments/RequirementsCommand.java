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

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigePointsConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.math.EquationUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * This class creates the requirements command argument for the skyprestige command.
 */
public class RequirementsCommand {
    private final @NotNull ComponentLogger logger;

    private final @NotNull LocaleManager localeManager;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull PrestigePointsConfigManager prestigePointsConfigManager;

    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public RequirementsCommand(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        this.logger = plugin.getComponentLogger();
        this.localeManager = localeManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.prestigePointsConfigManager = prestigePointsConfigManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the requirements command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the requirements command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("requirements")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.requirements") && ctx.getSender() instanceof Player)
                .then(Commands.argument("level", IntegerArgumentType.integer())
                        .suggests((ctx, suggestionsBuilder) -> {
                            prestigeConfigManager.getPrestigeLevels().forEach(suggestionsBuilder::suggest);

                            return suggestionsBuilder.buildFuture();
                        })
                        .executes(ctx -> {
                            Locale locale = localeManager.getConfiguration();
                            Player player = (Player) ctx.getSource().getSender();
                            UUID uuid = player.getUniqueId();

                            @Nullable PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
                            if(prestigePointsConfig == null || prestigePointsConfig.scaleFormula() == null) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.requirementsConfigError()));
                                return 0;
                            }

                            int level = ctx.getArgument("level", int.class);
                            @Nullable PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(level);
                            if(prestigeConfig == null) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.requirementsLevelNotFound()));
                                return 0;
                            }
                            if(prestigeConfig.requiredPrestigePoints() == null) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.requirementsConfigError()));
                                return 0;
                            }

                            BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
                            @Nullable Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
                            if(island == null) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.progressPlayerNotOnIsland()));
                                return 0;
                            }

                            @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                            if(islandData == null) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
                                logger.warn(AdventureUtil.deserialize("No island data found for the island " + island.getUniqueId() + "."));
                                return 0;
                            }

                            double requiredPoints;
                            if(prestigeConfig.scaleFactor() != null && prestigeConfig.scaleFactor() != 0) {
                                HashMap<String, String> variables = new HashMap<>();
                                variables.put("r", String.valueOf(prestigeConfig.requiredPrestigePoints()));
                                variables.put("p", String.valueOf(island.getMemberSet().size()));
                                variables.put("k", String.valueOf(prestigeConfig.scaleFactor()));

                                requiredPoints = EquationUtil.evaluateEquation(prestigePointsConfig.scaleFormula(), variables).intValue();
                            } else {
                                requiredPoints = prestigeConfig.requiredPrestigePoints();
                            }

                            player.sendMessage(AdventureUtil.deserialize(
                                    player,
                                    locale.prefix() + locale.requirementsPointsForLevel(),
                                    List.of(
                                            Placeholder.parsed("prestige_level", String.valueOf(level)),
                                            Placeholder.parsed("prestige_points", String.valueOf(requiredPoints)))));

                            return 1;
                        })
                ).build();
    }
}
