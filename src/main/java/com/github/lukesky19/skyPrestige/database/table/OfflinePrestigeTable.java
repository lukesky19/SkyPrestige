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
import com.github.lukesky19.skyPrestige.util.parameter.TimestampParameter;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.database.parameter.Parameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.StringParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class creates the table to store the data necessary to process when a player's island was prestiged while offline.
 */
public class OfflinePrestigeTable {
    private final @NotNull ComponentLogger logger;
    private final @NotNull QueueManager queueManager;
    private final @NotNull VersionsTable versionsTable;
    private final @NotNull String tableName = "skyprestige_offline_player_prestige";

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}.
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public OfflinePrestigeTable(
            @NotNull ComponentLogger logger,
            @NotNull QueueManager queueManager,
            @NotNull VersionsTable versionsTable) {
        this.logger = logger;
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates the table that stores the player's {@link UUID}, their latest island's id, the prestige level,
     * and the timestamp the prestige was completed. Also creates any indexes.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> createTable() {
        String tableCreationSql =
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "player_id VARCHAR(36) NOT NULL, " +
                "island_id TEXT NOT NULL, " +
                "level INTEGER NOT NULL DEFAULT 0, " +
                "prestige_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (player_id) REFERENCES skyprestige_player_ids(player_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY (island_id) REFERENCES skyprestige_island_ids(island_id) ON UPDATE CASCADE ON DELETE CASCADE)";
        String playerIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_offline_player_prestige_player_id ON " + tableName + "(player_id)";
        String islandIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_offline_player_prestige_island_id ON " + tableName + "(island_id)";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, playerIdIndexCreationSql, islandIdIndexCreationSql))
                .thenCompose(v -> versionsTable.getTableVersion(tableName).thenCompose(version -> {
                    if(version > 1) {
                        return versionsTable.updateVersion(tableName, 2);
                    }

                    return CompletableFuture.completedFuture(null);
                }))
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.deserialize("Offline Prestige Table creation failed: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Stores the {@link UUID} of a player whose island was prestiged while they were offline.
     * @param playerId The {@link UUID} of the player.
     * @param islandId The new island's unique id.
     * @param prestigeLevel The prestige level that was completed.
     */
    public void insertOfflinePrestige(@NotNull UUID playerId, @NotNull String islandId, int prestigeLevel) {
        String insertSql = "INSERT INTO " + tableName + " (player_id, island_id, level) VALUES (?, ?, ?)";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);
        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);
        IntegerParameter levelParameter = new IntegerParameter(prestigeLevel);

        queueManager.queueWriteTransaction(insertSql, List.of(playerIdParameter, islandIdParameter, levelParameter))
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.deserialize("Failed to insert offline prestige: " + ex.getMessage()));
                    return 0;
                });
    }

    /**
     * Removes any offline prestige from the table for the given player id.
     * @param playerId The {@link UUID} of the player.
     */
    public void removeOfflinePrestige(@NotNull UUID playerId) {
        String deleteSql = "DELETE FROM " + tableName + " WHERE player_id = ?";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);

        queueManager.queueWriteTransaction(deleteSql, List.of(playerIdParameter))
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.deserialize("Failed to remove offline prestige: " + ex.getMessage()));
                    return 0;
                });
    }

    /**
     * Get all prestige levels that occurred while the player was offline.
     * @param playerId The {@link UUID} of the player.
     * @return A {@link CompletableFuture} of a {@link List} of non-null {@link Integer}s for the prestige levels that
     * occurred while the player was offline. Ensure you handle any exceptions and accepting the result of the
     * {@link CompletableFuture}.
     */
    public @NotNull CompletableFuture<@NotNull List<@NotNull Integer>> getPrestigeLevels(@NotNull UUID playerId) {
        String selectSql = "SELECT level FROM " + tableName + " WHERE player_id = ?";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);

        return queueManager.queueReadTransaction(selectSql, List.of(playerIdParameter), resultSet -> {
            List<@NotNull Integer> offlinePrestigeLevels = new ArrayList<>();

            try {
                while(resultSet.next()) {
                    offlinePrestigeLevels.add(resultSet.getInt("level"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return offlinePrestigeLevels;
        })
        .exceptionally(ex -> {
            logger.error(AdventureUtil.deserialize("Failed to get offline prestige levels: " + ex.getMessage()));
            return new ArrayList<>();
        });
    }

    /**
     * Migrate the table from version 1 to version 2.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> migrate() {
        return versionsTable.getTableVersion(tableName).thenCompose(version -> {
            if(version <= 1) {
                List<OfflinePrestigeData> offlinePrestigeDataList = new ArrayList<>();
                String readSql = "SELECT player_id, island_id, level, prestige_time FROM " + tableName;

                return queueManager.queueReadTransaction(readSql, resultSet -> {
                    try {
                        while(resultSet.next()) {
                            offlinePrestigeDataList.add(new OfflinePrestigeData(
                                    resultSet.getString("player_id"),
                                    resultSet.getString("island_id"),
                                    resultSet.getInt("level"),
                                    resultSet.getTimestamp("prestige_time")));
                        }
                    } catch(SQLException e) {
                        throw new RuntimeException(e);
                    }

                    return null;
                }).thenCompose(o1 -> {
                    String deleteSql = "DROP TABLE " + tableName;

                    return queueManager.queueWriteTransaction(deleteSql).thenCompose(v2 -> createTable()
                            .thenCompose(v3 -> {
                                String insertSql = "INSERT INTO " + tableName + " (player_id, island_id, level) VALUES (?, ?, ?)";

                                if(!offlinePrestigeDataList.isEmpty()) {
                                    List<List<Parameter<?>>> listOfParameterLists = new ArrayList<>();

                                    offlinePrestigeDataList.forEach(offlinePrestigeData ->
                                            listOfParameterLists.add(List.of(
                                                    new StringParameter(offlinePrestigeData.playerId),
                                                    new StringParameter(offlinePrestigeData.islandId),
                                                    new IntegerParameter(offlinePrestigeData.level),
                                                    new TimestampParameter(offlinePrestigeData.prestigeTime)
                                            )));

                                    return queueManager.queueBulkWriteTransaction(insertSql, listOfParameterLists)
                                            .thenCompose(list -> versionsTable.updateVersion(tableName, 2));
                                } else {
                                    return versionsTable.updateVersion(tableName, 2);
                                }
                    }));
                });
            } else {
                return versionsTable.updateVersion(tableName, 2);
            }
        });
    }

    /**
     * This record temporarily stores offline prestige data for migration purposes.
     * @param playerId The player's {@link UUID} as a String.
     * @param islandId The island's id.
     * @param level The prestige level achieved.
     * @param prestigeTime The time the prestige occurred.
     */
    private record OfflinePrestigeData(
            @NotNull String playerId,
            @NotNull String islandId,
            @NotNull Integer level,
            @NotNull Timestamp prestigeTime) {}
}
