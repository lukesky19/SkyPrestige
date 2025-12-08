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
package com.github.lukesky19.skyPrestige.data.data.island;

import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.blueprints.dataobjects.BlueprintBundle;
import world.bentobox.bentobox.database.objects.Island;

/**
 * This class contains the data to process for resetting an island.
 */
public class IslandResetData {
    private final @NotNull Player player;
    private final  @NotNull User user;
    private final  @NotNull Island oldIsland;
    private final  @NotNull IslandData oldIslandData;
    private final @NotNull GameModeAddon gameModeAddon;
    private @Nullable BlueprintBundle blueprint;
    // Prestige Only
    private final @Nullable PrestigeConfig prestigeConfig;
    private final @Nullable Integer prestigeLevel;

    /**
     * Constructor
     * @param player The {@link Player}.
     * @param user The {@link User}.
     * @param oldIsland The old {@link Island}.
     * @param oldIslandData The old {@link IslandData}.
     * @param gameModeAddon The {@link GameModeAddon}.
     * @param prestigeConfig The {@link PrestigeConfig}.
     * @param prestigeLevel The prestige level.
     */
    public IslandResetData(
            @NotNull Player player,
            @NotNull User user,
            @NotNull Island oldIsland,
            @NotNull IslandData oldIslandData,
            @NotNull GameModeAddon gameModeAddon,
            @NotNull PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        this.player = player;
        this.user = user;
        this.oldIsland = oldIsland;
        this.oldIslandData = oldIslandData;
        this.gameModeAddon = gameModeAddon;
        this.prestigeConfig = prestigeConfig;
        this.prestigeLevel = prestigeLevel;
    }

    /**
     * Constructor
     * @param player The {@link Player}.
     * @param user The {@link User}.
     * @param oldIsland The old {@link Island}.
     * @param oldIslandData The old {@link IslandData}.
     * @param gameModeAddon The {@link GameModeAddon}.
     */
    public IslandResetData(
            @NotNull Player player,
            @NotNull User user,
            @NotNull Island oldIsland,
            @NotNull IslandData oldIslandData,
            @NotNull GameModeAddon gameModeAddon) {
        this.player = player;
        this.user = user;
        this.oldIsland = oldIsland;
        this.oldIslandData = oldIslandData;
        this.gameModeAddon = gameModeAddon;
        this.prestigeConfig = null;
        this.prestigeLevel = null;
    }

    /**
     * Is the data for an island prestige?
     * @return true if so, or false.
     */
    public boolean isPrestige() {
        return prestigeConfig != null && prestigeLevel != null && prestigeLevel >= 0;
    }

    /**
     * Get the player resetting the island.
     * @return A {@link Player}.
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Get the user resetting the island.
     * @return The {@link User}.
     */
    public @NotNull User getUser() {
        return user;
    }

    /**
     * Get the old {@link Island}.
     * @return The {@link Island}.
     */
    public @NotNull Island getOldIsland() {
        return oldIsland;
    }

    /**
     * Get the {@link IslandData} for the old island.
     * @return The {@link IslandData}.
     */
    public @NotNull IslandData getOldIslandData() {
        return oldIslandData;
    }

    /**
     * Get the {@link GameModeAddon}.
     * @return The {@link GameModeAddon}.
     */
    public @NotNull GameModeAddon getGameModeAddon() {
        return gameModeAddon;
    }

    /**
     * Get the {@link BlueprintBundle}.
     * @return The {@link BlueprintBundle} or null.
     */
    public @Nullable BlueprintBundle getBlueprint() {
        return blueprint;
    }

    /**
     * Set the {@link BlueprintBundle}.
     * @param blueprint The {@link BlueprintBundle}.
     */
    public void setBlueprint(@NotNull BlueprintBundle blueprint) {
        this.blueprint = blueprint;
    }

    /**
     * Get the {@link PrestigeConfig}.
     * @return The {@link PrestigeConfig} or null.
     */
    public @Nullable PrestigeConfig getPrestigeConfig() {
        return prestigeConfig;
    }

    /**
     * Get the prestige level.
     * @return The prestige level or null.
     */
    public @Nullable Integer getPrestigeLevel() {
        return prestigeLevel;
    }
}
