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
package com.github.lukesky19.skyPrestige.data.manager;

import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.common.api.data.abstracts.HashMapDataManager;
import com.github.lukesky19.skylib.common.api.data.interfaces.IPersistentDataManager;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages island data.
 */
public class IslandDataManager extends HashMapDataManager<String, IslandData> implements IPersistentDataManager<String, IslandData> {
    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public IslandDataManager(@NonNull DatabaseManager databaseManager, @NonNull HookManager hookManager) {
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;
    }

    /**
     * Load islands data for all islands the {@link UUID} is attached to.
     * @param uuid The {@link UUID} of a player.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    public @NonNull CompletableFuture<Void> loadDataByPlayerIdentifier(@NonNull UUID uuid) {
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        List<Island> islandList = bentoBoxHook.getIslands(uuid);
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        islandList.forEach(island -> {
            String islandId = island.getUniqueId();
            // Insert the island id's into the database if it doesn't exist already
            databaseManager.getIslandIdsTable().insertIslandId(islandId);

            // Get the IslandData for the island id
            IslandData islandData = getData(islandId);
            // If not loaded (null), create and load the data for that island.
            if(islandData == null) {
                // Create the new IslandData
                IslandData newIslandData = new IslandData(islandId);
                setData(islandId, newIslandData);

                // Load any data from the database.
                futureList.add(databaseManager.getIslandDataTable().loadIslandData(islandId, newIslandData).thenRun(() -> {}));
            }
        });

        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }

    /**
     * Load the {@link IslandData} for the island id provided.
     * @param islandId The island id to load data for.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    @Override
    public @NonNull CompletableFuture<Void> loadData(@NonNull String islandId) {
        IslandData islandData = new IslandData(islandId);

        // Insert the island id's into the database if it doesn't exist already
        databaseManager.getIslandIdsTable().insertIslandId(islandId);

        setData(islandId, islandData);

        return databaseManager.getIslandDataTable().loadIslandData(islandId, islandData).thenRun(() -> {});
    }

    /**
     * Save the {@link IslandData} for the island id provided.
     * @param islandId The island id to save data for.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    @Override
    public @NonNull CompletableFuture<Void> saveData(@NonNull String islandId) {
        IslandData islandData = dataMap.get(islandId);
        if(islandData == null) return CompletableFuture.completedFuture(null);

        return databaseManager.getIslandDataTable().saveIslandData(islandId, islandData);
    }

    /**
     * Save the {@link IslandData} provided for the island id provided.
     * @param islandId The island id to save data for.
     * @param islandData The {@link IslandData} to save.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    @Override
    public @NonNull CompletableFuture<Void> saveData(@NonNull String islandId, @NonNull IslandData islandData) {
        return databaseManager.getIslandDataTable().saveIslandData(islandId, islandData);
    }

    /**
     * Save all island data to the database.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    @Override
    public @NonNull CompletableFuture<Void> saveData() {
        return databaseManager.getIslandDataTable().saveIslandData(dataMap);
    }
}