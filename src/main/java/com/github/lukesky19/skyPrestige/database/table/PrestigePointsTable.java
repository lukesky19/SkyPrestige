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

import com.github.lukesky19.skyPrestige.data.IslandData;
import com.github.lukesky19.skyPrestige.util.parameter.CaseSensitiveStringParameter;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.database.parameter.impl.DoubleParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import com.github.lukesky19.skylib.api.database.queue.QueueManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages access to the table stores the prestige points for the island.
 */
public class PrestigePointsTable {
    private final @NotNull ComponentLogger logger;
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skyprestige_prestige_points";

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}.
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     */
    public PrestigePointsTable(
            @NotNull ComponentLogger logger,
            @NotNull QueueManager queueManager) {
        this.logger = logger;
        this.queueManager = queueManager;
    }

    /**
     * Create the table that stores {@link BlockType} and {@link EntityType} statistics for islands. Also creates any indexes.
     */
    public void createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "island_id TEXT NOT NULL, " +
                "points DOUBLE NOT NULL, " +
                "last_updated LONG NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (island_id) REFERENCES skyprestige_island_ids(island_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "UNIQUE (island_id));";
        String islandIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_skyprestige_prestige_points_island_id ON " + tableName + "(island_id);";

        queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, islandIdIndexCreationSql))
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.serialize("Prestige Points Table creation failed: " + ex.getMessage()));
                    return new ArrayList<>();
                });
    }

    /**
     * Save the prestige points for island id provided.
     * @param islandId The island id to save prestige points for.
     * @param prestigePoints The prestige points the island has.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> savePrestigePoints(@NotNull String islandId, double prestigePoints) {
        String updateSql = "INSERT INTO " + tableName + " (" +
                "island_id, " +
                "points, " +
                "last_updated) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (island_id) " +
                "DO UPDATE SET " +
                "points = ?, " +
                "last_updated = ? " +
                "WHERE last_updated < ?";
        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);
        DoubleParameter prestigePointsParameter = new DoubleParameter(prestigePoints);
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        return queueManager.queueWriteTransaction(updateSql, List.of(islandIdParameter, prestigePointsParameter, lastUpdatedParameter, prestigePointsParameter, lastUpdatedParameter, lastUpdatedParameter))
                .thenAccept(list -> {})
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.serialize("Failed to save prestige points: " + ex.getMessage()));
                    throw new RuntimeException(ex);
                });
    }

    /**
     * Load the prestige points for island id provided.
     * @param islandId The island id to load prestige points for.
     * @param islandData The {@link IslandData} to put the prestige points into.
     */
    public void loadPrestigePoints(@NotNull String islandId, @NotNull IslandData islandData) {
        String selectSql = "SELECT points FROM " + tableName + " WHERE island_id = ?";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);

        queueManager.queueReadTransaction(selectSql, List.of(islandIdParameter), resultSet -> {
            try {
                while(resultSet.next()) {
                    islandData.setPrestigePoints(resultSet.getDouble("points"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return null;
        });
    }

    /**
     * Reset all stored potion-related statistics for the given island id to 0.
     * @param islandId The island id to reset statistics for.
     * @return A {@link CompletableFuture} of type {@link Void} that can be used to determine when the method is complete.
     */
    public @NotNull CompletableFuture<Void> resetPrestigePoints(@NotNull String islandId) {
        String resetSql = "UPDATE " + tableName + " SET points = ?, last_updated = ? WHERE island_id = ? AND last_updated < ?";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);
        LongParameter zeroParameter = new LongParameter(0L);
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        return queueManager.queueWriteTransaction(resetSql, List.of(zeroParameter, lastUpdatedParameter, islandIdParameter, lastUpdatedParameter))
                .thenAccept(integer -> {})
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.serialize("Prestige Points reset failed: " + ex.getMessage()));
                    throw new RuntimeException(ex);
                });
    }
}
