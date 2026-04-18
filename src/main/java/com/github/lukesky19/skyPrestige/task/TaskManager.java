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
package com.github.lukesky19.skyPrestige.task;

import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.data.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.task.tasks.CacheTopTenTask;
import com.github.lukesky19.skyPrestige.task.tasks.CalculateTopTenTask;
import com.github.lukesky19.skyPrestige.task.tasks.MultiplierTask;
import com.github.lukesky19.skyPrestige.task.tasks.SaveTask;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This class manages the plugin's task that regularly saves island data to the database.
 */
public class TaskManager {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull SettingsManager settingsManager;
    private final @NonNull LocaleManager localeManager;

    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull LeaderboardManager leaderboardManager;
    private final @NonNull MultiplierManager multiplierManager;
    private final @NonNull HookManager hookManager;

    private @Nullable BukkitTask saveTask;
    private @Nullable BukkitTask cacheTopTenTask;
    private @Nullable BukkitTask calculateTopTenTask;
    private @Nullable BukkitTask multiplierTask;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param leaderboardManager  A {@link LeaderboardManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public TaskManager(
            @NonNull SkyPlugin plugin,
            @NonNull SettingsManager settingsManager,
            @NonNull LocaleManager localeManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull LeaderboardManager leaderboardManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull HookManager hookManager) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.islandDataManager = islandDataManager;
        this.leaderboardManager = leaderboardManager;
        this.multiplierManager = multiplierManager;
        this.hookManager = hookManager;
    }

    /**
     * Start all tasks. Any existing tasks will be stopped before starting new ones.
     */
    public void startTasks() {
        stopTasks();

        startSaveTask();
        startCacheTopTenTask();
        startCalculateTopTenTask();
        startMultiplierTask();
    }

    /**
     * Stop all tasks.
     */
    public void stopTasks() {
        stopSaveTask();
        stopCacheTopTenTask();
        stopCalculateTopTenTask();
        stopMultiplierTask();
    }

    /**
     * Starts the {@link SaveTask}.
     */
    private void startSaveTask() {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null || settings.saveFrequencySeconds() == null) {
            plugin.getComponentLogger().warn(AdventureUtility.deserialize("Unable to start the save task due to invalid plugin settings or save frequency seconds setting."));
            return;
        }

        long delayAndPeriod = 20L * 60L * settings.saveFrequencySeconds();

        saveTask = new SaveTask(islandDataManager).runTaskTimer(plugin, delayAndPeriod, delayAndPeriod);
    }

    /**
     * Starts the {@link CacheTopTenTask}.
     */
    private void startCacheTopTenTask() {
        long ticks = 20L * 60L * 5L;

        cacheTopTenTask = new CacheTopTenTask(leaderboardManager).runTaskTimer(plugin, ticks, ticks);
    }

    /**
     * Starts the {@link CalculateTopTenTask}.
     */
    private void startCalculateTopTenTask() {
        long ticks = 20L;

        calculateTopTenTask = new CalculateTopTenTask(leaderboardManager).runTaskTimer(plugin, ticks, ticks);
    }

    /**
     * Starts the {@link CalculateTopTenTask}.
     */
    private void startMultiplierTask() {
        long ticks = 20L;

        multiplierTask = new MultiplierTask(plugin, localeManager, islandDataManager, multiplierManager, hookManager).runTaskTimer(plugin, ticks, ticks);
    }

    /**
     * Stop the {@link SaveTask}.
     */
    private void stopSaveTask() {
        if(saveTask != null) {
            if(!saveTask.isCancelled()) {
                saveTask.cancel();
            }

            saveTask = null;
        }
    }

    /**
     * Stop the {@link CacheTopTenTask}.
     */
    private void stopCacheTopTenTask() {
        if(cacheTopTenTask != null) {
            if(!cacheTopTenTask.isCancelled()) {
                cacheTopTenTask.cancel();
            }

            cacheTopTenTask = null;
        }
    }

    /**
     * Stop the {@link CalculateTopTenTask}.
     */
    private void stopCalculateTopTenTask() {
        if(calculateTopTenTask != null) {
            if(!calculateTopTenTask.isCancelled()) {
                calculateTopTenTask.cancel();
            }

            calculateTopTenTask = null;
        }
    }

    /**
     * Stop the {@link CalculateTopTenTask}.
     */
    private void stopMultiplierTask() {
        if(multiplierTask != null) {
            if(!multiplierTask.isCancelled()) {
                multiplierTask.cancel();
            }

            multiplierTask = null;
        }
    }
}
