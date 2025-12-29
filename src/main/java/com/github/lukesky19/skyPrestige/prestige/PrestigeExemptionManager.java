package com.github.lukesky19.skyPrestige.prestige;

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.data.island.IslandResetData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.OfflinePrestigeTable;
import com.github.lukesky19.skyPrestige.database.table.OfflineStatusChangeTable;
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
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages the marking islands as exempt from prestige.
 */
public class PrestigeExemptionManager {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull DatabaseManager databaseManager;
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
     * @param guiManager A {@link GUIManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param playerSettingsProcessor A {@link PlayerSettingsProcessor} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param rewardsProcessor A {@link RewardsProcessor} instance.
     */
    public PrestigeExemptionManager(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull GUIManager guiManager,
            @NotNull HookManager hookManager,
            @NotNull PlayerSettingsProcessor playerSettingsProcessor,
            @NotNull IslandSettingsProcessor islandSettingsProcessor,
            @NotNull RewardsProcessor rewardsProcessor) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.guiManager = guiManager;
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;

        this.playerSettingsProcessor = playerSettingsProcessor;
        this.islandSettingsProcessor = islandSettingsProcessor;
        this.rewardsProcessor = rewardsProcessor;
    }

    /**
     * Toggles the player's island's prestige exemption status for whether they can prestige or not.
     * @param player The {@link Player}.
     * @param island The {@link Island}.
     * @param  islandData The {@link IslandData}.
     */
    public void toggleIslandPrestigeStatus(@NotNull Player player, @NotNull Island island, @NotNull IslandData islandData) {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null || settings.scaleFormula() == null) return;
        @NotNull Locale locale = localeManager.getConfiguration();
        @NotNull BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        UUID playerId = player.getUniqueId();

        // Check if the player is in a world managed by a GameModeAddon
        Optional<GameModeAddon> optionalGameModeAddon = bentoBoxHook.getGameModeAddon(player.getWorld());
        if(optionalGameModeAddon.isEmpty()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeStatusPlayerInWrongWorld()));
            return;
        }

        // Check if the player attempting to toggle the island's prestige status is not the owner or an island member.
        if((island.getOwner() == null || !island.getOwner().equals(playerId)) && !island.getMemberSet().contains(playerId)) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeStatusPlayerNotMemberOrOwner()));
            return;
        }

        // Open the blueprint GUI
        openBlueprintGUI(
                locale,
                player,
                island,
                islandData,
                optionalGameModeAddon.get());
    }

    /**
     * Toggle the island's prestige status to opt in or opt out from prestige
     * The method {@link #toggleIslandPrestigeStatus(Player, Island, IslandData)} should be run before this method.
     * @param player The {@link Player}.
     * @param user The {@link User} for the {@link Player}.
     * @param oldIsland The old {@link Island}.
     * @param oldIslandData The old island's {@link IslandData}.
     * @param gameModeAddon The {@link GameModeAddon}.
     * @param blueprintName The blueprint name to use.
     */
    public void toggleIslandPrestigeStatus(
            @NotNull Player player,
            @NotNull User user,
            @NotNull Island oldIsland,
            @NotNull IslandData oldIslandData,
            @NotNull GameModeAddon gameModeAddon,
            @NotNull String blueprintName) {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) return;
        Settings.OptInOutSettings prestigeOptInOutSettings = oldIslandData.isPrestigeExempt() ?  settings.optInSettings() : settings.optOutSettings();
        OptType optType = oldIslandData.isPrestigeExempt() ?  OptType.OPT_IN : OptType.OPT_OUT;
        int status = oldIslandData.isPrestigeExempt() ? 1 : 0;

        // Create the new island
        @Nullable Island newIsland = IslandCreator.builder(plugin, databaseManager, hookManager, islandSettingsProcessor)
                .user(user)
                .gameModeAddon(gameModeAddon)
                .oldIsland(oldIsland)
                .islandData(oldIslandData)
                .islandSettings(prestigeOptInOutSettings.islandSettings())
                .name(blueprintName)
                .prestigeExempt(!oldIslandData.isPrestigeExempt())
                .build();

        // If the new island failed to be created, log and error and return
        if(newIsland == null) {
            logger.error(AdventureUtil.deserialize("Island Creation failed for opt out."));
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

        // Insert players that were offline on prestige opt out to process player settings later
        OfflineStatusChangeTable offlineOptOutTable = databaseManager.getOfflineStatusChangeTable();
        offlineIslandMembers.forEach(offlineMemberId -> offlineOptOutTable.insertOfflineStatusChange(offlineMemberId, newIsland.getUniqueId(), status));

        // Process Player Settings
        playerSettingsProcessor.processPlayerSettings(
                prestigeOptInOutSettings.playerSettings(),
                player,
                onlineIslandMembers,
                offlineIslandMembers,
                prestigeOptInOutSettings.startingMoney(),
                prestigeOptInOutSettings.giveStartingMoneyToAllIslandMembers());

        // Process prestige rewards
        rewardsProcessor.processPostRewards(player, newIsland, onlineIslandMembers, offlineIslandMembers, prestigeOptInOutSettings.rewardConfig(), -1);

        if(optType.equals(OptType.OPT_OUT)) {
            // Clear any offline prestiges queued
            OfflinePrestigeTable offlinePrestigeTable = databaseManager.getOfflinePrestigeTable();
            newIsland.getMemberSet().forEach((offlinePrestigeTable::removeOfflinePrestige));
        }
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
            @NotNull Locale locale,
            @NotNull Player player,
            @NotNull Island island,
            @NotNull IslandData islandData,
            @NotNull GameModeAddon gameModeAddon) {
        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), player.getUniqueId());
        IslandResetData islandResetData = new IslandResetData(player, User.getInstance(player), island, islandData, gameModeAddon);
        BlueprintGUI.BlueprintMode blueprintMode = islandData.isPrestigeExempt() ? BlueprintGUI.BlueprintMode.OPT_IN : BlueprintGUI.BlueprintMode.OPT_OUT;

        // Let the player select a blueprint
        BlueprintGUI gui = new BlueprintGUI(
                plugin,
                settingsManager,
                localeManager,
                guiConfigManager,
                guiManager,
                identifier,
                hookManager,
                rewardsProcessor,
                islandResetData,
                data -> {
                    if(data.getBlueprint() == null) return;

                    toggleIslandPrestigeStatus(data.getPlayer(), data.getUser(), data.getOldIsland(), data.getOldIslandData(), data.getGameModeAddon(), data.getBlueprint().getUniqueId());
                },
                blueprintMode);

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
     * Process any offline prestige status change settings and rewards for the player.
     * @param player The player who was offline during a prestige opt-in or opt-out.
     */
    public void handleOfflineStatusChanges(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        CompletableFuture<List<Integer>> future = databaseManager.getOfflineStatusChangeTable().getOfflineStatusChanges(playerId);

        future.thenAccept(statusChangesList -> {
            if(statusChangesList.isEmpty()) return;

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if(!player.isOnline() || !player.isConnected()) return;
                @Nullable Settings settings = settingsManager.getConfiguration();
                if(settings == null) return;

                statusChangesList.forEach(status -> {
                    Settings.OptInOutSettings statusChangeSettings = status == 1 ? settings.optInSettings() : settings.optOutSettings();

                    playerSettingsProcessor.processPlayerSettingsOnLogin(
                            statusChangeSettings.playerSettings(),
                            player,
                            statusChangeSettings.startingMoney(),
                            statusChangeSettings.giveStartingMoneyToAllIslandMembers());

                    rewardsProcessor.processRewardsOnLogin(player, statusChangeSettings.rewardConfig(), -1);
                });

                databaseManager.getOfflineStatusChangeTable().removeOfflineStatusChange(playerId);
            }, 1L);
        });
    }

    private enum OptType {
        OPT_IN,
        OPT_OUT
    }
}