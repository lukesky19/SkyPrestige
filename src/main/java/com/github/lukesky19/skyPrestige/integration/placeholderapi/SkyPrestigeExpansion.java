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
package com.github.lukesky19.skyPrestige.integration.placeholderapi;

import com.github.lukesky19.skyPrestige.configuration.data.placeholder.PlaceholderConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.PlaceholderConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.data.leaderboard.Position;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.data.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigePointsManager;
import com.github.lukesky19.skyPrestige.util.number.NumberUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
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
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull PrestigePointsManager prestigePointsManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull LeaderboardManager leaderboardManager;
    private final @NotNull MultiplierManager multiplierManager;
    private final @NotNull PlaceholderConfigManager placeholderConfigManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param settingsManager A {@link SettingsManager} instance.
     * @param placeholderConfigManager A {@link PlaceholderConfigManager} instance.
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager A {@link IslandDataManager} instance.
     * @param leaderboardManager A {@link LeaderboardManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public SkyPrestigeExpansion(
            @NotNull SettingsManager settingsManager,
            @NotNull PlaceholderConfigManager placeholderConfigManager,
            @NotNull PrestigePointsManager prestigePointsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull LeaderboardManager leaderboardManager,
            @NotNull MultiplierManager multiplierManager,
            @NotNull HookManager hookManager) {
        this.settingsManager = settingsManager;
        this.prestigePointsManager = prestigePointsManager;
        this.islandDataManager = islandDataManager;
        this.leaderboardManager = leaderboardManager;
        this.multiplierManager = multiplierManager;
        this.placeholderConfigManager = placeholderConfigManager;
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
        @NotNull PlaceholderConfig placeholderConfig = placeholderConfigManager.getConfiguration();
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return null;

        switch(placeholder) {
            case "prestige_level" -> {
                // Get the player's island
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return placeholderConfig.prestigeLevel().noIslandText();
                // Get the IslandData
                @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                if(islandData == null) return placeholderConfig.prestigeLevel().noIslandText();
                if(islandData.isPrestigeExempt()) return placeholderConfig.prestigeLevel().optedOutText();

                // Return the island's prestige level
                return String.valueOf(islandData.getPrestigeLevel());
            }

            case "prestige_points" -> {
                // Get the player's island
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return placeholderConfig.prestigePoints().noIslandText();
                // Get the IslandData
                @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                if(islandData == null) return placeholderConfig.prestigePoints().noIslandText();
                if(islandData.isPrestigeExempt()) return placeholderConfig.prestigePoints().optedOutText();

                // Return the island's prestige points
                return NumberUtils.formatDecimal(islandData.getPrestigePoints());
            }

            case "required_prestige_points" -> {
                // Get the player's island
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return placeholderConfig.requiredPrestigePoints().noIslandText();
                // Get the IslandData
                @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                if(islandData == null) return placeholderConfig.requiredPrestigePoints().noIslandText();
                if(islandData.isPrestigeExempt()) return placeholderConfig.requiredPrestigePoints().optedOutText();

                // Recalculate the required prestige points of not cached already
                if(islandData.getRequiredPrestigePoints() == null) {
                    prestigePointsManager.recalculateRequiredPrestigePoints(island);
                }

                // Return the prestige points required to prestige
                return NumberUtils.formatDecimal(islandData.getRequiredPrestigePoints());
            }

            case "progress_bar" -> {
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return placeholderConfig.progressBar().noIslandText();
                @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                if(islandData == null) return placeholderConfig.progressBar().noIslandText();
                if(islandData.isPrestigeExempt()) return placeholderConfig.progressBar().optedOutText();

                if(islandData.getRequiredPrestigePoints() == null) {
                    prestigePointsManager.recalculateRequiredPrestigePoints(island);
                }

                int progressBarSize = settingsManager.getConfiguration() != null ?
                        settingsManager.getConfiguration().progressBarSize() : 10;
                StringBuilder bar = new StringBuilder();

                // Calculate current progress percentage
                double currentPercentage = islandData.getRequiredPrestigePoints() > 0 ?
                        Math.min((islandData.getPrestigePoints() / islandData.getRequiredPrestigePoints()) * 100, 100.0) : 0;

                // Calculate the number of filled bars
                int filledBars = (int) (currentPercentage / (100.0 / progressBarSize));

                for (int i = 0; i < progressBarSize; i++) {
                    if(i < filledBars) {
                        bar.append(placeholderConfig.progressBar().filledBarText());
                    } else {
                        bar.append(placeholderConfig.progressBar().emptyBarText());
                    }
                }

                return bar.toString();
            }

            case "progress_bar_legacy" -> {
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return placeholderConfig.legacyProgressBar().noIslandText();
                @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                if(islandData == null) return placeholderConfig.legacyProgressBar().noIslandText();
                if(islandData.isPrestigeExempt()) return placeholderConfig.legacyProgressBar().optedOutText();

                if(islandData.getRequiredPrestigePoints() == null) {
                    prestigePointsManager.recalculateRequiredPrestigePoints(island);
                }

                int progressBarSize = settingsManager.getConfiguration() != null ?
                        settingsManager.getConfiguration().progressBarSize() : 10;
                StringBuilder bar = new StringBuilder();

                // Calculate current progress percentage
                double currentPercentage = islandData.getRequiredPrestigePoints() > 0 ?
                        Math.min((islandData.getPrestigePoints() / islandData.getRequiredPrestigePoints()) * 100, 100.0) : 0;

                // Calculate the number of filled bars
                int filledBars = (int) (currentPercentage / (100.0 / progressBarSize));

                for (int i = 0; i < progressBarSize; i++) {
                    if(i < filledBars) {
                        bar.append(placeholderConfig.legacyProgressBar().filledBarText());
                    } else {
                        bar.append(placeholderConfig.legacyProgressBar().emptyBarText());
                    }
                }

                return bar.toString();
            }

            case "multiplier" -> {
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return placeholderConfig.multiplier().noIslandText();
                @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                if(islandData == null) return placeholderConfig.multiplier().noIslandText();

                return String.valueOf(multiplierManager.getMultiplier(island));
            }

            case "server_multiplier" -> {
                return String.valueOf(multiplierManager.getServerMultiplier());
            }

            case "server_multiplier_time" -> {
                return AdventureUtil.serialize(multiplierManager.getTimePlaceholder(placeholderConfig.multiplier().timeFormat(), multiplierManager.getServerMultiplierTime()));
            }

            case "server_multiplier_time_raw" -> {
                return String.valueOf(multiplierManager.getServerMultiplierTime());
            }

            case "island_multiplier" -> {
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return "0.0";

                return String.valueOf(multiplierManager.getIslandMultiplier(island));
            }

            case "island_multiplier_time" -> {
                @Nullable Island island = getIsland(bentoBoxHook, player);
                if(island == null) return "0";

                return AdventureUtil.serialize(multiplierManager.getTimePlaceholder(placeholderConfig.multiplier().timeFormat(), multiplierManager.getServerMultiplierTime()));
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
