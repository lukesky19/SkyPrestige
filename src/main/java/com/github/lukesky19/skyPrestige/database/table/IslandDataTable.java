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

import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.data.leaderboard.Position;
import com.github.lukesky19.skyPrestige.data.data.leaderboard.TopTen;
import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.util.key.PageSlotKey;
import com.github.lukesky19.skyPrestige.util.parameter.ByteArrayParameter;
import com.github.lukesky19.skyPrestige.util.parameter.CaseSensitiveStringParameter;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.database.parameter.Parameter;
import com.github.lukesky19.skylib.common.api.database.parameter.impl.DoubleParameter;
import com.github.lukesky19.skylib.common.api.database.parameter.impl.IntegerParameter;
import com.github.lukesky19.skylib.common.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.common.api.database.queue.MultiThreadQueueManager;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class creates a table to store island data.
 */
public class IslandDataTable {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull ComponentLogger logger;
    private final @NonNull QueueManager queueManager;
    private final @NonNull HookManager hookManager;
    private final @NonNull VersionsTable versionsTable;
    private final @NonNull String tableName = "skyprestige_island_data";

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     * @param hookManager A {@link HookManager} instance.
     * @param versionsTable A {@link VersionsTable} instance.
     */
    public IslandDataTable(
            @NonNull SkyPlugin plugin,
            @NonNull QueueManager queueManager,
            @NonNull HookManager hookManager,
            @NonNull VersionsTable versionsTable) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.queueManager = queueManager;
        this.hookManager = hookManager;
        this.versionsTable = versionsTable;
    }

    /**
     * Creates the table that stores island's vaults by island id. Also creates any indexes.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> createTable() {
        String tableCreationSql =
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "island_id TEXT UNIQUE NOT NULL, " +
                        "level INTEGER NOT NULL DEFAULT 0, " +
                        "points DOUBLE NOT NULL, " +
                        "multiplier DOUBLE NOT NULL, " +
                        "multiplier_time LONG NOT NULL, " +
                        "vault_data BLOB NOT NULL, " +
                        "leaderboard_exempt INTEGER NOT NULL DEFAULT 0, " +
                        "prestige_exempt INTEGER NOT NULL DEFAULT 0, " +
                        "last_updated LONG NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY (island_id) REFERENCES skyprestige_island_ids(island_id) ON UPDATE CASCADE ON DELETE CASCADE)";
        String islandIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_skyprestige_island_data_island_id ON " + tableName + "(island_id)";

        return queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, islandIdIndexCreationSql))
                .thenCompose(list -> versionsTable.updateVersion(tableName, 1))
                .exceptionally(ex -> {
                    logger.error(AdventureUtility.plain("Island Data Table creation failed: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Load the prestige level, points, vault data, and exemption status for the island id provided.
     * @param islandId The island id to load data for.
     * @param islandData The {@link IslandData} for the island.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<IslandData> loadIslandData(@NonNull String islandId, @NonNull IslandData islandData) {
        String selectSql = "SELECT level, points, multiplier, multiplier_time, vault_data, leaderboard_exempt, prestige_exempt FROM " + tableName + " WHERE island_id = ?";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);

        return queueManager.queueReadTransaction(selectSql, List.of(islandIdParameter), resultSet -> {
            try {
                if(resultSet.next()) {
                    int level = resultSet.getInt("level");
                    double points = resultSet.getDouble("points");
                    double multiplier = resultSet.getDouble("multiplier");
                    long multiplierTime = resultSet.getLong("multiplier_time");
                    boolean leaderboardExempt = resultSet.getBoolean("leaderboard_exempt");
                    boolean prestigeExempt = resultSet.getBoolean("prestige_exempt");
                    byte[] rawVaultData = resultSet.getBytes("vault_data");
                    Map<PageSlotKey, ItemStack> vaultData = deserializeItemMap(islandId, rawVaultData);

                    islandData.setPrestigeLevel(level);
                    islandData.setPrestigePoints(points);
                    islandData.setMultiplier(multiplier);
                    islandData.setMultiplierTime(multiplierTime);
                    islandData.setLeaderboardExempt(leaderboardExempt);
                    islandData.setPrestigeExempt(prestigeExempt);
                    islandData.setVaultItems(vaultData);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return islandData;
        });
    }

    /**
     * Save the island data for the island id provided.
     * @param islandId The Island's unique id.
     * @param islandData The {@link IslandData} to save.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> saveIslandData(@NonNull String islandId, @NonNull IslandData islandData) {
        String updateSql = "INSERT INTO " + tableName + " (" +
                "island_id, " +
                "level, " +
                "points, " +
                "multiplier, " +
                "multiplier_time, " +
                "vault_data, " +
                "leaderboard_exempt, " +
                "prestige_exempt, " +
                "last_updated) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (island_id) " +
                "DO UPDATE SET " +
                "level = ?, " +
                "points = ?, " +
                "multiplier = ?, " +
                "multiplier_time = ?, " +
                "vault_data = ?, " +
                "leaderboard_exempt = ?, " +
                "prestige_exempt = ?, " +
                "last_updated = ? " +
                "WHERE last_updated < ?";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);
        IntegerParameter prestigeLevelParameter = new IntegerParameter(islandData.getPrestigeLevel());
        DoubleParameter prestigePointsParameter = new DoubleParameter(islandData.getPrestigePoints());
        DoubleParameter multiplierParameter = new DoubleParameter(islandData.getMultiplier());
        LongParameter multiplierTimeParameter = new LongParameter(islandData.getMultiplierTime());
        ByteArrayParameter vaultDataParameter = new ByteArrayParameter(serializeItemMap(islandData.getVaultItems()));
        IntegerParameter leaderboardExemptParameter = new IntegerParameter(islandData.isLeaderboardExempt() ? 1 : 0);
        IntegerParameter prestigeExemptParameter = new IntegerParameter(islandData.isPrestigeExempt() ? 1 : 0);
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        return queueManager.queueWriteTransaction(updateSql, List.of(
                islandIdParameter,
                prestigeLevelParameter,
                prestigePointsParameter,
                multiplierParameter,
                multiplierTimeParameter,
                vaultDataParameter,
                leaderboardExemptParameter,
                prestigeExemptParameter,
                lastUpdatedParameter,
                prestigeLevelParameter,
                prestigePointsParameter,
                multiplierParameter,
                multiplierTimeParameter,
                vaultDataParameter,
                leaderboardExemptParameter,
                prestigeExemptParameter,
                lastUpdatedParameter,
                lastUpdatedParameter)).thenRun(() -> {});
    }

    /**
     * Save all island data provided.
     * @param islandDataMap A {@link Map} mapping island ids to {@link IslandData}.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> saveIslandData(@NonNull Map<String, IslandData> islandDataMap) {
        String updateSql = "INSERT INTO " + tableName + " (" +
                "island_id, " +
                "level, " +
                "points, " +
                "multiplier, " +
                "multiplier_time, " +
                "vault_data, " +
                "leaderboard_exempt, " +
                "prestige_exempt, " +
                "last_updated) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (island_id) " +
                "DO UPDATE SET " +
                "level = ?, " +
                "points = ?, " +
                "multiplier = ?, " +
                "multiplier_time = ?, " +
                "vault_data = ?, " +
                "leaderboard_exempt = ?, " +
                "prestige_exempt = ?, " +
                "last_updated = ? " +
                "WHERE last_updated < ?";
        List<List<Parameter<?>>> listOfParameterLists = new ArrayList<>();

        islandDataMap.forEach((islandId, islandData) -> {
            CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);
            IntegerParameter prestigeLevelParameter = new IntegerParameter(islandData.getPrestigeLevel());
            DoubleParameter prestigePointsParameter = new DoubleParameter(islandData.getPrestigePoints());
            DoubleParameter multiplierParameter = new DoubleParameter(islandData.getMultiplier());
            LongParameter multiplierTimeParameter = new LongParameter(islandData.getMultiplierTime());
            ByteArrayParameter vaultDataParameter = new ByteArrayParameter(serializeItemMap(islandData.getVaultItems()));
            IntegerParameter leaderboardExemptParameter = new IntegerParameter(islandData.isLeaderboardExempt() ? 1 : 0);
            IntegerParameter prestigeExemptParameter = new IntegerParameter(islandData.isPrestigeExempt() ? 1 : 0);
            LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

            listOfParameterLists.add(List.of(
                    islandIdParameter,
                    prestigeLevelParameter,
                    prestigePointsParameter,
                    multiplierParameter,
                    multiplierTimeParameter,
                    vaultDataParameter,
                    leaderboardExemptParameter,
                    prestigeExemptParameter,
                    lastUpdatedParameter,
                    prestigeLevelParameter,
                    prestigePointsParameter,
                    multiplierParameter,
                    multiplierTimeParameter,
                    vaultDataParameter,
                    leaderboardExemptParameter,
                    prestigeExemptParameter,
                    lastUpdatedParameter,
                    lastUpdatedParameter));
        });

        return queueManager.queueBulkWriteTransaction(updateSql, listOfParameterLists).thenRun(() -> {});
    }

    /**
     * Retrieves the {@link TopTen} based on prestige levels then prestige points.
     * @return A {@link CompletableFuture} containing the {@link TopTen} by prestige levels then prestige points.
     */
    public @NonNull CompletableFuture<@NonNull TopTen> getTopTenByPrestigeLevelAndPointsNotExempt() {
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);

        String sql = "SELECT island_id, level, points FROM " + tableName + " WHERE leaderboard_exempt = 0 ORDER BY level DESC, points DESC LIMIT 10";
        return queueManager.queueReadTransaction(sql, resultSet -> {
            List<Position> positionList = new LinkedList<>();

            try {
                while(resultSet.next()) {
                    String islandId = resultSet.getString("island_id");
                    Optional<Island> optionalIsland = bentoBoxHook.getIslandById(islandId);
                    int level = resultSet.getInt("level");
                    double points = resultSet.getDouble("points");
                    String ownerName = null;

                    if(optionalIsland.isPresent()) {
                        Island island = optionalIsland.get();

                        UUID ownerId = island.getOwner();

                        if(ownerId != null) {
                            OfflinePlayer player = plugin.getServer().getOfflinePlayer(ownerId);
                            ownerName = player.getName();
                        }
                    }

                    positionList.add(new Position(islandId, ownerName, level, points));
                }

                return new TopTen(positionList);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Serialize the vault data map into a byte array.
     * @param itemMap A byte array.
     * @return A byte array.
     * @throws RuntimeException on any IO exception.
     */
    public byte[] serializeItemMap(@NonNull Map<PageSlotKey, ItemStack> itemMap) {
        Map<PageSlotKey, byte[]> rawMap = new HashMap<>();

        itemMap.forEach((pageSlotKey, itemStack) -> {
            byte[] bytes = itemStack.serializeAsBytes();
            rawMap.put(pageSlotKey, bytes);
        });

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(rawMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Deserialize the byte array back into the vault data map.
     * @param islandId The island id whose data is being deserialized. Used for error messages.
     * @param mapBytes A byte array.
     * @return A {@link Map} mapping {@link PageSlotKey}s to {@link ItemStack}s.
     * @throws RuntimeException on any IOException or ClassNotFoundException.
     */
    @SuppressWarnings("unchecked")
    public @NonNull Map<PageSlotKey, ItemStack> deserializeItemMap(@NonNull String islandId, byte[] mapBytes) {
        try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(mapBytes))) {
            Map<PageSlotKey, ItemStack> itemMap = new HashMap<>();

            if(ois.readObject() instanceof Map<?, ?> map) {
                if(map.isEmpty()) return itemMap;

                if(!(map.keySet().stream().allMatch(key -> key instanceof PageSlotKey))) {
                    logger.warn(AdventureUtility.plain("The vault data for island id " + islandId + " is not in a valid or recognized format."));
                    return itemMap;
                }

                Map<PageSlotKey, byte[]> modernMap = (Map<PageSlotKey, byte[]>) map;
                modernMap.forEach((pageSlotKey, bytes) -> {
                    ItemStack itemStack = ItemStack.deserializeBytes(bytes);
                    itemMap.put(pageSlotKey, itemStack);
                });

                return itemMap;
            }

            logger.warn(AdventureUtility.plain("The vault data for island id " + islandId + " is not in a valid or recognized format."));
            return itemMap;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
