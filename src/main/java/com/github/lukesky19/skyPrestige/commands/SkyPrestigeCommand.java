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
package com.github.lukesky19.skyPrestige.commands;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.commands.arguments.*;
import com.github.lukesky19.skyPrestige.config.data.locale.Locale;
import com.github.lukesky19.skyPrestige.config.manager.gui.GUIConfigManager;
import com.github.lukesky19.skyPrestige.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.config.manager.prestige.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This class creates the main /skyprestige command to register.
 */
public class SkyPrestigeCommand {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull PrestigeManager prestigeManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull DatabaseManager databaseManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param prestigeManager A {@link PrestigeManager} instance.
     * @param islandDataManager A {@link IslandDataManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public SkyPrestigeCommand(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull PrestigeManager prestigeManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull GUIManager guiManager,
            @NotNull DatabaseManager databaseManager) {
        this.skyPrestige = skyPrestige;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.prestigeManager = prestigeManager;
        this.islandDataManager = islandDataManager;
        this.guiManager = guiManager;
        this.databaseManager = databaseManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} to register using the Lifecycle API for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} to register using the Lifecycle API for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        ComponentLogger logger = skyPrestige.getComponentLogger();

        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skyprestige")
            .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige"))
            .executes(ctx -> {
                Locale locale = localeManager.getLocale();

                if(ctx.getSource().getSender() instanceof Player player) {
                    prestigeManager.prestigeIsland(player);

                    return 1;
                } else {
                    logger.error(AdventureUtil.deserialize(locale.prestigePlayerOnly()));
                    return 0;
                }
            });

        ExchangeCommand exchangeCommand = new ExchangeCommand(skyPrestige, settingsManager, localeManager, guiConfigManager, guiManager, islandDataManager);
        FixCommand fixCommand = new FixCommand(skyPrestige);
        HelpCommand helpCommand = new HelpCommand(skyPrestige, localeManager);
        InfoCommand infoCommand = new InfoCommand(skyPrestige, localeManager, guiConfigManager, guiManager);
        PrestigeLevelCommand prestigeLevelCommand = new PrestigeLevelCommand(skyPrestige, localeManager, islandDataManager);
        ReloadCommand reloadCommand = new ReloadCommand(skyPrestige, localeManager);
        ProgressCommand progressCommand = new ProgressCommand(skyPrestige, settingsManager, localeManager, guiConfigManager, prestigeConfigManager, guiManager, islandDataManager);
        RequirementsCommand requirementsCommand = new RequirementsCommand(skyPrestige, settingsManager, localeManager, prestigeConfigManager, islandDataManager);
        RewardsCommand rewardsCommand = new RewardsCommand(skyPrestige, localeManager, guiConfigManager, prestigeConfigManager, guiManager, islandDataManager);
        PrestigePointsCommand prestigePointsCommand = new PrestigePointsCommand(skyPrestige, localeManager, islandDataManager);
        ValuesCommand valuesCommand = new ValuesCommand(skyPrestige, localeManager, guiConfigManager, guiManager);
        VaultCommand vaultCommand = new VaultCommand(skyPrestige, localeManager, guiConfigManager, guiManager, islandDataManager, databaseManager, settingsManager);

        builder.then(prestigeLevelCommand.createCommand());
        builder.then(fixCommand.createCommand());
        builder.then(helpCommand.createCommand());
        builder.then(infoCommand.createCommand());
        builder.then(reloadCommand.createCommand());
        builder.then(progressCommand.createCommand());
        builder.then(requirementsCommand.createCommand());
        builder.then(rewardsCommand.createCommand());
        builder.then(prestigePointsCommand.createCommand());
        builder.then(exchangeCommand.createCommand());
        builder.then(valuesCommand.createCommand());
        builder.then(vaultCommand.createCommand());

        return builder.build();
    }
}
