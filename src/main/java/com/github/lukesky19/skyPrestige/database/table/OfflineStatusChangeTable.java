package com.github.lukesky19.skyPrestige.database.table;

import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skyPrestige.util.parameter.CaseSensitiveStringParameter;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class creates the table to store the data necessary to process when a player's island's prestige status changes.
 * Prestige status refers to whether the island is opted in or out of prestige.
 */
public class OfflineStatusChangeTable {
    private final @NotNull ComponentLogger logger;
    private final @NotNull QueueManager queueManager;
    private final @NotNull VersionsTable versionsTable;
    private final @NotNull String tableName = "skyprestige_offline_status_change";

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}.
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public OfflineStatusChangeTable(
            @NotNull ComponentLogger logger,
            @NotNull QueueManager queueManager,
            @NotNull VersionsTable versionsTable) {
        this.logger = logger;
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates the table that stores the player's {@link UUID}, their latest island's id,
     * and the timestamp the prestige was completed. Also creates any indexes.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> createTable() {
        String tableCreationSql =
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "player_id VARCHAR(36) NOT NULL, " +
                        "island_id TEXT NOT NULL, " +
                        "status INTEGER NOT NULL, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
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
     * Stores the {@link UUID} of a player whose island was opted in or out of prestige while they were offline.
     * @param playerId The {@link UUID} of the player.
     * @param islandId The new island's unique id.
     * @param status 1 for opt in, 0 for opt out
     */
    public void insertOfflineStatusChange(@NotNull UUID playerId, @NotNull String islandId, int status) {
        String insertSql = "INSERT INTO " + tableName + " (player_id, island_id, status) VALUES (?, ?, ?)";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);
        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);
        IntegerParameter statusParameter = new IntegerParameter(status);

        queueManager.queueWriteTransaction(insertSql, List.of(playerIdParameter, islandIdParameter, statusParameter))
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.deserialize("Failed to insert or update offline prestige status change: " + ex.getMessage()));
                    return 0;
                });
    }

    /**
     * Removes any offline prestige status changes from the table for the given player id.
     * @param playerId The {@link UUID} of the player.
     */
    public void removeOfflineStatusChanges(@NotNull UUID playerId) {
        String deleteSql = "DELETE FROM " + tableName + " WHERE player_id = ?";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);

        queueManager.queueWriteTransaction(deleteSql, List.of(playerIdParameter))
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.deserialize("Failed to remove offline opt out: " + ex.getMessage()));
                    return 0;
                });
    }

    /**
     * Get any prestige status changes that occurred while the player was offline in the order they occurred (oldest to newest)
     * @param playerId The {@link UUID} of the player.
     * @return A {@link CompletableFuture} of a {@link List} of non-null {@link Integer}s for the statuses their island was changed to.
     * 1 refers to an opt-out (exempt from prestige), 0 refers to an opt-in (no longer exempt from prestige).
     * {@link CompletableFuture}.
     */
    public @NotNull CompletableFuture<@NotNull List<@NotNull Integer>> getOfflineStatusChanges(@NotNull UUID playerId) {
        String selectSql = "SELECT status FROM " + tableName + " WHERE player_id = ? ORDER BY timestamp ASC";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);

        return queueManager.queueReadTransaction(selectSql, List.of(playerIdParameter), resultSet -> {
                    List<@NotNull Integer> offlineStatusChanges = new ArrayList<>();

                    try {
                        while(resultSet.next()) {
                            offlineStatusChanges.add(resultSet.getInt("status"));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    return offlineStatusChanges;
                })
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.deserialize("Failed to get offline status changes: " + ex.getMessage()));
                    return new ArrayList<>();
                });
    }
}