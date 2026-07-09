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

import com.github.lukesky19.skyPrestige.commands.admin.*;
import com.github.lukesky19.skyPrestige.commands.shared.MultiplierCommand;
import com.github.lukesky19.skyPrestige.commands.user.*;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.*;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.data.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeExemptionManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeManager;
import com.github.lukesky19.skyPrestige.protection.ProtectionOrbManager;
import com.github.lukesky19.skyPrestige.requirements.RequirementsManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

/**
 * This class creates the main /skyprestige command to register.
 */
public class SkyPrestigeCommand {
    private final @NonNull SkyPlugin plugin;

    private final @NonNull SettingsManager settingsManager;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIConfigManager guiConfigManager;
    private final @NonNull PrestigePointsConfigManager prestigePointsConfigManager;
    private final @NonNull PrestigeConfigManager prestigeConfigManager;
    private final @NonNull OptInConfigManager optInConfigManager;
    private final @NonNull OptOutConfigManager optOutConfigManager;
    private final @NonNull VaultConfigManager vaultConfigManager;

    private final @NonNull PrestigeManager prestigeManager;
    private final @NonNull PrestigeExemptionManager prestigeExemptionManager;
    private final @NonNull RequirementsManager requirementsManager;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull LeaderboardManager leaderboardManager;
    private final @NonNull GUIManager guiManager;
    private final @NonNull DialogManager dialogManager;
    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull ProtectionOrbManager protectionOrbManager;
    private final @NonNull MultiplierManager multiplayerManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param optInConfigManager An {@link OptInConfigManager} instance.
     * @param optOutConfigManager An {@link OptOutConfigManager} instance.
     * @param prestigeManager A {@link PrestigeManager} instance.
     * @param prestigeExemptionManager A {@link PrestigeExemptionManager} instance.
     * @param requirementsManager A {@link RequirementsManager} instance.
     * @param islandDataManager A {@link IslandDataManager} instance.
     * @param leaderboardManager A {@link LeaderboardManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param dialogManager A {@link DialogManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param vaultConfigManager A {@link VaultConfigManager} instance.
     * @param protectionOrbManager A {@link ProtectionOrbManager}.
     * @param multiplayerManager A {@link MultiplierManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public SkyPrestigeCommand(
            @NonNull SkyPlugin plugin,
            @NonNull SettingsManager settingsManager,
            @NonNull LocaleManager localeManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NonNull PrestigeConfigManager prestigeConfigManager,
            @NonNull OptInConfigManager optInConfigManager,
            @NonNull OptOutConfigManager optOutConfigManager,
            @NonNull PrestigeManager prestigeManager,
            @NonNull PrestigeExemptionManager prestigeExemptionManager,
            @NonNull RequirementsManager requirementsManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull LeaderboardManager leaderboardManager,
            @NonNull GUIManager guiManager,
            @NonNull DialogManager dialogManager,
            @NonNull DatabaseManager databaseManager,
            @NonNull VaultConfigManager vaultConfigManager,
            @NonNull ProtectionOrbManager protectionOrbManager,
            @NonNull MultiplierManager multiplayerManager,
            @NonNull HookManager hookManager) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.prestigePointsConfigManager = prestigePointsConfigManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.optInConfigManager = optInConfigManager;
        this.optOutConfigManager = optOutConfigManager;
        this.prestigeManager = prestigeManager;
        this.prestigeExemptionManager = prestigeExemptionManager;
        this.requirementsManager = requirementsManager;
        this.islandDataManager = islandDataManager;
        this.leaderboardManager = leaderboardManager;
        this.guiManager = guiManager;
        this.dialogManager = dialogManager;
        this.databaseManager = databaseManager;
        this.vaultConfigManager = vaultConfigManager;
        this.protectionOrbManager = protectionOrbManager;
        this.multiplayerManager = multiplayerManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} to register using the Lifecycle API for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} to register using the Lifecycle API for the /skyprestige command.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        ComponentLogger logger = plugin.getComponentLogger();

        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skyprestige")
            .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige"))
            .executes(ctx -> {
                Locale locale = localeManager.getConfiguration();

                if(ctx.getSource().getSender() instanceof Player player) {
                    prestigeManager.prestigeIsland(player);

                    return 1;
                } else {
                    logger.error(AdventureUtility.deserialize(locale.prestigeMessages().prestigePlayerOnly()));
                    return 0;
                }
            });

        ExchangeCommand exchangeCommand = new ExchangeCommand(plugin, settingsManager, localeManager, guiConfigManager, guiManager, islandDataManager, hookManager, prestigeManager, prestigeExemptionManager);
        ExemptCommand exemptCommand = new ExemptCommand(plugin, localeManager, islandDataManager, hookManager);
        HelpCommand helpCommand = new HelpCommand(logger, localeManager);
        InfoCommand infoCommand = new InfoCommand(plugin, localeManager, guiConfigManager, guiManager);
        LeaderboardCommand leaderboardCommand = new LeaderboardCommand(localeManager, leaderboardManager);
        ManageIslandCommand manageIslandCommand = new ManageIslandCommand(plugin, localeManager, prestigeConfigManager, islandDataManager, multiplayerManager, hookManager, dialogManager);
        MultiplierCommand multiplierCommand = new MultiplierCommand(plugin, localeManager, multiplayerManager, dialogManager, hookManager);
        OptInCommand optInCommand = new OptInCommand(localeManager, islandDataManager, hookManager, prestigeExemptionManager);
        OptOutCommand optOutCommand = new OptOutCommand(localeManager, islandDataManager, hookManager, prestigeExemptionManager);
        PrestigeLevelCommand prestigeLevelCommand = new PrestigeLevelCommand(plugin, localeManager, islandDataManager, hookManager);
        ReloadCommand reloadCommand = new ReloadCommand(plugin, localeManager);
        ProtectionOrbCommand protectionOrbCommand = new ProtectionOrbCommand(localeManager, protectionOrbManager);
        RequirementsCommand requirementsCommand = new RequirementsCommand(plugin, localeManager, guiConfigManager, prestigeConfigManager, guiManager, requirementsManager,  islandDataManager, hookManager);
        RewardsCommand rewardsCommand = new RewardsCommand(plugin, localeManager, guiConfigManager, prestigeConfigManager, optInConfigManager, optOutConfigManager, guiManager, islandDataManager, hookManager);
        UnExemptCommand unExemptCommand = new UnExemptCommand(plugin, localeManager, islandDataManager);
        PrestigePointsCommand prestigePointsCommand = new PrestigePointsCommand(plugin, localeManager, islandDataManager, hookManager);
        ValuesCommand valuesCommand = new ValuesCommand(plugin, localeManager, guiConfigManager, guiManager, prestigePointsConfigManager);
        VaultCommand vaultCommand = new VaultCommand(plugin, localeManager, guiConfigManager, vaultConfigManager, guiManager, islandDataManager, databaseManager, prestigeManager, prestigeExemptionManager);

        builder.then(exchangeCommand.createCommand());
        builder.then(prestigeLevelCommand.createCommand());
        builder.then(exemptCommand.createCommand());
        builder.then(helpCommand.createCommand());
        builder.then(infoCommand.createCommand());
        builder.then(leaderboardCommand.createCommand());
        builder.then(manageIslandCommand.createCommand());
        builder.then(multiplierCommand.createCommand());
        builder.then(optInCommand.createCommand());
        builder.then(optOutCommand.createCommand());
        builder.then(reloadCommand.createCommand());
        builder.then(protectionOrbCommand.createCommand());
        builder.then(requirementsCommand.createCommand());
        builder.then(rewardsCommand.createCommand());
        builder.then(unExemptCommand.createCommand());
        builder.then(prestigePointsCommand.createCommand());
        builder.then(valuesCommand.createCommand());
        builder.then(vaultCommand.createCommand());

        return builder.build();
    }
}