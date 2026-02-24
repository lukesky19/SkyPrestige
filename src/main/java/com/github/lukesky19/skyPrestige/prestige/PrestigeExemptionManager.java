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
import com.github.lukesky19.skyPrestige.configuration.data.opt_in_out.OptInOutConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.OptInConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.OptOutConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.QueuedSettingsTable;
import com.github.lukesky19.skyPrestige.gui.abstracts.ConfirmGUI;
import com.github.lukesky19.skyPrestige.gui.gui.BlueprintGUI;
import com.github.lukesky19.skyPrestige.gui.gui.ConfirmOptInGUI;
import com.github.lukesky19.skyPrestige.gui.gui.ConfirmOptOutGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.island.AbstractIslandCreator;
import com.github.lukesky19.skyPrestige.integration.island.OptInOutIslandCreator;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.player.PlayerSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skyPrestige.util.enums.SettingsType;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;

/**
 * This class manages the marking islands as exempt from prestige.
 */
public class PrestigeExemptionManager {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull ComponentLogger logger;

    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIConfigManager guiConfigManager;
    private final @NonNull OptInConfigManager optInConfigManager;
    private final @NonNull OptOutConfigManager optOutConfigManager;

    private final @NonNull GUIManager guiManager;
    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull HookManager hookManager;

    private final @NonNull IslandSettingsProcessor islandSettingsProcessor;
    private final @NonNull RewardsProcessor rewardsProcessor;
    private final @NonNull PlayerSettingsProcessor playerSettingsProcessor;

    // Used to prevent multiple opt-ins/opt-outs being triggered for an island.
    private final @NonNull Set<String> inProgressExemptions = new HashSet<>();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param optInConfigManager An {@link OptInConfigManager} instance.
     * @param optOutConfigManager An {@link OptOutConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param playerSettingsProcessor A {@link PlayerSettingsProcessor} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param rewardsProcessor A {@link RewardsProcessor} instance.
     */
    public PrestigeExemptionManager(
            @NonNull SkyPlugin plugin,
            @NonNull LocaleManager localeManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull OptInConfigManager optInConfigManager,
            @NonNull OptOutConfigManager optOutConfigManager,
            @NonNull DatabaseManager databaseManager,
            @NonNull GUIManager guiManager,
            @NonNull HookManager hookManager,
            @NonNull PlayerSettingsProcessor playerSettingsProcessor,
            @NonNull IslandSettingsProcessor islandSettingsProcessor,
            @NonNull RewardsProcessor rewardsProcessor) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();

        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.optInConfigManager = optInConfigManager;
        this.optOutConfigManager = optOutConfigManager;

        this.guiManager = guiManager;
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;

        this.playerSettingsProcessor = playerSettingsProcessor;
        this.islandSettingsProcessor = islandSettingsProcessor;
        this.rewardsProcessor = rewardsProcessor;
    }

    /**
     * Is the island in the progress of opting in or out of prestige?
     * @param islandId The island id.
     * @return true or false.
     */
    public boolean isIslandExempting(@NonNull String islandId) {
        return inProgressExemptions.contains(islandId);
    }

    /**
     * Remove the island id from the list of islands opting in or out of prestige.
     * @param islandId The island id.
     */
    public void removeExempting(@NonNull String islandId) {
        inProgressExemptions.remove(islandId);
    }

    /**
     * Toggles the player's island's prestige exemption status for whether they can prestige or not.
     * @param player The {@link Player}.
     * @param island The {@link Island}.
     * @param islandData The {@link IslandData}.
     */
    public void toggleIslandPrestigeStatus(@NonNull Player player, @NonNull Island island, @NonNull IslandData islandData) {
        Locale locale = localeManager.getConfiguration();
        Locale.OptInMessages optInMessages = locale.optInMessages();
        Locale.OptOutMessages optOutMessages = locale.optOutMessages();

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        UUID playerId = player.getUniqueId();
        String islandId = island.getUniqueId();

        OptInOutConfig optInOutConfig = islandData.isPrestigeExempt() ? optInConfigManager.getConfiguration() : optOutConfigManager.getConfiguration();
        if(optInOutConfig == null) {
            logger.error(AdventureUtil.deserialize("Unable to toggle island prestige status due to invalid configuration."));
            return;
        }

        // Check if the player is in a world managed by a GameModeAddon
        Optional<GameModeAddon> optionalGameModeAddon = bentoBoxHook.getGameModeAddon(player.getWorld());
        if(optionalGameModeAddon.isEmpty()) {
            if(islandData.isPrestigeExempt()) { // Opting In
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + optInMessages.playerInWrongWorld()));
            } else { // Opting Out
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + optOutMessages.playerInWrongWorld()));
            }

            return;
        }

        // Check if the player attempting to toggle the island's prestige status is not the owner or an island member.
        if((island.getOwner() == null || !island.getOwner().equals(playerId)) && !island.getMemberSet().contains(playerId)) {
            if(islandData.isPrestigeExempt()) { // Opting In
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + optInMessages.playerNotMemberOrOwner()));
            } else { // Opting Out
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + optOutMessages.playerNotMemberOrOwner()));
            }

            return;
        }

        if(inProgressExemptions.contains(islandId)) {
            if(islandData.isPrestigeExempt()) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + optInMessages.optInInProgress()));
            } else {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + optOutMessages.optOutInProgress()));
            }
            return;
        }

        inProgressExemptions.add(islandId);

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
        rewardsProcessor.processEarlyRewards(player, onlineIslandMembers, offlineIslandMembers, optInOutConfig.rewardConfig());

        if(optInOutConfig.resetSettings().islandSettings().keepIsland()) {
            // Open the confirmation GUI
            openConfirmationGUI(
                    locale,
                    player,
                    island,
                    islandData,
                    optionalGameModeAddon.get());
        } else {
            // Open the blueprint selection GUI
            openBlueprintGUI(
                    locale,
                    player,
                    island,
                    islandData,
                    optionalGameModeAddon.get());
        }
    }

    /**
     * Toggle the island's prestige status to opt in or opt out from prestige
     * The method {@link #toggleIslandPrestigeStatus(Player, Island, IslandData)} should be run before this method.
     * @param player The {@link Player}.
     * @param oldIsland The old {@link Island}.
     * @param oldIslandData The old island's {@link IslandData}.
     * @param gameModeAddon The {@link GameModeAddon}.
     * @param blueprintName The blueprint name to use.
     */
    public void toggleIslandPrestigeStatus(
            @NonNull Player player,
            @NonNull Island oldIsland,
            @NonNull IslandData oldIslandData,
            @NonNull GameModeAddon gameModeAddon,
            @Nullable String blueprintName) {
        OptInOutConfig optInOutConfig = oldIslandData.isPrestigeExempt() ? optInConfigManager.getConfiguration() : optOutConfigManager.getConfiguration();
        if(optInOutConfig == null) {
            logger.error(AdventureUtil.deserialize("Unable to toggle island prestige status due to invalid configuration."));
            return;
        }
        // Store the new prestige exemption status.
        boolean newStatus = !oldIslandData.isPrestigeExempt();

        Island island;
        IslandData islandData = oldIslandData.clone();
        if(optInOutConfig.resetSettings().islandSettings().keepIsland()) {
            island = oldIsland;

            islandData.setPrestigeExempt(newStatus);
            islandData.setLeaderboardExempt(newStatus);

            islandSettingsProcessor.processIslandSettings(player, optInOutConfig.resetSettings().islandSettings(), oldIsland, island, islandData);
        } else {
            if(blueprintName == null) {
                logger.error(AdventureUtil.deserialize("Unable to opt in or out island for player " + player.getName() + " because the selected blueprint name is null."));
                return;
            }

            // Create the new island
            AbstractIslandCreator islandCreator = new OptInOutIslandCreator(
                    plugin,
                    databaseManager,
                    hookManager,
                    islandSettingsProcessor,
                    player,
                    oldIsland.getWorld(),
                    gameModeAddon,
                    blueprintName,
                    oldIsland,
                    oldIslandData,
                    optInOutConfig.resetSettings().islandSettings(),
                    newStatus);
            island = islandCreator.createIsland();

            // If the new island failed to be created, log and error and return
            if(island == null) {
                logger.error(AdventureUtil.deserialize("Island Creation failed for opt out."));
                return;
            }
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

        // Insert players that were offline on prestige opt out to process player settings later
        QueuedSettingsTable queuedSettingsTable = databaseManager.getQueuedSettingsTable();
        offlineIslandMembers.forEach(offlineMemberId ->
                queuedSettingsTable.clearQueuedSettings(offlineMemberId).thenAccept(v ->
                        queuedSettingsTable.queueSettings(
                                offlineMemberId,
                                island.getUniqueId(),
                                newStatus ? SettingsType.OPT_OUT : SettingsType.OPT_IN,
                                -1)));

        // Process Player Settings
        playerSettingsProcessor.processPlayerSettings(
                optInOutConfig.resetSettings().playerSettings(),
                player,
                onlineIslandMembers,
                offlineIslandMembers,
                optInOutConfig.resetSettings().startingMoney(),
                optInOutConfig.resetSettings().giveStartingMoneyToAllIslandMembers());

        // Process rewards
        rewardsProcessor.processPostRewards(player, island, onlineIslandMembers, offlineIslandMembers, optInOutConfig.rewardConfig());

        Locale locale = localeManager.getConfiguration();
        Locale.OptInMessages optInMessages = locale.optInMessages();
        Locale.OptOutMessages optOutMessages = locale.optOutMessages();

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("player", player.getName()));

        if(newStatus) {
            // Send island member messages
            Component islandMemberMessage = AdventureUtil.deserialize(locale.prefix() + optOutMessages.islandMemberMessage(), placeholders);
            onlineIslandMembers.forEach(islandMember -> islandMember.sendMessage(islandMemberMessage));
        } else {
            // Send island member messages
            Component islandMemberMessage = AdventureUtil.deserialize(locale.prefix() + optInMessages.islandMemberMessage(), placeholders);
            onlineIslandMembers.forEach(islandMember -> islandMember.sendMessage(islandMemberMessage));
        }

        inProgressExemptions.remove(island.getUniqueId());
    }

    /**
     * Open the blueprint selection GUI for the player.
     * @param locale The plugin's {@link Locale}.
     * @param player The {@link Player} prestiging.
     * @param island The {@link Island} being prestiged.
     * @param islandData The {@link IslandData} for the island being prestiged.
     * @param gameModeAddon The {@link GameModeAddon}.
     */
    private void openBlueprintGUI(
            @NonNull Locale locale,
            @NonNull Player player,
            @NonNull Island island,
            @NonNull IslandData islandData,
            @NonNull GameModeAddon gameModeAddon) {
        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), player.getUniqueId());

        // Let the player select a blueprint
        BlueprintGUI gui;
        if(islandData.isPrestigeExempt()) {
            gui = new BlueprintGUI(
                    plugin,
                    guiManager,
                    identifier,
                    localeManager,
                    guiConfigManager,
                    optInConfigManager,
                    hookManager,
                    rewardsProcessor,
                    this,
                    player,
                    island,
                    islandData,
                    gameModeAddon);
        } else {
            gui = new BlueprintGUI(
                    plugin,
                    guiManager,
                    identifier,
                    localeManager,
                    guiConfigManager,
                    optOutConfigManager,
                    hookManager,
                    rewardsProcessor,
                    this,
                    player,
                    island,
                    islandData,
                    gameModeAddon);
        }

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
     * Open the confirmation GUI for the player.
     * @param locale The plugin's {@link Locale}.
     * @param player The {@link Player} prestiging.
     * @param island The {@link Island} being prestiged.
     * @param islandData The {@link IslandData} for the island being prestiged.
     * @param gameModeAddon The {@link GameModeAddon}.
     */
    private void openConfirmationGUI(
            @NonNull Locale locale,
            @NonNull Player player,
            @NonNull Island island,
            @NonNull IslandData islandData,
            @NonNull GameModeAddon gameModeAddon) {
        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), player.getUniqueId());

        ConfirmGUI confirmGUI;
        if(!islandData.isPrestigeExempt()) {
            confirmGUI = new ConfirmOptOutGUI(
                    plugin,
                    localeManager,
                    guiConfigManager,
                    guiManager,
                    identifier,
                    optOutConfigManager,
                    rewardsProcessor,
                    this,
                    player,
                    island,
                    islandData,
                    gameModeAddon,
                    null);
        } else {
            confirmGUI = new ConfirmOptInGUI(
                    plugin,
                    localeManager,
                    guiConfigManager,
                    guiManager,
                    identifier,
                    optInConfigManager,
                    rewardsProcessor,
                    this,
                    player,
                    island,
                    islandData,
                    gameModeAddon,
                    null);
        }

        boolean creationResult = confirmGUI.create();
        if (!creationResult) {
            logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the confirm GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        boolean updateResult = confirmGUI.update();
        if (!updateResult) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the confirm GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        boolean openResult = confirmGUI.open();
        if (!openResult) {
            logger.error(AdventureUtil.deserialize("Unable to open the confirm GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
        }
    }
}