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
package com.github.lukesky19.skyPrestige.database.table.legacy;

import com.github.lukesky19.skyPrestige.database.queue.QueueManager;
import com.github.lukesky19.skyPrestige.util.key.PageSlotKey;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.HashMap;
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
     * Get all vault data stored in the table.
     * @return A {@link CompletableFuture} containing a {@link Map} mapping island ids to vault data.
     */
    public @NotNull CompletableFuture<@Nullable Map<String, Map<PageSlotKey, ItemStack>>> getVaultData() {
        String checkSql = "SELECT EXISTS (SELECT 1 FROM sqlite_master WHERE type='table' AND name='" + tableName + "')";
        String sql = "SELECT island_id, vault_data FROM " + tableName;

        return queueManager.queueReadTransaction(checkSql, resultSet -> {
            try {
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 1; // Return true if the table exists
                }
                return false;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(exists -> {
            if(exists) {
                return queueManager.queueReadTransaction(sql, resultSet -> {
                    Map<String, Map<PageSlotKey, ItemStack>> vaultDataMap = new HashMap<>();

                    try {
                        while(resultSet.next()) {
                            String islandId = resultSet.getString("island_id");
                            byte[] mapBytes = resultSet.getBytes("vault_data");
                            Map<PageSlotKey, ItemStack> vaultMap = deserializeItemMap(islandId, mapBytes);

                            vaultDataMap.put(islandId, vaultMap);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    return vaultDataMap;
                });
            }

            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Delete the table from the database if it exists.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> deleteTable() {
        String dropSql = "DROP TABLE IF EXISTS " + tableName;

        return queueManager.queueWriteTransaction(dropSql)
                .thenRun(() -> {})
                .exceptionally(ex -> null);
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
                    logger.warn(AdventureUtil.deserialize("The vault data for island id " + islandId + " is not in a valid or recognized format."));
                    return itemMap;
                }

                Map<PageSlotKey, byte[]> modernMap = (Map<PageSlotKey, byte[]>) map;
                modernMap.forEach((pageSlotKey, bytes) -> {
                    ItemStack itemStack = ItemStack.deserializeBytes(bytes);
                    itemMap.put(pageSlotKey, itemStack);
                });

                return itemMap;
            }

            logger.warn(AdventureUtil.deserialize("The vault data for island id " + islandId + " is not in a valid or recognized format."));
            return itemMap;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
