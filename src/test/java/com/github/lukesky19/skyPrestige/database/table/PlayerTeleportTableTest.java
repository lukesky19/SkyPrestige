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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link PlayerTeleportTable} class.
 * Most code is tested against a live database except for errors.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class PlayerTeleportTableTest extends AbstractTableTest {
    // Other Tables
    private IslandIdsTable islandIdsTable;
    private PlayerIdsTable playerIdsTable;

    // Classes being tested
    private PlayerTeleportTable livePlayerTeleportTable;
    private PlayerTeleportTable playerTeleportTableWithMockedQueueManager;

    /**
     * Set up the required data for the tests.
     */
    @Override
    @BeforeEach
    public void setup() {
        super.setup();

        server.addSimpleWorld("world");

        // Setup table classes
        VersionsTable versionsTable = new VersionsTable(liveQueueManager);
        islandIdsTable = new IslandIdsTable(liveQueueManager, versionsTable);
        playerIdsTable = new PlayerIdsTable(liveQueueManager, versionsTable);

        // Create tables
        versionsTable.createTable()
                .thenCompose(v1 -> islandIdsTable.createTable()
                        .thenCompose(v2 -> playerIdsTable.createTable())).join();

        // Setup classes for tests
        livePlayerTeleportTable = new PlayerTeleportTable(liveQueueManager, versionsTable);
        playerTeleportTableWithMockedQueueManager = new PlayerTeleportTable(mockedQueueManager, versionsTable);
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTable() {
        // Check that the table was created successfully and didn't error
        livePlayerTeleportTable.createTable()
                .thenAccept(Assertions::assertNull)
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTableError() {
        // Check that the table was created successfully and didn't error
        livePlayerTeleportTable.createTable()
                .thenAccept(Assertions::assertNull)
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Tests the insertion of a player id and island id as well as the retrieval of the island id.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testInsertPlayerIdAndIslandIdAndGetIslandId() {
        String islandId = "BSkyBlocK" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        islandIdsTable.insertIslandId(islandId).thenCompose(v1 -> {
            return playerIdsTable.insertPlayerId(playerId).thenCompose(v2 -> {
                return livePlayerTeleportTable.createTable().thenCompose(v3 -> {
                    return livePlayerTeleportTable.insertPlayerIdAndIslandId(playerId, islandId).thenCompose(v4 -> {
                        return livePlayerTeleportTable.getIslandId(playerId).thenApply(databaseIslandId -> {
                            assertEquals(islandId, databaseIslandId);
                            return null;
                        });
                    });
                });
            });
        }).join();
    }

    /**
     * Tests the deletion of a player id and island id from the table.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testDeletePlayerIdAndIslandId() {
        String islandId = "BSkyBlocK" + UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        islandIdsTable.insertIslandId(islandId).thenCompose(v1 -> {
            return playerIdsTable.insertPlayerId(playerId).thenCompose(v2 -> {
                return livePlayerTeleportTable.createTable().thenCompose(v3 -> {
                    return livePlayerTeleportTable.insertPlayerIdAndIslandId(playerId, islandId).thenCompose(v4 -> {
                        return livePlayerTeleportTable.getIslandId(playerId).thenCompose(databaseIslandId1 -> {
                            assertEquals(islandId, databaseIslandId1);

                            return livePlayerTeleportTable.deletePlayerIdAndIslandId(playerId).thenCompose(v5 -> {
                               return livePlayerTeleportTable.getIslandId(playerId).thenApply(databaseIslandId2 -> {
                                   assertNull(databaseIslandId2);
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
     * Test the retrieval of an island id, but an error occurs.
     */
    @Test
    @SuppressWarnings("resource") // The ResultSet here is a mock, so a try-with-resources block is unnecessary.
    public void testGetIslandIdError() {
        // Created a mocked ResultSet
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);

        // When the ResultSet is used, throw an SQLException for the test
        try {
            when(resultSetMock.next()).thenThrow(new SQLException("Test Error"));
        } catch (SQLException e) { // Required to make the IDE happy
            throw new RuntimeException(e);
        }

        // When a read transaction is queued, intercept the invocation to replace the existing ResultSet with the mocked one.
        when(mockedQueueManager.queueReadTransaction(anyString(), anyList(), Mockito.<Function<ResultSet, List<UUID>>>any()))
                .thenAnswer(invocation -> {
                    // Get the function
                    Function<ResultSet, List<UUID>> function = invocation.getArgument(2);
                    // Call the function with the mocked ResultSet instead.
                    return CompletableFuture.completedFuture(function.apply(resultSetMock));
                });

        // Ensure that a RunTimeException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                playerTeleportTableWithMockedQueueManager.getIslandId(UUID.randomUUID())
                        .join());

        // Ensure the error message is the same as the one used above
        assertEquals("Test Error", exception.getCause().getMessage());
    }
}
