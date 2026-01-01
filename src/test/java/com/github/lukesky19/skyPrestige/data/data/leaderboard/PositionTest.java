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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This class tests {@link Position}.
 */
@ExtendWith(MockitoExtension.class)
public class PositionTest {
    /**
     * Test the creation of a position record.
     */
    @Test
    public void testCreation() {
        String islandId = "BSkyBlock" + UUID.randomUUID();
        String ownerName = "lukeskywlker19";
        int prestigeLevel = 5;
        double prestigePoints = 100;

        Position position = new Position(islandId, ownerName, prestigeLevel, prestigePoints);

        assertNotNull(position);
        assertEquals(islandId, position.islandId());
        assertEquals(ownerName, position.ownerName());
        assertEquals(prestigeLevel, position.prestigeLevel());
        assertEquals(prestigePoints, position.prestigePoints());
    }
}
