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
package com.github.lukesky19.skyPrestige.placeholderapi;

import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.data.leaderboard.Position;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.data.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigePointsManager;
import com.github.lukesky19.skyPrestige.util.number.NumberUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * This class supplies placeholders that other plugins can access using PlaceholderAPI.
 */
public class SkyPrestigeExpansion extends PlaceholderExpansion {
    private final @NotNull PrestigePointsManager prestigePointsManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull LeaderboardManager leaderboardManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager A {@link IslandDataManager} instance.
     * @param leaderboardManager A {@link LeaderboardManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public SkyPrestigeExpansion(
            @NotNull PrestigePointsManager prestigePointsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull LeaderboardManager leaderboardManager,
            @NotNull HookManager hookManager) {
        this.prestigePointsManager = prestigePointsManager;
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
                // Get the player's island
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return "0";

                // Return the island's level
                return getIslandPrestigeLevel(island);
            }

            case "prestige_points" -> {
                // Get the player's island
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return "0";

                // Return the island's prestige points
                return getIslandPrestigePoints(island);
            }

            case "required_prestige_points" -> {
                // Get the player's island
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return "0";

                // Return the prestige points required to prestige
                return getIslandRequiredPrestigePoints(island);
            }

            case "progress_bar_minimessage" -> {
                StringBuilder bar = new StringBuilder();

                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return "";
                @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                if(islandData == null) return "";

                if(islandData.getRequiredPrestigePoints() == null) {
                    prestigePointsManager.recalculateRequiredPrestigePoints(island);
                }

                // Calculate current progress percentage
                double currentPercentage;
                if(islandData.getRequiredPrestigePoints() > 0) {
                    currentPercentage = Math.min(islandData.getPrestigePoints() / islandData.getRequiredPrestigePoints() * 100, 100);
                } else {
                    currentPercentage = 0.0;
                }

                for(int i = 1; i <= 10; i++) {
                    if(currentPercentage >= i * 10) {
                        bar.append("<green>|");
                    } else {
                        bar.append("<red>|");
                    }
                }

                return bar.toString();
            }

            case "progress_bar_legacy" -> {
                StringBuilder bar = new StringBuilder();

                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return "";
                @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                if(islandData == null) return "";

                if(islandData.getRequiredPrestigePoints() == null) {
                    prestigePointsManager.recalculateRequiredPrestigePoints(island);
                }

                // Calculate current progress percentage
                double currentPercentage;
                if(islandData.getRequiredPrestigePoints() > 0) {
                    currentPercentage = Math.min(islandData.getPrestigePoints() / islandData.getRequiredPrestigePoints() * 100, 100);
                } else {
                    currentPercentage = 0.0;
                }

                for(int i = 1; i <= 10; i++) {
                    if(currentPercentage >= i * 10) {
                        bar.append("&a|");
                    } else {
                        bar.append("&c|");
                    }
                }

                return bar.toString();
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

                    return NumberUtils.formatDecimal(position.prestigePoints());
                }
            }
        }

        return null; // Placeholder is unknown by the Expansion
    }

    /**
     * Get the player's island. They must be a member or owner of the island.<br>
     * First checks the island at the player's location (if any).<br>
     * Then checks the player's primary island (if any).<br>
     * Then gets the first island the player is a part of (if any).
     * @param player The {@link Player}.
     * @return The player's {@link Island} or null.
     */
    private @Nullable Island getIsland(@NotNull BentoBoxHook bentoBoxHook, @NotNull Player player) {
        UUID playerId = player.getUniqueId();

        // Get the Island at the player's location
        @Nullable Island island = getIslandAtPlayerLocation(bentoBoxHook, player, playerId);

        // If there is no island at the player's location, get their primary island for the world they are in
        if(island == null) island = getPrimaryIsland(bentoBoxHook, player, playerId);

        // If no primary island was found, attempt to get any island the player has.
        if(island == null) island = getAnyIsland(bentoBoxHook, playerId);

        // return the island
        return island;
    }

    /**
     * Get the {@link Island} where the player is standing and that they are a member or owner of.
     * @param bentoBoxHook A {@link BentoBoxHook}.
     * @param player The {@link Player}.
     * @param playerId The player's {@link UUID}.
     * @return An {@link Island} or null.
     */
    private @Nullable Island getIslandAtPlayerLocation(@NotNull BentoBoxHook bentoBoxHook, @NotNull Player player, @NotNull UUID playerId) {
        // Get the Island at the player's location
        @Nullable Island island = bentoBoxHook.getIslandAtLocation(player.getLocation()).orElse(null);

        // If the island is not null, check if they are the owner or a member
        if(island != null) {
            if((island.getOwner() != null && island.getOwner().equals(playerId)) || island.getMemberSet().contains(playerId)) {
                return island;
            }
        }

        return null;
    }

    /**
     * Get the player's primary {@link Island} for the world they are in.
     * @param bentoBoxHook A {@link BentoBoxHook}.
     * @param player The {@link Player}.
     * @param playerId The player's {@link UUID}.
     * @return An {@link Island} or null.
     */
    private @Nullable Island getPrimaryIsland(@NotNull BentoBoxHook bentoBoxHook, @NotNull Player player, @NotNull UUID playerId) {
        // Get the Island at the player's location
        @Nullable Island island = bentoBoxHook.getIsland(player.getWorld(), player.getUniqueId());

        // If the island is not null, check if they are the owner or a member
        if(island != null) {
            if((island.getOwner() != null && island.getOwner().equals(playerId)) || island.getMemberSet().contains(playerId)) {
                return island;
            }
        }

        return null;
    }

    /**
     * Get any {@link Island} the player owns or is a member of.
     * @param bentoBoxHook A {@link BentoBoxHook}.
     * @param playerId The player's {@link UUID}.
     * @return An {@link Island} or null.
     */
    private @Nullable Island getAnyIsland(@NotNull BentoBoxHook bentoBoxHook, @NotNull UUID playerId) {
        // Get the player's islands
        @NotNull List<Island> islandList = bentoBoxHook.getIslands(playerId);

        // Get the first island they own (if any)
        Optional<Island> ownedIsland = islandList.stream()
                .filter(island -> island.getOwner() != null && island.getOwner().equals(playerId))
                .findFirst();

        // Get the first island they are a member of (if any)
        Optional<Island> memberIsland = islandList.stream()
                .filter(island -> island.getMemberSet().contains(playerId))
                .findFirst();

        return ownedIsland.orElseGet(() -> memberIsland.orElse(null));
    }

    /**
     * Get the prestige level for the island provided.<br>
     * This will return 0 if the island has no IslandData.
     * @param island The {@link Island}.
     * @return The island's prestige level or 0.
     */
    private @NotNull String getIslandPrestigeLevel(@NotNull Island island) {
        // Get the IslandData for the island
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        // If no island data was found, return 0
        if(islandData == null) return "0";

        // return the prestige level
        return String.valueOf(islandData.getPrestigeLevel());
    }

    /**
     * Get the prestige points for the island provided.<br>
     * This will return 0 if the island has no IslandData.
     * @param island The {@link Island}.
     * @return The island's prestige points or 0.
     */
    private @NotNull String getIslandPrestigePoints(@NotNull Island island) {
        // Get the IslandData for the island
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        // If no island data was found, return 0
        if(islandData == null) return "0";

        // return the prestige points
        return NumberUtils.formatDecimal(islandData.getPrestigePoints());
    }

    /**
     * Get the prestige points required for the island to prestige.<br>
     * This will return 0 if any errors occur.
     * @param island The {@link Island}.
     * @return The player's primary island's prestige points or 0.
     */
    private @NotNull String getIslandRequiredPrestigePoints(@NotNull Island island) {
        // Get the IslandData for the island
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        // If no island data was found, return 0
        if(islandData == null) return "0";
        if(islandData.isPrestigeExempt()) return "0";

        // Recalculate the required prestige points of not cached already
        if(islandData.getRequiredPrestigePoints() == null) {
            prestigePointsManager.recalculateRequiredPrestigePoints(island);
        }

        // return the goal prestige points
        return NumberUtils.formatDecimal(islandData.getRequiredPrestigePoints());
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
