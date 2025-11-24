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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.gui.BlueprintGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.BSkyBlockHook;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.locale.Locale;
import com.github.lukesky19.skyPrestige.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.prestige.config.PrestigeConfig;
import com.github.lukesky19.skyPrestige.prestige.config.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.prestige.processor.OfflinePrestigeProcessor;
import com.github.lukesky19.skyPrestige.prestige.processor.PrestigeRewardsProcessor;
import com.github.lukesky19.skyPrestige.prestige.processor.PrestigeSettingsProcessor;
import com.github.lukesky19.skyPrestige.prestige.validator.PrestigeValidator;
import com.github.lukesky19.skyPrestige.settings.Settings;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.island.IslandCache;
import world.bentobox.bentobox.managers.island.NewIsland;

import java.io.IOException;
import java.util.*;

/**
 * This class manages the prestiging of islands.
 */
public class PrestigeManager {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull IslandsManager islandsManager;

    private final @NotNull PrestigeValidator prestigeValidator;
    private final @NotNull PrestigeSettingsProcessor prestigeSettingsProcessor;
    private final @NotNull PrestigeRewardsProcessor prestigeRewardsProcessor;
    private final @NotNull OfflinePrestigeProcessor offlinePrestigeManager;

    private final @NotNull List<String> prestigedIslandIds = new ArrayList<>();

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PrestigeManager(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull GUIManager guiManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull HookManager hookManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.guiManager = guiManager;
        this.hookManager = hookManager;
        this.islandsManager = BentoBox.getInstance().getIslandsManager();

        this.prestigeValidator = new PrestigeValidator(skyPrestige, localeManager, prestigeConfigManager, islandDataManager);
        this.prestigeSettingsProcessor = new PrestigeSettingsProcessor(skyPrestige, localeManager, islandDataManager, databaseManager, hookManager);
        this.prestigeRewardsProcessor = new PrestigeRewardsProcessor(skyPrestige, hookManager);
        this.offlinePrestigeManager = new OfflinePrestigeProcessor(skyPrestige, settingsManager, localeManager, prestigeConfigManager, databaseManager, hookManager);
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
            @NotNull IslandData islandData,
            @NotNull GameModeAddon gameModeAddon,
            @NotNull PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        // Let the player select a blueprint for the prestige
        BlueprintGUI gui = new BlueprintGUI(
                skyPrestige,
                localeManager,
                guiConfigManager,
                this,
                guiManager,
                gameModeAddon,
                island,
                islandData,
                player,
                prestigeConfig,
                prestigeLevel);

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

        // Create the new island
        @Nullable Island newIsland = createNewIsland(gameModeAddon, user, oldIsland, blueprintName);
        if(newIsland == null) return;
        IslandData newIslandData = oldIslandData.clone();

        // Get online island member's players
        List<Player> onlineIslandMembers = newIsland.getMemberSet().stream()
                .map(skyPrestige.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .filter(memberPlayer -> memberPlayer.isOnline() && memberPlayer.isConnected())
                .toList();
        // Get offline island member's uuids
        List<UUID> offlineMemberIds = newIsland.getMemberSet()
                .stream()
                .filter(memberId -> {
                    @Nullable Player memberPlayer = skyPrestige.getServer().getPlayer(memberId);

                    return memberPlayer == null || !memberPlayer.isOnline() || !memberPlayer.isConnected();
                })
                .toList();

        // Process PrestigeSettings
        prestigeSettingsProcessor.processPrestigeSettings(player, oldIsland, newIsland, onlineIslandMembers, offlineMemberIds, newIslandData, requiredPrestigePoints, prestigeConfig.prestigeSettings());

        // Clean up the old island
        this.cleanUpOldIsland(player, oldIsland);

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
     * Create a new {@link Island} from the old {@link Island}.
     * Island owner and members will be transferred over.
     * @param gameModeAddon The {@link GameModeAddon} to create the Island with.
     * @param user The {@link User} creating the island.
     * @param oldIsland The old {@link Island}.
     * @param blueprintName The blueprint to use for the new island.
     * @return The new {@link Island} or null if creation failed.
     */
    private @Nullable Island createNewIsland(
            @NotNull GameModeAddon gameModeAddon,
            @NotNull User user,
            @NotNull Island oldIsland,
            @NotNull String blueprintName) {
        String oldIslandId = oldIsland.getUniqueId();
        // Mark the old island's id as being prestiged.
        this.addPrestigedIslandId(oldIslandId);

        try {
            NewIsland.Builder islandBuilder = NewIsland.builder();
            islandBuilder.player(user);
            islandBuilder.addon(gameModeAddon);
            islandBuilder.reason(IslandEvent.Reason.RESET);
            islandBuilder.oldIsland(oldIsland);
            islandBuilder.name(blueprintName);

            Island newIsland = islandBuilder.build();

            // Set the island owner and members for the new island
            newIsland.setOwner(oldIsland.getOwner());
            newIsland.setMembers(new HashMap<>(oldIsland.getMembers()));

            IslandsManager islandsManager = BentoBox.getInstance().getIslandsManager();
            IslandCache islandCache = islandsManager.getIslandCache();

            // Make the new island associated with each island member and set the primary island as necessary.
            newIsland.getMemberSet()
                    .forEach(uuid -> {
                        islandCache.addPlayer(uuid, newIsland);

                        if(oldIsland.isPrimary(uuid)) islandCache.setPrimaryIsland(uuid, newIsland);
                    });

            IslandsManager.updateIsland(newIsland);

            return newIsland;
        } catch (IOException e) {
            // Remove the island id as being prestiged on error.
            removePrestigedIslandId(oldIslandId);

            // Log an error
            logger.error(AdventureUtil.deserialize("Failed to create new island. Error: " + e.getMessage()));

            // return null
            return null;
        }
    }

    /**
     * Clean up data from the old island.
     * @param prestigingPlayer The player who prestiged the island.
     * @param oldIsland The old {@link Island}
     */
    private void cleanUpOldIsland(@NotNull Player prestigingPlayer, @NotNull Island oldIsland) {
        IslandCache islandCache = islandsManager.getIslandCache();

        // Set the owner of the old island to null
        oldIsland.setOwner(null);
        // Remove the members from the old island
        oldIsland.getMemberSet().forEach(uuid -> islandCache.removePlayer(oldIsland, uuid));
        // Update the island
        IslandsManager.updateIsland(oldIsland);

        // Delete the old island
        islandsManager.deleteIsland(oldIsland, true, prestigingPlayer.getUniqueId());
    }

    /**
     * Process any offline prestiges for the player.
     * @param player The player who was offline during a prestige.
     */
    public void handleOfflinePrestiges(@NotNull Player player) {
        offlinePrestigeManager.processOfflinePrestiges(player);
    }
}