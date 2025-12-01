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

import java.util.UUID;

/**
 * This class acts as a single key for use in a Map to store an island id and {@link UUID}.
 * @param islandId The island id.
 * @param uuid The {@link UUID}.
 */
public record IslandIdUUIDKey(@NotNull String islandId, @NotNull UUID uuid) {
    /**
     * Checks if the object provided is that of a IslandIdUUIDKey and if the island id and {@link UUID} are equal.
     * @param compareObject The {@link Object} to compare.
     * @return true if equal, otherwise false.
     */
    @Override
    public boolean equals(@NotNull Object compareObject) {
        if(this == compareObject) return true;
        if(!(compareObject instanceof IslandIdUUIDKey(String compareIslandId, UUID compareUuid))) return false;
        return this.islandId.equals(compareIslandId) && this.uuid.equals(compareUuid);
    }
}
