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

import com.github.lukesky19.skyPrestige.dataHandler.manager.LeaderboardManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * This task updates the top ten islands.
 */
public class CalculateTopTenTask extends BukkitRunnable {
    private final @NotNull LeaderboardManager leaderboardManager;

    /**
     * Constructor
     * @param leaderboardManager A {@link LeaderboardManager} instance.
     */
    public CalculateTopTenTask(@NotNull LeaderboardManager leaderboardManager) {
        this.leaderboardManager = leaderboardManager;
    }

    /**
     * Update the top 10 islands.
     */
    @Override
    public void run() {
        leaderboardManager.updateTopTen();
    }
}
