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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.database.connection.ConnectionManager;
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skyPrestige.database.table.*;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

/**
 * This class manages the database for the plugin.
 */
public class DatabaseManager {
    private final ConnectionManager connectionManager;
    private final QueueManager queueManager;

    private final IslandIdsTable islandIdsTable;
    private final PlayerIdsTable playerIdsTable;
    private final PrestigeLevelsTable prestigeLevelsTable;
    private final PrestigePointsTable prestigePointsTable;
    private final OfflinePrestigeTable offlinePrestigeTable;
    private final PlayerLogoutLocationsTables playerLogoutLocationsTables;
    private final PlayerTeleportTable playerTeleportTable;
    private final IslandVaultsTable islandVaultsTable;

    /**
     * Get the {@link IslandIdsTable}.
     * @return The {@link IslandIdsTable}
     */
    public IslandIdsTable getIslandIdsTable() {
        return islandIdsTable;
    }

    /**
     * Get the {@link PlayerIdsTable}.
     * @return The {@link PlayerIdsTable}
     */
    public PlayerIdsTable getPlayerIdsTable() {
        return playerIdsTable;
    }

    /**
     * Get the {@link PrestigeLevelsTable}.
     * @return The {@link PrestigeLevelsTable}
     */
    public PrestigeLevelsTable getPrestigeLevelsTable() {
        return prestigeLevelsTable;
    }

    /**
     * Get the {@link PrestigePointsTable}.
     * @return The {@link PrestigePointsTable}
     */
    public PrestigePointsTable getPrestigePointsTable() {
        return prestigePointsTable;
    }

    /**
     * Get the {@link OfflinePrestigeTable}.
     * @return The {@link OfflinePrestigeTable}
     */
    public OfflinePrestigeTable getOfflinePrestigeTable() {
        return offlinePrestigeTable;
    }

    /**
     * Get the {@link PlayerLogoutLocationsTables}.
     * @return The {@link PlayerLogoutLocationsTables}
     */
    public PlayerLogoutLocationsTables getPlayerLogoutLocationsTables() {
        return playerLogoutLocationsTables;
    }

    /**
     * Get the {@link PlayerTeleportTable}.
     * @return The {@link PlayerTeleportTable}
     */
    public PlayerTeleportTable getPlayerTeleportTable() {
        return playerTeleportTable;
    }

    /**
     * Get the {@link IslandVaultsTable}.
     * @return The {@link IslandVaultsTable}
     */
    public IslandVaultsTable getIslandVaultsTable() {
        return islandVaultsTable;
    }

    /**
     * Constructor
     * Initializes the {@link ConnectionManager}, {@link QueueManager}, and all tables.
     * @param skyPrestige The main plugin's instance.
     */
    public DatabaseManager(@NotNull SkyPrestige skyPrestige) {
        ComponentLogger logger = skyPrestige.getComponentLogger();
        connectionManager = new ConnectionManager(skyPrestige);
        queueManager = new QueueManager(connectionManager);

        islandIdsTable = new IslandIdsTable(logger, queueManager);
        islandIdsTable.createTable();

        playerIdsTable = new PlayerIdsTable(queueManager);
        playerIdsTable.createTable();

        // Initialize Tables
        prestigeLevelsTable = new PrestigeLevelsTable(logger, queueManager);
        prestigeLevelsTable.createTable();

        prestigePointsTable = new PrestigePointsTable(logger, queueManager);
        prestigePointsTable.createTable();

        offlinePrestigeTable = new OfflinePrestigeTable(logger, queueManager);
        offlinePrestigeTable.createTable();

        playerLogoutLocationsTables = new PlayerLogoutLocationsTables(queueManager);
        playerLogoutLocationsTables.createTable();

        playerTeleportTable = new PlayerTeleportTable(queueManager);
        playerTeleportTable.createTable();

        islandVaultsTable = new IslandVaultsTable(logger, queueManager);
        islandVaultsTable.createTable();
    }

    /**
     * Shuts down the queue processing reads and writes to the database and closes any active connections.
     */
    public void cleanUp() {
        queueManager.shutdownQueue().thenAccept(v -> connectionManager.closeConnections());
    }
}
