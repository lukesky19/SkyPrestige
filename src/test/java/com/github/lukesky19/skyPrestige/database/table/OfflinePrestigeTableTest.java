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
package com.github.lukesky19.skyPrestige.database.table;

import com.github.lukesky19.skyPrestige.database.table.abstracts.AbstractTableTest;
import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link OfflinePrestigeTable} class.
 * Most code is tested against a live database except for errors.
 */
public class OfflinePrestigeTableTest extends AbstractTableTest {
    @Mock
    private ComponentLogger logger;

    // Other Tables
    private VersionsTable versionsTable;
    private IslandIdsTable islandIdsTable;
    private PlayerIdsTable playerIdsTable;

    // Classes being tested
    private OfflinePrestigeTable liveOfflinePrestigeTable;
    private OfflinePrestigeTable offlinePrestigeTableWithMockedQueueManager;

    /**
     * Set up the required data for the tests.
     */
    @Override
    @BeforeEach
    public void setup() {
        super.setup();

        // Setup table classes
        versionsTable = new VersionsTable(liveQueueManager);
        islandIdsTable = new IslandIdsTable(liveQueueManager, versionsTable);
        playerIdsTable = new PlayerIdsTable(liveQueueManager, versionsTable);

        // Create tables
        versionsTable.createTable()
                .thenCompose(v1 -> islandIdsTable.createTable()
                        .thenCompose(v2 -> playerIdsTable.createTable())).join();

        // Setup classes for tests
        liveOfflinePrestigeTable = new OfflinePrestigeTable(logger, liveQueueManager, versionsTable);
        offlinePrestigeTableWithMockedQueueManager = new OfflinePrestigeTable(logger, mockedQueueManager, versionsTable);
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTable() {
        // Check that the table was created successfully and didn't error
        liveOfflinePrestigeTable.createTable()
                .thenAccept(Assertions::assertNull)
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Tests the creation of the table in the database, but the table is already created.
     */
    @Test
    public void testCreateTableExisting() {
        // Check that the table was created successfully and didn't error
        liveOfflinePrestigeTable.createTable()
                .thenCompose(v1 ->
                        versionsTable.updateVersion("skyprestige_offline_player_prestige", 2)
                                .thenCompose(v2 -> liveOfflinePrestigeTable.createTable()
                                        .thenAccept(Assertions::assertNull)))
        .exceptionally(ex -> {
            fail("Future completed exceptionally: " + ex.getMessage());
            return null;
        }).join();
    }

    /**
     * Tests the creation of the table in the database, but an error occurs.
     */
    @Test
    public void testCreateTableError() {
        // When a bulk write transaction is queued, return a failed future
        when(mockedQueueManager.queueBulkWriteTransaction(anyList()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        // Check that the table creation errored
        offlinePrestigeTableWithMockedQueueManager.createTable()
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(any(Component.class));
                    return null;
                })
                .join();
    }

    /**
     * Test the insertion of an offline prestige.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testInsertOfflinePrestige() {
        UUID playerId = UUID.randomUUID();
        String islandId = "BSkyBlock" + UUID.randomUUID();
        int prestigeLevel = 2;

        liveOfflinePrestigeTable.createTable().thenCompose(v1 -> {
            return islandIdsTable.insertIslandId(islandId).thenCompose(v2 -> {
                return playerIdsTable.insertPlayerId(playerId).thenCompose(v3 -> {
                    return liveOfflinePrestigeTable.insertOfflinePrestige(playerId, islandId, prestigeLevel).thenCompose(v4 -> {
                        return getData(playerId).thenApply(data -> {
                            assertNotNull(data);
                            assertEquals(playerId.toString(), data.playerId());
                            assertEquals(islandId, data.islandId());
                            assertEquals(prestigeLevel, data.level());
                            return null;
                        });
                    });
                });
            });
        }).join();
    }

    /**
     * Test the insertion of an offline prestige, but an error occurs.
     */
    @Test
    public void testInsertOfflinePrestigeError() {
        // When a write transaction is queued, return a failed future
        when(mockedQueueManager.queueWriteTransaction(anyString(), anyList()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        // Check that the table creation errored
        offlinePrestigeTableWithMockedQueueManager.insertOfflinePrestige(UUID.randomUUID(), "BSkyBlock" + UUID.randomUUID(), 2)
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(any(Component.class));
                    return null;
                })
                .join();
    }

    /**
     * Test the deletion of an offline prestige.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testRemoveOfflinePrestige() {
        UUID playerId = UUID.randomUUID();
        String islandId = "BSkyBlock" + UUID.randomUUID();
        int prestigeLevel = 2;

        liveOfflinePrestigeTable.createTable().thenCompose(v1 -> {
            return islandIdsTable.insertIslandId(islandId).thenCompose(v2 -> {
                return playerIdsTable.insertPlayerId(playerId).thenCompose(v3 -> {
                    return liveOfflinePrestigeTable.insertOfflinePrestige(playerId, islandId, prestigeLevel).thenCompose(v4 -> {
                        return getData(playerId).thenCompose(data1 -> {
                            assertNotNull(data1);
                            assertEquals(playerId.toString(), data1.playerId());
                            assertEquals(islandId, data1.islandId());
                            assertEquals(prestigeLevel, data1.level());

                            return liveOfflinePrestigeTable.removeOfflinePrestige(playerId).thenCompose(v5 -> {
                                return getData(playerId).thenApply(data2 -> {
                                    assertNull(data2);
                                    return null;
                                });
                            });
                        });
                    });
                });
            });
        }).join();
    }

    /**
     * Test the deletion of an offline prestige, but an error occurs.
     */
    @Test
    public void testRemoveOfflinePrestigeError() {
        // When a write transaction is queued, return a failed future
        when(mockedQueueManager.queueWriteTransaction(anyString(), anyList()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        // Check that the table creation errored
        offlinePrestigeTableWithMockedQueueManager.removeOfflinePrestige(UUID.randomUUID())
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(any(Component.class));
                    return null;
                })
                .join();
    }

    /**
     * Test the retrieval of all offline prestige levels.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testGetPrestigeLevels() {
        UUID playerId = UUID.randomUUID();
        String islandId = "BSkyBlock" + UUID.randomUUID();
        int prestigeLevel = 2;

        liveOfflinePrestigeTable.createTable().thenCompose(v1 -> {
            return playerIdsTable.insertPlayerId(playerId).thenCompose(v2 -> {
                return islandIdsTable.insertIslandId(islandId).thenCompose(v5 -> {
                    return liveOfflinePrestigeTable.insertOfflinePrestige(playerId, islandId, prestigeLevel).thenCompose(v8 -> {
                        return liveOfflinePrestigeTable.getPrestigeLevels(playerId).thenApply(list -> {
                            assertTrue(list.contains(prestigeLevel));
                            return null;
                        });
                    });
                });
            });
        }).join();
    }

    /**
     * Test the retrieval of all offline prestige levels, but a SQLException error occurs.
     */
    @Test
    @SuppressWarnings("resource") // The ResultSet here is a mock, so a try-with-resources block is unnecessary.
    public void testGetPrestigeLevelsSQLExceptionError() {
        // Created a mocked ResultSet
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);

        // When the ResultSet is used, throw an SQLException for the test
        try {
            when(resultSetMock.next()).thenThrow(new SQLException("Test Error"));
        } catch (SQLException e) { // Required to make the IDE happy
            throw new RuntimeException(e);
        }

        // When a read transaction is queued, intercept the invocation to replace the existing ResultSet with the mocked one.
        when(mockedQueueManager.queueReadTransaction(anyString(), anyList(), Mockito.<Function<ResultSet, List<Integer>>>any()))
                .thenAnswer(invocation -> {
                    // Get the function
                    Function<ResultSet, List<Integer>> function = invocation.getArgument(2);
                    // Call the function with the mocked ResultSet instead.
                    return CompletableFuture.completedFuture(function.apply(resultSetMock));
                });

        // Ensure that a RunTimeException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                offlinePrestigeTableWithMockedQueueManager.getPrestigeLevels(UUID.randomUUID())
                        .join());

        // Ensure the error message is the same as the one used above
        assertEquals("Test Error", exception.getCause().getMessage());
    }

    /**
     * Test the retrieval of all offline prestige levels, but the future completes exceptionally.
     */
    @Test
    public void testGetPrestigeLevelsQueueManagerError() {
        when(mockedQueueManager.queueReadTransaction(anyString(), anyList(), Mockito.<Function<ResultSet, List<Integer>>>any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        offlinePrestigeTableWithMockedQueueManager.getPrestigeLevels(UUID.randomUUID()).join();

        // Ensure an error message is logged
        verify(logger).error(any(Component.class));
    }

    /**
     * Get the offline prestige data from table. Used for validation.
     * @param playerId The {@link UUID} to get data for.
     * @return A {@link CompletableFuture} containing the {@link OfflinePrestigeTable} or null.
     */
    private @NotNull CompletableFuture<@Nullable OfflinePrestigeData> getData(@NotNull UUID playerId) {
        String readSql = "SELECT player_id, island_id, level, prestige_time FROM skyprestige_offline_player_prestige WHERE player_id = ?";

        return liveQueueManager.queueReadTransaction(readSql, List.of(new UUIDParameter(playerId)), resultSet -> {
            @Nullable OfflinePrestigeData data = null;

            try {
                if(resultSet.next()) {
                    data = new OfflinePrestigeData(
                            resultSet.getString("player_id"),
                            resultSet.getString("island_id"),
                            resultSet.getInt("level"),
                            resultSet.getTimestamp("prestige_time"));
                }

                return data;
            } catch(SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * This record stores offline prestige data for validation.
     * @param playerId The player id.
     * @param islandId The island id.
     * @param level The prestige level.
     * @param prestigeTime The prestige timestamp.
     */
    private record OfflinePrestigeData(
            @NotNull String playerId,
            @NotNull String islandId,
            int level,
            @NotNull Timestamp prestigeTime) {}
}
