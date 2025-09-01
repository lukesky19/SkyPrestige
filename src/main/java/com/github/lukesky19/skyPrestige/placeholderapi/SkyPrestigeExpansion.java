/*
    SkyPlayTime tracks play time with options to not track play time for inactive (AFK) players.
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
package com.github.lukesky19.skyPrestige.placeholderapi;

import com.github.lukesky19.skyPrestige.data.IslandData;
import com.github.lukesky19.skyPrestige.manager.island.IslandDataManager;
import com.github.lukesky19.skyPrestige.util.NumberUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * This class supplies placeholders that other plugins can access using PlaceholderAPI.
 */
public class SkyPrestigeExpansion extends PlaceholderExpansion {
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull IslandsManager islandsManager;

    /**
     * Constructor
     * @param islandDataManager A {@link IslandDataManager} instance.
     */
    public SkyPrestigeExpansion(@NotNull IslandDataManager islandDataManager) {
        this.islandDataManager = islandDataManager;
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
    }

    /**
     * Get the author of the expansion.
     * @return The name of the author.
     */
    @Override
    public @NotNull String getAuthor() {
        return "lukeskywlker19";
    }

    /**
     * Get the identifier of the expansion.
     * @return The identifier for the expansion.
     */
    @Override
    public @NotNull String getIdentifier() {
        return "SkyPrestige";
    }

    /**
     * Get the version of the expansion.
     * @return The version of the expansion.
     */
    @Override
    public @NotNull String getVersion() {
        return "1.0.0.0";
    }

    /**
     * Set the persist value to always be true so that PlaceholderAPI will not unregister the expansion on reload.
     * @return Always returns true.
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * When a request is made to the expansion for a placeholder, attempt to parse the placeholder and return the result.
     * @param player The {@link OfflinePlayer} making the request.
     * @param params The placeholder.
     * @return The resolved placeholder text or null.
     * @throws RuntimeException Is thrown if a {@link InterruptedException} or {@link ExecutionException} occurs.
     */
    @Override
    public @Nullable String onPlaceholderRequest(@NotNull Player player, @NotNull String params) {
        switch(params.toLowerCase()) {
            case "prestige_level" -> {
                UUID uuid = player.getUniqueId();

                // Get the Island at the player's location
                Optional<Island> optionalCurrentIsland = islandsManager.getIslandAt(player.getLocation());
                // If there is no island at the player's location, return the prestige level for the player's primary island.
                if(optionalCurrentIsland.isEmpty()) return getPrimaryIslandPrestigeLevel(uuid);

                Island currentIsland = optionalCurrentIsland.get();
                // If the player isn't a member or owner of the current island, return the prestige level for the player's primary island.
                if(!currentIsland.getMemberSet().contains(uuid)) return getPrimaryIslandPrestigeLevel(uuid);

                // Get the IslandData for the current island
                @Nullable IslandData islandData = islandDataManager.getIslandData(currentIsland.getUniqueId());
                // If the island doesn't have any island data, return the prestige level for the player's primary island.
                if(islandData == null) return getPrimaryIslandPrestigeLevel(uuid);

                // Return the prestige level for the current island
                return String.valueOf(islandData.getPrestigeLevel());
            }

            case "prestige_points" -> {
                UUID uuid = player.getUniqueId();

                // Get the Island at the player's location
                Optional<Island> optionalCurrentIsland = islandsManager.getIslandAt(player.getLocation());
                // If there is no island at the player's location, return the prestige points for the player's primary island.
                if(optionalCurrentIsland.isEmpty()) return getPrimaryIslandPrestigePoints(uuid);

                Island currentIsland = optionalCurrentIsland.get();
                // If the player isn't a member or owner of the current island, return the prestige points for the player's primary island.
                if(!currentIsland.getMemberSet().contains(uuid)) return getPrimaryIslandPrestigePoints(uuid);

                // Get the IslandData for the current island
                @Nullable IslandData islandData = islandDataManager.getIslandData(currentIsland.getUniqueId());
                // If the island doesn't have any island data, return the prestige points for the player's primary island.
                if(islandData == null) return getPrimaryIslandPrestigePoints(uuid);

                // Return the prestige points for the current island
                return String.valueOf(islandData.getPrestigePoints());
            }
        }

        return null; // Placeholder is unknown by the Expansion
    }

    /**
     * Get the prestige level for the player's primary island.<br>
     * If there are multiple islands marked as primary, this will find the first one.<br>
     * This will return 0 as a String if the primary island can't be found or the IslandData for the primary island cannot be found.
     * @param uuid The {@link UUID} of the player.
     * @return The player's primary island's prestige level or 0.
     */
    private @NotNull String getPrimaryIslandPrestigeLevel(@NotNull UUID uuid) {
        // Find the first primary island
        Optional<Island> optionalPrimaryIsland = islandsManager.getIslands(uuid).stream().filter(island -> island.isPrimary(uuid)).findFirst();
        // If no primary island is found, return 0
        if(optionalPrimaryIsland.isEmpty()) return "0";
        // Get the primary island
        Island primaryIsland = optionalPrimaryIsland.get();

        // Get the IslandData for the primary island
        @Nullable IslandData primaryIslandData = islandDataManager.getIslandData(primaryIsland.getUniqueId());
        // If no island data was found, return 0
        if(primaryIslandData == null) return "0";

        // return the prestige level
        return String.valueOf(primaryIslandData.getPrestigeLevel());
    }

    /**
     * Get the prestige points for the player's primary island.<br>
     * If there are multiple islands marked as primary, this will find the first one.<br>
     * This will return 0 as a String if the primary island can't be found or the IslandData for the primary island cannot be found.
     * @param uuid The {@link UUID} of the player.
     * @return The player's primary island's prestige points or 0.
     */
    private @NotNull String getPrimaryIslandPrestigePoints(@NotNull UUID uuid) {
        // Find the first primary island
        Optional<Island> optionalPrimaryIsland = islandsManager.getIslands(uuid).stream().filter(island -> island.isPrimary(uuid)).findFirst();
        // If no primary island is found, return 0
        if(optionalPrimaryIsland.isEmpty()) return "0";
        // Get the primary island
        Island primaryIsland = optionalPrimaryIsland.get();

        // Get the IslandData for the primary island
        @Nullable IslandData primaryIslandData = islandDataManager.getIslandData(primaryIsland.getUniqueId());
        // If no island data was found, return 0
        if(primaryIslandData == null) return "0";

        // return the prestige points
        return NumberUtils.formatDecimal(primaryIslandData.getPrestigePoints());
    }
}
