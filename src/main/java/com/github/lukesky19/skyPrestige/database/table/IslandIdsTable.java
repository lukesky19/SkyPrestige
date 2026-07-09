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

import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skyPrestige.util.parameter.CaseSensitiveStringParameter;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class creates a table to store all island ids.
 */
public class IslandIdsTable {
    private final @NonNull QueueManager queueManager;
    private final @NonNull VersionsTable versionsTable;
    private final @NonNull String tableName = "skyprestige_island_ids";

    /**
     * Constructor
     * @param queueManager A {@link QueueManager} instance.
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public IslandIdsTable(
            @NonNull QueueManager queueManager,
            @NonNull VersionsTable versionsTable) {
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates a table to store all island ids as a string.
     * Queues the table creation and index creation sql.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (island_id TEXT PRIMARY KEY NOT NULL UNIQUE);";
        String indexCreationSql = "CREATE INDEX IF NOT EXISTS idx_island_ids_island_id ON " + tableName + "(island_id);";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, indexCreationSql))
                .thenCompose(list -> versionsTable.updateVersion(tableName, 1));
    }

    /**
     * Stores the unique id of an island inside the island ids table.
     * @param islandId The unique id of an island.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> insertIslandId(@NonNull String islandId) {
        String insertIslandIdSql = "INSERT INTO " + tableName + " (island_id) VALUES (?) ON CONFLICT (island_id) DO NOTHING";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);

        return queueManager.queueWriteTransaction(insertIslandIdSql, List.of(islandIdParameter)).thenRun(() -> {});
    }

    /**
     * Replaces the old island id with the new island id.
     * @param oldIslandId The old island's id.
     * @param newIslandId The new island's id.
     * @return A {@link CompletableFuture} of type {@link Void} that can be used to determine when the method is complete.
     */
    public @NonNull CompletableFuture<Void> updateIslandId(@NonNull String oldIslandId, @NonNull String newIslandId) {
        String updateSql = "UPDATE " + tableName + " SET island_id = ? WHERE island_id = ?";

        CaseSensitiveStringParameter oldIslandIdParameter = new CaseSensitiveStringParameter(oldIslandId);
        CaseSensitiveStringParameter newIslandIdParameter = new CaseSensitiveStringParameter(newIslandId);

        return queueManager.queueWriteTransaction(updateSql, List.of(newIslandIdParameter, oldIslandIdParameter)).thenRun(() -> {});
    }

    /**
     * Get a list of all island ids stored in the table.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link String}s for the island ids in the table.
     */
    public @NonNull CompletableFuture<@NonNull List<@NonNull String>> getIslandIds() {
        String sql = "SELECT island_id FROM " + tableName;

        return queueManager.queueReadTransaction(sql, resultSet -> {
            List<@NonNull String> islandIdList = new ArrayList<>();

            try {
                while(resultSet.next()) {
                    islandIdList.add(resultSet.getString("island_id"));
                }
            } catch(SQLException e) {
                throw new RuntimeException(e);
            }

            return islandIdList;
        });
    }
}
