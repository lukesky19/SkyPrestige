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
package com.github.lukesky19.skyPrestige.placeholder;

import com.github.lukesky19.skyPrestige.core.util.number.NumberUtils;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.leaderboard.Position;
import com.github.lukesky19.skyPrestige.dataHandler.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.dataHandler.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.hook.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * This class supplies placeholders that other plugins can access using PlaceholderAPI.
 */
public class SkyPrestigeExpansion extends PlaceholderExpansion {
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull LeaderboardManager leaderboardManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull DecimalFormat decimalFormat = new DecimalFormat("#.##");

    /**
     * Constructor
     * @param islandDataManager A {@link IslandDataManager} instance.
     * @param leaderboardManager A {@link LeaderboardManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public SkyPrestigeExpansion(
            @NotNull IslandDataManager islandDataManager,
            @NotNull LeaderboardManager leaderboardManager,
            @NotNull HookManager hookManager) {
        this.islandDataManager = islandDataManager;
        this.leaderboardManager = leaderboardManager;
        this.hookManager = hookManager;
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
        String placeholder = params.toLowerCase();
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return null;

        switch(placeholder) {
            case "prestige_level" -> {
                UUID uuid = player.getUniqueId();

                // Get the Island at the player's location
                Optional<Island> optionalCurrentIsland = bentoBoxHook.getIslandAtLocation(player.getLocation());
                // If there is no island at the player's location, return the prestige level for the player's primary island.
                if(optionalCurrentIsland.isEmpty()) return getPrimaryIslandPrestigeLevel(player);

                Island currentIsland = optionalCurrentIsland.get();
                // If the player isn't a member or owner of the current island, return the prestige level for the player's primary island.
                if(!currentIsland.getMemberSet().contains(uuid)) return getPrimaryIslandPrestigeLevel(player);

                // Get the IslandData for the current island
                @Nullable IslandData islandData = islandDataManager.getData(currentIsland.getUniqueId());
                // If the island doesn't have any island data, return the prestige level for the player's primary island.
                if(islandData == null) return getPrimaryIslandPrestigeLevel(player);

                // Return the prestige level for the current island
                return String.valueOf(islandData.getPrestigeLevel());
            }

            case "prestige_points" -> {
                UUID uuid = player.getUniqueId();

                // Get the Island at the player's location
                Optional<Island> optionalCurrentIsland = bentoBoxHook.getIslandAtLocation(player.getLocation());
                // If there is no island at the player's location, return the prestige points for the player's primary island.
                if(optionalCurrentIsland.isEmpty()) return getPrimaryIslandPrestigePoints(player);

                Island currentIsland = optionalCurrentIsland.get();
                // If the player isn't a member or owner of the current island, return the prestige points for the player's primary island.
                if(!currentIsland.getMemberSet().contains(uuid)) return getPrimaryIslandPrestigePoints(player);

                // Get the IslandData for the current island
                @Nullable IslandData islandData = islandDataManager.getData(currentIsland.getUniqueId());
                // If the island doesn't have any island data, return the prestige points for the player's primary island.
                if(islandData == null) return getPrimaryIslandPrestigePoints(player);

                // Return the prestige points for the current island
                return String.valueOf(islandData.getPrestigePoints());
            }

            default -> {
                if(placeholder.startsWith("top_name")) {
                    String[] split = placeholder.split("_");
                    if(split.length != 3) return "";

                    @Nullable Integer positionNumber = getInteger(split[2]);
                    if(positionNumber == null) return "";
                    if(positionNumber > 10) return "";

                    @Nullable Position position = leaderboardManager.getPositionAtPositionNumber(positionNumber);
                    if(position == null) return "";

                    return position.ownerName();
                } else if(placeholder.startsWith("top_level")) {
                    String[] split = placeholder.split("_");
                    if(split.length != 3) return "";

                    @Nullable Integer positionNumber = getInteger(split[2]);
                    if(positionNumber == null) return "";
                    if(positionNumber > 10) return "";

                    @Nullable Position position = leaderboardManager.getPositionAtPositionNumber(positionNumber);
                    if(position == null) return "";

                    return String.valueOf(position.prestigeLevel());
                } else if(placeholder.startsWith("top_points")) {
                    String[] split = placeholder.split("_");
                    if(split.length != 3) return "";

                    @Nullable Integer positionNumber = getInteger(split[2]);
                    if(positionNumber == null) return "";
                    if(positionNumber > 10) return "";

                    @Nullable Position position = leaderboardManager.getPositionAtPositionNumber(positionNumber);
                    if(position == null) return "";

                    return String.valueOf(decimalFormat.format(position.prestigePoints()));
                }
            }
        }

        return null; // Placeholder is unknown by the Expansion
    }

    /**
     * Get the prestige level for the player's current or last known island.<br>
     * This will return 0 as a String if BentoBox isn't hooked into, an island can't be found, or the island has no IslandData.
     * @param player The {@link Player}.
     * @return The player's primary island's prestige level or 0.
     */
    private @NotNull String getPrimaryIslandPrestigeLevel(@NotNull Player player) {
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return "0";

        // Find the active island
        @Nullable Island primaryIsland = bentoBoxHook.getIsland(player.getWorld(), player.getUniqueId());
        // If no island is found, return 0
        if(primaryIsland == null) return "0";

        // Get the IslandData for the primary island
        @Nullable IslandData primaryIslandData = islandDataManager.getData(primaryIsland.getUniqueId());
        // If no island data was found, return 0
        if(primaryIslandData == null) return "0";

        // return the prestige level
        return String.valueOf(primaryIslandData.getPrestigeLevel());
    }

    /**
     * Get the prestige points for the player's current or last known island.<br>
     * This will return 0 as a String if BentoBox isn't hooked into, an island can't be found, or the island has no IslandData.
     * @param player The {@link Player}.
     * @return The player's primary island's prestige points or 0.
     */
    private @NotNull String getPrimaryIslandPrestigePoints(@NotNull Player player) {
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return "0";

        // Find the active island
        @Nullable Island primaryIsland = bentoBoxHook.getIsland(player.getWorld(), player.getUniqueId());
        // If no island is found, return 0
        if(primaryIsland == null) return "0";

        // Get the IslandData for the primary island
        @Nullable IslandData primaryIslandData = islandDataManager.getData(primaryIsland.getUniqueId());
        // If no island data was found, return 0
        if(primaryIslandData == null) return "0";

        // return the prestige points
        return NumberUtils.formatDecimal(primaryIslandData.getPrestigePoints());
    }

    /**
     * Get an {@link Integer} from a {@link String}.
     * @param text The {@link String} to parse.
     * @return The {@link Integer} or null.
     */
    private @Nullable Integer getInteger(@NotNull String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
