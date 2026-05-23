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
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.QuestRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.QueuedSettingsTable;
import com.github.lukesky19.skyPrestige.gui.abstracts.ConfirmGUI;
import com.github.lukesky19.skyPrestige.gui.gui.BlueprintGUI;
import com.github.lukesky19.skyPrestige.gui.gui.ConfirmPrestigeGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.hooks.LMBQuestHook;
import com.github.lukesky19.skyPrestige.integration.island.AbstractIslandCreator;
import com.github.lukesky19.skyPrestige.integration.island.PrestigeIslandCreator;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.player.PlayerSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skyPrestige.requirements.RequirementsManager;
import com.github.lukesky19.skyPrestige.util.enums.SettingsType;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.leonardobishop.quests.common.player.QPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;

/**
 * This class manages the prestiging of islands.
 */
public class PrestigeManager {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull ComponentLogger logger;

    private final @NonNull SettingsManager settingsManager;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIConfigManager guiConfigManager;
    private final @NonNull PrestigeConfigManager prestigeConfigManager;
    private final @NonNull RequirementsManager requirementsManager;

    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull GUIManager guiManager;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull HookManager hookManager;

    private final @NonNull IslandSettingsProcessor islandSettingsProcessor;
    private final @NonNull RewardsProcessor rewardsProcessor;
    private final @NonNull PlayerSettingsProcessor playerSettingsProcessor;

    // Used to prevent multiple prestiges being triggered for an island.
    private final @NonNull Set<String> inProgressPrestiges = new HashSet<>();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param requirementsManager A {@link RequirementsManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param playerSettingsProcessor A {@link PlayerSettingsProcessor} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param rewardsProcessor A {@link RewardsProcessor} instance.
     */
    public PrestigeManager(
            @NonNull SkyPlugin plugin,
            @NonNull SettingsManager settingsManager,
            @NonNull LocaleManager localeManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull PrestigeConfigManager prestigeConfigManager,
            @NonNull RequirementsManager requirementsManager,
            @NonNull DatabaseManager databaseManager,
            @NonNull GUIManager guiManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager,
            @NonNull PlayerSettingsProcessor playerSettingsProcessor,
            @NonNull IslandSettingsProcessor islandSettingsProcessor,
            @NonNull RewardsProcessor rewardsProcessor) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.requirementsManager = requirementsManager;
        this.databaseManager = databaseManager;
        this.guiManager = guiManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;

        this.islandSettingsProcessor = islandSettingsProcessor;
        this.rewardsProcessor = rewardsProcessor;
        this.playerSettingsProcessor = playerSettingsProcessor;
    }

    /**
     * Is the island in the progress of prestiging?
     * @apiNote This is if the player has started the process, but hasn't confirmed the process yet.
     * @param islandId The island id.
     * @return true or false.
     */
    public boolean isIslandPrestiging(@NonNull String islandId) {
        return inProgressPrestiges.contains(islandId);
    }

    /**
     * Remove the island id from the list of islands prestiging.
     * @param islandId The island id.
     */
    public void removePrestige(@NonNull String islandId) {
        inProgressPrestiges.remove(islandId);
    }

    /**
     * Checks if the player's island can be prestiged and opens the blueprint selection GUI, the confirmation GUI, or displays an error.
     * @param player The {@link Player}.
     */
    public void prestigeIsland(@NonNull Player player) {
        Locale locale = localeManager.getConfiguration();
        Locale.PrestigeMessages prestigeMessages = locale.prestigeMessages();
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        UUID playerId = player.getUniqueId();

        // Check if the player is in a world managed by a GameModeAddon
        Optional<GameModeAddon> optionalGameModeAddon = bentoBoxHook.getGameModeAddon(player.getWorld());
        if(optionalGameModeAddon.isEmpty()) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + prestigeMessages.playerInWrongWorld()));
            return;
        }

        // Check if the player is on an island
        Optional<Island> optionalIsland = bentoBoxHook.getIslandAtLocation(player.getLocation());
        if(optionalIsland.isEmpty()) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + prestigeMessages.playerNotOnIsland()));
            return;
        }
        Island island = optionalIsland.get();
        String islandId = island.getUniqueId();

        if(inProgressPrestiges.contains(islandId)) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.prestigeMessages().prestigeInProgress()));
            return;
        }

        inProgressPrestiges.add(islandId);

        // Check if the player attempting to toggle the island's prestige status is not the owner or an island member.
        if((island.getOwner() == null || !island.getOwner().equals(playerId)) && !island.getMemberSet().contains(playerId)) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + prestigeMessages.playerNotMemberOrOwner()));
            return;
        }

        // Get the island data for the island.
        IslandData islandData = islandDataManager.getData(islandId);
        if(islandData == null) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.islandDataNotFound()));
            logger.error(AdventureUtility.plain("No Island data found for player " + player.getName() + "'s island. Island Id: " + islandId));
            return;
        }

        if(islandData.isPrestigeExempt()) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + prestigeMessages.islandOptedOut()));
            return;
        }

        // Get the next prestige level and the config for that level
        int nextPrestigeLevel = islandData.getPrestigeLevel() + 1;
        // If the prestige config is null, the player is at the max prestige level
        PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(nextPrestigeLevel);
        if(prestigeConfig == null) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + prestigeMessages.prestigeLevelMax()));
            return;
        }

        player.sendMessage(AdventureUtility.deserialize("<red>Gathering necessary data for prestige..."));

        // Get a list of QPlayers for all island members
        LMBQuestHook lmbQuestHook = hookManager.getHook(LMBQuestHook.class);
        List<QPlayer> qPlayerList = lmbQuestHook.getQPlayerList(island.getMemberSet());

        Server server = player.getServer();
        // Get online island member's players
        List<Player> onlineIslandMembers = island.getMemberSet().stream()
                .map(server::getPlayer)
                .filter(Objects::nonNull)
                .filter(memberPlayer -> memberPlayer.isOnline() && memberPlayer.isConnected())
                .toList();
        // Get all island member's offline player.
        List<OfflinePlayer> offlinePlayerList = island.getMemberSet().stream()
                .map(server::getOfflinePlayer)
                .toList();
        // Get offline island member's unique ids that are actually offline
        List<UUID> offlineIslandMembersIds = offlinePlayerList
                .stream()
                .filter(offlinePlayer -> !offlinePlayer.isOnline() && !offlinePlayer.isConnected())
                .map(OfflinePlayer::getUniqueId)
                .toList();

        int memberCount = island.getMemberSet().size();
        Map<ItemStack, Boolean> requiredItems = requirementsManager.calculateRequiredItems(memberCount, islandData, prestigeConfig.inventoryRequirements());
        double requiredMoney = requirementsManager.calculateRequiredMoney(memberCount, islandData, prestigeConfig.moneyRequirement());
        double requiredPrestigePoints = requirementsManager.calculateRequiredPrestigePoints(memberCount, islandData, prestigeConfig.prestigePointsRequirement());
        List<String> requiredQuestIds = prestigeConfig.questRequirements().stream().map(QuestRequirement::questId).filter(Objects::nonNull).toList();

        if(requirementsManager.areRequirementsNotMet(player, onlineIslandMembers, offlinePlayerList, qPlayerList, islandData, requiredItems.keySet(), requiredMoney, requiredPrestigePoints, requiredQuestIds)) {
            inProgressPrestiges.remove(islandId);
            return;
        }

        // Process early rewards (will be undone if canceled)
        rewardsProcessor.processEarlyRewards(player, onlineIslandMembers, offlineIslandMembersIds, prestigeConfig.rewardConfig());

        if(prestigeConfig.prestigeSettings().islandSettings().keepIsland()) {
            // Open the prestige confirmation GUI
            openConfirmationGUI(
                    locale,
                    player,
                    island,
                    islandData,
                    optionalGameModeAddon.get(),
                    prestigeConfig,
                    nextPrestigeLevel);
        } else {
            // Open the blueprint selection GUI
            openBlueprintGUI(
                    locale,
                    player,
                    island,
                    islandData,
                    optionalGameModeAddon.get(),
                    prestigeConfig,
                    nextPrestigeLevel);
        }
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
            @NonNull Locale locale,
            @NonNull Player player,
            @NonNull Island island,
            @NonNull IslandData islandData,
            @NonNull GameModeAddon gameModeAddon,
            @NonNull PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), player.getUniqueId());

        // Let the player select a blueprint for the prestige
        BlueprintGUI gui = new BlueprintGUI(
                plugin,
                guiManager,
                identifier,
                localeManager,
                guiConfigManager,
                hookManager,
                rewardsProcessor,
                this,
                player,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                prestigeLevel);

        // Create the GUI
        boolean creationResult = gui.create();
        if(!creationResult) {
            logger.error(AdventureUtility.plain("Unable to create the InventoryView for the blueprint GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        // Update the GUI
        boolean updateResult = gui.update();
        if(!updateResult) {
            logger.error(AdventureUtility.plain("Unable to decorate the blueprint GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        // Open the GUI
        boolean openResult = gui.open();
        if(!openResult) {
            logger.error(AdventureUtility.plain("Unable to open the blueprint GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
        }
    }

    /**
     * Open the confirmation GUI for the player.
     * @param locale The plugin's {@link Locale}.
     * @param player The {@link Player} prestiging.
     * @param island The {@link Island} being prestiged.
     * @param islandData The {@link IslandData} for the island being prestiged.
     * @param gameModeAddon The {@link GameModeAddon}.
     * @param prestigeConfig The {@link PrestigeConfig}.
     * @param prestigeLevel The prestige level.
     */
    private void openConfirmationGUI(
            @NonNull Locale locale,
            @NonNull Player player,
            @NonNull Island island,
            @NonNull IslandData islandData,
            @NonNull GameModeAddon gameModeAddon,
            @NonNull PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), player.getUniqueId());

        ConfirmGUI confirmGUI = new ConfirmPrestigeGUI(
                plugin,
                localeManager,
                guiConfigManager,
                guiManager,
                identifier,
                rewardsProcessor,
                this,
                player,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                prestigeLevel,
                null);

        boolean creationResult = confirmGUI.create();
        if (!creationResult) {
            logger.error(AdventureUtility.plain("Unable to create the InventoryView for the confirm GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        boolean updateResult = confirmGUI.update();
        if (!updateResult) {
            logger.error(AdventureUtility.plain("Unable to decorate the confirm GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        boolean openResult = confirmGUI.open();
        if (!openResult) {
            logger.error(AdventureUtility.plain("Unable to open the confirm GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
        }
    }

    /**
     * Prestige the player's island.
     * The method {@link #prestigeIsland(Player)} should be run before this method.
     * @param player The {@link Player}.
     * @param oldIsland The old {@link Island}.
     * @param oldIslandData The old island's {@link IslandData}.
     * @param gameModeAddon The {@link GameModeAddon}.
     * @param blueprintName The blueprint name to use. May be null if the island isn't being reset.
     * @param prestigeConfig The {@link PrestigeConfig} for the next prestige level.
     * @param prestigeLevel The prestige level the island is moving to.
     */
    public void prestigeIsland(
            @NonNull Player player,
            @NonNull Island oldIsland,
            @NonNull IslandData oldIslandData,
            @NonNull GameModeAddon gameModeAddon,
            @Nullable String blueprintName,
            @NonNull PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) return;
        Locale locale = localeManager.getConfiguration();
        String islandId = oldIsland.getUniqueId();

        // Get a list of QPlayers for all island members
        LMBQuestHook lmbQuestHook = hookManager.getHook(LMBQuestHook.class);
        List<QPlayer> qPlayerList = lmbQuestHook.getQPlayerList(oldIsland.getMemberSet());

        Server server = player.getServer();
        // Get online island member's players
        List<Player> onlineIslandMembers = oldIsland.getMemberSet().stream()
                .map(plugin.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .filter(memberPlayer -> memberPlayer.isOnline() && memberPlayer.isConnected())
                .toList();
        // Get all island member's offline player.
        List<OfflinePlayer> offlinePlayerList = oldIsland.getMemberSet().stream()
                .map(server::getOfflinePlayer)
                .toList();
        // Get offline island member's unique ids that are actually offline
        List<UUID> offlineIslandMembersIds = offlinePlayerList
                .stream()
                .filter(offlinePlayer -> !offlinePlayer.isOnline() && !offlinePlayer.isConnected())
                .map(OfflinePlayer::getUniqueId)
                .toList();

        int memberCount = oldIsland.getMemberSet().size();
        Map<ItemStack, Boolean> requiredItems = requirementsManager.calculateRequiredItems(memberCount, oldIslandData, prestigeConfig.inventoryRequirements());
        double requiredMoney = requirementsManager.calculateRequiredMoney(memberCount, oldIslandData, prestigeConfig.moneyRequirement());
        double requiredPrestigePoints = requirementsManager.calculateRequiredPrestigePoints(memberCount, oldIslandData, prestigeConfig.prestigePointsRequirement());
        List<String> requiredQuestIds = prestigeConfig.questRequirements().stream().map(QuestRequirement::questId).filter(Objects::nonNull).toList();

        if(requirementsManager.areRequirementsNotMet(player, onlineIslandMembers, offlinePlayerList, qPlayerList, oldIslandData, requiredItems.keySet(), requiredMoney, requiredPrestigePoints, requiredQuestIds)) {
            inProgressPrestiges.remove(islandId);
            return;
        }

        Island island;
        IslandData islandData = oldIslandData.clone();
        if(prestigeConfig.prestigeSettings().islandSettings().keepIsland()) {
            // Keep the existing island
            island = oldIsland;

            // Process requirements
            requirementsManager.removeRequiredItems(onlineIslandMembers, requiredItems);
            if(prestigeConfig.moneyRequirement().removeMoney()) {
                requirementsManager.removeRequiredMoney(offlinePlayerList, requiredMoney);
            }
            if(prestigeConfig.prestigePointsRequirement().removePrestigePoints()) {
                requirementsManager.removeRequiredPrestigePoints(oldIslandData, requiredPrestigePoints);
            }

            // Process island settings
            islandData.setPrestigeLevel(prestigeLevel);
            islandSettingsProcessor.processIslandSettings(
                    player,
                    prestigeConfig.prestigeSettings().islandSettings(),
                    oldIsland,
                    island,
                    islandData);
        } else {
            if(blueprintName == null) {
                logger.error(AdventureUtility.plain("Unable to prestige island for player " + player.getName() + " because the selected blueprint name is null."));
                return;
            }

            // Create the new island
            AbstractIslandCreator islandCreator = new PrestigeIslandCreator(
                    plugin,
                    databaseManager,
                    hookManager,
                    islandSettingsProcessor,
                    requirementsManager,
                    player,
                    onlineIslandMembers,
                    offlinePlayerList,
                    oldIsland.getWorld(),
                    gameModeAddon,
                    blueprintName,
                    oldIsland,
                    islandData,
                    prestigeConfig,
                    prestigeLevel,
                    requiredPrestigePoints,
                    requiredMoney,
                    requiredItems);
            island = islandCreator.createIsland();

            // If the new island failed to be created, log and error and return
            if(island == null) {
                logger.error(AdventureUtility.plain("Island Creation failed for prestige."));
                return;
            }
        }

        // Insert players that were offline on island prestige to give rewards later.
        QueuedSettingsTable queuedSettingsTable = databaseManager.getQueuedSettingsTable();
        offlineIslandMembersIds.forEach(offlineMemberId -> queuedSettingsTable.queueSettings(offlineMemberId, island.getUniqueId(), SettingsType.PRESTIGE, prestigeLevel));

        // Process Player Settings
        playerSettingsProcessor.processPlayerSettings(
                prestigeConfig.prestigeSettings().playerSettings(),
                player,
                onlineIslandMembers,
                offlineIslandMembersIds,
                prestigeConfig.prestigeSettings().startingMoney(),
                prestigeConfig.prestigeSettings().giveStartingMoneyToAllIslandMembers());

        // Process prestige rewards
        rewardsProcessor.processPostRewards(player, island, onlineIslandMembers, offlineIslandMembersIds, prestigeConfig.rewardConfig());

        // Send announcement messages
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("player", player.getName()));
        placeholders.add(Placeholder.parsed("prestige_level", String.valueOf(prestigeLevel)));

        Component announcementMessage = AdventureUtility.deserialize(locale.prefix() + locale.prestigeMessages().prestigeAnnouncement(), placeholders);
        plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(announcementMessage));

        Component islandMemberMessage = AdventureUtility.deserialize(locale.prefix() + locale.prestigeMessages().prestigeIslandMemberMessage(), placeholders);
        onlineIslandMembers.forEach(islandMember -> islandMember.sendMessage(islandMemberMessage));

        inProgressPrestiges.remove(islandId);
    }
}