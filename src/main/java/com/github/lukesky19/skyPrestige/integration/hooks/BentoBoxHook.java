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
package com.github.lukesky19.skyPrestige.integration.hooks;

import com.github.lukesky19.skyPrestige.integration.interfaces.Hook;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.blueprints.dataobjects.BlueprintBundle;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This class manages interfacing with the BentoBox plugin.
 */
public class BentoBoxHook implements Hook {
    private @NotNull IslandWorldManager islandWorldManager;
    private @NotNull IslandsManager islandsManager;
    private @NotNull BlueprintsManager blueprintsManager;
    private @NotNull AddonsManager addonsManager;
    private @NotNull PlayersManager playersManager;

    /**
     * Constructor
     */
    public BentoBoxHook() {
        initialize();
    }

    /**
     * Get the {@link BentoBox} instance and any other classes necessary.
     */
    @Override
    public void initialize() {
        @NotNull BentoBox bentoBox = BentoBox.getInstance();
        islandWorldManager = bentoBox.getIWM();
        islandsManager = bentoBox.getIslandsManager();
        blueprintsManager = bentoBox.getBlueprintsManager();
        addonsManager = bentoBox.getAddonsManager();
        playersManager = bentoBox.getPlayersManager();
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return true;
    }

    /**
     * Get the {@link IslandWorldManager}.
     * @return The {@link IslandWorldManager}.
     */
    public @NotNull IslandWorldManager getIslandWorldManager() {
        return islandWorldManager;
    }

    /**
     * Get the {@link IslandsManager}.
     * @return The {@link IslandsManager}.
     */
    public @NotNull IslandsManager getIslandsManager() {
        return islandsManager;
    }

    /**
     * Get the {@link PlayersManager}.
     * @return The {@link PlayersManager}
     */
    public @NotNull PlayersManager getPlayersManager() {
        return playersManager;
    }

    /**
     * Get the {@link BlueprintsManager}.
     * @return The {@link BlueprintsManager}
     */
    public @NotNull BlueprintsManager getBlueprintsManager() {
        return blueprintsManager;
    }

    /**
     * Attempt to a {@link Map} mapping blueprint names to {@link BlueprintBundle}s.
     * @param gameModeAddon The {@link GameModeAddon} to get blueprints for.
     * @return A {@link Map} mapping blueprint names to {@link BlueprintBundle}s or null.
     */
    public @NotNull Map<String, BlueprintBundle> getBlueprints(@NotNull GameModeAddon gameModeAddon) {
        return blueprintsManager.getBlueprintBundles(gameModeAddon);
    }

    /**
     * Attempt to get the {@link List} of {@link Island}s the player has.
     * @param playerId The player's {@link UUID}.
     * @return A {@link List} of {@link Island} or null.
     */
    public @NotNull List<Island> getIslands(@NotNull UUID playerId) {
        return islandsManager.getIslands(playerId);
    }

    /**
     * Get the active island for the {@link World} and player {@link UUID} provided.
     * @param world The {@link World}.
     * @param playerId The player's {@link UUID}.
     * @return An {@link Island}. May be null.
     */
    public @Nullable Island getIsland(@NotNull World world, @NotNull UUID playerId) {
        return islandsManager.getIsland(world, playerId);
    }

    /**
     * Attempt to get the island by its id.
     * @param islandId The island's id.
     * @return An {@link Optional} containing an {@link Island}.
     */
    public @NotNull Optional<Island> getIslandById(@NotNull String islandId) {
        return islandsManager.getIslandById(islandId, false);
    }

    /**
     * Attempt to get the island at the given location.
     * @param location The {@link Location}.
     * @return An {@link Optional} containing an {@link Island}.
     */
    public @NotNull Optional<Island> getIslandAtLocation(@NotNull Location location) {
        return islandsManager.getIslandAt(location);
    }

    /**
     * Get the {@link GameModeAddon} that governs the world provided.
     * @param world The {@link World}.
     * @return An {@link Optional} containing a {@link GameModeAddon}.
     */
    public @NotNull Optional<GameModeAddon> getGameModeAddon(@NotNull World world) {
        return addonsManager.getGameModeAddons().stream()
                .filter(gameModeAddon -> gameModeAddon.inWorld(world))
                .findFirst();
    }

    /**
     * Get the default island protection range for the world.
     * @param world The {@link World}.
     * @return The default island protection range.
     * @throws RuntimeException If the world is not managed by a {@link GameModeAddon}.
     */
    public int getDefaultProtectionRange(@NotNull World world) {
        return getGameModeAddon(world).map(gameModeAddon ->
                gameModeAddon.getWorldSettings().getIslandProtectionRange())
                .orElseThrow(() -> new RuntimeException("No default island size found."));
    }

    /**
     * Set the provided island's protection range to the provided size.
     * @param playerId The {@link UUID} of the player changing the island size.
     * @param island The island to set the island size for.
     * @param oldSize The old island's size.
     * @param newSize The new island's size to set.
     */
    public void setIslandSize(@NotNull UUID playerId, @NotNull Island island, int oldSize, int newSize) {
        // Set the island range
        island.setProtectionRange(newSize);

        // Call an island range change event
        IslandEvent.builder()
                .island(island).location(island.getCenter())
                .reason(IslandEvent.Reason.RANGE_CHANGE)
                .involvedPlayer(playerId)
                .admin(true)
                .protectionRange(newSize, oldSize)
                .build();
    }
}