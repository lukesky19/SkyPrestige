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
package com.github.lukesky19.skyPrestige.database.table.legacy;

import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import com.github.lukesky19.skylib.api.database.queue.QueueManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages access to the table stores the prestige points for the island.
 */
public class PrestigePointsTable {
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skyprestige_prestige_points";

    /**
     * Constructor
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     */
    public PrestigePointsTable(@NotNull QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    /**
     * Get all prestige points stored in the table.
     * @return A {@link CompletableFuture} containing a {@link Map} mapping island ids to prestige points.
     */
    public @NotNull CompletableFuture<@Nullable Map<String, Double>> getPrestigePoints() {
        String checkSql = "SELECT EXISTS (SELECT 1 FROM sqlite_master WHERE type='table' AND name='" + tableName + "')";
        String sql = "SELECT island_id, points FROM " + tableName;

        return queueManager.queueReadTransaction(checkSql, resultSet -> {
            try {
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 1; // Return true if the table exists
                }
                return false;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(exists -> {
            if(exists) {
                return queueManager.queueReadTransaction(sql, resultSet -> {
                    Map<String, Double> pointsMap = new HashMap<>();

                    try {
                        while(resultSet.next()) {
                            pointsMap.put(resultSet.getString("island_id"), resultSet.getDouble("points"));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    return pointsMap;
                });
            }

            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Delete the table from the database if it exists.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> deleteTable() {
        String dropSql = "DROP TABLE IF EXISTS " + tableName;

        return queueManager.queueWriteTransaction(dropSql)
                .thenRun(() -> {})
                .exceptionally(ex -> null);
    }
}
