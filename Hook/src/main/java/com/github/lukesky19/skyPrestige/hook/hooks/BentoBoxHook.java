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
package com.github.lukesky19.skyPrestige.hook.hooks;

import com.github.lukesky19.skyPrestige.hook.interfaces.Hook;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.blueprints.dataobjects.BlueprintBundle;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.BlueprintsManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.island.IslandCache;
import world.bentobox.bentobox.managers.island.NewIsland;

import java.io.IOException;
import java.util.*;

/**
 * This class manages interfacing with the BentoBox plugin.
 */
public class BentoBoxHook implements Hook {
    private final @NotNull ComponentLogger logger;
    private @Nullable BentoBox bentoBox;
    private @Nullable IslandsManager islandsManager;
    private @Nullable BlueprintsManager blueprintsManager;
    private @Nullable AddonsManager addonsManager;

    /**
     * Constructor
     * @param logger A {@link ComponentLogger} instance.
     */
    public BentoBoxHook(@NotNull ComponentLogger logger) {
        this.logger = logger;
    }

    /**
     * Get the {@link BentoBox} instance and any other classes necessary.
     */
    @Override
    public void initialize() {
        bentoBox = BentoBox.getInstance();
        islandsManager = bentoBox.getIslandsManager();
        blueprintsManager = bentoBox.getBlueprintsManager();
        addonsManager = bentoBox.getAddonsManager();
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return bentoBox != null && islandsManager != null && blueprintsManager != null && addonsManager != null;
    }

    /**
     * Attempt to reset an island.
     * @param user The {@link User} resetting the island.
     * @param gameModeAddon The {@link GameModeAddon} to reset the island for.
     * @param island The {@link Island} to reset.
     * @param blueprintName The blueprint name to use.
     * @return The new {@link Island} or null.
     */
    public @Nullable Island resetIsland(
            @NotNull User user,
            @NotNull GameModeAddon gameModeAddon,
            @NotNull Island island,
            @NotNull String blueprintName) {
        if(bentoBox == null || islandsManager == null || blueprintsManager == null) return null;

        try {
            NewIsland.Builder islandBuilder = NewIsland.builder();
            islandBuilder.player(user);
            islandBuilder.addon(gameModeAddon);
            islandBuilder.reason(IslandEvent.Reason.RESET);
            islandBuilder.oldIsland(island);
            islandBuilder.name(blueprintName);

            Island newIsland = islandBuilder.build();

            // Set the island owner and members for the new island
            newIsland.setOwner(island.getOwner());
            newIsland.setMembers(new HashMap<>(island.getMembers()));

            IslandCache islandCache = islandsManager.getIslandCache();

            // Make the new island associated with each island member and set the primary island as necessary.
            newIsland.getMemberSet()
                    .forEach(uuid -> {
                        islandCache.addPlayer(uuid, newIsland);

                        if(island.isPrimary(uuid)) islandCache.setPrimaryIsland(uuid, newIsland);
                    });

            IslandsManager.updateIsland(newIsland);

            return newIsland;
        } catch (IOException e) {
            // Log an error
            logger.error(AdventureUtil.deserialize("Failed to create new island. Error: " + e.getMessage()));

            // return null
            return null;
        }
    }

    /**
     * Attempt to delete an island.
     * @param playerId The {@link UUID} to attribute the island deletion to.
     * @param island The {@link Island} to delete.
     */
    public void deleteIsland(@NotNull UUID playerId, @NotNull Island island) {
        if(bentoBox == null || islandsManager == null || blueprintsManager == null) return;
        IslandCache islandCache = islandsManager.getIslandCache();

        // Set the owner of the old island to null
        island.setOwner(null);
        // Remove the members from the old island
        island.getMemberSet().forEach(uuid -> islandCache.removePlayer(island, uuid));
        // Update the island
        IslandsManager.updateIsland(island);

        // Delete the old island
        islandsManager.deleteIsland(island, true, playerId);
    }

    /**
     * Attempt to a {@link Map} mapping blueprint names to {@link BlueprintBundle}s.
     * @param gameModeAddon The {@link GameModeAddon} to get blueprints for.
     * @return A {@link Map} mapping blueprint names to {@link BlueprintBundle}s or null.
     */
    public @Nullable Map<String, BlueprintBundle> getBlueprints(@NotNull GameModeAddon gameModeAddon) {
        if(blueprintsManager == null) return null;

        return blueprintsManager.getBlueprintBundles(gameModeAddon);
    }

    /**
     * Attempt to get the {@link List} of {@link Island}s the player has.
     * @param playerId The player's {@link UUID}.
     * @return A {@link List} of {@link Island} or null.
     */
    public @Nullable List<Island> getIslands(@NotNull UUID playerId) {
        if(islandsManager == null) return null;

        return islandsManager.getIslands(playerId);
    }

    /**
     * Get the active island for the {@link World} and player {@link UUID} provided.
     * @param world The {@link World}.
     * @param playerId The player's {@link UUID}.
     * @return An {@link Optional} containing an {@link Island}.
     */
    public @NotNull Optional<Island> getIsland(@NotNull World world, @NotNull UUID playerId) {
        if(islandsManager == null) return Optional.empty();

        return Optional.ofNullable(islandsManager.getIsland(world, playerId));
    }

    /**
     * Attempt to get the island by its id.
     * @param islandId The island's id.
     * @return An {@link Optional} containing an {@link Island}.
     */
    public @NotNull Optional<Island> getIslandById(@NotNull String islandId) {
        if(islandsManager == null) return Optional.empty();

        return islandsManager.getIslandById(islandId);
    }

    /**
     * Attempt to get the island at the given location.
     * @param location The {@link Location}.
     * @return An {@link Optional} containing an {@link Island}.
     */
    public @NotNull Optional<Island> getIslandAtLocation(@NotNull Location location) {
        if(islandsManager == null) return Optional.empty();

        return islandsManager.getIslandAt(location);
    }

    /**
     * Get the {@link GameModeAddon} that governs the world provided.
     * @param world The {@link World}.
     * @return An {@link Optional} containing a {@link GameModeAddon}.
     */
    public @NotNull Optional<GameModeAddon> getGameModeAddon(@NotNull World world) {
        if(addonsManager == null) return Optional.empty();

        return addonsManager.getGameModeAddons().stream()
                .filter(gameModeAddon -> gameModeAddon.inWorld(world))
                .findFirst();
    }
}
