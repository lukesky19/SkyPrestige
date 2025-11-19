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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.locale.Locale;
import com.github.lukesky19.skyPrestige.config.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.config.data.settings.Settings;
import com.github.lukesky19.skyPrestige.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.config.manager.prestige.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.math.EquationUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * This class creates the requirements command argument for the skyprestige command.
 */
public class RequirementsCommand {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull IslandDataManager islandDataManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager}
     * @param localeManager A {@link LocaleManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     */
    public RequirementsCommand(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull IslandDataManager islandDataManager) {
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.islandDataManager = islandDataManager;
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
                            Locale locale = localeManager.getLocale();
                            Player player = (Player) ctx.getSource().getSender();
                            UUID uuid = player.getUniqueId();

                            @Nullable Settings settings = settingsManager.getSettings();
                            if(settings == null || settings.scaleFormula() == null) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.requirementsConfigError()));
                                return 0;
                            }

                            int level = ctx.getArgument("level", int.class);
                            @Nullable PrestigeConfig prestigeConfig = prestigeConfigManager.getPrestigeConfig(level);
                            if(prestigeConfig == null) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.requirementsLevelNotFound()));
                                return 0;
                            }
                            if(prestigeConfig.requiredPrestigePoints() == null) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.requirementsConfigError()));
                                return 0;
                            }

                            @Nullable Island island = BentoBox.getInstance().getIslandsManager().getIsland(player.getWorld(), uuid);
                            if(island == null) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.progressPlayerNotOnIsland()));
                                return 0;
                            }

                            @Nullable IslandData islandData = islandDataManager.getIslandData(island.getUniqueId());
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

                                requiredPoints = EquationUtil.evaluateEquation(settings.scaleFormula(), variables).intValue();
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
