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
package com.github.lukesky19.skyPrestige.task.tasks;

import com.github.lukesky19.skyPrestige.dataHandler.manager.MultiplierManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * This task updates the multiplier cooldown seconds and either applies or removes the scheduled multiplier.
 */
public class MultiplierTask extends BukkitRunnable {
    private final @NotNull MultiplierManager multiplierManager;

    /**
     * Constructor
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public MultiplierTask(@NotNull MultiplierManager multiplierManager) {
        this.multiplierManager = multiplierManager;
    }

    /**
     * Removes 1 from the cooldown seconds and either applies or removes the scheduled multiplier when the cooldown reaches 0.
     */
    @Override
    public void run() {
        if(multiplierManager.getEventDuration() > 0) {
            multiplierManager.removeEventDurationSeconds(1);

            if(multiplierManager.getEventDuration() == 0) {
                multiplierManager.endEvent(true);
            }
        }

        if(multiplierManager.getTimeUntilNextEvent() > 0) {
            multiplierManager.removeTimeUntilNextEvent(1);

            if(multiplierManager.getTimeUntilNextEvent() == 0) {
                multiplierManager.startEvent(true);
            }
        }
    }
}
