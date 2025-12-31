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
package com.github.lukesky19.skyPrestige.prestige;

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.ResetSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.player.PlayerSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.*;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.data.island.IslandResetData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.OfflinePrestigeTable;
import com.github.lukesky19.skyPrestige.gui.gui.BlueprintGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.island.IslandCreator;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.player.PlayerSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.math.EquationUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages the prestiging of islands.
 */
public class PrestigeManager {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;

    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull PrestigePointsConfigManager prestigePointsConfigManager;

    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;

    private final @NotNull IslandSettingsProcessor islandSettingsProcessor;
    private final @NotNull RewardsProcessor rewardsProcessor;
    private final @NotNull PlayerSettingsProcessor playerSettingsProcessor;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param playerSettingsProcessor A {@link PlayerSettingsProcessor} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param rewardsProcessor A {@link RewardsProcessor} instance.
     */
    public PrestigeManager(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull GUIManager guiManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull PlayerSettingsProcessor playerSettingsProcessor,
            @NotNull IslandSettingsProcessor islandSettingsProcessor,
            @NotNull RewardsProcessor rewardsProcessor) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.prestigePointsConfigManager = prestigePointsConfigManager;
        this.databaseManager = databaseManager;
        this.guiManager = guiManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;

        this.islandSettingsProcessor = islandSettingsProcessor;
        this.rewardsProcessor = rewardsProcessor;
        this.playerSettingsProcessor = playerSettingsProcessor;
    }

    /**
     * Checks if the player's island can be prestiged and opens the blueprint selection GUI or displays an error.
     * @param player The {@link Player}.
     */
    public void prestigeIsland(@NotNull Player player) {
        PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
        if(prestigePointsConfig == null || prestigePointsConfig.scaleFormula() == null) {
            logger.error(AdventureUtil.deserialize("Unable to prestige island for player " + player.getName() + " because of invalid prestige points config."));
            return;
        }

        @NotNull Locale locale = localeManager.getConfiguration();
        @NotNull BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        UUID playerId = player.getUniqueId();

        // Check if the player is in a world managed by a GameModeAddon
        Optional<GameModeAddon> optionalGameModeAddon = bentoBoxHook.getGameModeAddon(player.getWorld());
        if(optionalGameModeAddon.isEmpty()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigePlayerInWrongWorld()));
            return;
        }

        // Check if the player is on an island
        Optional<Island> optionalIsland = bentoBoxHook.getIslandAtLocation(player.getLocation());
        if(optionalIsland.isEmpty()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigePlayerNotOnIsland()));
            return;
        }
        Island island = optionalIsland.get();

        // Check if the player attempting to toggle the island's prestige status is not the owner or an island member.
        if((island.getOwner() == null || !island.getOwner().equals(playerId)) && !island.getMemberSet().contains(playerId)) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigePlayerNotMemberOrOwner()));
            return;
        }

        // Get the island data for the island.
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
            logger.error(AdventureUtil.deserialize("No Island data found for player " + player.getName() + "'s island. Island Id: " + island.getUniqueId()));
            return;
        }

        if(islandData.isPrestigeExempt()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeIslandOptedOut()));
            return;
        }

        // Get the next prestige level and the config for that level
        int nextPrestigeLevel = islandData.getPrestigeLevel() + 1;
        // If the prestige config is null, the player is at the max prestige level
        @Nullable PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(nextPrestigeLevel);
        if(prestigeConfig == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandPrestigeLevelMax()));
            return;
        }

        // Check if the base prestige points are valid
        @Nullable Double basePrestigePoints = prestigeConfig.requiredPrestigePoints();
        if(basePrestigePoints == null || basePrestigePoints <= 0) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeConfigRequirementError()));
            logger.error(AdventureUtil.deserialize("The required prestige points for prestige level " + nextPrestigeLevel + " is invalid."));
            return;
        }

        // Calculate the required prestige points for the prestige level.
        double requiredPrestigePoints = calculateRequiredPrestigePoints(prestigePointsConfig.scaleFormula(), prestigeConfig.scaleFactor(), island.getMemberSet().size(), basePrestigePoints);

        // Check if the player's island has enough prestige points to prestige
        if(lacksRequiredPrestigePoints(islandData, requiredPrestigePoints)) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeNotEnoughPrestigePoints()));
            return;
        }

        // Get online island member's players
        List<Player> onlineIslandMembers = island.getMemberSet().stream()
                .map(plugin.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .filter(memberPlayer -> memberPlayer.isOnline() && memberPlayer.isConnected())
                .toList();
        // Get offline island member's unique ids
        List<UUID> offlineIslandMembers = island.getMemberSet()
                .stream()
                .map(memberId -> player.getServer().getOfflinePlayer(memberId))
                .filter(offlinePlayer -> !offlinePlayer.isOnline() && !offlinePlayer.isConnected())
                .map(OfflinePlayer::getUniqueId)
                .toList();

        // Process early rewards (will be undone if cancelled)
        rewardsProcessor.processEarlyRewards(player, onlineIslandMembers, offlineIslandMembers, prestigeConfig.rewardConfig());

        // Open the blueprint GUI
        openBlueprintGUI(
                locale,
                player,
                island,
                islandData,
                optionalGameModeAddon.get(),
                prestigeConfig,
                nextPrestigeLevel,
                requiredPrestigePoints);
    }

    /**
     * Open the blueprint selection GUI for the player.
     * @param locale The plugin's {@link Locale}.
     * @param player The {@link Player} prestiging.
     * @param island The {@link Island} being prestiged.
     * @param islandData The {@link IslandData} for the island being prestiged.
     * @param gameModeAddon The {@link GameModeAddon}.
     * @param prestigeConfig The {@link PrestigeConfig}.
     * @param prestigeLevel The prestige level.
     * @param prestigePoints The prestige points required to prestige.
     */
    private void openBlueprintGUI(
            @NotNull Locale locale,
            @NotNull Player player,
            @NotNull Island island,
            @NotNull IslandData islandData,
            @NotNull GameModeAddon gameModeAddon,
            @NotNull PrestigeConfig prestigeConfig,
            int prestigeLevel,
            double prestigePoints) {
        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), player.getUniqueId());
        IslandResetData islandResetData = new IslandResetData(player, User.getInstance(player), island, islandData, gameModeAddon, prestigeConfig, prestigeLevel, prestigePoints);

        // Let the player select a blueprint for the prestige
        BlueprintGUI gui = new BlueprintGUI(
                plugin,
                guiManager,
                identifier,
                localeManager,
                guiConfigManager,
                hookManager,
                rewardsProcessor,
                islandResetData,
                data -> {
                    if(data.getBlueprint() == null
                            || data.getPrestigeConfig() == null
                            || data.getPrestigeLevel() == null
                            || data.getPrestigeLevel() == -1
                            || data.getPrestigePoints() == null
                            || data.getPrestigePoints() == -1) return;
                    prestigeIsland(data.getPlayer(), data.getUser(), data.getOldIsland(), data.getOldIslandData(),
                            data.getGameModeAddon(), data.getBlueprint().getUniqueId(), data.getPrestigeConfig(),
                            data.getPrestigeLevel(), data.getPrestigePoints());
                });

        // Create the GUI
        boolean creationResult = gui.create();
        if(!creationResult) {
            logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the blueprint GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        // Update the GUI
        boolean updateResult = gui.update();
        if(!updateResult) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the blueprint GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        // Open the GUI
        boolean openResult = gui.open();
        if(!openResult) {
            logger.error(AdventureUtil.deserialize("Unable to open the blueprint GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
        }
    }

    /**
     * Prestige the player's island.
     * The method {@link #prestigeIsland(Player)} should be run before this method.
     * @param player The {@link Player}.
     * @param user The {@link User} for the {@link Player}.
     * @param oldIsland The old {@link Island}.
     * @param oldIslandData The old island's {@link IslandData}.
     * @param gameModeAddon The {@link GameModeAddon}.
     * @param blueprintName The blueprint name to use.
     * @param prestigeConfig The {@link PrestigeConfig} for the next prestige level.
     * @param prestigeLevel The prestige level the island is moving to.
     * @param requiredPrestigePoints The required prestige points to prestige the island.
     */
    public void prestigeIsland(
            @NotNull Player player,
            @NotNull User user,
            @NotNull Island oldIsland,
            @NotNull IslandData oldIslandData,
            @NotNull GameModeAddon gameModeAddon,
            @NotNull String blueprintName,
            @NotNull PrestigeConfig prestigeConfig,
            int prestigeLevel,
            double requiredPrestigePoints) {
        @Nullable Settings settings = settingsManager.getConfiguration();
        if(settings == null) return;
        @NotNull Locale locale = localeManager.getConfiguration();

        // Check if the player's island has enough prestige points to prestige
        if(lacksRequiredPrestigePoints(oldIslandData, requiredPrestigePoints)) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeNotEnoughPrestigePoints()));
            return;
        }

        // Create the new island
        @Nullable Island newIsland = IslandCreator.builder(plugin, databaseManager, hookManager, islandSettingsProcessor)
                .user(user)
                .gameModeAddon(gameModeAddon)
                .oldIsland(oldIsland)
                .islandData(oldIslandData)
                .islandSettings(prestigeConfig.prestigeSettings().islandSettings())
                .name(blueprintName)
                .requiredPrestigePoints(requiredPrestigePoints)
                .prestigeLevel(prestigeLevel)
                .build();

        // If the new island failed to be created, log and error and return
        if(newIsland == null) {
            logger.error(AdventureUtil.deserialize("Island Creation failed for prestige."));
            return;
        }

        // Get online island member's players
        List<Player> onlineIslandMembers = newIsland.getMemberSet().stream()
                .map(plugin.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .filter(memberPlayer -> memberPlayer.isOnline() && memberPlayer.isConnected())
                .toList();
        // Get offline island member's unique ids
        List<UUID> offlineIslandMembers = newIsland.getMemberSet()
                .stream()
                .map(memberId -> player.getServer().getOfflinePlayer(memberId))
                .filter(offlinePlayer -> !offlinePlayer.isOnline() && !offlinePlayer.isConnected())
                .map(OfflinePlayer::getUniqueId)
                .toList();

        // Insert players that were offline on island prestige to give rewards later.
        OfflinePrestigeTable offlinePrestigeTable = databaseManager.getOfflinePrestigeTable();
        offlineIslandMembers.forEach(offlineMemberId -> offlinePrestigeTable.insertOfflinePrestige(offlineMemberId, newIsland.getUniqueId(), prestigeLevel));

        // Process Player Settings
        playerSettingsProcessor.processPlayerSettings(
                prestigeConfig.prestigeSettings().playerSettings(),
                player,
                onlineIslandMembers,
                offlineIslandMembers,
                prestigeConfig.prestigeSettings().startingMoney(),
                prestigeConfig.prestigeSettings().giveStartingMoneyToAllIslandMembers());

        // Process prestige rewards
        rewardsProcessor.processPostRewards(player, newIsland, onlineIslandMembers, offlineIslandMembers, prestigeConfig.rewardConfig(), prestigeLevel);
    }

    /**
     * Process any offline prestige settings and rewards for the player.
     * @param player The player who was offline during a prestige.
     */
    public void handleOfflinePrestiges(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        CompletableFuture<List<Integer>> future = databaseManager.getOfflinePrestigeTable().getPrestigeLevels(playerId);

        future.thenAccept(prestigeLevels -> {
            if(prestigeLevels.isEmpty()) return;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if(!player.isOnline() || !player.isConnected()) return;
                @NotNull Map<Integer, PrestigeConfig> prestigeConfigMap = prestigeConfigManager.getPrestigeConfigMapForLevels(prestigeLevels);
                if (prestigeConfigMap.isEmpty()) return;

                @Nullable Settings settings = settingsManager.getConfiguration();
                if(settings == null) {
                    logger.error(AdventureUtil.deserialize("Unable to process offline prestige for player " + player.getName() + " due to invalid plugin settings."));
                    return;
                }

                prestigeConfigMap.forEach((prestigeLevel, prestigeConfig) -> {
                    ResetSettings prestigeSettings = prestigeConfig.prestigeSettings();
                    PlayerSettings playerSettings = prestigeSettings.playerSettings();
                    RewardConfig rewardConfig = prestigeConfig.rewardConfig();

                    playerSettingsProcessor.processPlayerSettingsOnLogin(
                            playerSettings,
                            player,
                            prestigeSettings.startingMoney(),
                            prestigeSettings.giveStartingMoneyToAllIslandMembers());

                    rewardsProcessor.processRewardsOnLogin(player, rewardConfig, prestigeLevel);
                });

                databaseManager.getOfflinePrestigeTable().removeOfflinePrestige(playerId);
            }, 1L);
        });
    }

    /**
     * Calculate the prestige points required to prestige an island.
     * @param scaleFormula The scale formula from {@link PrestigePointsConfig#scaleFormula()}.
     * @param scaleFactor The scale factor from {@link PrestigeConfig#scaleFactor()}.
     * @param islandMemberCount The number of island members on the island's team.
     * @param basePrestigePoints The base required prestige points.
     * @return The required prestige points.
     */
    private double calculateRequiredPrestigePoints(
            @Nullable String scaleFormula,
            @Nullable Double scaleFactor,
            int islandMemberCount,
            double basePrestigePoints) {
        double requiredPoints;
        if(scaleFormula != null && scaleFactor != null && scaleFactor != 0) {
            HashMap<String, String> variables = new HashMap<>();
            variables.put("r", String.valueOf(basePrestigePoints));
            variables.put("p", String.valueOf(islandMemberCount));
            variables.put("k", String.valueOf(scaleFactor));

            requiredPoints = EquationUtil.evaluateEquation(scaleFormula, variables).intValue();
        } else {
            requiredPoints = basePrestigePoints;
        }

        return requiredPoints;
    }

    /**
     * Does the island lack the required prestige points to prestige?
     * @param islandData The {@link IslandData} for the island prestiging.
     * @param requiredPrestigePoints The prestige points required.
     * @return true if the island lacks the required prestige points, or false.
     */
    private boolean lacksRequiredPrestigePoints(@NotNull IslandData islandData, double requiredPrestigePoints) {
        return islandData.getPrestigePoints() < requiredPrestigePoints;
    }
}