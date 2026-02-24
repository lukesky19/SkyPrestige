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
package com.github.lukesky19.skyPrestige.data.data.island;

import com.github.lukesky19.skyPrestige.multiplier.Multiplier;
import com.github.lukesky19.skyPrestige.util.key.PageSlotKey;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * This class tests {@link IslandData}.
 */
@ExtendWith(MockitoExtension.class)
public class IslandDataTest {
    /**
     * Test that the default constructor throws an error.
     */
    @Test
    public void testDefaultIslandDataConstructor() {
        assertThrows(RuntimeException.class, IslandData::new);
    }

    /**
     * Test the constructor with an island id only.
     */
    @Test
    public void testIslandIdIslandDataConstructor() {
        assertDoesNotThrow(() -> new IslandData("BSkyBlock" + UUID.randomUUID()));
    }

    /**
     * Test the constructor with all data.
     */
    @Test
    public void testFullIslandDataConstructor() {
        assertDoesNotThrow(() -> new IslandData("BSkyBlock" + UUID.randomUUID(), 0, 0, new Multiplier(), false, false, new HashMap<>()));
    }

    /**
     * Test the cloning of island data.
     */
    @Test
    public void testClone() {
        IslandData islandData1 = new IslandData("BSkyBlock" + UUID.randomUUID());
        IslandData islandData2 = islandData1.clone();

        assertNotSame(islandData1, islandData2);
    }

    /**
     * Tests if two island data classes are equal by island id.
     */
    @Test
    public void testEqualsByIslandId() {
        String islandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData1 = new IslandData(islandId);
        IslandData islandData2 = new IslandData(islandId);

        assertEquals(islandData1, islandData2);
    }

    /**
     * Tests if the two objects are not equal due to comparing a non-island data class.
     */
    @Test
    public void testEqualsNotIslandData() {
        IslandData islandData1 = new IslandData("BSkyBlock" + UUID.randomUUID());
        String dummy = "Dummy";

        // This purposely passes incompatible types for testing purposes.
        assertFalse(islandData1.equals(dummy));
    }

    /**
     * Tests if two island data objects are not equal because of island ids.
     */
    @Test
    public void testEqualsNotEqualByIslandId() {
        IslandData islandData1 = new IslandData("BSkyBlock" + UUID.randomUUID());
        IslandData islandData2 = new IslandData("BSkyBlock" + UUID.randomUUID());

        // Test not equal by island id
        assertNotEquals(islandData1, islandData2);
    }

    /**
     * Tests if two island data objects are not equal because of prestige level.
     */
    @Test
    public void testEqualsNotEqualByPrestigeLevel() {
        String islandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData1 = new IslandData(islandId, 0, 0, new Multiplier(), false, false, new HashMap<>());
        IslandData islandData2 = new IslandData(islandId, 1, 0, new Multiplier(), false, false, new HashMap<>());

        assertNotEquals(islandData1, islandData2);
    }

    /**
     * Tests if two island data objects are not equal because of prestige points.
     */
    @Test
    public void testEqualsNotEqualByPrestigePoints() {
        String islandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData1 = new IslandData(islandId, 0, 0, new Multiplier(), false, false, new HashMap<>());
        IslandData islandData2 = new IslandData(islandId, 0, 100, new Multiplier(), false, false, new HashMap<>());

        assertNotEquals(islandData1, islandData2);
    }

    /**
     * Tests if two island data objects are not equal because of the multiplier.
     */
    @Test
    public void testEqualsNotEqualByMultiplier() {
        String islandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData1 = new IslandData(islandId, 0, 100, new Multiplier(1, 100), false, false, new HashMap<>());
        IslandData islandData2 = new IslandData(islandId, 0, 100, new Multiplier(2, 200), false, false, new HashMap<>());

        assertNotEquals(islandData1, islandData2);
    }

    /**
     * Tests if two island data objects are not equal because of leaderboard exemption status.
     */
    @Test
    public void testEqualsNotEqualByLeaderboardExemption() {
        String islandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData1 = new IslandData(islandId, 0, 0, new Multiplier(), false, false, new HashMap<>());
        IslandData islandData2 = new IslandData(islandId, 0, 0, new Multiplier(), true, false, new HashMap<>());

        assertNotEquals(islandData1, islandData2);
    }

    /**
     * Tests if two island data objects are not equal because of prestige exemption status.
     */
    @Test
    public void testEqualsNotEqualByPrestigeExemption() {
        String islandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData1 = new IslandData(islandId, 0, 0, new Multiplier(), false, false, new HashMap<>());
        IslandData islandData2 = new IslandData(islandId, 0, 0, new Multiplier(), false, true, new HashMap<>());

        assertNotEquals(islandData1, islandData2);
    }

    /**
     * Tests if two island data objects are not equal because of vault items.
     */
    @Test
    public void testEqualsNotEqualByVaultItems() {
        String islandId = "BSkyBlock" + UUID.randomUUID();
        ItemStack itemStack = mock(ItemStack.class);

        IslandData islandData1 = new IslandData(islandId, 0, 0, new Multiplier(), false, false, new HashMap<>());
        IslandData islandData2 = new IslandData(islandId, 0, 0, new Multiplier(), false, false, Map.of(new PageSlotKey(0, 10), itemStack));

        assertNotEquals(islandData1, islandData2);
    }

    /**
     * Test setting island id.
     */
    @Test
    public void testSetIslandId() {
        String oldIslandId = "BSkyBlock" + UUID.randomUUID();
        String newIslandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData = new IslandData(oldIslandId);

        islandData.setIslandId(newIslandId);

        assertEquals(islandData.getIslandId(), newIslandId);
    }

    /**
     * Test getting island id.
     */
    @Test
    public void testGetIslandId() {
        String islandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData = new IslandData(islandId);

        assertEquals(islandData.getIslandId(), islandId);
    }

    /**
     * Test setting prestige level.
     */
    @Test
    public void testSetPrestigeLevel() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        islandData.setPrestigeLevel(1);

        assertEquals(1, islandData.getPrestigeLevel());
    }

    /**
     * Test setting prestige level below zero.
     */
    @Test
    public void testSetPrestigeLevelBelowZero() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        islandData.setPrestigeLevel(-1);

        assertEquals(0, islandData.getPrestigeLevel());
    }

    /**
     * Test getting prestige level.
     */
    @Test
    public void testGetPrestigeLevel() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        assertEquals(0, islandData.getPrestigeLevel());
    }

    /**
     * Test setting prestige points.
     */
    @Test
    public void testSetPrestigePoints() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        islandData.setPrestigePoints(1);

        assertEquals(1, islandData.getPrestigePoints());
    }

    /**
     * Test setting prestige points below zero.
     */
    @Test
    public void testSetPrestigePointsBelowZero() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        islandData.setPrestigePoints(-1);

        assertEquals(0, islandData.getPrestigePoints());
    }

    /**
     * Test adding prestige points.
     */
    @Test
    public void testAddPrestigePoints() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        islandData.addPrestigePoints(5);

        assertEquals(5, islandData.getPrestigePoints());
    }

    /**
     * Test removing prestige points.
     */
    @Test
    public void testRemovePrestigePoints() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        islandData.setPrestigePoints(25);

        islandData.removePrestigePoints(5);

        assertEquals(20, islandData.getPrestigePoints());
    }

    /**
     * Test removing prestige points resulting in points below zero.
     */
    @Test
    public void testRemovePrestigePointsBelowZero() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        islandData.removePrestigePoints(5);

        assertEquals(0, islandData.getPrestigePoints());
    }

    /**
     * Test getting prestige points.
     */
    @Test
    public void testGetPrestigePoints() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        assertEquals(0, islandData.getPrestigeLevel());
    }

    /**
     * Test getting the leaderboard exemption status.
     */
    @Test
    public void testIsLeaderboardExempt() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        assertFalse(islandData.isLeaderboardExempt());
    }

    /**
     * Test setting the leaderboard exemption status.
     */
    @Test
    public void testSetLeaderboardExempt() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        islandData.setLeaderboardExempt(true);

        assertTrue(islandData.isLeaderboardExempt());
    }

    /**
     * Test getting the prestige exemption status.
     */
    @Test
    public void testIsPrestigeExempt() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        assertFalse(islandData.isPrestigeExempt());
    }

    /**
     * Test setting the prestige exemption status.
     */
    @Test
    public void testSetPrestigeExempt() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        islandData.setPrestigeExempt(true);

        assertTrue(islandData.isPrestigeExempt());
    }

    /**
     * Test adding an item to the vault.
     */
    @Test
    public void testAddVaultItem() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());
        PageSlotKey pageSlotKey = new PageSlotKey(0, 10);
        ItemStack itemStack = mock(ItemStack.class);

        islandData.addVaultItem(0, 10, itemStack);

        Map<PageSlotKey, ItemStack> vaultItems = islandData.getVaultItems();
        assertEquals(1, vaultItems.size());
        assertTrue(vaultItems.containsKey(pageSlotKey));
        assertEquals(itemStack, vaultItems.get(pageSlotKey));
    }

    /**
     * Test removing an item to the vault.
     */
    @Test
    public void testRemoveVaultItem() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());
        PageSlotKey pageSlotKey = new PageSlotKey(0, 10);
        ItemStack itemStack = mock(ItemStack.class);

        islandData.addVaultItem(0, 10, itemStack);
        islandData.removeVaultItem(0, 10);

        Map<PageSlotKey, ItemStack> vaultItems = islandData.getVaultItems();
        assertEquals(0, vaultItems.size());
        assertFalse(vaultItems.containsKey(pageSlotKey));
    }

    /**
     * Test getting vault items on a specific page in the vault.
     */
    @Test
    public void testGetVaultItemsByPageNumber() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());
        ItemStack itemStack1 = mock(ItemStack.class);
        ItemStack itemStack2 = mock(ItemStack.class);

        islandData.addVaultItem(0, 10, itemStack1);
        islandData.addVaultItem(1, 10, itemStack2);

        Map<Integer, ItemStack> pageItems = islandData.getVaultItemsByPageNumber(0);
        assertEquals(1, pageItems.size());
        assertTrue(pageItems.containsKey(10));
    }

    /**
     * Test getting vault items on a specific page in the vault, but the page is empty.
     */
    @Test
    public void testGetVaultItemsByPageNumberEmptyMap() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());

        Map<Integer, ItemStack> pageItems = islandData.getVaultItemsByPageNumber(0);
        assertTrue(pageItems.isEmpty());
    }

    /**
     * Test clearing vault items.
     */
    @Test
    public void testClearVaultItems() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());
        ItemStack itemStack = mock(ItemStack.class);

        islandData.addVaultItem(0, 10, itemStack);

        assertFalse(islandData.getVaultItems().isEmpty());

        islandData.clearVaultItems();

        assertTrue(islandData.getVaultItems().isEmpty());
    }

    /**
     * Test the setting of vault items.
     */
    @Test
    public void testSetVaultItems() {
        IslandData islandData = new IslandData("BSkyBlock" + UUID.randomUUID());
        Map<PageSlotKey, ItemStack> vaultItems = new HashMap<>();
        ItemStack itemStack = mock(ItemStack.class);
        vaultItems.put(new PageSlotKey(0, 10), itemStack);

        islandData.setVaultItems(vaultItems);

        assertEquals(vaultItems, islandData.getVaultItems());
    }
}
