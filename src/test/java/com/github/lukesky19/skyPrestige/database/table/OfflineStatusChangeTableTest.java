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
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link OfflineStatusChangeTable} class.
 * Most code is tested against a live database except for errors.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class OfflineStatusChangeTableTest extends AbstractTableTest {
    @Mock
    private ComponentLogger logger;

    private IslandIdsTable islandIdsTable;
    private PlayerIdsTable playerIdsTable;

    // Classes being tested
    private OfflineStatusChangeTable liveOfflineStatusChangeTable;
    private OfflineStatusChangeTable offlineStatusChangeTableWithMockedQueueManager;

    /**
     * Set up the required data for the tests.
     * @param testInfo The {@link TestInfo}.
     */
    @BeforeEach
    public void setup(@NotNull TestInfo testInfo) {
        super.setup(testInfo);

        // Setup table classes
        // Other Tables
        VersionsTable versionsTable = new VersionsTable(liveQueueManager);
        islandIdsTable = new IslandIdsTable(liveQueueManager, versionsTable);
        playerIdsTable = new PlayerIdsTable(liveQueueManager, versionsTable);

        // Create tables
        versionsTable.createTable()
                .thenCompose(v1 -> islandIdsTable.createTable()
                        .thenCompose(v2 -> playerIdsTable.createTable())).join();

        // Setup classes for tests
        liveOfflineStatusChangeTable = new OfflineStatusChangeTable(logger, liveQueueManager, versionsTable);
        offlineStatusChangeTableWithMockedQueueManager = new OfflineStatusChangeTable(logger, mockedQueueManager, versionsTable);
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTable() {
        // Check that the table was created successfully and didn't error
        liveOfflineStatusChangeTable.createTable()
                .thenAccept(Assertions::assertNull)
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
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
        offlineStatusChangeTableWithMockedQueueManager.createTable()
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(any(Component.class));
                    return null;
                })
                .join();
    }

    /**
     * Test the insertion of an offline status change.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testInsertOfflineStatusChange() {
        UUID playerId = UUID.randomUUID();
        String islandId = "BSkyBlock" + UUID.randomUUID();
        boolean status = true;

        liveOfflineStatusChangeTable.createTable().thenCompose(v1 -> {
            return islandIdsTable.insertIslandId(islandId).thenCompose(v2 -> {
                return playerIdsTable.insertPlayerId(playerId).thenCompose(v3 -> {
                    return liveOfflineStatusChangeTable.insertOfflineStatusChange(playerId, islandId, status).thenCompose(v4 -> {
                        return getData(playerId).thenApply(data -> {
                            assertNotNull(data);
                            assertEquals(playerId.toString(), data.playerId());
                            assertEquals(islandId, data.islandId());
                            assertEquals(status, data.status());
                            return null;
                        });
                    });
                });
            });
        }).join();
    }

    /**
     * Test the insertion of an offline status change, but an error occurs.
     */
    @Test
    public void testInsertOfflineStatusChangeError() {
        // When a write transaction is queued, return a failed future
        when(mockedQueueManager.queueWriteTransaction(anyString(), anyList()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        // Check that the table creation errored
        offlineStatusChangeTableWithMockedQueueManager.insertOfflineStatusChange(UUID.randomUUID(), "BSkyBlock" + UUID.randomUUID(), true)
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(any(Component.class));
                    return null;
                })
                .join();
    }

    /**
     * Test the deletion of an offline status change.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testRemoveOfflineStatusChange() {
        UUID playerId = UUID.randomUUID();
        String islandId = "BSkyBlock" + UUID.randomUUID();
        boolean status = true;

        liveOfflineStatusChangeTable.createTable().thenCompose(v1 -> {
            return islandIdsTable.insertIslandId(islandId).thenCompose(v2 -> {
                return playerIdsTable.insertPlayerId(playerId).thenCompose(v3 -> {
                    return liveOfflineStatusChangeTable.insertOfflineStatusChange(playerId, islandId, status).thenCompose(v4 -> {
                        return getData(playerId).thenCompose(data1 -> {
                            assertNotNull(data1);
                            assertEquals(playerId.toString(), data1.playerId());
                            assertEquals(islandId, data1.islandId());
                            assertEquals(status, data1.status);

                            return liveOfflineStatusChangeTable.removeOfflineStatusChange(playerId).thenCompose(v5 -> {
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
     * Test the deletion of an offline status change, but an error occurs.
     */
    @Test
    public void testRemoveOfflineStatusChangeError() {
        // When a write transaction is queued, return a failed future
        when(mockedQueueManager.queueWriteTransaction(anyString(), anyList()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        // Check that the table creation errored
        offlineStatusChangeTableWithMockedQueueManager.removeOfflineStatusChange(UUID.randomUUID())
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(any(Component.class));
                    return null;
                })
                .join();
    }

    /**
     * Test the retrieval of all offline status change statuses.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testGetOfflineStatusChanges() {
        UUID playerId = UUID.randomUUID();
        String islandId = "BSkyBlock" + UUID.randomUUID();
        boolean status = true;

        liveOfflineStatusChangeTable.createTable().thenCompose(v1 -> {
            return playerIdsTable.insertPlayerId(playerId).thenCompose(v2 -> {
                return islandIdsTable.insertIslandId(islandId).thenCompose(v5 -> {
                    return liveOfflineStatusChangeTable.insertOfflineStatusChange(playerId, islandId, status).thenCompose(v8 -> {
                        return liveOfflineStatusChangeTable.getOfflineStatusChanges(playerId).thenApply(list -> {
                            assertTrue(list.contains(status));
                            return null;
                        });
                    });
                });
            });
        }).join();
    }

    /**
     * Test the retrieval of all offline status change statuses, but a SQLException error occurs.
     */
    @Test
    @SuppressWarnings("resource") // The ResultSet here is a mock, so a try-with-resources block is unnecessary.
    public void testGetOfficeStatusChangesSQLExceptionError() {
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
                offlineStatusChangeTableWithMockedQueueManager.getOfflineStatusChanges(UUID.randomUUID())
                        .join());

        // Ensure the error message is the same as the one used above
        assertEquals("Test Error", exception.getCause().getMessage());
    }

    /**
     * Test the retrieval of all offline status change statuses, but the future completes exceptionally.
     */
    @Test
    public void testGetOfflineStatusChangesQueueManagerError() {
        when(mockedQueueManager.queueReadTransaction(anyString(), anyList(), Mockito.<Function<ResultSet, List<Integer>>>any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        offlineStatusChangeTableWithMockedQueueManager.getOfflineStatusChanges(UUID.randomUUID()).join();

        // Ensure an error message is logged
        verify(logger).error(any(Component.class));
    }

    /**
     * Get the offline status change data for the player id provided.
     * @param playerId The player's {@link UUID}.
     * @return A {@link CompletableFuture} containing the {@link OfflineStatusData} or null.
     */
    private @NotNull CompletableFuture<@Nullable OfflineStatusData> getData(@NotNull UUID playerId) {
        String readSql = "SELECT player_id, island_id, status FROM skyprestige_offline_status_change WHERE player_id = ?";

        return liveQueueManager.queueReadTransaction(readSql, List.of(new UUIDParameter(playerId)), resultSet -> {
            try {
                if(resultSet.next()) {
                    return new OfflineStatusData(
                            resultSet.getString("player_id"),
                            resultSet.getString("island_id"),
                            resultSet.getBoolean("status"));
                }

                return null;
            } catch(SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * This record stores offline status change data for test validation purposes.
     * @param playerId The player's {@link UUID} as a String.
     * @param islandId The island's id.
     * @param status The status.
     */
    private record OfflineStatusData(
            @NotNull String playerId,
            @NotNull String islandId,
            boolean status) {}
}
