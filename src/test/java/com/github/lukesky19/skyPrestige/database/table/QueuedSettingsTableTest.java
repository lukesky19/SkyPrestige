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

import com.github.lukesky19.skyPrestige.data.data.reset.QueuedSettings;
import com.github.lukesky19.skyPrestige.database.table.abstracts.AbstractTableTest;
import com.github.lukesky19.skyPrestige.util.enums.SettingsType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.NonNull;
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
 * This class tests the {@link QueuedSettingsTable} class.
 * Most code is tested against a live database except for errors.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class QueuedSettingsTableTest  extends AbstractTableTest {
    @Mock
    private ComponentLogger logger;

    private IslandIdsTable islandIdsTable;
    private PlayerIdsTable playerIdsTable;

    // Classes being tested
    private QueuedSettingsTable liveQueuedSettingsTable;
    private QueuedSettingsTable queuedSettingsTableWithMockedQueueManager;

    /**
     * Set up the required data for the tests.
     * @param testInfo The {@link TestInfo}.
     */
    @BeforeEach
    public void setup(@NonNull TestInfo testInfo) {
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
        liveQueuedSettingsTable = new QueuedSettingsTable(logger, liveQueueManager, versionsTable);
        queuedSettingsTableWithMockedQueueManager = new QueuedSettingsTable(logger, mockedQueueManager, versionsTable);
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTable() {
        // Check that the table was created successfully and didn't error
        liveQueuedSettingsTable.createTable()
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
        queuedSettingsTableWithMockedQueueManager.createTable()
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(any(Component.class));
                    return null;
                })
                .join();
    }

    /**
     * Test the queuing of settings.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testQueueSettings() {
        UUID playerId = UUID.randomUUID();
        String islandId = "BSkyBlock" + UUID.randomUUID();

        liveQueuedSettingsTable.createTable().thenCompose(v1 -> {
            return islandIdsTable.insertIslandId(islandId).thenCompose(v2 -> {
                return playerIdsTable.insertPlayerId(playerId).thenCompose(v3 -> {
                    return liveQueuedSettingsTable.queueSettings(playerId, islandId, SettingsType.PRESTIGE, 1).thenCompose(v4 -> {
                        return liveQueuedSettingsTable.getQueuedSettings(playerId).thenApply(list -> {
                            assertFalse(list.isEmpty());
                            assertEquals(1, list.size());
                            QueuedSettings queuedSettings = list.getFirst();
                            assertEquals(playerId, queuedSettings.playerId());
                            assertEquals(islandId, queuedSettings.islandId());
                            assertEquals(SettingsType.PRESTIGE, queuedSettings.settingsType());
                            assertEquals(1, queuedSettings.prestigeLevel());
                            return null;
                        });
                    });
                });
            });
        }).join();
    }

    /**
     * Test the queuing of settings, but an error occurs.
     */
    @Test
    public void testQueueSettingsError() {
        // When a write transaction is queued, return a failed future
        when(mockedQueueManager.queueWriteTransaction(anyString(), anyList()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        // Check that the table creation errored
        queuedSettingsTableWithMockedQueueManager.queueSettings(UUID.randomUUID(), "BSkyBlock" + UUID.randomUUID(), SettingsType.PRESTIGE, 1)
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(any(Component.class));
                    return null;
                })
                .join();
    }

    /**
     * Test the clearing of queued settings.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testClearQueuedSettings() {
        UUID playerId = UUID.randomUUID();
        String islandId = "BSkyBlock" + UUID.randomUUID();

        liveQueuedSettingsTable.createTable().thenCompose(v1 -> {
            return islandIdsTable.insertIslandId(islandId).thenCompose(v2 -> {
                return playerIdsTable.insertPlayerId(playerId).thenCompose(v3 -> {
                    return liveQueuedSettingsTable.queueSettings(playerId, islandId, SettingsType.PRESTIGE, 1).thenCompose(v4 -> {
                        return liveQueuedSettingsTable.getQueuedSettings(playerId).thenCompose(list1 -> {
                            assertFalse(list1.isEmpty());
                            assertEquals(1, list1.size());
                            QueuedSettings queuedSettings = list1.getFirst();
                            assertEquals(playerId, queuedSettings.playerId());
                            assertEquals(islandId, queuedSettings.islandId());
                            assertEquals(SettingsType.PRESTIGE, queuedSettings.settingsType());
                            assertEquals(1, queuedSettings.prestigeLevel());

                            return liveQueuedSettingsTable.clearQueuedSettings(playerId).thenCompose(v5 -> {
                                return liveQueuedSettingsTable.getQueuedSettings(playerId).thenApply(list2 -> {
                                    assertTrue(list2.isEmpty());
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
    public void testClearQueuedSettingsError() {
        // When a write transaction is queued, return a failed future
        when(mockedQueueManager.queueWriteTransaction(anyString(), anyList()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test Error")));

        // Check that the table creation errored
        queuedSettingsTableWithMockedQueueManager.clearQueuedSettings(UUID.randomUUID())
                .thenAccept(v -> fail("Table creation should of failed exceptionally."))
                .exceptionally(ex -> {
                    verify(logger).error(any(Component.class));
                    return null;
                })
                .join();
    }

    /**
     * Test the retrieval of queued settings, but an error occurs.
     */
    @Test
    public void testGetQueuedSettingsQueueManagerError() {
        // When a read transaction is queued, intercept the invocation to replace the existing ResultSet with the mocked one.
        when(mockedQueueManager.queueReadTransaction(anyString(), anyList(), Mockito.<Function<ResultSet, List<QueuedSettings>>>any()))
                .thenAnswer(invocation -> {
                    // Return a failed future
                    return CompletableFuture.failedFuture(new RuntimeException("Runtime Test Error"));
                });

        // Ensure that a RunTimeException is thrown
        CompletableFuture<List<QueuedSettings>> future = queuedSettingsTableWithMockedQueueManager.getQueuedSettings(UUID.randomUUID());
        future.join();

        verify(logger).error(any(Component.class));

        future.exceptionally(ex -> {
            // Ensure the error message is the same as the one used above
            assertEquals("Runtime Test Error", ex.getMessage());
            return null;
        });
    }

    /**
     * Test the retrieval of queued settings, but an error occurs.
     */
    @Test
    @SuppressWarnings("resource") // The ResultSet here is a mock, so a try-with-resources block is unnecessary.
    public void testGetQueuedSettingsResultSetError() {
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
                queuedSettingsTableWithMockedQueueManager.getQueuedSettings(UUID.randomUUID())
                        .join());

        // Ensure the error message is the same as the one used above
        assertEquals("Test Error", exception.getCause().getMessage());
    }
}