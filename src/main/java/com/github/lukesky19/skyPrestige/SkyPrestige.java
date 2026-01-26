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
import com.github.lukesky19.skyPrestige.configuration.manager.*;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.data.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skyPrestige.integration.hooks.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.integration.placeholderapi.SkyPrestigeExpansion;
import com.github.lukesky19.skyPrestige.integration.skyshop.MultiplierConfigurationProcessor;
import com.github.lukesky19.skyPrestige.integration.skyshop.MultiplierConfigurationSerializer;
import com.github.lukesky19.skyPrestige.listener.connection.PlayerJoinListener;
import com.github.lukesky19.skyPrestige.listener.connection.PlayerQuitListener;
import com.github.lukesky19.skyPrestige.listener.gui.GUIListener;
import com.github.lukesky19.skyPrestige.listener.island.IslandListener;
import com.github.lukesky19.skyPrestige.listener.points.*;
import com.github.lukesky19.skyPrestige.listener.points.brewing.FreshBrewListener;
import com.github.lukesky19.skyPrestige.listener.protection.ProtectionOrbListener;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeExemptionManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigePointsManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.player.PlayerSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skyPrestige.protection.ProtectionOrbManager;
import com.github.lukesky19.skyPrestige.task.TaskManager;
import com.github.lukesky19.skyPrestige.teleportation.TeleportationManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skyshop.api.SkyShopAPI;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The main class for the SkyPrestige plugin.
 */
public class SkyPrestige extends SkyPlugin {
    // Plugin Classes
    private GUIConfigManager guiConfigManager;
    private LocaleManager localeManager;
    private PlaceholderConfigManager placeholderConfigManager;
    private OptInConfigManager optInConfigManager;
    private OptOutConfigManager optOutConfigManager;
    private PrestigeConfigManager prestigeConfigManager;
    private PrestigePointsConfigManager prestigePointsConfigManager;
    private ProtectionOrbConfigManager protectionOrbConfigManager;
    private SettingsManager settingsManager;
    private VaultConfigManager vaultConfigManager;

    private GUIManager guiManager;
    private DatabaseManager databaseManager;
    private IslandDataManager islandDataManager;
    private LeaderboardManager leaderboardManager;
    private TaskManager taskManager;
    private ProtectionOrbManager protectionOrbManager;

    private @Nullable SkyPrestigeExpansion skyPrestigeExpansion;

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

        // Hooks
        HookManager hookManager = new HookManager(this);
        // Database
        databaseManager = new DatabaseManager(this, hookManager);
        CompletableFuture<Void> databaseFuture = databaseManager.setup();

        // Config managers
        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);
        placeholderConfigManager = new PlaceholderConfigManager(this);
        guiConfigManager = new GUIConfigManager(this);
        prestigeConfigManager = new PrestigeConfigManager(this);
        optInConfigManager = new OptInConfigManager(this);
        optOutConfigManager = new OptOutConfigManager(this);
        prestigePointsConfigManager = new PrestigePointsConfigManager(this);
        protectionOrbConfigManager = new ProtectionOrbConfigManager(this);
        vaultConfigManager = new VaultConfigManager(this);

        guiManager = new GUIManager();
        islandDataManager = new IslandDataManager(databaseManager, hookManager);
        leaderboardManager = new LeaderboardManager(this, islandDataManager, databaseManager, hookManager);
        MultiplierManager multiplierManager = new MultiplierManager(this, localeManager, islandDataManager);
        taskManager = new TaskManager(this, settingsManager, localeManager, islandDataManager, leaderboardManager, multiplierManager, hookManager);
        protectionOrbManager = new ProtectionOrbManager(this, protectionOrbConfigManager);
        IslandSettingsProcessor islandSettingsProcessor = new IslandSettingsProcessor(this, hookManager, databaseManager, islandDataManager);
        PlayerSettingsProcessor playerSettingsProcessor = new PlayerSettingsProcessor(hookManager, protectionOrbManager);
        RewardsProcessor rewardsProcessor = new RewardsProcessor(this, hookManager);
        PrestigePointsManager prestigePointsManager = new PrestigePointsManager(this.getComponentLogger(), prestigeConfigManager, prestigePointsConfigManager, islandDataManager);
        PrestigeManager prestigeManager = new PrestigeManager(this, settingsManager, localeManager, guiConfigManager, prestigeConfigManager, prestigePointsConfigManager, prestigePointsManager, databaseManager, guiManager, islandDataManager, hookManager, playerSettingsProcessor, islandSettingsProcessor, rewardsProcessor);
        PrestigeExemptionManager prestigeExemptionManager = new PrestigeExemptionManager(this, localeManager, guiConfigManager, optInConfigManager, optOutConfigManager, databaseManager, guiManager, hookManager, playerSettingsProcessor, islandSettingsProcessor, rewardsProcessor);
        TeleportationManager teleportationManager = new TeleportationManager(this, settingsManager, databaseManager, hookManager);

        // Register Commands
        SkyPrestigeCommand skyPrestigeCommand = new SkyPrestigeCommand(this, settingsManager, localeManager, guiConfigManager, prestigeConfigManager, optInConfigManager, optOutConfigManager, prestigeManager, prestigeExemptionManager, prestigePointsManager, islandDataManager, leaderboardManager, guiManager, databaseManager, vaultConfigManager, protectionOrbManager, multiplierManager, hookManager);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(skyPrestigeCommand.createCommand(),
                                "Command to manage and use the SkyPrestige plugin.",
                                List.of("prestige")));

        // Create and register the SkyPrestigeAPI
        SkyPrestigeAPI skyPrestigeAPI = new SkyPrestigeAPI(multiplierManager);
        this.getServer().getServicesManager().register(SkyPrestigeAPI.class, skyPrestigeAPI, this, ServicePriority.Lowest);

        // Register PlaceholderAPI Expansion
        registerExpansion(prestigePointsManager, multiplierManager, hookManager);

        // Listeners
        PluginManager pluginManager = this.getServer().getPluginManager();

        // Brewing-related Listeners
        pluginManager.registerEvents(new FreshBrewListener(hookManager), this);

        // Connection-related Listeners
        pluginManager.registerEvents(new PlayerJoinListener(databaseManager, prestigeManager, prestigeExemptionManager, islandDataManager, teleportationManager), this);
        pluginManager.registerEvents(new PlayerQuitListener(this, databaseManager, islandDataManager, hookManager), this);

        // Prestige-related Listeners
        pluginManager.registerEvents(new IslandListener(this, settingsManager, optInConfigManager, optOutConfigManager, prestigePointsManager, databaseManager, islandDataManager, islandSettingsProcessor, rewardsProcessor), this);

        // Protection Orb Listener
        pluginManager.registerEvents(new ProtectionOrbListener(localeManager, protectionOrbManager), this);

        // GUI-related Listeners
        pluginManager.registerEvents(new GUIListener(guiManager, hookManager), this);

        // Prestige Points Listeners
        if(hookManager.getHook(RoseStackerHook.class).isHooked()) {
            pluginManager.registerEvents(new BlockStackListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new BlockUnstackListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new SpawnerStackListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new SpawnerUnstackListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new EntityStackMultipleDeathListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        }

        pluginManager.registerEvents(new BlockBreakListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new BlockHarvestListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new BlockPlaceListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new CauldronListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new CraftItemListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new InventoryCloseListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new InventoryOpenListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerAnvilEnchantListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBeeHiveListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBoneMealListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBottleDragonsBreathListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBottleWaterListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBreedListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBrewListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBrushBlockListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBucketEmptyListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBucketFillListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerCakeConsumeListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerCompostListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerDropItemListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerEnchantmentTableEnchantListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerFishListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerItemConsumeListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerKillEntityListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerMilkCowListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerNameEntityListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerPickupItemListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerRenameItemListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerShearBlockListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerShearEntityListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerSleepListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerStripLogListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerTameEntityListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerThrowItemListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerUnwaxBlockListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerUnwaxEntityListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerWaxBlockListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerWaxEntityListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        if(hookManager.getHook(SkyPlayTimeHook.class).isHooked()) {
            pluginManager.registerEvents(new SkyPlayTimeListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        }
        pluginManager.registerEvents(new SmeltItemListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new WaterLogListener(this, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager), this);

        @Nullable Plugin plugin = this.getServer().getPluginManager().getPlugin("SkyShop");
        if(plugin != null && plugin.isEnabled()) {
            @Nullable RegisteredServiceProvider<SkyShopAPI> rsp = this.getServer().getServicesManager().getRegistration(SkyShopAPI.class);
            if(rsp != null) {
                // Register SkyShop integration if the api is already registered
                rsp.getProvider().register(
                        "skyprestige:multiplier",
                        new MultiplierConfigurationSerializer(),
                        new MultiplierConfigurationProcessor(localeManager, hookManager, multiplierManager));
            }
        }

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
        unregisterExpansion();

        // Unregister SkyShop integration
        @Nullable Plugin plugin = this.getServer().getPluginManager().getPlugin("SkyShop");
        if(plugin != null && plugin.isEnabled()) {
            @Nullable RegisteredServiceProvider<SkyShopAPI> rsp = this.getServer().getServicesManager().getRegistration(SkyShopAPI.class);
            if(rsp != null) {
                SkyShopAPI skyShopAPI = rsp.getProvider();

                // Unregister integration with SkyShop
                skyShopAPI.unregisterProcessor("skyprestige:multiplier");
            }
        }

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
        // Close any open GUIs
        guiManager.closeOpenGUIs(false);

        // Reload plugin configurations
        settingsManager.loadConfiguration();
        localeManager.loadConfiguration();
        placeholderConfigManager.loadConfiguration();
        optInConfigManager.loadConfiguration();
        optOutConfigManager.loadConfiguration();
        prestigePointsConfigManager.loadConfiguration();
        protectionOrbConfigManager.loadConfiguration();
        vaultConfigManager.loadConfiguration();
        prestigeConfigManager.loadConfigurations();
        guiConfigManager.reload();

        leaderboardManager.updateDatabaseTopTen();
        protectionOrbManager.reload();

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

    /**
     * This method registers the PlaceholderAPI expansion if PlaceholderAPI is enabled.
     */
    private void registerExpansion(
            @NotNull PrestigePointsManager prestigePointsManager,
            @NotNull MultiplierManager multiplierManager,
            @NotNull HookManager hookManager) {
        if(this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if(skyPrestigeExpansion == null) {
                skyPrestigeExpansion = new SkyPrestigeExpansion(
                        settingsManager,
                        placeholderConfigManager,
                        prestigePointsManager,
                        islandDataManager,
                        leaderboardManager,
                        multiplierManager,
                        hookManager);
            }

            skyPrestigeExpansion.register();
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
}
