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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link PlayerIdsTable} class.
 * All code is tested against a live database.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class PlayerIdsTableTest extends AbstractTableTest {
    // Classes being tested
    private PlayerIdsTable livePlayerIdsTable;

    /**
     * Set up the required data for the tests.
     * @param testInfo The {@link TestInfo}.
     */
    @BeforeEach
    public void setup(@NonNull TestInfo testInfo) {
        super.setup(testInfo);

        // Setup versions table
        VersionsTable versionsTable = new VersionsTable(liveQueueManager);
        versionsTable.createTable().join();

        // Setup class being tested
        livePlayerIdsTable = new PlayerIdsTable(liveQueueManager, versionsTable);
    }

    /**
     * Tests the creation of the table in the database.
     */
    @Test
    public void testCreateTable() {
        // Check that the table was created successfully and didn't error
        livePlayerIdsTable.createTable()
                .thenAccept(Assertions::assertNull)
                .exceptionally(ex -> {
                    fail("Future completed exceptionally: " + ex.getMessage());
                    return null;
                })
                .join();
    }

    /**
     * Test the storing of a player id in the database.
     */
    @Test
    public void testInsertPlayerId() {
        UUID playerId = UUID.randomUUID();

        livePlayerIdsTable.createTable().thenCompose(v1 ->
                livePlayerIdsTable.insertPlayerId(playerId).thenCompose(v2 ->
                        getPlayerId(playerId).thenApply(databasePlayerId -> {
                            assertNotNull(databasePlayerId);
                            assertEquals(playerId, databasePlayerId);
                            return null;
                        })
                )
        ).join();
    }

    /**
     * Get the player id for the provided player id from the database. Used to test if a database operation succeeded.
     * @param playerId The player id to get.
     * @return A {@link CompletableFuture} containing the player id as a {@link UUID} or null.
     */
    private @NonNull CompletableFuture<@Nullable UUID> getPlayerId(@NonNull UUID playerId) {
        String selectSql = "SELECT player_id FROM skyprestige_player_ids WHERE player_id = ?";

        return liveQueueManager.queueReadTransaction(selectSql, List.of(new UUIDParameter(playerId)), resultSet -> {
            try {
                if(resultSet.next()) {
                    return UUID.fromString(resultSet.getString("player_id"));
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
