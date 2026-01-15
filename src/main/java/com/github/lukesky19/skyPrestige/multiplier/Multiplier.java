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
package com.github.lukesky19.skyPrestige.multiplier;

import org.jetbrains.annotations.NotNull;

/**
 * This class stores the data for a multiplier.
 */
public class Multiplier {
    private double multiplier = 0.0;
    private long time = 0;

    /**
     * Default Constructor
     */
    public Multiplier() {}

    /**
     * Constructor
     * @param multiplier The multiplier.
     * @param time The time limit.
     */
    public Multiplier(double multiplier, long time) {
        this.multiplier = Math.max(0.0, multiplier);
        this.time = Math.max(-1L, time);
    }

    /**
     * Creates a new {@link Multiplier}.
     * @return The cloned {@link Multiplier}.
     */
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod") // A constructor is used to clone data instead.
    public @NotNull Multiplier clone() {
        return new Multiplier(multiplier, time);
    }

    /**
     * Checks if the contents of the two Multiplier objects are equal.
     * Will return false for any non-Multiplier object.
     * @param compareObject The {@link Object} to compare.
     * @return true if equal, otherwise false.
     */
    @Override
    public boolean equals(@NotNull Object compareObject) {
        if(!(compareObject instanceof Multiplier compareMultiplier)) return false;

        return this.getMultiplier() == compareMultiplier.getMultiplier()
                && this.getTime() == compareMultiplier.getTime();
    }

    /**
     * Get the multiplier.
     * @return The multiplier.
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Get the multiplier time.
     * @return The multiplier time.
     */
    public long getTime() {
        return time;
    }

    /**
     * Set the multiplier
     * @param multiplier The multiplier
     */
    public void setMultiplier(double multiplier) {
        this.multiplier = Math.max(0.0, multiplier);
        if(this.multiplier == 0.0) time = 0;
    }

    /**
     * Set the multiplier time.
     * @param time The multiplier time.
     */
    public void setTime(long time) {
        this.time = Math.max(0, time);
        if(this.time == 0) multiplier = 0;
    }

    /**
     * Add to the multiplier.
     * @param multiplier The multiplier to add.
     */
    public void addMultiplier(double multiplier) {
        this.multiplier += Math.max(0, multiplier);
    }

    /**
     * Add to the multiplier time.
     * @param time The multiplier time to add.
     */
    public void addTime(long time) {
        if(time <= 0) return;

        this.time += time;
    }

    /**
     * Remove from the multiplier.
     * @param multiplier The multiplier to remove.
     */
    public void removeMultiplier(double multiplier) {
        this.multiplier -= Math.max(0, multiplier);
        if(this.multiplier == 0.0) time = 0;
    }

    /**
     * Remove time from the multiplier time.
     * @param time The multiplier time to remove.
     */
    public void removeTime(long time) {
        if(this.time <= 0) return;
        if(time <= 0) return;

        this.time = Math.max(0, (this.time - time));
        if(this.time == 0) multiplier = 0;
    }
}