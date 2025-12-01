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
package com.github.lukesky19.skyPrestige.core.util.key;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * This class acts as a single key for use in a Map to store a page number and slot number.
 * @param page The page number.
 * @param slot The slot number.
 */
public record PageSlotKey(int page, int slot) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Checks if the object provided is that of a PageSlotKey and if the page number and slot number are equal.
     * @param compareObject The {@link Object} to compare.
     * @return true if the object is equal, otherwise false.
     */
    @Override
    public boolean equals(@NotNull Object compareObject) {
        if(this == compareObject) return true;
        if(!(compareObject instanceof PageSlotKey(int comparePage, int compareSlot))) return false;
        return this.page == comparePage && this.slot == compareSlot;
    }

    /**
     * Hashes the page number and slot.
     * @return A hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(page, slot);
    }
}
