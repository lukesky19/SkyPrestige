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
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.IslandDataTable;
import com.github.lukesky19.skyPrestige.database.table.IslandIdsTable;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * This class tests {@link IslandDataManager}.
 */
@ExtendWith(MockitoExtension.class)
public class IslandDataManagerTest {
    @Mock
    private DatabaseManager databaseManager;
    @Mock
    private IslandIdsTable islandIdsTable;
    @Mock
    private IslandDataTable islandDataTable;
    @Mock
    private HookManager hookManager;
    @Mock
    private BentoBoxHook bentoBoxHook;
    @Mock
    private Island island1;
    @Mock
    private Island island2;

    private IslandDataManager islandDataManager;

    /**
     * Set up the required data for the tests.
     */
    @BeforeEach
    public void setup() {
        islandDataManager = new IslandDataManager(databaseManager, hookManager);
    }

    /**
     * Test loading island data by player identifier.
     */
    @Test
    public void testLoadDataByPlayerIdentifier() {
        String islandId1 = "BSkyBlock" + UUID.randomUUID();
        String islandId2 = "BSkyBlock" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        when(island1.getUniqueId()).thenReturn(islandId1);
        when(island2.getUniqueId()).thenReturn(islandId2);

        when(hookManager.getHook(BentoBoxHook.class)).thenReturn(bentoBoxHook);

        when(databaseManager.getIslandIdsTable()).thenReturn(islandIdsTable);
        when(databaseManager.getIslandDataTable()).thenReturn(islandDataTable);

        when(bentoBoxHook.getIslands(playerId)).thenReturn(List.of(island1, island2));

        CompletableFuture<Void> future = islandDataManager.loadDataByPlayerIdentifier(playerId);
        future.join();

        assertTrue(future.isDone());

        verify(islandIdsTable).insertIslandId(eq(islandId1));
        verify(islandIdsTable).insertIslandId(eq(islandId2));

        verify(islandDataTable).loadIslandData(eq(islandId1), any(IslandData.class));
        verify(islandDataTable).loadIslandData(eq(islandId2), any(IslandData.class));

        assertNotNull(islandDataManager.getData(islandId1));
        assertNotNull(islandDataManager.getData(islandId2));
    }

    /**
     * Test loading island data by player identifier, but their island already has it's data loaded.
     */
    @Test
    public void testLoadDataByPlayerIdentifierAlreadyLoaded() {
        when(hookManager.getHook(BentoBoxHook.class)).thenReturn(bentoBoxHook);
        when(databaseManager.getIslandIdsTable()).thenReturn(islandIdsTable);

        String islandId = "BSkyBlock" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        IslandData islandData = new IslandData(islandId);

        islandDataManager.setData(islandId, islandData);

        when(island1.getUniqueId()).thenReturn(islandId);
        when(bentoBoxHook.getIslands(playerId)).thenReturn(List.of(island1));

        CompletableFuture<Void> future = islandDataManager.loadDataByPlayerIdentifier(playerId);
        future.join();

        assertTrue(future.isDone());

        verify(islandIdsTable).insertIslandId(eq(islandId));
        verify(islandDataTable, never()).loadIslandData(eq(islandId), any(IslandData.class));

        assertNotNull(islandDataManager.getData(islandId));
    }

    /**
     * Test loading island data by island identifier.
     */
    @Test
    public void testLoadDataByIslandId() {
        when(databaseManager.getIslandIdsTable()).thenReturn(islandIdsTable);
        when(databaseManager.getIslandDataTable()).thenReturn(islandDataTable);

        String islandId = "BSkyBlock" + UUID.randomUUID();

        CompletableFuture<Void> future = islandDataManager.loadData(islandId);
        future.join();

        assertTrue(future.isDone());

        verify(islandDataTable).loadIslandData(eq(islandId), any(IslandData.class));

        assertNotNull(islandDataManager.getData(islandId));
    }

    /**
     * Test saving island data by island identifier.
     */
    @Test
    public void testSaveDataByIslandId() {
        when(databaseManager.getIslandDataTable()).thenReturn(islandDataTable);

        String islandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData = new IslandData(islandId);
        islandDataManager.setData(islandId, islandData);

        islandDataManager.saveData(islandId);

       verify(islandDataTable).saveIslandData(islandId, islandData);
    }

    /**
     * Test saving island data by island identifier, but no island data is loaded.
     */
    @Test
    public void testSaveDataByIslandIdNoData() {
        String islandId = "BSkyBlock" + UUID.randomUUID();

        islandDataManager.saveData(islandId);

        verify(islandDataTable, never()).saveIslandData(islandId, eq(any(IslandData.class)));
    }

    /**
     * Test saving island data by providing the island identifier and island data directly.
     */
    @Test
    public void testSaveDataByIslandIdAndIslandData() {
        when(databaseManager.getIslandDataTable()).thenReturn(islandDataTable);

        String islandId = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData = new IslandData(islandId);

        islandDataManager.saveData(islandId, islandData);

        verify(islandDataTable).saveIslandData(islandId, islandData);
    }

    /**
     * Test saving all stored island data.
     */
    @Test
    public void testSaveDataAllData() {
        when(databaseManager.getIslandDataTable()).thenReturn(islandDataTable);

        String islandId1 = "BSkyBlock" + UUID.randomUUID();
        String islandId2 = "BSkyBlock" + UUID.randomUUID();
        String islandId3 = "BSkyBlock" + UUID.randomUUID();
        IslandData islandData1 = new IslandData(islandId1);
        IslandData islandData2 = new IslandData(islandId2);
        IslandData islandData3 = new IslandData(islandId3);

        islandDataManager.setData(islandId1, islandData1);
        islandDataManager.setData(islandId2, islandData2);
        islandDataManager.setData(islandId3, islandData3);

        CompletableFuture<Void> future = islandDataManager.saveData();
        future.join();

        assertTrue(future.isDone());

        verify(islandDataTable).saveIslandData(anyMap());
    }
}
