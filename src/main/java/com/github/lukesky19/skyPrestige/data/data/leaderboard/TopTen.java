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
package com.github.lukesky19.skyPrestige.data.data.leaderboard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class stores the top ten positions.
 */
public class TopTen {
    private final @Nullable Position @NotNull [] positions = new Position[10];

    /**
     * Constructor
     */
    public TopTen() {}

    /**
     * Constructor
     * @param positionList A {@link List} of {@link Position}s that contain the top 10 positions.
     */
    public TopTen(@NotNull List<Position> positionList) {
        setPositions(positionList);
    }

    /**
     * Set the top ten positions.
     * @param positionList A {@link List} of {@link Position}s that contain the top 10 positions.
     */
    public void setPositions(@NotNull List<Position> positionList) {
        for(int i = 0; i < positionList.size() && i < positions.length; i++) {
            positions[i] = positionList.get(i);
        }
    }

    /**
     * Get a {@link List} of all non-null {@link Position}s.
     * @return A {@link List} of all non-null {@link Position}s.
     */
    public @NotNull List<@NotNull Position> getPositions() {
        return Arrays.stream(positions).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Get the {@link Position} at the provided position number.
     * @param positionNumber The position number.
     * @return A {@link Position} or null.
     */
    public @Nullable Position getPosition(int positionNumber) {
        return positions[positionNumber - 1];
    }
}

