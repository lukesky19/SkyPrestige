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
import com.github.lukesky19.skylib.api.database.parameter.Parameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.StringParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Creates a table to store the location and island the player logged out on.
 */
public class PlayerLogoutLocationsTable {
    private final @NotNull QueueManager queueManager;
    private final @NotNull VersionsTable versionsTable;
    private final @NotNull String tableName = "skyprestige_player_logout_locations";

    /**
     * Constructor
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public PlayerLogoutLocationsTable(
            @NotNull QueueManager queueManager,
            @NotNull VersionsTable versionsTable) {
        this.queueManager = queueManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates a table to store the location and island the player logged out on.
     * Queues the table creation and index creation sql.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "player_id VARCHAR(36) NOT NULL UNIQUE, " +
                "world TEXT NOT NULL, " +
                "x INTEGER NOT NULL, " +
                "z INTEGER NOT NULL, " +
                "last_updated LONG NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (player_id) REFERENCES skyprestige_player_ids(player_id) ON UPDATE CASCADE ON DELETE CASCADE)";
        String playerIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_player_logout_locations_player_id ON " + tableName + "(player_id)";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, playerIdIndexCreationSql))
                .thenCompose(list -> versionsTable.updateVersion(tableName, 1));
    }

    /**
     * Store the logout location for a player.
     * @param uuid The {@link UUID} of the player.
     * @param location The {@link Location} the player logged out at.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> setPlayerLogoutLocation(@NotNull UUID uuid, @NotNull Location location) {
        String updateSql = "INSERT INTO " + tableName + " (" +
                "player_id, " +
                "world, " +
                "x, " +
                "z, " +
                "last_updated) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (player_id) " +
                "DO UPDATE SET " +
                "world = ?, " +
                "x = ?, " +
                "z = ?, " +
                "last_updated = ? " +
                "WHERE last_updated < ?";

        UUIDParameter playerIdParameter = new UUIDParameter(uuid);
        StringParameter worldNameParameter = new StringParameter(location.getWorld().getName());
        IntegerParameter xParameter = new IntegerParameter(location.getBlockX());
        IntegerParameter zParameter = new IntegerParameter(location.getBlockZ());
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        List<Parameter<?>> parameterList = new ArrayList<>();
        parameterList.add(playerIdParameter);
        parameterList.add(worldNameParameter);
        parameterList.add(xParameter);
        parameterList.add(zParameter);
        parameterList.add(lastUpdatedParameter);

        parameterList.add(worldNameParameter);
        parameterList.add(xParameter);
        parameterList.add(zParameter);
        parameterList.add(lastUpdatedParameter);

        parameterList.add(lastUpdatedParameter);

        return queueManager.queueWriteTransaction(updateSql, parameterList).thenRun(() -> {});
    }

    /**
     * Get a {@link List} of {@link UUID}s where their logout location is within the provided bounds.
     * @param worldName The name of the world.
     * @param minX The island's min X coordinate.
     * @param minZ The island's min Z coordinate.
     * @param maxX The island's max X coordinate.
     * @param maxZ The island's max Z coordinate.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link UUID}s. The list may be empty.
     */
    public @NotNull CompletableFuture<List<UUID>> getPlayerIdsWithinByBounds(@NotNull String worldName, int minX, int maxX, int minZ, int maxZ) {
        String selectSql = "SELECT player_id FROM " + tableName + " WHERE world = ? AND x >= ? AND x <= ? AND z >= ? AND z <= ?";

        StringParameter worldNameParameter = new StringParameter(worldName);
        IntegerParameter minXParameter = new IntegerParameter(minX);
        IntegerParameter maxXParameter = new IntegerParameter(maxX);
        IntegerParameter minZParameter = new IntegerParameter(minZ);
        IntegerParameter maxZParameter = new IntegerParameter(maxZ);

        List<Parameter<?>> parameterList = List.of(worldNameParameter, minXParameter, maxXParameter, minZParameter, maxZParameter);

        return queueManager.queueReadTransaction(selectSql, parameterList, resultSet -> {
            List<UUID> playerIds = new ArrayList<>();

            try {
                while(resultSet.next()) {
                    UUID uuid = UUID.fromString(resultSet.getString("player_id"));

                    playerIds.add(uuid);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return playerIds;
        });
    }
}
