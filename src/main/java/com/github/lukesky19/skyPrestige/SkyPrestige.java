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
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.hooks.LMBQuestHook;
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
import com.github.lukesky19.skyPrestige.processor.queued.QueuedSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.reset.SettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skyPrestige.protection.ProtectionOrbManager;
import com.github.lukesky19.skyPrestige.requirements.RequirementsManager;
import com.github.lukesky19.skyPrestige.task.TaskManager;
import com.github.lukesky19.skyPrestige.teleportation.TeleportationManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.version.VersionUtil;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.github.lukesky19.skyshop.api.SkyShopAPI;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

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
    private DialogManager dialogManager;
    private DatabaseManager databaseManager;
    private IslandDataManager islandDataManager;
    private LeaderboardManager leaderboardManager;
    private TaskManager taskManager;
    private ProtectionOrbManager protectionOrbManager;
    private PrestigePointsManager prestigePointsManager;

    private QueuedSettingsProcessor queuedSettingsProcessor;

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
        if(!checkSkyLibVersion()) return;
        // Check server version
        if(!checkServerVersion()) return;

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
        dialogManager = new DialogManager(this);
        islandDataManager = new IslandDataManager(databaseManager, hookManager);
        leaderboardManager = new LeaderboardManager(this, islandDataManager, databaseManager, hookManager);
        MultiplierManager multiplierManager = new MultiplierManager(this, localeManager, islandDataManager);
        taskManager = new TaskManager(this, settingsManager, localeManager, islandDataManager, leaderboardManager, multiplierManager, hookManager);
        protectionOrbManager = new ProtectionOrbManager(this, protectionOrbConfigManager);
        IslandSettingsProcessor islandSettingsProcessor = new IslandSettingsProcessor(this, hookManager, databaseManager, islandDataManager);
        PlayerSettingsProcessor playerSettingsProcessor = new PlayerSettingsProcessor(hookManager, protectionOrbManager);
        RewardsProcessor rewardsProcessor = new RewardsProcessor(this, hookManager);
        SettingsProcessor settingsProcessor = new SettingsProcessor(this, databaseManager, hookManager, settingsManager, prestigeConfigManager, optInConfigManager, optOutConfigManager, islandSettingsProcessor, playerSettingsProcessor, rewardsProcessor);
        queuedSettingsProcessor = new QueuedSettingsProcessor(this, databaseManager, settingsManager, prestigeConfigManager, optInConfigManager, optOutConfigManager, playerSettingsProcessor, rewardsProcessor);
        prestigePointsManager = new PrestigePointsManager(prestigePointsConfigManager);
        RequirementsManager requirementsManager = new RequirementsManager(this, localeManager, hookManager);
        PrestigeManager prestigeManager = new PrestigeManager(this, settingsManager, localeManager, guiConfigManager, prestigeConfigManager, requirementsManager, databaseManager, guiManager, islandDataManager, hookManager, playerSettingsProcessor, islandSettingsProcessor, rewardsProcessor);
        PrestigeExemptionManager prestigeExemptionManager = new PrestigeExemptionManager(this, localeManager, guiConfigManager, optInConfigManager, optOutConfigManager, databaseManager, guiManager, hookManager, playerSettingsProcessor, islandSettingsProcessor, rewardsProcessor);
        TeleportationManager teleportationManager = new TeleportationManager(this, databaseManager, hookManager);

        // Register Commands
        SkyPrestigeCommand skyPrestigeCommand = new SkyPrestigeCommand(this, settingsManager, localeManager, guiConfigManager, prestigePointsConfigManager, prestigeConfigManager, optInConfigManager, optOutConfigManager, prestigeManager, prestigeExemptionManager, requirementsManager, islandDataManager, leaderboardManager, guiManager, dialogManager, databaseManager, vaultConfigManager, protectionOrbManager, multiplierManager, hookManager);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(skyPrestigeCommand.createCommand(),
                                "Command to manage and use the SkyPrestige plugin.",
                                List.of("prestige")));

        // Create and register the SkyPrestigeAPI
        SkyPrestigeAPI skyPrestigeAPI = new SkyPrestigeAPI(multiplierManager);
        this.getServer().getServicesManager().register(SkyPrestigeAPI.class, skyPrestigeAPI, this, ServicePriority.Lowest);

        // Register PlaceholderAPI Expansion
        registerExpansion(requirementsManager, multiplierManager, hookManager);

        // Listeners
        PluginManager pluginManager = this.getServer().getPluginManager();

        // Brewing-related Listeners
        pluginManager.registerEvents(new FreshBrewListener(hookManager), this);

        // Connection-related Listeners
        pluginManager.registerEvents(new PlayerJoinListener(databaseManager, islandDataManager, teleportationManager, hookManager, queuedSettingsProcessor), this);
        pluginManager.registerEvents(new PlayerQuitListener(this, databaseManager, islandDataManager, hookManager), this);

        // Prestige-related Listeners
        pluginManager.registerEvents(new IslandListener(this, settingsManager, optInConfigManager, optOutConfigManager, databaseManager, islandDataManager, settingsProcessor, rewardsProcessor, guiManager), this);

        // Protection Orb Listener
        pluginManager.registerEvents(new ProtectionOrbListener(localeManager, protectionOrbManager), this);

        // GUI-related Listeners
        pluginManager.registerEvents(new GUIListener(guiManager, hookManager), this);

        // Prestige Points Listeners
        if(hookManager.getHook(RoseStackerHook.class).isHooked()) {
            pluginManager.registerEvents(new BlockStackListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new BlockUnstackListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new SpawnerStackListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new SpawnerUnstackListener(this, prestigePointsConfigManager,prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
            pluginManager.registerEvents(new EntityStackMultipleDeathListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        }

        pluginManager.registerEvents(new BlockBreakListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new BlockHarvestListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new BlockPlaceListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new CauldronListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new CraftItemListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new InventoryCloseListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new InventoryOpenListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerAnvilEnchantListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBeeHiveListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBoneMealListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBottleDragonsBreathListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBottleWaterListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBreedListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBrewListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBrushBlockListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBucketEmptyListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerBucketFillListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerCakeConsumeListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerCompostListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerDropItemListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerEnchantmentTableEnchantListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerFishListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerItemConsumeListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerKillEntityListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerMilkCowListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerNameEntityListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerPickupItemListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerRenameItemListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerShearBlockListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerShearEntityListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerSleepListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerStripLogListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerTameEntityListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerThrowItemListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerUnwaxBlockListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerUnwaxEntityListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerWaxBlockListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        pluginManager.registerEvents(new PlayerWaxEntityListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        if(hookManager.getHook(SkyPlayTimeHook.class).isHooked()) {
            pluginManager.registerEvents(new SkyPlayTimeListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        }
        pluginManager.registerEvents(new SmeltItemListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        if(pluginManager.isPluginEnabled("SkyEnchants")) {
            pluginManager.registerEvents(new MultiBlockBreakListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);
        }
        pluginManager.registerEvents(new WaterLogListener(this, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager), this);

        Plugin plugin = this.getServer().getPluginManager().getPlugin("SkyShop");
        if(plugin != null && plugin.isEnabled()) {
            RegisteredServiceProvider<SkyShopAPI> rsp = this.getServer().getServicesManager().getRegistration(SkyShopAPI.class);
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

        // Load player quest data
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        LMBQuestHook lmbQuestHook = hookManager.getHook(LMBQuestHook.class);
        if(lmbQuestHook.isHooked()) {
            this.getServer().getOnlinePlayers().forEach(player -> {
                UUID uuid = player.getUniqueId();

                List<Island> playerIslands = bentoBoxHook.getIslands(uuid);
                playerIslands.forEach(island -> lmbQuestHook.loadPlayerData(island.getMemberSet()));
            });
        }

        databaseFuture.thenAccept(_ -> {
            // Load player data for any online players.
            this.getServer().getOnlinePlayers().forEach(player -> {
                UUID uuid = player.getUniqueId();

                // Insert the player's uuid into the database if it doesn't exist
                databaseManager.getPlayerIdsTable().insertPlayerId(uuid);

                // Load the player's island data
                islandDataManager.loadDataByPlayerIdentifier(uuid);

                // Process any settings queued while the player was offline
                queuedSettingsProcessor.processQueuedSettings(player);

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
        Plugin plugin = this.getServer().getPluginManager().getPlugin("SkyShop");
        if(plugin != null && plugin.isEnabled()) {
            RegisteredServiceProvider<SkyShopAPI> rsp = this.getServer().getServicesManager().getRegistration(SkyShopAPI.class);
            if(rsp != null) {
                SkyShopAPI skyShopAPI = rsp.getProvider();

                // Unregister integration with SkyShop
                skyShopAPI.unregisterProcessor("skyprestige:multiplier");
            }
        }

        // Close any open GUIs
        if(guiManager != null) guiManager.closeOpenGUIs(true);

        if(dialogManager != null) dialogManager.closeOpenDialogs();

        // Stop the running tasks
        if(taskManager != null) taskManager.stopTasks();

        // Save any loaded island data and clean up the database.
        if(islandDataManager != null) {
            if(!islandDataManager.getAllData().isEmpty()) {
                CompletableFuture<Void> saveFuture = islandDataManager.saveData();
                saveFuture.join();

                saveFuture
                        .thenAccept(_ -> this.getComponentLogger().info(
                                AdventureUtility.deserialize("Successfully saved island data on plugin disable.")))
                        .exceptionally(ex -> {
                            this.getComponentLogger().error(
                                    AdventureUtility.deserialize("Failed to save island data on plugin disable. " +
                                            "Data loss will occur. Error: " + ex.getMessage()));
                            return null;
                        });
            }
        }

        if(databaseManager != null) {
            databaseManager.cleanUp();
        }
    }

    /**
     * This method is run to reload any plugin data.
     */
    @Override
    public void reload() {
        // Close any open GUIs
        guiManager.closeOpenGUIs(false);

        // Close any open Dialogs
        dialogManager.closeOpenDialogs();

        // Clear caches
        prestigePointsManager.clearCaches();

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
            int first = Integer.parseInt(splitVersion[0]);

            if(first >= 2) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtility.deserialize("SkyLib Version 2.0.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }

    /**
     * Checks if the Server's version is supported.
     * @return true if it is, false if not.
     */
    private boolean checkServerVersion() {
        if(VersionUtil.isLegacy()) {
            if(VersionUtil.getMajorVersion() >= 21 && VersionUtil.getMinorVersion() >= 6) {
                return true;
            }
        } else {
            return true;
        }

        this.getComponentLogger().error(AdventureUtility.deserialize("SkyPrestige requires the server to run Minecraft 1.21.6 or newer. Server version: " + VersionUtil.getMinecraftVersion()));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }

    /**
     * This method registers the PlaceholderAPI expansion if PlaceholderAPI is enabled.
     */
    private void registerExpansion(
            @NonNull RequirementsManager requirementsManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull HookManager hookManager) {
        if(this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if(skyPrestigeExpansion == null) {
                skyPrestigeExpansion = new SkyPrestigeExpansion(
                        settingsManager,
                        prestigeConfigManager,
                        placeholderConfigManager,
                        requirementsManager,
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