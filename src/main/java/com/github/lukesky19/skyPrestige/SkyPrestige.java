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
package com.github.lukesky19.skyPrestige;

import com.github.lukesky19.skyPrestige.commands.SkyPrestigeCommand;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.data.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skyPrestige.integration.hooks.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.connection.PlayerJoinListener;
import com.github.lukesky19.skyPrestige.listener.connection.PlayerQuitListener;
import com.github.lukesky19.skyPrestige.listener.gui.GUIListener;
import com.github.lukesky19.skyPrestige.listener.island.IslandListener;
import com.github.lukesky19.skyPrestige.listener.points.*;
import com.github.lukesky19.skyPrestige.listener.points.brewing.FreshBrewListener;
import com.github.lukesky19.skyPrestige.listener.protection.ProtectionOrbListener;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.placeholderapi.PlaceholderManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.player.PlayerSettingsProcessor;
import com.github.lukesky19.skyPrestige.protection.ProtectionOrbManager;
import com.github.lukesky19.skyPrestige.task.TaskManager;
import com.github.lukesky19.skyPrestige.teleportation.TeleportationManager;
import com.github.lukesky19.skyPrestige.vault.VaultManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The main class for the SkyPrestige plugin.
 */
public final class SkyPrestige extends SkyPlugin {
    // Plugin Classes
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private PrestigeConfigManager prestigeConfigManager;
    private GUIConfigManager guiConfigManager;
    private GUIManager guiManager;
    private DatabaseManager databaseManager;
    private IslandDataManager islandDataManager;
    private LeaderboardManager leaderboardManager;
    private TaskManager taskManager;
    private ProtectionOrbManager protectionOrbManager;
    private MultiplierManager multiplierManager;
    private PlaceholderManager placeholderManager;

    /**
     * Default Constructor
     */
    public SkyPrestige() {}

    /**
     * This method is run when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        // Check SkyLib version
        if(!checkSkyLibVersion()) {
            return;
        }

        // Set up plugin classes
        HookManager hookManager = new HookManager(this);
        databaseManager = new DatabaseManager(this);
        CompletableFuture<Void> databaseFuture = databaseManager.setup();
        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);
        guiConfigManager = new GUIConfigManager(this);
        prestigeConfigManager = new PrestigeConfigManager(this);
        guiManager = new GUIManager();
        islandDataManager = new IslandDataManager(databaseManager, hookManager);
        leaderboardManager = new LeaderboardManager(this, islandDataManager, databaseManager, hookManager);
        multiplierManager = new MultiplierManager(this, settingsManager, localeManager);
        taskManager = new TaskManager(this, settingsManager, islandDataManager, leaderboardManager, multiplierManager);
        protectionOrbManager = new ProtectionOrbManager(this, settingsManager);
        IslandSettingsProcessor islandSettingsProcessor = new IslandSettingsProcessor(this, hookManager, databaseManager, islandDataManager);
        PlayerSettingsProcessor playerSettingsProcessor = new PlayerSettingsProcessor(hookManager, protectionOrbManager);
        PrestigeManager prestigeManager = new PrestigeManager(this, settingsManager, localeManager, guiConfigManager, prestigeConfigManager, databaseManager, guiManager, islandDataManager, hookManager, playerSettingsProcessor, islandSettingsProcessor);
        TeleportationManager teleportationManager = new TeleportationManager(this, settingsManager, localeManager, databaseManager, hookManager);
        VaultManager vaultManager = new VaultManager(settingsManager);
        placeholderManager = new PlaceholderManager(this, islandDataManager, leaderboardManager, hookManager);

        // Register Commands
        SkyPrestigeCommand skyPrestigeCommand = new SkyPrestigeCommand(this, settingsManager, localeManager, guiConfigManager, prestigeConfigManager, prestigeManager, islandDataManager, leaderboardManager, guiManager, databaseManager, vaultManager, protectionOrbManager, multiplierManager, hookManager);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(skyPrestigeCommand.createCommand(),
                                "Command to manage and use the SkyPrestige plugin.",
                                List.of("prestige")));

        // Listeners
        PluginManager pluginManager = this.getServer().getPluginManager();

        // Brewing-related Listeners
        pluginManager.registerEvents(new FreshBrewListener(hookManager), this);

        // Connection-related Listeners
        pluginManager.registerEvents(new PlayerJoinListener(databaseManager, prestigeManager, islandDataManager, teleportationManager), this);
        pluginManager.registerEvents(new PlayerQuitListener(this, databaseManager, islandDataManager, hookManager), this);

        // Prestige-related Listeners
        pluginManager.registerEvents(new IslandListener(this, settingsManager, databaseManager, islandDataManager, islandSettingsProcessor), this);

        // Protection Orb Listener
        pluginManager.registerEvents(new ProtectionOrbListener(localeManager, protectionOrbManager), this);

        // GUI-related Listeners
        pluginManager.registerEvents(new GUIListener(guiManager, hookManager), this);

        // Prestige Points Listeners
        if(hookManager.getHook(RoseStackerHook.class).isHooked()) {
            pluginManager.registerEvents(new BlockStackListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new BlockUnstackListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new SpawnerStackListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new SpawnerUnstackListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new EntityStackMultipleDeathListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        }

        pluginManager.registerEvents(new BlockBreakListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new BlockHarvestListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new BlockPlaceListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new CauldronListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new CraftItemListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new InventoryCloseListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new InventoryOpenListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerAnvilEnchantListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBeeHiveListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBoneMealListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBottleDragonsBreathListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBottleWaterListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBreedListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBrewListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBrushBlockListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBucketEmptyListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBucketFillListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerCakeConsumeListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerCompostListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerDropItemListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerEnchantmentTableEnchantListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerFishListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerItemConsumeListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerKillEntityListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerMilkCowListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerNameEntityListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerPickupItemListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerRenameItemListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerShearBlockListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerShearEntityListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerSleepListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerStripLogListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerTameEntityListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerThrowItemListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerUnwaxBlockListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerUnwaxEntityListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerWaxBlockListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerWaxEntityListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        if(hookManager.getHook(SkyPlayTimeHook.class).isHooked()) {
            pluginManager.registerEvents(new SkyPlayTimeListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        }
        pluginManager.registerEvents(new SmeltItemListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new WaterLogListener(this, settingsManager, islandDataManager, hookManager, multiplierManager), this);

        // Reload the plugin
        reload();

        databaseFuture.thenAccept(v -> {
            // Load player data for any online players.
            this.getServer().getOnlinePlayers().forEach(player -> {
                UUID uuid = player.getUniqueId();

                // Insert the player's uuid into the database if it doesn't exist
                databaseManager.getPlayerIdsTable().insertPlayerId(uuid);

                // Load the player's island data
                islandDataManager.loadDataByPlayerIdentifier(uuid);

                // Handle any prestiges that occurred while the player was offline
                prestigeManager.handleOfflinePrestiges(player);

                // Handle any queued teleports for the player.
                teleportationManager.handleQueuedTeleports(player);
            });
        });
    }

    /**
     * This method is run when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        // Unregister the PlaceholderAPI Expansion
        if(placeholderManager != null) placeholderManager.unregisterExpansion();

        // Close any open GUIs
        if(guiManager != null) guiManager.closeOpenGUIs(true);

        // Stop the running tasks
        if(taskManager != null) taskManager.stopTasks();

        // Save any loaded island data and clean up the database.
        if(islandDataManager != null) {
            islandDataManager.saveData().thenAccept(v -> {
                if(databaseManager != null) {
                    databaseManager.cleanUp();
                }
            });
        } else {
            if(databaseManager != null) {
                databaseManager.cleanUp();
            }
        }
    }

    /**
     * This method is run to reload any plugin data.
     */
    @Override
    public void reload() {
        // Reload the PlaceholderAPI Expansion
        placeholderManager.reload();

        // Close any open GUIs
        guiManager.closeOpenGUIs(false);

        // Reload plugin configuration
        settingsManager.loadConfiguration();
        localeManager.loadConfiguration();
        guiConfigManager.reload();
        prestigeConfigManager.loadConfigurations();
        leaderboardManager.updateDatabaseTopTen();
        protectionOrbManager.reload();
        multiplierManager.reload();

        // (Re-)start the plugin's task
        taskManager.startTasks();
    }

    /**
     * Checks if the Server has the proper SkyLib version.
     * @return true if it does, false if not.
     */
    private boolean checkSkyLibVersion() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin skyLib = pluginManager.getPlugin("SkyLib");
        if(skyLib != null && skyLib.isEnabled()) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int second = Integer.parseInt(splitVersion[1]);

            if(second >= 4) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtil.deserialize("SkyLib Version 1.4.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }
}
