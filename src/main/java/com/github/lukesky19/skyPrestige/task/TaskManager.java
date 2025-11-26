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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.leaderboard.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.points.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.settings.Settings;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.task.tasks.CacheTopTenTask;
import com.github.lukesky19.skyPrestige.task.tasks.CalculateTopTenTask;
import com.github.lukesky19.skyPrestige.task.tasks.MultiplierTask;
import com.github.lukesky19.skyPrestige.task.tasks.SaveTask;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages the plugin's task that regularly saves island data to the database.
 */
public class TaskManager {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull LeaderboardManager leaderboardManager;
    private final @NotNull MultiplierManager multiplierManager;

    private @Nullable BukkitTask saveTask;
    private @Nullable BukkitTask cacheTopTenTask;
    private @Nullable BukkitTask calculateTopTenTask;
    private @Nullable BukkitTask multiplierTask;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param leaderboardManager  A {@link LeaderboardManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public TaskManager(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull LeaderboardManager leaderboardManager,
            @NotNull MultiplierManager multiplierManager) {
        this.skyPrestige = skyPrestige;
        this.settingsManager = settingsManager;
        this.islandDataManager = islandDataManager;
        this.leaderboardManager = leaderboardManager;
        this.multiplierManager = multiplierManager;
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
        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null || settings.saveFrequencySeconds() == null) {
            skyPrestige.getComponentLogger().warn(AdventureUtil.deserialize("Unable to start the save task due to invalid plugin settings or save frequency seconds setting."));
            return;
        }

        long delayAndPeriod = 20L * 60L * settings.saveFrequencySeconds();

        saveTask = new SaveTask(islandDataManager).runTaskTimer(skyPrestige, delayAndPeriod, delayAndPeriod);
    }

    /**
     * Starts the {@link CacheTopTenTask}.
     */
    private void startCacheTopTenTask() {
        long ticks = 20L * 60L * 5L;

        cacheTopTenTask = new CacheTopTenTask(leaderboardManager).runTaskTimer(skyPrestige, ticks, ticks);
    }

    /**
     * Starts the {@link CalculateTopTenTask}.
     */
    private void startCalculateTopTenTask() {
        long ticks = 20L;

        calculateTopTenTask = new CalculateTopTenTask(leaderboardManager).runTaskTimer(skyPrestige, ticks, ticks);
    }

    /**
     * Starts the {@link CalculateTopTenTask}.
     */
    private void startMultiplierTask() {
        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null) return;
        if(!settings.multiplierEventSettings().enabled()) return;

        long ticks = 20L;

        multiplierTask = new MultiplierTask(multiplierManager).runTaskTimer(skyPrestige, ticks, ticks);
    }

    /**
     * Stop the {@link SaveTask}.
     */
    public void stopSaveTask() {
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
    public void stopCacheTopTenTask() {
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
    public void stopCalculateTopTenTask() {
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
    public void stopMultiplierTask() {
        if(multiplierTask != null) {
            if(!multiplierTask.isCancelled()) {
                multiplierTask.cancel();
            }

            multiplierTask = null;
        }
    }
}
