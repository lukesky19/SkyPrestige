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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link TopTen}.
 */
@ExtendWith(MockitoExtension.class)
public class TopTenTest {
    /**
     * Tests the default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        TopTen topTen = new TopTen();

        assertNotNull(topTen);
        assertTrue(topTen.getPositions().isEmpty());
    }

    /**
     * Tests the constructor that takes a list of positions.
     */
    @Test
    public void testConstructorWithPositionsList() {
        List<Position> positionList = new ArrayList<>();
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "lukeskywlker19", 5, 100));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "jeb_", 3, 25));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Notch", 1, 85));

        TopTen topTen = new TopTen(positionList);
        assertFalse(topTen.getPositions().isEmpty());
        assertEquals(3, topTen.getPositions().size());
    }

    /**
     * Test the setting and getting of positions.
     */
    @Test
    public void testSetGetPositions() {
        TopTen topTen = new TopTen();

        List<Position> positionList = new ArrayList<>();
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "lukeskywlker19", 5, 100));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "jeb_", 3, 25));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Notch", 1, 85));

        topTen.setPositions(positionList);

        assertFalse(topTen.getPositions().isEmpty());
        assertEquals(3, topTen.getPositions().size());
    }

    /**
     * Test the setting and getting of positions, but the position list contains more than 10 positions.
     */
    @Test
    public void testSetGetPositionsMoreThanTen() {
        TopTen topTen = new TopTen();

        List<Position> positionList = new ArrayList<>();
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "lukeskywlker19", 20, 100));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "jeb_", 19, 25));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Notch", 18, 85));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player1", 17, 125));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player2", 16, 85));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player3", 15, 35));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player4", 14, 4));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player5", 13, 60));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player6", 12, 15));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player7", 11, 9));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player8", 10, 35));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player9", 5, 115));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Player10", 1, 1250));

        topTen.setPositions(positionList);

        assertFalse(topTen.getPositions().isEmpty());
        assertEquals(10, topTen.getPositions().size());
    }

    /**
     * Test getting a position by a position number.
     */
    @Test
    public void testGetPositionByNumber() {
        TopTen topTen = new TopTen();

        List<Position> positionList = new ArrayList<>();
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "lukeskywlker19", 5, 100));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "jeb_", 3, 25));
        positionList.add(new Position("BSkyBlock" + UUID.randomUUID(), "Notch", 1, 85));

        topTen.setPositions(positionList);

        Position position1 = topTen.getPosition(1);
        Position position2 = topTen.getPosition(2);
        Position position3 = topTen.getPosition(3);

        assertNotNull(position1);
        assertNotNull(position2);
        assertNotNull(position3);

        assertEquals(position1, positionList.get(0));
        assertEquals(position2, positionList.get(1));
        assertEquals(position3, positionList.get(2));
    }
}
