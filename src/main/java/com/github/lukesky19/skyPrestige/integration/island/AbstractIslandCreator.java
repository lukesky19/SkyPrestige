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
package com.github.lukesky19.skyPrestige.integration.island;

import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.BlueprintsManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.managers.island.DefaultNewIslandLocationStrategy;
import world.bentobox.bentobox.managers.island.IslandCache;
import world.bentobox.bentobox.managers.island.NewIslandLocationStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class can be extended to create an island creator.
 */
public abstract class AbstractIslandCreator {
    /**
     * The plugin's {@link ComponentLogger}.
     */
    protected final @NonNull ComponentLogger logger;
    /**
     * The {@link DatabaseManager}.
     */
    protected final @NonNull DatabaseManager databaseManager;
    /**
     * The {@link IslandSettingsProcessor}.
     */
    protected final @NonNull IslandSettingsProcessor islandSettingsProcessor;

    /**
     * The {@link PlayersManager}.
     */
    protected final @NonNull PlayersManager playersManager;
    /**
     * The {@link IslandsManager}.
     */
    protected final @NonNull IslandsManager islandsManager;
    /**
     * The {@link BlueprintsManager}.
     */
    protected final @NonNull BlueprintsManager blueprintsManager;

    /**
     * The player who initiated the island creation.
     */
    protected final @NonNull Player player;
    /**
     * The associated user for the player.
     */
    protected final @NonNull User user;

    /**
     * The world the creation is occurring in.
     */
    protected final @NonNull World world;
    /**
     * The {@link GameModeAddon} the creation is occurring with.
     */
    protected final @NonNull GameModeAddon gameModeAddon;
    /**
     * The blueprint name/id to create the island with.
     */
    protected final @NonNull String blueprintName;
    /**
     * The location strategy to use when selecting a location to create the island at.
     */
    protected final @NonNull NewIslandLocationStrategy locationStrategy;

    /**
     * The old {@link Island}.
     */
    protected final @NonNull Island oldIsland;

    /**
     * The newly created {@link Island}.
     */
    protected @Nullable Island newIsland;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param player The {@link Player} creating the island.
     * @param world The {@link World} the island is being created in.
     * @param gameModeAddon the {@link GameModeAddon} the island is being created for.
     * @param blueprintName The blueprint name to use.
     * @param oldIsland The old {@link Island}.
     */
    public AbstractIslandCreator(
            @NonNull SkyPlugin plugin,
            @NonNull DatabaseManager databaseManager,
            @NonNull HookManager hookManager,
            @NonNull IslandSettingsProcessor islandSettingsProcessor,
            @NonNull Player player,
            @NonNull World world,
            @NonNull GameModeAddon gameModeAddon,
            @NonNull String blueprintName,
            @NonNull Island oldIsland) {
        this.logger = plugin.getComponentLogger();
        this.databaseManager = databaseManager;
        this.islandSettingsProcessor = islandSettingsProcessor;

        this.player = player;
        this.user = User.getInstance(player);

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        this.playersManager = bentoBoxHook.getPlayersManager();
        this.islandsManager = bentoBoxHook.getIslandsManager();
        this.blueprintsManager = bentoBoxHook.getBlueprintsManager();

        this.world = world;
        this.gameModeAddon = gameModeAddon;
        this.blueprintName = blueprintName;
        this.locationStrategy = new DefaultNewIslandLocationStrategy();

        this.oldIsland = oldIsland;
    }

    /**
     * Create the new island.
     * @return The created island or null.
     */
    public @Nullable Island createIsland() {
        Location newIslandLocation = getReservedIslandCenter();
        if(newIslandLocation == null) {
            createIslandAtNextAvailableLocation();
        }

        if(newIsland == null) return null;

        // Clean up old user data
        cleanUp();

        // Copy island owner
        newIsland.setOwner(oldIsland.getOwner());
        // Copy island members
        newIsland.setMembers(new HashMap<>(oldIsland.getMembers()));

        // Update IslandCache and primary islands for all island members
        IslandCache islandCache = islandsManager.getIslandCache();
        newIsland.getMemberSet()
                .forEach(uuid -> {
                    islandCache.addPlayer(uuid, newIsland);

                    if(oldIsland.isPrimary(uuid)) {
                        islandCache.setPrimaryIsland(uuid, newIsland);
                    }
                });

        // Execute logic defined before the blueprint is pasted
        beforeBlueprintPaste();

        // Should NMS be used to paste the island's blueprint?
        boolean useNMS = !user.getWorld().equals(newIsland.getWorld())
                || (user.getLocation().distance(newIsland.getCenter()) >= Bukkit.getViewDistance() * 16D);
        // Generate the island blocks from the blueprint, then execute post-creation tasks
        blueprintsManager.paste(gameModeAddon, newIsland, blueprintName, this::postCreation, useNMS);

        // Save island
        IslandsManager.updateIsland(newIsland);

        return newIsland;
    }

    /**
     * Update the homes for island members, teleport online island members to the new island, and queue players to be teleported on login if offline and on the old island.
     */
    protected void postCreation() {
        if(newIsland == null) return;

        List<User> islandMembers = newIsland.getMemberSet().stream().map(User::getInstance).filter(User::isPlayer).toList();
        List<User> onlineIslandMembers = islandMembers.stream().filter(user -> user.getPlayer().isOnline() && user.getPlayer().isConnected()).toList();

        // Update the home location for each island member.
        Location newIslandSpawnPoint = newIsland.getSpawnPoint(World.Environment.NORMAL);
        if(newIslandSpawnPoint != null) {
            // Set the home location for each island member
            islandMembers.forEach(_ -> newIsland.addHome("", newIslandSpawnPoint));

            // Old island bounds
            String worldName = oldIsland.getWorld().getName();
            int minX = Math.min(oldIsland.getMinProtectedX(), oldIsland.getMinProtectedX());
            int maxX = Math.max(oldIsland.getMinProtectedX(), oldIsland.getMaxProtectedX());
            int minZ = Math.min(oldIsland.getMinProtectedZ(), oldIsland.getMaxProtectedZ());
            int maxZ = Math.max(oldIsland.getMinProtectedZ(), oldIsland.getMaxProtectedZ());

            // Teleport online island members to the new island that are on the old island
            onlineIslandMembers.forEach(islandMemberUser -> {
                Player islandMemberPlayer = islandMemberUser.getPlayer();

                if(isInBounds(islandMemberPlayer, worldName, minX, maxX, minZ, maxZ)) {
                    islandMemberPlayer.setVelocity(new Vector(0, 0, 0));
                    islandMemberPlayer.setFallDistance(0F);

                    islandMemberPlayer.teleportAsync(newIslandSpawnPoint, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            });

            CompletableFuture<List<UUID>> playerIdsOnOldIslandFuture = databaseManager.getPlayerLogoutLocationsTables()
                    .getPlayerIdsWithinByBounds(worldName, minX, maxX, minZ, maxZ);

            // Queue any offline players within the old island's bounds to be teleported on login.
            playerIdsOnOldIslandFuture.thenAccept(playerIdsOnOldIsland -> playerIdsOnOldIsland.forEach(playerId -> {
                if(onlineIslandMembers.stream().noneMatch(user -> user.getUniqueId().equals(playerId))) {
                    databaseManager.getPlayerTeleportTable().insertPlayerIdAndIslandId(playerId, newIsland.getUniqueId());
                }
            }));
        }

        beforeDeletion();

        // Delete the old island
        islandsManager.deleteIsland(oldIsland, true, user.getUniqueId());

        afterDeletion();
    }

    /**
     * This method is run before the island blueprint is pasted in.
     */
    protected abstract void beforeBlueprintPaste();

    /**
     * This method is run after the island is fully completed, but before the old island is deleted.
     */
    protected abstract void beforeDeletion();

    /**
     * This method is run after the old island has been deleted.
     */
    protected abstract void afterDeletion();

    /**
     * Get the {@link Location} for the center of the user's old island.
     * @return A {@link Location} or null.
     */
    protected @Nullable Location getReservedIslandCenter() {
        // Check if the player has an island
        if(islandsManager.hasIsland(world, user)) {
            // Get the island
            newIsland = islandsManager.getIsland(world, user);

            // Check if the island is not null and is reserved.
            // Reserved refers to having no blocks except a single bedrock.
            if(newIsland != null && newIsland.isReserved()) {
                // Set as not reserved
                newIsland.setReserved(false);

                // Return island center
                return newIsland.getCenter();
            }
        }

        return null;
    }

    /**
     * Attempt to create an island at the next available location.
     */
    protected void createIslandAtNextAvailableLocation() {
        Location islandLocation = this.locationStrategy.getNextLocation(world);
        if(islandLocation == null) {
            logger.error(AdventureUtility.plain("No unoccupied location was found to create an island at."));
            return;
        }

        // Create the new island
        newIsland = islandsManager.createIsland(islandLocation, user.getUniqueId());
        if(newIsland == null) {
            logger.error(AdventureUtility.plain("Failed to create a new island at unoccupied location."));
        }
    }

    /**
     * Clean up any user data.
     */
    protected void cleanUp() {
        oldIsland.getMemberSet().stream()
                .map(User::getInstance)
                .filter(User::isPlayer)
                .forEach(user -> {
                    // Reset Deaths
                    playersManager.setDeaths(world, user.getUniqueId(), 0);
                });
    }

    /**
     * Is the player's location within the old island's bounds?
     * @param player The {@link Player}.
     * @param worldName The world name.
     * @param minX The min X.
     * @param maxX The max X.
     * @param minZ The min Z.
     * @param maxZ The max Z.
     * @return true if the player is in the bounds, or false if not.
     */
    protected boolean isInBounds(@NonNull Player player, @NonNull String worldName, int minX, int maxX, int minZ, int maxZ) {
        Location playerLocation = player.getLocation();
        String playerWorldName = playerLocation.getWorld().getName();
        int playerX = playerLocation.getBlockX();
        int playerZ = playerLocation.getBlockZ();

        return playerWorldName.equals(worldName) && playerX >= minX && playerX <= maxX && playerZ >= minZ && playerZ <= maxZ;
    }
}