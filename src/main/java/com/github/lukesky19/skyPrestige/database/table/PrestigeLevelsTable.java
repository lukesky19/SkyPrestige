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
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skyPrestige.util.parameter.CaseSensitiveStringParameter;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
     * @param logger The plugin's {@link ComponentLogger}.
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     */
    public PrestigeLevelsTable(@NotNull ComponentLogger logger, @NotNull QueueManager queueManager) {
        this.logger = logger;
        this.queueManager = queueManager;
    }

    /**
     * Creates a table to store prestige levels for island ids.
     * Queues the table creation and index creation sql.
     */
    public void createTable() {
        String tableCreationSql =
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "island_id TEXT UNIQUE NOT NULL, " +
                "level INTEGER NOT NULL DEFAULT 0, " +
                "last_updated LONG NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (island_id) REFERENCES skyprestige_island_ids(island_id) ON UPDATE CASCADE ON DELETE CASCADE)";
        String indexCreationSql = "CREATE INDEX IF NOT EXISTS idx_prestige_levels_island_id ON " + tableName + "(island_id);";

        queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, indexCreationSql))
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.serialize("Prestige levels table creation failed: " + ex.getMessage()));
                    return new ArrayList<>();
                });
    }

    /**
     * Set desired prestige level for the given island id.
     * @param islandId The island id to update.
     * @param level The prestige level to store in the database.
     * @return A {@link CompletableFuture} of type {@link Void} that can be used to determine when the method is complete.
     */
    public @NotNull CompletableFuture<Void> setLevel(@NotNull String islandId, int level) {
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (island_id, level, last_updated) VALUES (?, ?, ?) " +
                "ON CONFLICT (island_id) DO UPDATE SET level = ?, last_updated = ? WHERE last_updated < ?";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);
        IntegerParameter levelParameter = new IntegerParameter(level);
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        return queueManager.queueWriteTransaction(insertOrUpdateSql, List.of(islandIdParameter, levelParameter, lastUpdatedParameter, levelParameter, lastUpdatedParameter, lastUpdatedParameter))
                .thenAccept(integer -> {})
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.serialize("Failed to set island prestige level: " + ex.getMessage()));
                    throw new RuntimeException(ex);
                });
    }

    /**
     * Gets the prestige level for the given island id.
     * @param islandId The island's unique id.
     * @param islandData The {@link IslandData} to set the prestige level for.
     */
    public void loadIslandLevel(
            @NotNull String islandId,
            @NotNull IslandData islandData) {
        String selectSql = "SELECT level FROM " + tableName + " WHERE island_id = ?";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);

        queueManager.queueReadTransaction(selectSql, List.of(islandIdParameter), resultSet -> {
            try {
                if(resultSet.next()) {
                    islandData.setPrestigeLevel(resultSet.getInt("level"));
                } else {
                    islandData.setPrestigeLevel(0);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return null;
        });
    }
}
