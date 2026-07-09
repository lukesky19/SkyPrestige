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
package com.github.lukesky19.skyPrestige.listener.island;

import com.github.lukesky19.skyPrestige.configuration.data.opt_in_out.OptInOutConfig;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.OptInConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.OptOutConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.processor.reset.SettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandResetEvent;
import world.bentobox.bentobox.api.events.team.TeamJoinEvent;
import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;

/**
 * This class listens for events related to islands.
 */
public class IslandListener implements Listener {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull ComponentLogger logger;

    private final @NonNull SettingsManager settingsManager;
    private final @NonNull OptInConfigManager optInConfigManager;
    private final @NonNull OptOutConfigManager optOutConfigManager;

    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull GUIManager guiManager;

    private final @NonNull SettingsProcessor settingsProcessor;
    private final @NonNull RewardsProcessor rewardsProcessor;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param optInConfigManager An {@link OptInConfigManager} instance.
     * @param optOutConfigManager An {@link OptOutConfigManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param settingsProcessor A {@link SettingsProcessor} instance.
     * @param rewardsProcessor A {@link RewardsProcessor} instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public IslandListener(
            @NonNull SkyPlugin plugin,
            @NonNull SettingsManager settingsManager,
            @NonNull OptInConfigManager optInConfigManager,
            @NonNull OptOutConfigManager optOutConfigManager,
            @NonNull DatabaseManager databaseManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull SettingsProcessor settingsProcessor,
            @NonNull RewardsProcessor rewardsProcessor,
            @NonNull GUIManager guiManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.settingsManager = settingsManager;
        this.optInConfigManager = optInConfigManager;
        this.optOutConfigManager = optOutConfigManager;
        this.databaseManager = databaseManager;
        this.islandDataManager = islandDataManager;
        this.settingsProcessor = settingsProcessor;
        this.rewardsProcessor = rewardsProcessor;
        this.guiManager = guiManager;
    }

    /**
     * Listens to when an island is created and if the island was not a part of a prestige, adds the island id to the database.
     * @param islandCreatedEvent An {@link IslandCreatedEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandCreation(IslandCreatedEvent islandCreatedEvent) {
        Island island = islandCreatedEvent.getIsland();
        String islandId = island.getUniqueId();

        databaseManager.getIslandIdsTable().insertIslandId(island.getUniqueId());

        IslandData islandData = new IslandData(islandId);
        islandDataManager.setData(islandId, islandData);

        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.warn(AdventureUtility.plain("Unable to apply starting prestige settings due to invalid plugin settings."));
            return;
        }

        if(settings.startOptedOut()) {
            islandData.setPrestigeExempt(true);

            OptInOutConfig optOutConfig = optOutConfigManager.getConfiguration();
            if(optOutConfig == null) {
                logger.warn(AdventureUtility.plain("Unable to apply starting prestige rewards due invalid opt-out config."));
                return;
            }

            UUID playerId = islandCreatedEvent.getPlayerUUID();
            Player player = plugin.getServer().getPlayer(playerId);
            if(player == null || !player.isOnline() || !player.isConnected()) {
                logger.warn(AdventureUtility.plain("Unable to apply starting prestige rewards due to the player being invalid."));
                return;
            }

            if(settings.applyOptOutRewardsForInitialIslands()) {
                rewardsProcessor.processEarlyRewards(player, List.of(player), List.of(), optOutConfig.rewardConfig());
                rewardsProcessor.processPostRewards(player, island, List.of(player), List.of(), optOutConfig.rewardConfig());
            }
        } else {
            if(settings.applyOptInRewardsForInitialIslands()) {
                OptInOutConfig optInConfig = optInConfigManager.getConfiguration();
                if(optInConfig == null) {
                    logger.warn(AdventureUtility.plain("Unable to apply starting prestige rewards due invalid opt-in config."));
                    return;
                }

                UUID playerId = islandCreatedEvent.getPlayerUUID();
                Player player = plugin.getServer().getPlayer(playerId);
                if(player == null || !player.isOnline() || !player.isConnected()) {
                    logger.warn(AdventureUtility.plain("Unable to apply starting prestige rewards due to the player being invalid."));
                    return;
                }

                if(settings.applyOptOutRewardsForInitialIslands()) {
                    rewardsProcessor.processEarlyRewards(player, List.of(player), List.of(), optInConfig.rewardConfig());
                    rewardsProcessor.processPostRewards(player, island, List.of(player), List.of(), optInConfig.rewardConfig());
                }
            }
        }
    }

    /**
     * Listens to when an island is reset and if the island was not a part of a prestige, and resets the island's prestige points and prestige level if configured.
     * @param islandResetEvent An {@link IslandResetEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandReset(IslandResetEvent islandResetEvent) {
        UUID playerId = islandResetEvent.getPlayerUUID();
        Player player = plugin.getServer().getPlayer(playerId);
        if(player == null || !player.isOnline() || !player.isConnected()) return;
        Island oldIsland = islandResetEvent.getOldIsland();
        Island newIsland = islandResetEvent.getIsland();

        // Close any open GUIs
        guiManager.closeGUIsByIslandId(oldIsland.getUniqueId());

        settingsProcessor.onIslandReset(player, oldIsland, newIsland);
    }

    /**
     * Listens to when a player joins an island's team and reverts early rewards and applies settings.
     * @param teamJoinEvent A {@link TeamJoinEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandTeamJoin(TeamJoinEvent teamJoinEvent) {
        UUID playerId = teamJoinEvent.getPlayerUUID();
        Player player = plugin.getServer().getPlayer(playerId);
        Island island = teamJoinEvent.getIsland();
        IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) return;

        rewardsProcessor.revertEarlyRewards(playerId);

        settingsProcessor.onTeamJoin(island, islandData, playerId, player);
    }

    /**
     * Listens to when a player leaves an island's team and reverts early rewards and applies settings.
     * @param teamLeaveEvent A {@link TeamLeaveEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandTeamLeave(TeamLeaveEvent teamLeaveEvent) {
        UUID playerId = teamLeaveEvent.getPlayerUUID();
        Player player = plugin.getServer().getPlayer(playerId);
        Island island = teamLeaveEvent.getIsland();

        rewardsProcessor.revertEarlyRewards(playerId);

        settingsProcessor.onTeamLeave(island, playerId, player);
    }

    /**
     * Listens to when a player is kicked from an island's team and reverts early rewards and applies settings.
     * @param teamKickEvent A {@link TeamKickEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandTeamKick(TeamKickEvent teamKickEvent) {
        UUID playerId = teamKickEvent.getPlayerUUID();
        Player player = plugin.getServer().getPlayer(playerId);
        Island island = teamKickEvent.getIsland();

        rewardsProcessor.revertEarlyRewards(playerId);

        settingsProcessor.onTeamKick(island, playerId, player);
    }
}