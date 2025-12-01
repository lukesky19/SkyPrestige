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
import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class creates a table to store player ids (UUIDs) that should be teleported on login.
 */
public class PlayerTeleportTable {
    private final @NotNull QueueManager queueManager;
    private final @NotNull VersionsTable versionsTable;
    private final @NotNull String tableName = "skyprestige_player_teleports";

    /**
     * Constructor
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public PlayerTeleportTable(
            @NotNull QueueManager queueManager,
            @NotNull VersionsTable versionsTable) {
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates a table to store player ids (UUIDs) that should be teleported on login.
     * Queues the table creation and index creation sql.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "player_id VARCHAR(36) NOT NULL UNIQUE, " +
                "island_id TEXT NOT NULL, " +
                "FOREIGN KEY (player_id) REFERENCES skyprestige_player_ids(player_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY (island_id) REFERENCES skyprestige_island_ids(island_id) ON UPDATE CASCADE ON DELETE CASCADE)";
        String playerIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_player_teleports_player_id ON " + tableName + "(player_id)";
        String islandIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_player_teleports_island_id ON " + tableName + "(island_id)";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, playerIdIndexCreationSql, islandIdIndexCreationSql)).thenCompose(list -> versionsTable.updateVersion(tableName, 1));
    }

    /**
     * Store the {@link UUID} and island id to teleport them to.
     * @param playerId The {@link UUID} of the player.
     * @param islandId The island id to teleport the player to.
     */
    public void insertPlayerIdAndIslandId(@NotNull UUID playerId, @NotNull String islandId) {
        String insertSql = "INSERT INTO " + tableName + " (player_id, island_id) VALUES (?, ?) ON CONFLICT (player_id) DO NOTHING";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);
        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);

        queueManager.queueWriteTransaction(insertSql, List.of(playerIdParameter, islandIdParameter));
    }

    /**
     * Delete any data associated with the player id provided.
     * @param playerId The {@link UUID} of the player.
     */
    public void deletePlayerIdAndIslandId(@NotNull UUID playerId) {
        String deleteSql = "DELETE FROM " +  tableName + " WHERE player_id = ?";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);

        queueManager.queueWriteTransaction(deleteSql, List.of(playerIdParameter));
    }

    /**
     * Get the island id to teleport the player to (if any).
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} containing the island id or null.
     */
    public @NotNull CompletableFuture<@Nullable String> getIslandId(@NotNull UUID uuid) {
        String selectSql = "SELECT island_id FROM " + tableName + " WHERE player_id = ?";

        UUIDParameter playerIdParameter = new UUIDParameter(uuid);

        return queueManager.queueReadTransaction(selectSql, List.of(playerIdParameter), resultSet -> {
            try {
                if(resultSet.next()) {
                    return resultSet.getString("island_id");
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
