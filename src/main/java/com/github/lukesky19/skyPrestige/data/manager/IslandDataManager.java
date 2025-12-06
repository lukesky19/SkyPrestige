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
import com.github.lukesky19.skylib.api.common.abstracts.data.HashMapDataManager;
import com.github.lukesky19.skylib.api.common.interfaces.data.IPersistentDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages island data.
 */
public class IslandDataManager extends HashMapDataManager<String, IslandData> implements IPersistentDataManager<String, IslandData> {
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public IslandDataManager(@NotNull DatabaseManager databaseManager, @NotNull HookManager hookManager) {
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;
    }

    /**
     * Load islands data for all islands the {@link UUID} is attached to.
     * @param uuid The {@link UUID} of a player.
     */
    public void loadDataByPlayerIdentifier(@NotNull UUID uuid) {
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        @NotNull List<Island> islandList = bentoBoxHook.getIslands(uuid);

        islandList.forEach(island -> {
            String islandId = island.getUniqueId();
            // Insert the island id's into the database if it doesn't exist already
            databaseManager.getIslandIdsTable().insertIslandId(islandId);

            // Get the IslandData for the island id
            @Nullable IslandData islandData = getData(islandId);
            // If not loaded (null), create and load the data for that island.
            if(islandData == null) {
                // Create the new IslandData
                IslandData newIslandData = new IslandData();
                setData(islandId, newIslandData);

                // Load any data from the database.
                databaseManager.getIslandDataTable().loadIslandData(islandId, newIslandData);
            }
        });
    }

    /**
     * Load the {@link IslandData} for the island id provided.
     * @param islandId The island id to load data for.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    @Override
    public @NotNull CompletableFuture<Void> loadData(@NotNull String islandId) {
        IslandData islandData = new IslandData();

        setData(islandId, islandData);

        return databaseManager.getIslandDataTable().loadIslandData(islandId, islandData);
    }

    /**
     * Save the {@link IslandData} for the island id provided.
     * @param islandId The island id to save data for.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    @Override
    public @NotNull CompletableFuture<Void> saveData(@NotNull String islandId) {
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
    public @NotNull CompletableFuture<Void> saveData(String islandId, IslandData islandData) {
        return databaseManager.getIslandDataTable().saveIslandData(islandId, islandData);
    }

    /**
     * Save all island data to the database.
     * @return A {@link CompletableFuture} of type {@link Void} when complete.
     */
    @Override
    public @NotNull CompletableFuture<Void> saveData() {
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        dataMap.keySet().forEach(islandId -> futureList.add(saveData(islandId)));

        return CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
    }
}