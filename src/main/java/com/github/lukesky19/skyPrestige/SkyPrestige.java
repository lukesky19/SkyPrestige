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
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.listener.GUIListener;
import com.github.lukesky19.skyPrestige.gui.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.RoseStackerHook;
import com.github.lukesky19.skyPrestige.hook.impl.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.leaderboard.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.listener.connection.PlayerJoinListener;
import com.github.lukesky19.skyPrestige.listener.connection.PlayerQuitListener;
import com.github.lukesky19.skyPrestige.listener.island.IslandListener;
import com.github.lukesky19.skyPrestige.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.placeholderapi.SkyPrestigeExpansion;
import com.github.lukesky19.skyPrestige.points.listener.*;
import com.github.lukesky19.skyPrestige.points.listener.brewing.FreshBrewListener;
import com.github.lukesky19.skyPrestige.points.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.config.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.prestige.manager.PrestigeManager;
import com.github.lukesky19.skyPrestige.protection.listener.ProtectionOrbListener;
import com.github.lukesky19.skyPrestige.protection.manager.ProtectionOrbManager;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.task.TaskManager;
import com.github.lukesky19.skyPrestige.teleport.TeleportationManager;
import com.github.lukesky19.skyPrestige.vault.VaultManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The main class for the SkyPrestige plugin.
 */
public final class SkyPrestige extends JavaPlugin {
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
    private SkyPrestigeExpansion skyPrestigeExpansion;

    /**
     * Default Constructor
     */
    public SkyPrestige() {}

    /**
     * This method is run when the plugin is enabled to set up required data.
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
        guiManager = new GUIManager(this);
        islandDataManager = new IslandDataManager(databaseManager);
        leaderboardManager = new LeaderboardManager(this, islandDataManager, databaseManager);
        multiplierManager = new MultiplierManager(this, settingsManager, localeManager);
        taskManager = new TaskManager(this, settingsManager, islandDataManager, leaderboardManager, multiplierManager);
        PrestigeManager prestigeManager = new PrestigeManager(this, settingsManager, localeManager, guiConfigManager, prestigeConfigManager, guiManager, islandDataManager, databaseManager, hookManager);
        TeleportationManager teleportationManager = new TeleportationManager(this, settingsManager, localeManager, databaseManager);
        VaultManager vaultManager = new VaultManager(settingsManager);
        protectionOrbManager = new ProtectionOrbManager(this, settingsManager);

        // Register Commands
        SkyPrestigeCommand skyPrestigeCommand = new SkyPrestigeCommand(this, settingsManager, localeManager, guiConfigManager, prestigeConfigManager, prestigeManager, islandDataManager, leaderboardManager, guiManager, databaseManager, vaultManager, protectionOrbManager, multiplierManager);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(skyPrestigeCommand.createCommand(),
                                "Command to manage and use the SkyPrestige plugin.",
                                List.of("prestige")));

        // Listeners
        PluginManager pluginManager = this.getServer().getPluginManager();

        // Brewing-related Listeners
        pluginManager.registerEvents(new FreshBrewListener(), this);

        // Connection-related Listeners
        pluginManager.registerEvents(new PlayerJoinListener(databaseManager, prestigeManager, islandDataManager, teleportationManager), this);
        pluginManager.registerEvents(new PlayerQuitListener(this, databaseManager, islandDataManager), this);

        // Prestige-related Listeners
        pluginManager.registerEvents(new IslandListener(this, settingsManager, localeManager, databaseManager, islandDataManager, prestigeManager, hookManager), this);

        // Protection Orb Listener
        pluginManager.registerEvents(new ProtectionOrbListener(localeManager, protectionOrbManager), this);

        // GUI-related Listeners
        pluginManager.registerEvents(new GUIListener(guiManager), this);

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

        // Register the PlaceholderAPI expansion
        registerExpansion();

        // Reload the plugin
        reload();

        databaseFuture.thenAccept(v -> {
            // Load player data for any online players.
            this.getServer().getOnlinePlayers().forEach(player -> {
                UUID uuid = player.getUniqueId();

                // Insert the player's uuid into the database if it doesn't exist
                databaseManager.getPlayerIdsTable().insertPlayerId(uuid);

                // Load the player's island data
                islandDataManager.loadIslandData(uuid);

                // Handle any prestiges that occurred while the player was offline
                prestigeManager.handleOfflinePrestiges(player);

                // Handle any queued teleports for the player.
                teleportationManager.handleQueuedTeleports(player);
            });
        });
    }

    /**
     * This method is run when the plugin is disabled and is used to clean up any data.
     */
    @Override
    public void onDisable() {
        // Unregister the PlaceholderAPI expansion
        unregisterExpansion();

        // Close any open GUIs
        if(guiManager != null) guiManager.closeOpenGUIs(true);

        // Stop the save task
        if(taskManager != null) taskManager.stopSaveTask();

        // Save any loaded island data and clean up the database.
        if(islandDataManager != null) {
            islandDataManager.saveIslandData().thenAccept(v -> {
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
    public void reload() {
        // Close any open GUIs
        guiManager.closeOpenGUIs(false);

        // Reload plugin configuration
        settingsManager.reload();
        localeManager.reload();
        guiConfigManager.reload();
        prestigeConfigManager.reload();
        leaderboardManager.updateDatabaseTopTen();
        protectionOrbManager.reload();
        multiplierManager.reload();

        // (Re-)start the plugin's task
        taskManager.startTasks();
    }

    /**
     * This method registers the PlaceholderAPI expansion if PlaceholderAPI is enabled.
     */
    private void registerExpansion() {
        if(this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if(skyPrestigeExpansion == null) {
                skyPrestigeExpansion = new SkyPrestigeExpansion(islandDataManager, leaderboardManager);
                skyPrestigeExpansion.register();
            }
        }
    }

    /**
     * This method unregisters the PlaceholderAPI expansion if PlaceholderAPI is enabled.
     */
    private void unregisterExpansion() {
        if(this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if(skyPrestigeExpansion != null) {
                skyPrestigeExpansion.unregister();
            }
        }
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
