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
import com.github.lukesky19.skyPrestige.config.data.settings.Settings;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
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
    private @Nullable BukkitTask saveTask;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     */
    public TaskManager(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager) {
        this.skyPrestige = skyPrestige;
        this.settingsManager = settingsManager;
        this.islandDataManager = islandDataManager;
    }

    /**
     * Starts the {@link BukkitTask} that periodically saves island data.
     * If the task is already running, it will stop the task before starting it again.
     */
    public void startSaveTask() {
        stopSaveTask();

        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null || settings.saveFrequencySeconds() == null) {
            skyPrestige.getComponentLogger().warn(AdventureUtil.deserialize("Unable to start the save task due to invalid plugin settings or save frequency seconds setting."));
            return;
        }

        long delayAndPeriod = 20L * 60L * settings.saveFrequencySeconds();

        saveTask = skyPrestige.getServer().getScheduler().runTaskTimer(skyPrestige, () ->
                islandDataManager.saveIslandData(), delayAndPeriod, delayAndPeriod);
    }

    /**
     * Stop the {@link BukkitTask} that periodically saves island data.
     */
    public void stopSaveTask() {
        if(saveTask == null || saveTask.isCancelled()) return;

        saveTask.cancel();
        saveTask = null;
    }
}
