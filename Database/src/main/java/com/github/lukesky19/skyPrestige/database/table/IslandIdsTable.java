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

import com.github.lukesky19.skyPrestige.core.util.parameter.CaseSensitiveStringParameter;
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class creates a table to store all island ids.
 */
public class IslandIdsTable {
    private final @NotNull ComponentLogger logger;
    private final @NotNull QueueManager queueManager;
    private final @NotNull VersionsTable versionsTable;
    private final @NotNull String tableName = "skyprestige_island_ids";

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}.
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public IslandIdsTable(
            @NotNull ComponentLogger logger,
            @NotNull QueueManager queueManager,
            @NotNull VersionsTable versionsTable) {
        this.logger = logger;
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates a table to store all island ids as a string.
     * Queues the table creation and index creation sql.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (island_id TEXT PRIMARY KEY NOT NULL UNIQUE);";
        String indexCreationSql = "CREATE INDEX IF NOT EXISTS idx_island_ids_island_id ON " + tableName + "(island_id);";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, indexCreationSql)).thenCompose(list -> versionsTable.updateVersion(tableName, 1));
    }

    /**
     * Stores the unique id of an island inside the island ids table.
     * @param islandId The unique id of an island.
     */
    public void insertIslandId(@NotNull String islandId) {
        String insertIslandIdSql = "INSERT INTO " + tableName + " (island_id) VALUES (?) ON CONFLICT (island_id) DO NOTHING";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);

        queueManager.queueWriteTransaction(insertIslandIdSql, List.of(islandIdParameter));
    }

    /**
     * Replaces the old island id with the new island id.
     * @param oldIslandId The old island's id.
     * @param newIslandId The new island's id.
     * @return A {@link CompletableFuture} of type {@link Void} that can be used to determine when the method is complete.
     */
    public @NotNull CompletableFuture<Void> updateIslandId(@NotNull String oldIslandId, @NotNull String newIslandId) {
        String updateSql = "UPDATE " + tableName + " SET island_id = ? WHERE island_id = ?";

        CaseSensitiveStringParameter oldIslandIdParameter = new CaseSensitiveStringParameter(oldIslandId);
        CaseSensitiveStringParameter newIslandIdParameter = new CaseSensitiveStringParameter(newIslandId);

        return queueManager.queueWriteTransaction(updateSql, List.of(newIslandIdParameter, oldIslandIdParameter))
                .thenAccept(integer -> {})
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.deserialize("Failed to update old island id " + oldIslandId + " to new island id " + newIslandId + " Error: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Delete an island id from the table.
     * Deletion wil cascade to other tables.
     * @param islandId The island id to delete.
     */
    public void deleteIslandId(@NotNull String islandId) {
        String deletionSql = "DELETE FROM " + tableName + " WHERE island_id = ?";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);

        queueManager.queueWriteTransaction(deletionSql, List.of(islandIdParameter));
    }

    /**
     * Get a list of all island ids stored in the table.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link String}s for the island ids in the table.
     */
    public @NotNull CompletableFuture<@NotNull List<@NotNull String>> getIslandIds() {
        String sql = "SELECT island_id FROM " + tableName;

        return queueManager.queueReadTransaction(sql, resultSet -> {
            @NotNull List<@NotNull String> islandIdList = new ArrayList<>();

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
