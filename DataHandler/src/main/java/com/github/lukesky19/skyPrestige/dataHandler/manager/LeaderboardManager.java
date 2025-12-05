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
package com.github.lukesky19.skyPrestige.dataHandler.manager;

import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.leaderboard.Position;
import com.github.lukesky19.skyPrestige.data.leaderboard.TopTen;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.IslandDataTable;
import com.github.lukesky19.skyPrestige.hook.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;

/**
 * This class manages obtaining data to display leaderboards and marking whether players are excluded from the leaderboard or not.
 */
public class LeaderboardManager {
    private final @NotNull Server server;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull HookManager hookManager;
    // Cached top ten from the database.
    private @NotNull TopTen databaseTopTen = new TopTen();
    private @NotNull TopTen topTen = new TopTen();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public LeaderboardManager(
            @NotNull SkyPlugin plugin,
            @NotNull IslandDataManager islandDataManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull HookManager hookManager) {
        this.server = plugin.getServer();
        this.islandDataManager = islandDataManager;
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;
    }

    /**
     * Update the cached top ten from the database.
     */
    public void updateDatabaseTopTen() {
        IslandDataTable islandDataTable = databaseManager.getIslandDataTable();
        islandDataTable.getTopTenByPrestigeLevelAndPointsNotExempt().thenAccept(topTen -> this.databaseTopTen = topTen);
    }

    /**
     * Calculate the top ten islands by prestige levels and prestige points.
     */
    public void updateTopTen() {
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return;

        @NotNull TopTen resultTopTen = new TopTen();
        // Get a list of all non-null database positions.
        @NotNull List<@NotNull Position> databasePositions = databaseTopTen.getPositions();

        // Get loaded island data
        @NotNull Map<String, IslandData> loadedIslandData = islandDataManager.getAllData();
        // Calculate the top ten positions from the online island data.
        @NotNull List<Position> onlineTopTenPositions = loadedIslandData.entrySet().stream()
                .filter(entry -> !entry.getValue().isExempt())
                .map(entry -> {
                    String islandId = entry.getKey();
                    IslandData islandData = entry.getValue();

                    Optional<Island> optionalIsland = bentoBoxHook.getIslandById(islandId);
                    if(optionalIsland.isPresent()) {
                        Island island = optionalIsland.get();

                        // Attempt to get the island's owner's name
                        @Nullable String ownerName = "Unknown Island Owner";
                        if(island.getOwner() != null) {
                            @Nullable Player player = server.getPlayer(island.getOwner());
                            if(player != null && player.isOnline() && player.isConnected()) {
                                ownerName = player.getName();
                            }
                        }

                        return new Position(entry.getKey(), ownerName, islandData.getPrestigeLevel(), islandData.getPrestigePoints());
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Position::prestigeLevel).reversed()
                        .thenComparing(Comparator.comparingDouble(Position::prestigePoints).reversed()))
                .limit(10)
                .toList();

        // Combine database and online positions
        List<Position> combinedPositions = new ArrayList<>();
        Set<String> seenIslands = new HashSet<>();
        // Add online positions if their position wasn't added already
        onlineTopTenPositions.forEach(position -> {
            if(seenIslands.add(position.islandId())) {
                combinedPositions.add(position);
            }
        });
        // Add database positions if their position wasn't added already
        databasePositions.forEach(position -> {
            if(seenIslands.add(position.islandId())) {
                combinedPositions.add(position);
            }
        });

        // Sort list to get final positions
        List<Position> finalPositions = combinedPositions.stream()
                .sorted(Comparator.comparingInt(Position::prestigeLevel).reversed()
                        .thenComparing(Comparator.comparingDouble(Position::prestigePoints).reversed()))
                .limit(10)
                .toList();

        // Set the positions in the resulting top ten
        resultTopTen.setPositions(finalPositions);

        this.topTen = resultTopTen;
    }

    /**
     * Get the {@link TopTen} islands by prestige level and points that are not exempt.
     * @return The {@link TopTen}.
     */
    public @NotNull TopTen getTopTenNotExempt() {
        return topTen;
    }

    /**
     * From the {@link TopTen}, get the {@link Position} at the positon number provided.
     * NOTE: Anything less than or equal to 0 or greater than 10 will always return null.
     * @param positionNumber The position number to get.
     * @return A {@link Position}. May be null.
     */
    public @Nullable Position getPositionAtPositionNumber(int positionNumber) {
        TopTen topTen = getTopTenNotExempt();

        return topTen.getPosition(positionNumber);
    }
}
