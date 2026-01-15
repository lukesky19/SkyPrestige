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
import com.github.lukesky19.skyPrestige.util.parameter.CaseSensitiveStringParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link IslandIdsTable} class.
 * Most code is tested against a live database except for errors.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class IslandIdTableTest extends AbstractTableTest {
    // Classes being tested
    private IslandIdsTable liveIslandIdsTable;
    private IslandIdsTable islandIdsTableWithMockedQueueManager;

    /**
     * Set up the required data for the tests.
     * @param testInfo The {@link TestInfo}.
     */
    @BeforeEach
    public void setup(@NotNull TestInfo testInfo) {
        super.setup(testInfo);

        // Setup versions table
        VersionsTable versionsTable = new VersionsTable(liveQueueManager);
        versionsTable.createTable().join();

        // Setup classes being tested
        liveIslandIdsTable = new IslandIdsTable(liveQueueManager, versionsTable);
        islandIdsTableWithMockedQueueManager = new IslandIdsTable(mockedQueueManager, versionsTable);
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTable() {
        // Check that the table was created successfully and didn't error
        liveIslandIdsTable.createTable()
                .thenAccept(Assertions::assertNull)
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Tests inserting an island id into the table.
     */
    @Test
    public void testInsertIslandId() {
        String islandId = "BSkyBlock" + UUID.randomUUID();

        liveIslandIdsTable.createTable()
                .thenCompose(v1 -> liveIslandIdsTable.insertIslandId(islandId)
                        .thenCompose(v2 -> getIslandId(islandId)
                                .thenApply(databaseIslandId -> {
                                    assertNotNull(databaseIslandId);
                                    assertEquals(islandId, databaseIslandId);
                                    return null;
                                })))
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Tests updating an island id in the table.
     */
    @Test
    public void testUpdateIslandId() {
        String oldIslandId = "BSkyBlock" + UUID.randomUUID();
        String newIslandId = "BSkyBlock" + UUID.randomUUID();

        liveIslandIdsTable.createTable()
                .thenCompose(v1 -> liveIslandIdsTable.insertIslandId(oldIslandId)
                        .thenCompose(v2 -> liveIslandIdsTable.updateIslandId(oldIslandId, newIslandId)
                                .thenCompose(v3 -> getIslandId(newIslandId)
                                        .thenApply(databaseIslandId -> {
                                            assertNotNull(databaseIslandId);
                                            assertEquals(newIslandId, databaseIslandId);
                                            return null;
                                        }))))
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Tests getting all island ids stored in the table.
     */
    @Test
    public void testGetIslandIds() {
        String islandId1 = "BSkyBlock" + UUID.randomUUID();
        String islandId2 = "BSkyBlock" + UUID.randomUUID();
        String islandId3 = "BSkyBlock" + UUID.randomUUID();
        String islandId4 = "BSkyBlock" + UUID.randomUUID();
        String islandId5 = "BSkyBlock" + UUID.randomUUID();

        liveIslandIdsTable.createTable()
                .thenCompose(v1 -> liveIslandIdsTable.insertIslandId(islandId1)
                        .thenCompose(v2 -> liveIslandIdsTable.insertIslandId(islandId2)
                                .thenCompose(v3 -> liveIslandIdsTable.insertIslandId(islandId3)
                                        .thenCompose(v4 -> liveIslandIdsTable.insertIslandId(islandId4)
                                                .thenCompose(v5 -> liveIslandIdsTable.insertIslandId(islandId5)
                                                        .thenCompose(v6 -> liveIslandIdsTable.getIslandIds()
                                                                .thenApply(list -> {
                                                                    assertFalse(list.isEmpty());
                                                                    assertTrue(list.contains(islandId1));
                                                                    assertTrue(list.contains(islandId2));
                                                                    assertTrue(list.contains(islandId3));
                                                                    assertTrue(list.contains(islandId4));
                                                                    assertTrue(list.contains(islandId5));
                                                                    return null;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Tests the retrieval of island ids from the database, but an error occurs.
     */
    @Test
    @SuppressWarnings("resource") // The ResultSet here is a mock, so a try-with-resources block is unnecessary.
    public void testGetIslandIdsError() {
        // Created a mocked ResultSet
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);

        // When the ResultSet is used, throw an SQLException for the test
        try {
            when(resultSetMock.next()).thenThrow(new SQLException("Test Error"));
        } catch (SQLException e) { // Required to make the IDE happy
            throw new RuntimeException(e);
        }

        // When a read transaction is queued, intercept the invocation to replace the existing ResultSet with the mocked one.
        when(mockedQueueManager.queueReadTransaction(anyString(), Mockito.<Function<ResultSet, List<String>>>any()))
                .thenAnswer(invocation -> {
                    // Get the function
                    Function<ResultSet, List<String>> function = invocation.getArgument(1);
                    // Call the function with the mocked ResultSet instead.
                    return CompletableFuture.completedFuture(function.apply(resultSetMock));
                });

        // Ensure that a RunTimeException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> islandIdsTableWithMockedQueueManager.getIslandIds().join());

        // Ensure the error message is the same as the one used above
        assertEquals("Test Error", exception.getCause().getMessage());
    }

    /**
     * Get the island id for the provided island id from the database. Used to test if a database operation succeeded.
     * @param islandId The island id to get.
     * @return A {@link CompletableFuture} containing the island id as a {@link String} or null.
     */
    private @NotNull CompletableFuture<@Nullable String> getIslandId(@NotNull String islandId) {
        String selectSql = "SELECT island_id FROM skyprestige_island_ids WHERE island_id = ?";

        return liveQueueManager.queueReadTransaction(selectSql, List.of(new CaseSensitiveStringParameter(islandId)), resultSet -> {
            try {
                if(resultSet.next()) {
                    return resultSet.getString("island_id");
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}