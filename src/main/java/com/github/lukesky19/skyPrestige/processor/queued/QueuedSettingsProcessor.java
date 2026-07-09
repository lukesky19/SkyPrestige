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
package com.github.lukesky19.skyPrestige.processor.queued;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.configuration.data.opt_in_out.OptInOutConfig;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.interfaces.IResetSettings;
import com.github.lukesky19.skyPrestige.configuration.manager.OptInConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.OptOutConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.reset.QueuedSettings;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.QueuedSettingsTable;
import com.github.lukesky19.skyPrestige.processor.player.PlayerSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skylib.paper.api.placeholderapi.PlaceholderAPIUtil;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages processing settings for prestige, opt-in, opt-out, island reset, team leave, or team kick when the player was offline.
 */
public class QueuedSettingsProcessor {
    private final @NonNull SkyPrestige plugin;
    private final @NonNull DatabaseManager databaseManager;

    private final @NonNull SettingsManager settingsManager;
    private final @NonNull PrestigeConfigManager prestigeConfigManager;
    private final @NonNull OptInConfigManager optInConfigManager;
    private final @NonNull OptOutConfigManager optOutConfigManager;

    private final @NonNull PlayerSettingsProcessor playerSettingsProcessor;
    private final @NonNull RewardsProcessor rewardsProcessor;

    /**
     * Constructor
     * @param plugin A {@link SkyPrestige} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param optInConfigManager An {@link OptInConfigManager} instance.
     * @param optOutConfigManager An {@link OptOutConfigManager} instance.
     * @param playerSettingsProcessor A {@link PlayerSettingsProcessor} instance.
     * @param rewardsProcessor A {@link RewardsProcessor} instance.
     */
    public QueuedSettingsProcessor(
            @NonNull SkyPrestige plugin,
            @NonNull DatabaseManager databaseManager,
            @NonNull SettingsManager settingsManager,
            @NonNull PrestigeConfigManager prestigeConfigManager,
            @NonNull OptInConfigManager optInConfigManager,
            @NonNull OptOutConfigManager optOutConfigManager,
            @NonNull PlayerSettingsProcessor playerSettingsProcessor,
            @NonNull RewardsProcessor rewardsProcessor) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.settingsManager = settingsManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.optInConfigManager = optInConfigManager;
        this.optOutConfigManager = optOutConfigManager;
        this.playerSettingsProcessor = playerSettingsProcessor;
        this.rewardsProcessor = rewardsProcessor;
    }

    /**
     * Process any queued settings to apply for the player.
     * @param player The {@link Player}.
     */
    public void processQueuedSettings(@NonNull Player player) {
        UUID playerId = player.getUniqueId();
        QueuedSettingsTable queuedSettingsTable = databaseManager.getQueuedSettingsTable();
        CompletableFuture<List<QueuedSettings>> future = queuedSettingsTable.getQueuedSettings(playerId);

        future.thenAccept(statusChangesList -> {
            if(statusChangesList.isEmpty()) return;

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if(!player.isOnline() || !player.isConnected()) return;

                statusChangesList.forEach(queuedSettings -> {
                    switch(queuedSettings.settingsType()) {
                        case PRESTIGE -> {
                            PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(queuedSettings.prestigeLevel());
                            if(prestigeConfig != null) {
                                IResetSettings resetSettings = prestigeConfig.prestigeSettings();
                                RewardConfig rewardConfig = prestigeConfig.rewardConfig();

                                playerSettingsProcessor.processPlayerSettingsOnLogin(
                                        resetSettings.getPlayerSettings(),
                                        player,
                                        resetSettings.getStartingMoney(),
                                        resetSettings.giveStartingMoneyToAllIslandMembers());

                                rewardsProcessor.processRewardsOnLogin(player, rewardConfig);
                            }
                        }

                        case OPT_IN -> {
                            OptInOutConfig optInConfig = optInConfigManager.getConfiguration();
                            if(optInConfig != null) {
                                IResetSettings resetSettings = optInConfig.resetSettings();

                                playerSettingsProcessor.processPlayerSettingsOnLogin(
                                        resetSettings.getPlayerSettings(),
                                        player,
                                        resetSettings.getStartingMoney(),
                                        resetSettings.giveStartingMoneyToAllIslandMembers());

                                rewardsProcessor.processRewardsOnLogin(player, optInConfig.rewardConfig());
                            }
                        }

                        case OPT_OUT -> {
                            OptInOutConfig optOutConfig = optOutConfigManager.getConfiguration();
                            if(optOutConfig != null) {
                                IResetSettings resetSettings = optOutConfig.resetSettings();

                                playerSettingsProcessor.processPlayerSettingsOnLogin(
                                        resetSettings.getPlayerSettings(),
                                        player,
                                        resetSettings.getStartingMoney(),
                                        resetSettings.giveStartingMoneyToAllIslandMembers());

                                rewardsProcessor.processRewardsOnLogin(player, optOutConfig.rewardConfig());
                            }
                        }

                        case ISLAND_RESET -> {
                            Settings settings = settingsManager.getConfiguration();
                            if(settings != null) {
                                IResetSettings resetSettings = settings.islandResetSettings();

                                playerSettingsProcessor.processPlayerSettingsOnLogin(
                                        resetSettings.getPlayerSettings(),
                                        player,
                                        resetSettings.getStartingMoney(),
                                        resetSettings.giveStartingMoneyToAllIslandMembers());

                                // Execute commands for player
                                Server server = plugin.getServer();
                                ConsoleCommandSender sender = server.getConsoleSender();
                                resetSettings.getCommands().forEach(command ->
                                        server.dispatchCommand(sender, PlaceholderAPIUtil.parsePlaceholders(player, command)));
                            }
                        }

                        case TEAM_JOIN -> {
                            Settings settings = settingsManager.getConfiguration();
                            if(settings != null) {
                                IResetSettings resetSettings = settings.teamJoinSettings();

                                playerSettingsProcessor.processPlayerSettingsOnLogin(
                                        resetSettings.getPlayerSettings(),
                                        player,
                                        resetSettings.getStartingMoney(),
                                        resetSettings.giveStartingMoneyToAllIslandMembers());

                                // Execute commands for player
                                Server server = plugin.getServer();
                                ConsoleCommandSender sender = server.getConsoleSender();
                                resetSettings.getCommands().forEach(command ->
                                        server.dispatchCommand(sender, PlaceholderAPIUtil.parsePlaceholders(player, command)));

                                if(queuedSettings.prestigeLevel() == -1) {
                                    if(settings.applyOptOutRewardsForInitialIslands()) {
                                        OptInOutConfig optOutConfig = optOutConfigManager.getConfiguration();
                                        if (optOutConfig != null) {
                                            rewardsProcessor.processRetroactive(player, playerId, optOutConfig.rewardConfig());
                                        }
                                    }
                                } else {
                                    if(settings.applyOptInRewardsForInitialIslands()) {
                                        OptInOutConfig optInConfig = optInConfigManager.getConfiguration();
                                        if (optInConfig != null) {
                                            rewardsProcessor.processRetroactive(player, playerId, optInConfig.rewardConfig());
                                        }
                                    }

                                    if(queuedSettings.prestigeLevel() > 0) {
                                        for(int i = 1; i <= queuedSettings.prestigeLevel(); i++) {
                                            PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(i);
                                            if(prestigeConfig == null) continue;

                                            rewardsProcessor.processRetroactiveOnLogin(player, prestigeConfig.rewardConfig());
                                        }
                                    }
                                }
                            }
                        }

                        case TEAM_LEAVE -> {
                            Settings settings = settingsManager.getConfiguration();
                            if(settings != null) {
                                IResetSettings resetSettings = settings.teamLeaveSettings();

                                playerSettingsProcessor.processPlayerSettingsOnLogin(
                                        resetSettings.getPlayerSettings(),
                                        player,
                                        resetSettings.getStartingMoney(),
                                        resetSettings.giveStartingMoneyToAllIslandMembers());

                                // Execute commands for player
                                Server server = plugin.getServer();
                                ConsoleCommandSender sender = server.getConsoleSender();
                                resetSettings.getCommands().forEach(command ->
                                        server.dispatchCommand(sender, PlaceholderAPIUtil.parsePlaceholders(player, command)));
                            }
                        }

                        case TEAM_KICK -> {
                            Settings settings = settingsManager.getConfiguration();
                            if(settings != null) {
                                IResetSettings resetSettings = settings.teamKickSettings();

                                playerSettingsProcessor.processPlayerSettingsOnLogin(
                                        resetSettings.getPlayerSettings(),
                                        player,
                                        resetSettings.getStartingMoney(),
                                        resetSettings.giveStartingMoneyToAllIslandMembers());

                                // Execute commands for player
                                Server server = plugin.getServer();
                                ConsoleCommandSender sender = server.getConsoleSender();
                                resetSettings.getCommands().forEach(command ->
                                        server.dispatchCommand(sender, PlaceholderAPIUtil.parsePlaceholders(player, command)));
                            }
                        }
                    }
                });

                queuedSettingsTable.clearQueuedSettings(playerId);
            }, 1L);
        });
    }
}