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
import com.github.lukesky19.skyPrestige.util.key.PageSlotKey;
import com.github.lukesky19.skyPrestige.util.parameter.ByteArrayParameter;
import com.github.lukesky19.skyPrestige.util.parameter.CaseSensitiveStringParameter;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This class creates the table to store the data necessary to process when a player's island was prestiged while offline.
 */
public class IslandVaultsTable {
    private final @NotNull ComponentLogger logger;
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skyprestige_vaults";

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}.
     * @param queueManager A class instance that extends {@link MultiThreadQueueManager}
     */
    public IslandVaultsTable(@NotNull ComponentLogger logger, @NotNull QueueManager queueManager) {
        this.logger = logger;
        this.queueManager = queueManager;
    }

    /**
     * Creates the table that stores island's vaults by island id. Also creates any indexes.
     */
    public void createTable() {
        String tableCreationSql =
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "island_id TEXT UNIQUE NOT NULL, " +
                        "vault_data BLOB NOT NULL, " +
                        "last_updated LONG NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY (island_id) REFERENCES skyprestige_island_ids(island_id) ON UPDATE CASCADE ON DELETE CASCADE)";
        String islandIdIndexCreationSql = "CREATE INDEX IF NOT EXISTS idx_skyprestige_vaults_island_id ON " + tableName + "(island_id)";

        queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, islandIdIndexCreationSql))
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.serialize("Vaults Table creation failed: " + ex.getMessage()));
                    return new ArrayList<>();
                });
    }

    /**
     * Set the stored vault data for the island id provided.
     * @param islandId The island id.
     * @param vaultMap A {@link Map} {@link PageSlotKey}s to {@link ItemStack}s.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> setVaultData(@NotNull String islandId, @NotNull Map<PageSlotKey, ItemStack> vaultMap) {
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (island_id, vault_data, last_updated) VALUES (?, ?, ?) " +
                "ON CONFLICT (island_id) DO UPDATE SET vault_data = ?, last_updated = ? WHERE last_updated < ?";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);
        ByteArrayParameter byteArrayParameter = new ByteArrayParameter(serializeItemMap(vaultMap));
        LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

        return queueManager.queueWriteTransaction(insertOrUpdateSql, List.of(islandIdParameter, byteArrayParameter, lastUpdatedParameter, byteArrayParameter, lastUpdatedParameter, lastUpdatedParameter))
                .thenAccept(integer -> {})
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.serialize("Failed to set island vault data: " + ex.getMessage()));
                    throw new RuntimeException(ex);
                });
    }

    /**
     * Gets the vault data for the given island id.
     * @param islandId The island's unique id.
     * @param islandData The {@link IslandData} to set the vault data for.
     */
    public void loadVaultData(
            @NotNull String islandId,
            @NotNull IslandData islandData) {
        String selectSql = "SELECT vault_data FROM " + tableName + " WHERE island_id = ?";

        CaseSensitiveStringParameter islandIdParameter = new CaseSensitiveStringParameter(islandId);

        queueManager.queueReadTransaction(selectSql, List.of(islandIdParameter), resultSet -> {
            try {
                if(resultSet.next()) {
                    byte[] mapBytes = resultSet.getBytes("vault_data");

                    Map<PageSlotKey, ItemStack> vaultMap = deserializeItemMap(islandId, mapBytes);

                    islandData.setVaultItems(vaultMap);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return null;
        });
    }

    /**
     * Serialize the vault data map into a byte array.
     * @param itemMap A byte array.
     * @return A byte array.
     * @throws RuntimeException on any IO exception.
     */
    private byte[] serializeItemMap(@NotNull Map<PageSlotKey, ItemStack> itemMap) {
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
    private @NotNull Map<PageSlotKey, ItemStack> deserializeItemMap(@NotNull String islandId, byte[] mapBytes) {
        try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(mapBytes))) {
            Map<PageSlotKey, ItemStack> itemMap = new HashMap<>();

            if(ois.readObject() instanceof Map<?, ?> map) {
                if(map.isEmpty()) return itemMap;

                boolean isValidType = true;
                for(Object key : map.keySet()) {
                    if(!(key instanceof PageSlotKey)) {
                        isValidType = false;
                        break;
                    }

                    break;
                }

                if(!isValidType) {
                    logger.warn(AdventureUtil.serialize("The vault data for island id " + islandId + " is not in a valid or recognized format."));
                    return itemMap;
                }

                Map<PageSlotKey, byte[]> modernMap = (Map<PageSlotKey, byte[]>) map;
                modernMap.forEach((pageSlotKey, bytes) -> {
                    ItemStack itemStack = ItemStack.deserializeBytes(bytes);
                    itemMap.put(pageSlotKey, itemStack);
                });

                return itemMap;
            }

            logger.warn(AdventureUtil.serialize("The vault data for island id " + islandId + " is not in a valid or recognized format."));
            return itemMap;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
