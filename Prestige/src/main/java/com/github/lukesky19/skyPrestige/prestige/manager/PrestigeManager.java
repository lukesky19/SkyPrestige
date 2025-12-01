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
package com.github.lukesky19.skyPrestige.prestige.manager;

import com.github.lukesky19.skyPrestige.configuration.data.interfaces.PlayerSettings;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.gui.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.prestige.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.core.abstracts.SkyPlugin;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.island.IslandResetData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.gui.BlueprintGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.hook.hooks.BSkyBlockHook;
import com.github.lukesky19.skyPrestige.hook.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.prestige.validator.PrestigeValidator;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.player.PlayerSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.prestige.PrestigeRewardsProcessor;
import com.github.lukesky19.skyPrestige.processor.prestige.PrestigeSettingsProcessor;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
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
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull HookManager hookManager;

    private final @NotNull PrestigeValidator prestigeValidator;
    private final @NotNull PrestigeSettingsProcessor prestigeSettingsProcessor;
    private final @NotNull PrestigeRewardsProcessor prestigeRewardsProcessor;
    private final @NotNull PlayerSettingsProcessor playerSettingsProcessor;

    private final @NotNull List<String> prestigedIslandIds = new ArrayList<>();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param playerSettingsProcessor A {@link PlayerSettingsProcessor} instance.
     */
    public PrestigeManager(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull GUIManager guiManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull IslandSettingsProcessor islandSettingsProcessor,
            @NotNull PlayerSettingsProcessor playerSettingsProcessor) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.databaseManager = databaseManager;
        this.guiManager = guiManager;
        this.hookManager = hookManager;
        this.playerSettingsProcessor = playerSettingsProcessor;

        this.prestigeValidator = new PrestigeValidator(plugin, localeManager, prestigeConfigManager, islandDataManager, hookManager);
        this.prestigeSettingsProcessor = new PrestigeSettingsProcessor(islandDataManager, databaseManager, islandSettingsProcessor, playerSettingsProcessor);
        this.prestigeRewardsProcessor = new PrestigeRewardsProcessor(plugin, hookManager);
    }

    /**
     * Checks if an island is in the process of being prestige.
     * @param islandId The island id to check.
     * @return true if the island is being prestiged, otherwise false.
     */
    public boolean isIslandIdPrestiged(@NotNull String islandId) {
        return prestigedIslandIds.contains(islandId);
    }

    /**
     * Adds an island id to the list of islands in the process of being prestiged.
     * @param islandId The island id to add.
     */
    public void addPrestigedIslandId(@NotNull String islandId) {
        prestigedIslandIds.add(islandId);
    }

    /**
     * Removes an island id from the list of islands in the process of being prestiged.
     * @param islandId The island id to remove.
     */
    public void removePrestigedIslandId(@NotNull String islandId) {
        prestigedIslandIds.removeIf(listIslandId -> listIslandId.equals(islandId));
    }

    /**
     * Checks if the player's island can be prestiged and opens the blueprint selection GUI or displays an error.
     * @param player The {@link Player}.
     */
    public void prestigeIsland(@NotNull Player player) {
        Settings settings = settingsManager.getSettings();
        if(settings == null || settings.scaleFormula() == null) return;
        @NotNull Locale locale = localeManager.getLocale();

        // Check if the player is in a world managed by a GameModeAddon
        @Nullable GameModeAddon gameModeAddon = prestigeValidator.validateGameModeAddon(player);
        if(gameModeAddon == null) return;

        // Check if the player is on an island
        @Nullable Island island = prestigeValidator.validateIsland(player);
        if(island == null) return;

        // Check if the player attempting to prestige owns the island or is a member
        if(!prestigeValidator.isPlayerIslandOwnerOrMember(player, island)) return;

        // Get the island data for the island.
        @Nullable IslandData islandData = prestigeValidator.validateIslandData(player, island);
        if(islandData == null) return;

        // Get the next prestige level and the config for that level
        int nextPrestigeLevel = islandData.getPrestigeLevel() + 1;
        // If the prestige config is null, the player is at the max prestige level
        @Nullable PrestigeConfig prestigeConfig = prestigeValidator.validatePrestigeConfig(player, nextPrestigeLevel);
        if(prestigeConfig == null) return;

        // Check if the base prestige points are valid
        @Nullable Double basePrestigePoints = prestigeValidator.validateBasePrestigePoints(
                player,
                prestigeConfig.requiredPrestigePoints(),
                nextPrestigeLevel);
        if(basePrestigePoints == null) return;

        // Check if the player's island has enough prestige points to prestige
        if(!prestigeValidator.hasRequiredPrestigePoints(
                player,
                islandData,
                settings.scaleFormula(),
                prestigeConfig.scaleFactor(),
                island.getMemberSet().size(),
                basePrestigePoints))
            return;

        // Open the blueprint GUI
        openBlueprintGUI(
                locale,
                player,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                nextPrestigeLevel);
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
     */
    private void openBlueprintGUI(
            @NotNull Locale locale,
            @NotNull Player player,
            @NotNull Island island,
            @NotNull com.github.lukesky19.skyPrestige.data.island.IslandData islandData,
            @NotNull GameModeAddon gameModeAddon,
            @NotNull PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        IslandResetData islandResetData = new IslandResetData(player, User.getInstance(player), island, islandData, gameModeAddon, prestigeConfig, prestigeLevel);

        // Let the player select a blueprint for the prestige
        BlueprintGUI gui = new BlueprintGUI(
                plugin,
                localeManager,
                guiConfigManager,
                guiManager,
                hookManager,
                islandResetData,
                data -> {
                    if(data.getBlueprint() == null
                            || data.getPrestigeConfig() == null
                            || data.getPrestigeLevel() == null
                            || data.getPrestigeLevel() == -1) return;
                    prestigeIsland(data.getPlayer(), data.getUser(), data.getOldIsland(), data.getOldIslandData(), data.getGameModeAddon(), data.getBlueprint().getUniqueId(), data.getPrestigeConfig(), data.getPrestigeLevel());
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
     */
    public void prestigeIsland(
            @NotNull Player player,
            @NotNull User user,
            @NotNull Island oldIsland,
            @NotNull IslandData oldIslandData,
            @NotNull GameModeAddon gameModeAddon,
            @NotNull String blueprintName,
            @NotNull PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;
        if(settings.scaleFormula() == null) return;
        Locale locale = localeManager.getLocale();
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return;
        String oldIslandId = oldIsland.getUniqueId();

        // Check if the base prestige points are valid
        @Nullable Double basePrestigePoints = prestigeValidator.validateBasePrestigePoints(
                player,
                prestigeConfig.requiredPrestigePoints(),
                prestigeLevel);
        if(basePrestigePoints == null) return;

        // Get the required prestige points to prestige the island
        double requiredPrestigePoints = prestigeValidator.calculateRequiredPrestigePoints(settings.scaleFormula(), prestigeConfig.scaleFactor(), oldIsland.getMemberSet().size(), basePrestigePoints);

        // Check if the player's island has enough prestige points to prestige
        if(!prestigeValidator.hasRequiredPrestigePoints(player, oldIslandData, requiredPrestigePoints))
            return;

        // Mark the Island id as being prestiged
        addPrestigedIslandId(oldIslandId);
        // Create the new island
        @Nullable Island newIsland = bentoBoxHook.resetIsland(user, gameModeAddon, oldIsland, blueprintName);
        if(newIsland == null) {
            // If the island failed to reset, remove the island id as a prestiged island id.
            removePrestigedIslandId(oldIslandId);
            return;
        }
        IslandData newIslandData = oldIslandData.clone();

        // Get online island member's players
        List<Player> onlineIslandMembers = newIsland.getMemberSet().stream()
                .map(plugin.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .filter(memberPlayer -> memberPlayer.isOnline() && memberPlayer.isConnected())
                .toList();
        // Get offline island member's players
        List<OfflinePlayer> offlineIslandMembers = newIsland.getMemberSet()
                .stream()
                .map(memberId -> player.getServer().getOfflinePlayer(memberId))
                .filter(OfflinePlayer::hasPlayedBefore)
                .toList();

        // Process PrestigeSettings
        prestigeSettingsProcessor.processPrestigeSettings(player, oldIsland, newIsland, onlineIslandMembers, offlineIslandMembers, newIslandData, requiredPrestigePoints, prestigeConfig.prestigeSettings());

        // Clean up the old island
        bentoBoxHook.deleteIsland(player.getUniqueId(), oldIsland);

        // Process prestige rewards
        prestigeRewardsProcessor.processPrestigeRewards(player, newIsland, onlineIslandMembers, prestigeConfig.rewardConfig(), prestigeLevel);

        // Send the plugin's teleport notice if BSkyBlock teleports the player to the island upon creation
        BSkyBlockHook bSkyBlockHook = hookManager.getHook(BSkyBlockHook.class);
        if(bSkyBlockHook.isHooked()) {
            if(bSkyBlockHook.isTeleportPlayerToIslandUponIslandCreation()) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandMemberIslandTeleportNotice()));
            }
        }
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
            if(!player.isOnline() || !player.isConnected()) return;
            @NotNull Map<Integer, PrestigeConfig> prestigeConfigMap = prestigeConfigManager.getPrestigeConfigMap(prestigeLevels);
            if (prestigeConfigMap.isEmpty()) return;

            @Nullable Settings settings = settingsManager.getSettings();
            if (settings == null) {
                logger.error(AdventureUtil.deserialize("Unable to process offline prestige for player " + player.getName() + " due to invalid plugin settings."));
                return;
            }

            prestigeConfigMap.forEach((prestigeLevel, prestigeConfig) -> {
                PrestigeConfig.PrestigeSettings prestigeSettings = prestigeConfig.prestigeSettings();
                PlayerSettings playerSettings = prestigeSettings.playerSettings();
                PrestigeConfig.RewardConfig rewardConfig = prestigeConfig.rewardConfig();

                playerSettingsProcessor.processPlayerSettingsOnLogin(
                        playerSettings,
                        player,
                        prestigeSettings.startingMoney(),
                        prestigeSettings.giveStartingMoneyToAllIslandMembers());

                prestigeRewardsProcessor.processPrestigeRewardsOnLogin(player, rewardConfig, prestigeLevel);
            });

            databaseManager.getOfflinePrestigeTable().removeOfflinePrestige(playerId);
        });
    }
}