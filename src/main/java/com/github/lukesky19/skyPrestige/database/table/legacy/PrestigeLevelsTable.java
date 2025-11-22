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

import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This class creates a table to store the prestige level for islands.
 */
public class PrestigeLevelsTable {
    private final @NotNull ComponentLogger logger;
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skyprestige_prestige_levels";

    /**
     * Constructor
     * @param logger A {@link ComponentLogger} instance.
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     */
    public PrestigeLevelsTable(@NotNull ComponentLogger logger, @NotNull QueueManager queueManager) {
        this.logger = logger;
        this.queueManager = queueManager;
    }

    /**
     * Get all prestige levels stored in the table.
     * @return A {@link CompletableFuture} containing a {@link Map} mapping island ids to prestige levels.
     */
    public @NotNull CompletableFuture<@Nullable Map<String, Integer>> getPrestigeLevels() {
        String checkSql = "SELECT EXISTS (SELECT 1 FROM sqlite_master WHERE type='table' AND name='" + tableName + "')";
        String sql = "SELECT island_id, level FROM " + tableName;

        return queueManager.queueReadTransaction(checkSql, resultSet -> {
            try {
                if(resultSet.next()) {
                    return resultSet.getBoolean(1);
                }

                return false;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(result -> {
            if(result) {
                return queueManager.queueReadTransaction(sql, resultSet -> {
                    Map<String, Integer> levelsMap = new HashMap<>();

                    try {
                        while (resultSet.next()) {
                            levelsMap.put(resultSet.getString("island_id"), resultSet.getInt("level"));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    return levelsMap;
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
