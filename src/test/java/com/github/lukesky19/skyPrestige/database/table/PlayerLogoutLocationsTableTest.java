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
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * This class tests the {@link PlayerLogoutLocationsTable} class.
 * Most code is tested against a live database except for errors.
 */
public class PlayerLogoutLocationsTableTest extends AbstractTableTest {
    // Other Tables
    private PlayerIdsTable playerIdsTable;

    // Classes being tested
    private PlayerLogoutLocationsTable livePlayerLogoutLocationsTables;
    private PlayerLogoutLocationsTable playerLogoutLocationsTablesWithMockedQueueManager;

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
        playerIdsTable = new PlayerIdsTable(liveQueueManager, versionsTable);

        // Create tables
        versionsTable.createTable()
                .thenCompose(v1 -> playerIdsTable.createTable()).join();

        // Setup classes for tests
        livePlayerLogoutLocationsTables = new PlayerLogoutLocationsTable(liveQueueManager, versionsTable);
        playerLogoutLocationsTablesWithMockedQueueManager = new PlayerLogoutLocationsTable(mockedQueueManager, versionsTable);
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTable() {
        // Check that the table was created successfully and didn't error
        livePlayerLogoutLocationsTables.createTable()
                .thenAccept(Assertions::assertNull)
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Tests setting a player's logout location in the database.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testSetPlayerLogoutLocation() {
        @Nullable World world = server.getWorld("world");
        if(world == null) {
            fail("The server does not have a world setup!");
            return;
        }

        UUID playerId = UUID.randomUUID();
        Location testLocation = new Location(world, 100, 0, 100);

        playerIdsTable.insertPlayerId(playerId).thenCompose(v1 -> {
            return livePlayerLogoutLocationsTables.createTable().thenCompose(v2 -> {
                return livePlayerLogoutLocationsTables.setPlayerLogoutLocation(playerId, testLocation).thenCompose(v3 -> {
                    return getPlayerLogoutLocation(playerId).thenApply(databaseLocation -> {
                        assertEquals(testLocation, databaseLocation);
                        return null;
                    });
                });
            });
        }).join();
    }

    /**
     * Test getting player ids from the database that are within the bounds.
     */
    @Test
    @SuppressWarnings("CodeBlock2Expr") // In my opinion, it is more readable to have the code blocks than lambda expressions here.
    public void testGetPlayerIdsWithinByBounds() {
        @Nullable World world = server.getWorld("world");
        if(world == null) {
            fail("The server does not have a world setup!");
            return;
        }

        UUID playerId = UUID.randomUUID();
        Location testLocation = new Location(world, 100, 0, 100);

        playerIdsTable.insertPlayerId(playerId).thenCompose(v1 -> {
            return livePlayerLogoutLocationsTables.createTable().thenCompose(v2 -> {
                return livePlayerLogoutLocationsTables.setPlayerLogoutLocation(playerId, testLocation).thenCompose(v3 -> {
                    return livePlayerLogoutLocationsTables.getPlayerIdsWithinByBounds("world", 0, 200, 0, 200).thenApply(list -> {
                        assertTrue(list.contains(playerId));
                        return null;
                    });
                });
            });
        }).join();
    }

    /**
     * Test getting player ids from the database that are within the bounds, but an error occurs.
     */
    @Test
    @SuppressWarnings("resource") // The ResultSet here is a mock, so a try-with-resources block is unnecessary.
    public void testGetPlayerIdsWithinByBoundsError() {
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
                playerLogoutLocationsTablesWithMockedQueueManager.getPlayerIdsWithinByBounds("world", 0, 200, 0, 200)
                        .join());

        // Ensure the error message is the same as the one used above
        assertEquals("Test Error", exception.getCause().getMessage());
    }

    /**
     * Get the player's logout location. Used to test if a database operation succeeded.
     * @param playerId The player id.
     * @return A {@link CompletableFuture} containing the player's logout {@link Location} or null. The y value will always be 0.
     */
    private @NotNull CompletableFuture<@Nullable Location> getPlayerLogoutLocation(@NotNull UUID playerId) {
        String selectSql = "SELECT world, x, z FROM skyprestige_player_logout_locations WHERE player_id = ?";

        return liveQueueManager.queueReadTransaction(selectSql, List.of(new UUIDParameter(playerId)), resultSet -> {
            try {
                if(resultSet.next()) {
                    String worldName = resultSet.getString("world");
                    @Nullable World world = server.getWorld(worldName);
                    if(world == null) return null;
                    int x = resultSet.getInt("x");
                    int z = resultSet.getInt("z");

                    return new Location(world, x, 0, z);
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
