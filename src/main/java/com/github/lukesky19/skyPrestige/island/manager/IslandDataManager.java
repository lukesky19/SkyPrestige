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
package com.github.lukesky19.skyPrestige.island.manager;

import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages island data.
 */
public class IslandDataManager {
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull Map<String, IslandData> islandDataMap = new HashMap<>();

    /**
     * Constructor
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public IslandDataManager(@NotNull DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Get a {@link Map} mapping island ids to {@link IslandData}.
     * @return The {@link Map} mapping island ids to {@link IslandData}.
     */
    public @NotNull Map<String, IslandData> getIslandData() {
        return islandDataMap;
    }

    /**
     * Get the {@link IslandData} for the island id provided. May be null.
     * @param islandId The island id to get island data for.
     * @return The {@link IslandData} or null.
     */
    public @Nullable IslandData getIslandData(@NotNull String islandId) {
        return islandDataMap.get(islandId);
    }

    /**
     * Set the {@link IslandData} for the island id provided.
     * @param islandId The island id.
     * @param islandData The {@link IslandData}
     */
    public void setIslandData(@NotNull String islandId, @NotNull IslandData islandData) {
        islandDataMap.put(islandId, islandData);
    }

    /**
     * Remove the {@link IslandData} for the island id provided.
     * @apiNote This only removes the data from memory, not the database.
     * @param islandId The island id.
     */
    public void removeIslandData(@NotNull String islandId) {
        islandDataMap.remove(islandId);
    }

    /**
     * Load islands data for all islands the {@link UUID} is attached to.
     * @param uuid The {@link UUID} of a player.
     */
    public void loadIslandData(@NotNull UUID uuid) {
        BentoBox bentoBox = BentoBox.getInstance();
        List<Island> islandList = bentoBox.getIslandsManager().getIslands(uuid);

        islandList.forEach(island -> {
            String islandId = island.getUniqueId();
            // Insert the island id's into the database if it doesn't exist already
            databaseManager.getIslandIdsTable().insertIslandId(islandId);

            // Get the IslandData for the island id
            IslandData islandData = getIslandData(islandId);
            // If not loaded (null), create and load the data for that island.
            if(islandData == null) {
                // Create the new IslandData
                IslandData newIslandData = new IslandData();
                setIslandData(islandId, newIslandData);

                // Load any data from the database.
                databaseManager.getIslandDataTable().loadIslandData(islandId, newIslandData);
            }
        });
    }

    /**
     * Save the {@link IslandData} for the island id provided.
     * @param islandId The island id to save data for.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> saveIslandData(@NotNull String islandId) {
        IslandData islandData = islandDataMap.get(islandId);
        if(islandData == null) return CompletableFuture.completedFuture(null);

        return databaseManager.getIslandDataTable().saveIslandData(islandId, islandData);
    }

    /**
     * Save all island data to the database.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NotNull CompletableFuture<Void> saveIslandData() {
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        islandDataMap.keySet().forEach(islandId -> futureList.add(saveIslandData(islandId)));

        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }
}
