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
import com.github.lukesky19.skylib.common.api.database.parameter.impl.UUIDParameter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class creates a table to store all {@link Player}'s {@link UUID}s.
 */
public class PlayerIdsTable {
    private final @NonNull QueueManager queueManager;
    private final @NonNull VersionsTable versionsTable;
    private final @NonNull String tableName = "skyprestige_player_ids";

    /**
     * Constructor
     * @param queueManager A {@link QueueManager} instance.
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public PlayerIdsTable(
            @NonNull QueueManager queueManager,
            @NonNull VersionsTable versionsTable) {
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates a table to store all {@link Player}'s {@link UUID}s as a string.
     * Queues the table creation and index creation sql.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (player_id VARCHAR(36) NOT NULL UNIQUE);";
        String indexCreationSql = "CREATE INDEX IF NOT EXISTS idx_player_ids_player_id ON " + tableName + "(player_id);";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, indexCreationSql)).thenCompose(list -> versionsTable.updateVersion(tableName, 1));
    }

    /**
     * Stores the {@link UUID} of the {@link Player} inside the player ids table.
     * @param uuid The {@link UUID} of the {@link Player}.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> insertPlayerId(@NonNull UUID uuid) {
        String insertPlayerIdSql = "INSERT INTO " + tableName + " (player_id) VALUES (?) ON CONFLICT (player_id) DO NOTHING";

        UUIDParameter parameter = new UUIDParameter(uuid);

        return queueManager.queueWriteTransaction(insertPlayerIdSql, List.of(parameter)).thenRun(() -> {});
    }
}
