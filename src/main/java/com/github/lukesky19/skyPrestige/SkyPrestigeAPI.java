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

import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.Island;

/**
 * This class acts as the API for SkyPrestige.
 */
public class SkyPrestigeAPI {
    private final @NotNull MultiplierManager multiplierManager;

    /**
     * Constructor
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public SkyPrestigeAPI(@NotNull MultiplierManager multiplierManager) {
        this.multiplierManager = multiplierManager;
    }

    /**
     * Add to the server multiplier.
     * @param multiplier The multiplier.
     * @param notice Should online players be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean addServerMultiplier(double multiplier, boolean notice) {
        return multiplierManager.addServerMultiplier(null, multiplier, null, notice);
    }

    /**
     * Add to the server multiplier time.
     * @param time The multiplier time.
     * @param notice Should online players be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean addServerMultiplier(long time, boolean notice) {
        return multiplierManager.addServerMultiplier(null, null, time, notice);
    }

    /**
     * Add to the server multiplier and multiplier time.
     * @param multiplier The multiplier.
     * @param time The multiplier time.
     * @param notice Should online players be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean addServerMultiplier(double multiplier, long time, boolean notice) {
        return multiplierManager.addServerMultiplier(null, multiplier, time, notice);
    }

    /**
     * Remove from the server multiplier.
     * @param multiplier The multiplier.
     * @param notice Should online players be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean removeServerMultiplier(double multiplier, boolean notice) {
        return multiplierManager.removeServerMultiplier(null, multiplier, null, notice);
    }

    /**
     * Remove from the server multiplier time.
     * @param time The multiplier time.
     * @param notice Should online players be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean removeServerMultiplier(long time, boolean notice) {
        return multiplierManager.removeServerMultiplier(null, null, time, notice);
    }

    /**
     * Remove from the server multiplier and multiplier time.
     * @param multiplier The multiplier.
     * @param time The multiplier time.
     * @param notice Should online players be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean removeServerMultiplier(double multiplier, long time, boolean notice) {
        return multiplierManager.removeServerMultiplier(null, multiplier, time, notice);
    }

    /**
     * Set the server multiplier.
     * @param multiplier The multiplier.
     * @param notice Should online players be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean setServerMultiplier(double multiplier, boolean notice) {
        return multiplierManager.setServerMultiplier(null, multiplier, null, notice);
    }

    /**
     * Set the server multiplier time.
     * @param time The multiplier time.
     * @param notice Should online players be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean setServerMultiplier(long time, boolean notice) {
        return multiplierManager.setServerMultiplier(null, null, time, notice);
    }

    /**
     * Set the server multiplier and multiplier time.
     * @param multiplier The multiplier.
     * @param time The multiplier time.
     * @param notice Should online players be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean setServerMultiplier(double multiplier, long time, boolean notice) {
        return multiplierManager.setServerMultiplier(null, multiplier, time, notice);
    }

    /**
     * Get the server multiplier.
     * @return The server multiplier.
     */
    public double getServerMultiplier() {
        return multiplierManager.getServerMultiplier();
    }

    /**
     * Get the remaining time in seconds the server multiplier will last for.
     * @return The remaining time.
     */
    public long getServerMultiplierTime() {
        return multiplierManager.getServerMultiplierTime();
    }

    /**
     * Add to the island multiplier.
     * @param island The {@link Island}.
     * @param multiplier The multiplier.
     * @param notice Should the island members be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean addIslandMultiplier(@NotNull Island island, double multiplier, boolean notice) {
        return multiplierManager.addIslandMultiplier(null, island, multiplier, null, notice);
    }

    /**
     * Add to the island multiplier time.
     * @param island The {@link Island}.
     * @param time The multiplier time.
     * @param notice Should the island members be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean addIslandMultiplier(@NotNull Island island, long time, boolean notice) {
        return multiplierManager.addIslandMultiplier(null, island, null, time, notice);
    }

    /**
     * Add to the island multiplier and multiplier time.
     * @param island The {@link Island}.
     * @param multiplier The multiplier.
     * @param time The multiplier time.
     * @param notice Should the island members be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean addIslandMultiplier(@NotNull Island island, double multiplier, long time, boolean notice) {
        return multiplierManager.addIslandMultiplier(null, island, multiplier, time, notice);
    }

    /**
     * Remove from the island multiplier.
     * @param island The {@link Island}.
     * @param multiplier The multiplier.
     * @param notice Should the island members be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean removeIslandMultiplier(@NotNull Island island, double multiplier, boolean notice) {
        return multiplierManager.removeIslandMultiplier(null, island, multiplier, null, notice);
    }

    /**
     * Remove from the island multiplier time.
     * @param island The {@link Island}.
     * @param time The multiplier time.
     * @param notice Should the island members be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean removeIslandMultiplier(@NotNull Island island, long time, boolean notice) {
        return multiplierManager.removeIslandMultiplier(null, island, null, time, notice);
    }

    /**
     * Remove from the island multiplier and multiplier time.
     * @param island The {@link Island}.
     * @param multiplier The multiplier.
     * @param time The multiplier time.
     * @param notice Should the island members be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean removeIslandMultiplier(@NotNull Island island, double multiplier, long time, boolean notice) {
        return multiplierManager.removeIslandMultiplier(null, island, multiplier, time, notice);
    }

    /**
     * Set the island multiplier.
     * @param island The {@link Island}.
     * @param multiplier The multiplier.
     * @param notice Should the island members be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean setIslandMultiplier(@NotNull Island island, double multiplier, boolean notice) {
        return multiplierManager.setIslandMultiplier(null, island, multiplier, null, notice);
    }

    /**
     * Set the island multiplier time.
     * @param island The {@link Island}.
     * @param time The multiplier time.
     * @param notice Should the island members be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean setIslandMultiplier(@NotNull Island island, long time, boolean notice) {
        return multiplierManager.setIslandMultiplier(null, island, null, time, notice);
    }

    /**
     * Set the island multiplier and multiplier time.
     * @param island The {@link Island}.
     * @param multiplier The multiplier.
     * @param time The multiplier time.
     * @param notice Should the island members be notified of the change?
     * @return true if successful, false if not.
     */
    public boolean setIslandMultiplier(@NotNull Island island, double multiplier, long time, boolean notice) {
        return multiplierManager.setIslandMultiplier(null, island, multiplier, time, notice);
    }

    /**
     * Get the island multiplier.
     * @param island The {@link Island}.
     * @return The island multiplier.
     */
    public double getIslandMultiplier(@NotNull Island island) {
        return multiplierManager.getIslandMultiplier(island);
    }

    /**
     * Get the remaining time in seconds the island multiplier will last for.
     * @param island The {@link Island}.
     * @return The remaining time.
     */
    public long getIslandMultiplierTime(@NotNull Island island) {
        return multiplierManager.getIslandMultiplierTime(island);
    }

    /**
     * Get the effective multiplier.
     * @param island The {@link Island} to get the multiplier for.
     * @return The effective multiplier. Will always be greater than or equal to 1.0.
     */
    public double getEffectiveMultiplier(@NotNull Island island) {
        return multiplierManager.getMultiplier(island);
    }
}