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

import com.github.lukesky19.skyPrestige.core.util.key.PageSlotKey;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.database.connection.ConnectionManager;
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skyPrestige.database.table.*;
import com.github.lukesky19.skyPrestige.database.table.legacy.IslandVaultsTable;
import com.github.lukesky19.skyPrestige.database.table.legacy.PrestigeLevelsTable;
import com.github.lukesky19.skyPrestige.database.table.legacy.PrestigePointsTable;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages the database for the plugin.
 */
public class DatabaseManager {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull ConnectionManager connectionManager;
    private final @NotNull QueueManager queueManager;

    private IslandIdsTable islandIdsTable;
    private PlayerIdsTable playerIdsTable;
    private IslandDataTable islandDataTable;
    private OfflinePrestigeTable offlinePrestigeTable;
    private PlayerLogoutLocationsTables playerLogoutLocationsTables;
    private PlayerTeleportTable playerTeleportTable;
    // Legacy only
    private PrestigeLevelsTable prestigeLevelsTable;
    private PrestigePointsTable prestigePointsTable;
    private IslandVaultsTable islandVaultsTable;

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
     * Get the {@link IslandDataTable}.
     * @return The {@link IslandDataTable}
     */
    public IslandDataTable getIslandDataTable() {
        return islandDataTable;
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
     * Constructor
     * Initializes the {@link ConnectionManager}, {@link QueueManager}, and all tables.
     * @param plugin A {@link SkyPlugin}.
     */
    public DatabaseManager(@NotNull SkyPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        connectionManager = new ConnectionManager(plugin);
        queueManager = new QueueManager(connectionManager);
    }

    /**
     * Setup all database tables and run any migration as needed.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> setup() {
        @NotNull List<CompletableFuture<Void>> futureList = new ArrayList<>();

        VersionsTable versionsTable = new VersionsTable(queueManager);
        futureList.add(versionsTable.createTable());

        islandIdsTable = new IslandIdsTable(logger, queueManager, versionsTable);
        futureList.add(islandIdsTable.createTable());

        playerIdsTable = new PlayerIdsTable(queueManager, versionsTable);
        futureList.add(playerIdsTable.createTable());

        islandDataTable = new IslandDataTable(plugin, queueManager, versionsTable);
        futureList.add(islandDataTable.createTable());

        offlinePrestigeTable = new OfflinePrestigeTable(logger, queueManager, versionsTable);
        futureList.add(offlinePrestigeTable.createTable().thenCompose(v -> offlinePrestigeTable.migrate()));

        playerLogoutLocationsTables = new PlayerLogoutLocationsTables(queueManager, versionsTable);
        futureList.add(playerLogoutLocationsTables.createTable());

        playerTeleportTable = new PlayerTeleportTable(queueManager, versionsTable);
        futureList.add(playerTeleportTable.createTable());

        // Legacy
        prestigeLevelsTable = new PrestigeLevelsTable(queueManager);
        prestigePointsTable = new PrestigePointsTable(queueManager);
        islandVaultsTable = new IslandVaultsTable(logger, queueManager);

        // Migrate any legacy data after all tables are created and updated (if needed)
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        return allFutures.thenCompose(v -> migrate());
    }

    /**
     * Shuts down the queue processing reads and writes to the database and closes any active connections.
     */
    public void cleanUp() {
        queueManager.shutdownQueue().thenAccept(v -> connectionManager.closeConnections());
    }

    /**
     * Migrate data from legacy tables to the new table.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> migrate() {
        return islandIdsTable.getIslandIds().thenCompose(islandIdList ->
                prestigeLevelsTable.getPrestigeLevels().thenCompose(levelsMap -> {
                    if(levelsMap == null) return CompletableFuture.completedFuture(null);

                    return prestigePointsTable.getPrestigePoints().thenCompose(pointsMap -> {
                        if(pointsMap == null) return CompletableFuture.completedFuture(null);

                        return islandVaultsTable.getVaultData().thenCompose(vaultDataMap -> {
                            if(vaultDataMap == null) return CompletableFuture.completedFuture(null);
                            Map<String, IslandData> islandDataMap = new HashMap<>();

                            islandIdList.forEach(islandId -> {
                                @NotNull IslandData islandData = new IslandData();
                                @Nullable Integer level = levelsMap.get(islandId);
                                @Nullable Double points = pointsMap.get(islandId);
                                @Nullable Map<PageSlotKey, ItemStack> vaultMap = vaultDataMap.get(islandId);

                                if(level != null) {
                                    islandData.setPrestigeLevel(level);
                                }

                                if(points != null) {
                                    islandData.setPrestigePoints(points);
                                }

                                if(vaultMap != null) {
                                    islandData.setVaultItems(vaultMap);
                                }

                                islandDataMap.put(islandId, islandData);
                            });

                            return islandDataTable.saveIslandData(islandDataMap)
                                    .thenCompose(v1 -> prestigeLevelsTable.deleteTable()
                                            .thenCompose(v2 -> prestigePointsTable.deleteTable()
                                                    .thenCompose(v3 -> islandVaultsTable.deleteTable())));
                        });
                    });
        }));
    }
}