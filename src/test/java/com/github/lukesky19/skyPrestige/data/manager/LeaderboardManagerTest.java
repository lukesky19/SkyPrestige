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
package com.github.lukesky19.skyPrestige.data.manager;

import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.data.leaderboard.Position;
import com.github.lukesky19.skyPrestige.data.data.leaderboard.TopTen;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.IslandDataTable;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.Multiplier;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LeaderboardManagerTest {
    @Mock
    private SkyPlugin plugin;
    @Mock
    private Server server;
    @Mock
    private DatabaseManager databaseManager;
    @Mock
    private IslandDataTable islandDataTable;
    @Mock
    private IslandDataManager islandDataManager;
    @Mock
    private HookManager hookManager;
    @Mock
    private BentoBoxHook bentoBoxHook;
    @Mock
    private TopTen topTen;
    @Mock
    private Island island1;
    @Mock
    private Island island3;
    @Mock
    private Player player1;
    @Mock
    private Player player3;
    @Mock
    private OfflinePlayer offlinePlayer;

    private LeaderboardManager leaderboardManager;

    /**
     * Setup before each the test.
     */
    @BeforeEach
    public void beforeEach() {
        when(plugin.getServer()).thenReturn(server);

        leaderboardManager = new LeaderboardManager(plugin, islandDataManager, databaseManager, hookManager);
    }

    /**
     * Test the updating of the cached top ten from the database.
     */
    @Test
    public void testUpdateDatabaseTopTen() {
        when(databaseManager.getIslandDataTable()).thenReturn(islandDataTable);
        when(islandDataTable.getTopTenByPrestigeLevelAndPointsNotExempt()).thenReturn(CompletableFuture.completedFuture(new TopTen()));

        leaderboardManager.updateDatabaseTopTen();

        verify(islandDataTable).getTopTenByPrestigeLevelAndPointsNotExempt();
    }

    /**
     * Test the updating of the cached top ten islands by prestige levels and prestige points.
     */
    @Test
    public void testUpdateTopTen() {
        when(databaseManager.getIslandDataTable()).thenReturn(islandDataTable);
        when(islandDataTable.getTopTenByPrestigeLevelAndPointsNotExempt()).thenReturn(CompletableFuture.completedFuture(topTen));
        when(hookManager.getHook(BentoBoxHook.class)).thenReturn(bentoBoxHook);

        String islandId1 = "BSkyBlock" + UUID.randomUUID();
        String islandId2 = "BSkyBlock" + UUID.randomUUID();
        String islandId3 = "BSkyBlock" + UUID.randomUUID();
        String islandId4 = "BSkyBlock" + UUID.randomUUID();
        String islandId5 = "BSkyBlock" + UUID.randomUUID();
        String islandId6 = "BSkyBlock" + UUID.randomUUID();
        String islandId7 = "BSkyBlock" + UUID.randomUUID();
        String islandId8 = "BSkyBlock" + UUID.randomUUID();
        String islandId9 = "BSkyBlock" + UUID.randomUUID();
        String islandId10 = "BSkyBlock" + UUID.randomUUID();

        UUID island1Owner = UUID.randomUUID();
        UUID island3Owner = UUID.randomUUID();

        when(bentoBoxHook.getIslandById(islandId1)).thenReturn(Optional.of(island1));
        when(bentoBoxHook.getIslandById(islandId3)).thenReturn(Optional.of(island3));
        when(bentoBoxHook.getIslandById(islandId4)).thenReturn(Optional.empty());

        when(island1.getOwner()).thenReturn(island1Owner);
        when(island3.getOwner()).thenReturn(island3Owner);

        when(server.getPlayer(island1Owner)).thenReturn(player1);
        when(player1.getName()).thenReturn("player1");
        when(player1.isOnline()).thenReturn(true);
        when(player1.isConnected()).thenReturn(true);
        when(server.getPlayer(island3Owner)).thenReturn(player3);
        when(player3.getName()).thenReturn("player3");
        when(player3.isOnline()).thenReturn(true);
        when(player3.isConnected()).thenReturn(true);

        when(topTen.getPositions()).thenReturn(
                List.of(
                        new Position(islandId1, "player1", 10, 10),
                        new Position(islandId2, "player2", 9, 9),
                        new Position(islandId3, "player3", 8, 8),
                        new Position(islandId4, "player4", 7, 7),
                        new Position(islandId5, "player5", 6, 6),
                        new Position(islandId6, "player6", 5, 5),
                        new Position(islandId7, "player7", 4, 4),
                        new Position(islandId8, "player8", 3, 3),
                        new Position(islandId9, "player9", 2, 2),
                        new Position(islandId10, "player10", 1, 1)));

        when(islandDataManager.getAllData()).thenReturn(
                Map.of(
                        islandId1, new IslandData(islandId1, 25, 150.0, new Multiplier(), false, false, new HashMap<>()),
                        islandId2, new IslandData(islandId2, 20, 125.0, new Multiplier(), true, false, new HashMap<>()),
                        islandId3, new IslandData(islandId3, 15, 100.0, new Multiplier(), false, false, new HashMap<>()),
                        islandId4, new IslandData(islandId4, 20, 125.0, new Multiplier(), false, false, new HashMap<>())
                ));

        leaderboardManager.updateDatabaseTopTen().join();

        leaderboardManager.updateTopTen();

        TopTen testTopTen = leaderboardManager.getTopTenNotExempt();

        assertNotNull(testTopTen.getPosition(1));
        assertEquals(testTopTen.getPosition(1), new Position(islandId1, "player1", 25, 150));
        assertNotNull(testTopTen.getPosition(2));
        assertEquals(testTopTen.getPosition(2), new Position(islandId3, "player3", 15, 100));
        assertNotNull(testTopTen.getPosition(3));
        assertEquals(testTopTen.getPosition(3), new Position(islandId2, "player2", 9, 9.0));
        assertNotNull(testTopTen.getPosition(4));
        assertEquals(testTopTen.getPosition(4), new Position(islandId4, "player4", 7, 7));
        assertNotNull(testTopTen.getPosition(5));
        assertEquals(testTopTen.getPosition(5), new Position(islandId5, "player5", 6, 6));
        assertNotNull(testTopTen.getPosition(6));
        assertEquals(testTopTen.getPosition(6), new Position(islandId6, "player6", 5, 5));
        assertNotNull(testTopTen.getPosition(7));
        assertEquals(testTopTen.getPosition(7), new Position(islandId7, "player7", 4, 4));
        assertNotNull(testTopTen.getPosition(8));
        assertEquals(testTopTen.getPosition(8), new Position(islandId8, "player8", 3, 3));
        assertNotNull(testTopTen.getPosition(9));
        assertEquals(testTopTen.getPosition(9), new Position(islandId9, "player9", 2, 2));
        assertNotNull(testTopTen.getPosition(10));
        assertEquals(testTopTen.getPosition(10), new Position(islandId10, "player10", 1, 1));
    }

    /**
     * Test getting a positon at a specific position number.
     */
    @Test
    public void testGetPositionAtPositionNumber() {
        assertNull(leaderboardManager.getPositionAtPositionNumber(1));
    }

    /**
     * Test getting the player name from an online player
     */
    @Test
    public void testGetPlayerNameFromPlayer() {
        UUID player1Id = UUID.randomUUID();
        when(server.getPlayer(player1Id)).thenReturn(player1);
        when(player1.isOnline()).thenReturn(true);
        when(player1.isConnected()).thenReturn(true);
        when(player1.getName()).thenReturn("lukeskywlker19");

        String testName = leaderboardManager.getPlayerName(player1Id);
        assertNotNull(testName);
        assertEquals("lukeskywlker19", testName);
    }

    /**
     * Test getting the player name, but no online player was found.
     */
    @Test
    public void testGetPlayerNamePlayerNull() {
        UUID player1Id = UUID.randomUUID();
        when(server.getOfflinePlayer(player1Id)).thenReturn(offlinePlayer);
        when(offlinePlayer.getName()).thenReturn("lukeskywlker19");

        String testName = leaderboardManager.getPlayerName(player1Id);
        assertNotNull(testName);
        assertEquals("lukeskywlker19", testName);
    }

    /**
     * Test getting the player name, but the player is not online.
     */
    @Test
    public void testGetPlayerNamePlayerNotOnline() {
        UUID player1Id = UUID.randomUUID();
        when(server.getPlayer(player1Id)).thenReturn(player1);
        when(player1.isOnline()).thenReturn(false);
        when(server.getOfflinePlayer(player1Id)).thenReturn(offlinePlayer);
        when(offlinePlayer.getName()).thenReturn("lukeskywlker19");

        String testName = leaderboardManager.getPlayerName(player1Id);
        assertNotNull(testName);
        assertEquals("lukeskywlker19", testName);
    }

    /**
     * Test getting the player name, but the player is not connected.
     */
    @Test
    public void testGetPlayerNamePlayerNotConnected() {
        UUID player1Id = UUID.randomUUID();
        when(server.getPlayer(player1Id)).thenReturn(player1);
        when(player1.isOnline()).thenReturn(true);
        when(player1.isConnected()).thenReturn(false);
        when(server.getOfflinePlayer(player1Id)).thenReturn(offlinePlayer);
        when(offlinePlayer.getName()).thenReturn("lukeskywlker19");

        String testName = leaderboardManager.getPlayerName(player1Id);
        assertNotNull(testName);
        assertEquals("lukeskywlker19", testName);
    }

    /**
     * Get the player's name, but no valid name was found so the placeholder text is returned.
     */
    @Test
    public void testGetPlayerNamePlaceholder() {
        UUID player1Id = UUID.randomUUID();
        when(server.getOfflinePlayer(player1Id)).thenReturn(offlinePlayer);

        String testName = leaderboardManager.getPlayerName(player1Id);
        assertNotNull(testName);
        assertEquals("Unknown Island Owner", testName);
    }
}
