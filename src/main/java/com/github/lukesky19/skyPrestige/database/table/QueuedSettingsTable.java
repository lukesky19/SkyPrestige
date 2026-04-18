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

import com.github.lukesky19.skyPrestige.data.data.reset.QueuedSettings;
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skyPrestige.util.enums.SettingsType;
import com.github.lukesky19.skyPrestige.util.parameter.CaseSensitiveStringParameter;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.common.api.database.parameter.impl.StringParameter;
import com.github.lukesky19.skylib.common.api.database.parameter.impl.UUIDParameter;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages interfacing with the table that stores the players that need settings applied to on login.
 */
public class QueuedSettingsTable {
    private final @NonNull ComponentLogger logger;
    private final @NonNull QueueManager queueManager;
    private final @NonNull VersionsTable versionsTable;
    private final @NonNull String tableName = "skyprestige_queued_settings";

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}.
     * @param queueManager A {@link QueueManager} instance.
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public QueuedSettingsTable(
            @NonNull ComponentLogger logger,
            @NonNull QueueManager queueManager,
            @NonNull VersionsTable versionsTable) {
        this.logger = logger;
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates the table that stores the player's {@link UUID}, their latest island's id, the prestige level,
     * and the timestamp the prestige was completed. Also creates any indexes.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> createTable() {
        String tableCreationSql =
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "player_id VARCHAR(36) NOT NULL, " +
                        "island_id TEXT NOT NULL, " +
                        "settings_type TEXT NOT NULL, " +
                        "prestige_level INTEGER NOT NULL DEFAULT -1, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (player_id) REFERENCES skyprestige_player_ids(player_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                        "FOREIGN KEY (island_id) REFERENCES skyprestige_island_ids(island_id) ON UPDATE CASCADE ON DELETE CASCADE)";
        String playerIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_skyprestige_queued_settings_player_id ON " + tableName + "(player_id)";
        String islandIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_skyprestige_queued_settings_island_id ON " + tableName + "(island_id)";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, playerIdIndexCreationSql, islandIdIndexCreationSql))
                .thenCompose(v -> versionsTable.updateVersion(tableName, 1))
                .exceptionally(ex -> {
                    logger.error(AdventureUtility.plain("Queued Settings Table creation failed: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Stores the {@link UUID} of the player and which settings to apply to them on login.
     * @param playerId The {@link UUID} of the player.
     * @param islandId The {@link Island}'s island id.
     * @param settingsType The {@link SettingsType}.
     * @param prestigeLevel The prestige level. Use -1 if not {@link SettingsType#PRESTIGE}.
     * If {@link SettingsType#TEAM_JOIN}, the prestige level should be the prestige level at the time of joining or -1 if opted out.
     * @return A {@link CompletableFuture} of type {@link Void}.
     */
    public @NonNull CompletableFuture<Void> queueSettings(
            @NonNull UUID playerId,
            @NonNull String islandId,
            @NonNull SettingsType settingsType,
            int prestigeLevel) {
        String insertSql = "INSERT INTO " + tableName + " (player_id, island_id, settings_type, prestige_level) VALUES (?, ?, ?, ?)";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);
        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);
        StringParameter settingsTypeParameter = new StringParameter(settingsType.toString());
        IntegerParameter prestigeLevelParameter = new IntegerParameter(prestigeLevel);

        return queueManager.queueWriteTransaction(insertSql, List.of(playerIdParameter, islandIdParameter, settingsTypeParameter, prestigeLevelParameter))
                .thenRun(() -> {})
                .exceptionally(ex -> {
                    logger.error(AdventureUtility.plain("Failed to queue settings: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Removes any queued settings from the table for the given player id.
     * @param playerId The {@link UUID} of the player.
     * @return A {@link CompletableFuture} of type {@link Void}.
     */
    public @NonNull CompletableFuture<Void> clearQueuedSettings(@NonNull UUID playerId) {
        String deleteSql = "DELETE FROM " + tableName + " WHERE player_id = ?";

        UUIDParameter playerIdParameter = new UUIDParameter(playerId);

        return queueManager.queueWriteTransaction(deleteSql, List.of(playerIdParameter))
                .thenRun(() -> {})
                .exceptionally(ex -> {
                    logger.error(AdventureUtility.plain("Failed to remove queued settings: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Get all queued settings to apply to the player, sorted by oldest to newest.
     * @param playerId The {@link UUID} of the player.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link QueuedSettings} sorted from oldest to newest.
     */
    public @NonNull CompletableFuture<@NonNull List<@NonNull QueuedSettings>> getQueuedSettings(@NonNull UUID playerId) {
        String sql = "SELECT island_id, settings_type, prestige_level, timestamp FROM " + tableName + " WHERE player_id = ? ORDER BY timestamp ASC";

        return queueManager.queueReadTransaction(sql, List.of(new UUIDParameter(playerId)), resultSet -> {
                    List<@NonNull QueuedSettings> queuedSettingsList = new ArrayList<>();

                    try {
                        while(resultSet.next()) {
                            queuedSettingsList.add(
                                    new QueuedSettings(
                                            playerId,
                                            resultSet.getString("island_id"),
                                            SettingsType.valueOf(resultSet.getString("settings_type").toUpperCase()),
                                            resultSet.getInt("prestige_level"),
                                            resultSet.getTimestamp("timestamp").getTime()));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    return queuedSettingsList;
                })
                .exceptionally(ex -> {
                    logger.error(AdventureUtility.plain("Failed to get queued settings: " + ex.getMessage()));
                    return null;
                });
    }
}