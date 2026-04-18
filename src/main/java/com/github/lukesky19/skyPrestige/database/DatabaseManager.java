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
package com.github.lukesky19.skyPrestige.database;

import com.github.lukesky19.skyPrestige.database.connection.ConnectionManager;
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skyPrestige.database.table.*;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages the database for the plugin.
 */
public class DatabaseManager {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull ComponentLogger logger;
    private final @NonNull ConnectionManager connectionManager;
    private final @NonNull QueueManager queueManager;
    private final @NonNull HookManager hookManager;

    private IslandIdsTable islandIdsTable;
    private PlayerIdsTable playerIdsTable;
    private IslandDataTable islandDataTable;
    private PlayerLogoutLocationsTable playerLogoutLocationsTables;
    private PlayerTeleportTable playerTeleportTable;
    private QueuedSettingsTable queuedSettingsTable;

    /**
     * Get the {@link IslandIdsTable}.
     * @return The {@link IslandIdsTable}
     */
    public @NonNull IslandIdsTable getIslandIdsTable() {
        return islandIdsTable;
    }

    /**
     * Get the {@link PlayerIdsTable}.
     * @return The {@link PlayerIdsTable}
     */
    public @NonNull PlayerIdsTable getPlayerIdsTable() {
        return playerIdsTable;
    }

    /**
     * Get the {@link IslandDataTable}.
     * @return The {@link IslandDataTable}
     */
    public @NonNull IslandDataTable getIslandDataTable() {
        return islandDataTable;
    }

    /**
     * Get the {@link PlayerLogoutLocationsTable}.
     * @return The {@link PlayerLogoutLocationsTable}
     */
    public @NonNull PlayerLogoutLocationsTable getPlayerLogoutLocationsTables() {
        return playerLogoutLocationsTables;
    }

    /**
     * Get the {@link PlayerTeleportTable}.
     * @return The {@link PlayerTeleportTable}
     */
    public @NonNull PlayerTeleportTable getPlayerTeleportTable() {
        return playerTeleportTable;
    }

    /**
     * Get the {@link QueuedSettingsTable}.
     * @return The {@link QueuedSettingsTable}
     */
    public @NonNull QueuedSettingsTable getQueuedSettingsTable() {
        return queuedSettingsTable;
    }

    /**
     * Constructor
     * Initializes the {@link ConnectionManager}, {@link QueueManager}, and all tables.
     * @param plugin A {@link SkyPlugin}.
     * @param hookManager A {@link HookManager} instance.
     */
    public DatabaseManager(@NonNull SkyPlugin plugin, @NonNull HookManager hookManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.hookManager = hookManager;
        connectionManager = new ConnectionManager(plugin);
        queueManager = new QueueManager(connectionManager);
    }

    /**
     * Setup all database tables and run any migration as needed.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> setup() {
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        VersionsTable versionsTable = new VersionsTable(queueManager);
        futureList.add(versionsTable.createTable());

        islandIdsTable = new IslandIdsTable(queueManager, versionsTable);
        futureList.add(islandIdsTable.createTable());

        playerIdsTable = new PlayerIdsTable(queueManager, versionsTable);
        futureList.add(playerIdsTable.createTable());

        islandDataTable = new IslandDataTable(plugin, queueManager, hookManager, versionsTable);
        futureList.add(islandDataTable.createTable());

        playerLogoutLocationsTables = new PlayerLogoutLocationsTable(queueManager, versionsTable);
        futureList.add(playerLogoutLocationsTables.createTable());

        playerTeleportTable = new PlayerTeleportTable(queueManager, versionsTable);
        futureList.add(playerTeleportTable.createTable());

        queuedSettingsTable = new QueuedSettingsTable(logger, queueManager, versionsTable);
        futureList.add(queuedSettingsTable.createTable());

        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    /**
     * Shuts down the queue processing reads and writes to the database and closes any active connections.
     */
    public void cleanUp() {
        queueManager.shutdownQueue().thenAccept(v -> connectionManager.closeConnections());
    }
}