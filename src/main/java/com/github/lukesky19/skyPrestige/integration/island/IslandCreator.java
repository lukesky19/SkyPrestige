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

import com.github.lukesky19.skyPrestige.configuration.interfaces.IslandSettingsInterface;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
 * This class can be used to create a new {@link Island} from an old {@link Island}.
 */
public class IslandCreator {
    private final @NotNull ComponentLogger logger;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull IslandSettingsProcessor islandSettingsProcessor;

    private final @NotNull PlayersManager playersManager;
    private final @NotNull IslandsManager islandsManager;
    private final @NotNull BlueprintsManager blueprintsManager;

    private final @Nullable User user;
    private final @Nullable World world;
    private final @Nullable GameModeAddon gameModeAddon;
    private final @Nullable Island oldIsland;
    private final @Nullable IslandData islandData;
    private final @Nullable IslandSettingsInterface islandSettings;
    private final @Nullable Double requiredPrestigePoints;
    private final @Nullable Integer prestigeLevel;
    private final @NotNull String blueprintName;
    private final @NotNull NewIslandLocationStrategy locationStrategy;

    private @Nullable Island newIsland;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param islandSettingsProcessor A {@link IslandSettingsProcessor} instance.
     * @param builder An {@link Builder}.
     * @throws RuntimeException If the builder lacks the required data to create an island.
     */
    public IslandCreator(
            @NotNull SkyPlugin plugin,
            @NotNull DatabaseManager databaseManager,
            @NotNull HookManager hookManager,
            @NotNull IslandSettingsProcessor islandSettingsProcessor,
            @NotNull IslandCreator.Builder builder) throws RuntimeException {
        this.logger = plugin.getComponentLogger();
        this.databaseManager = databaseManager;
        this.islandSettingsProcessor = islandSettingsProcessor;

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        this.playersManager = bentoBoxHook.getPlayersManager();
        this.islandsManager = bentoBoxHook.getIslandsManager();
        this.blueprintsManager = bentoBoxHook.getBlueprintsManager();

        this.oldIsland = builder.oldIsland;
        this.user = builder.user;
        this.world = builder.world;
        this.gameModeAddon = builder.gameModeAddon;
        this.islandData = builder.oldIslandData;
        this.islandSettings = builder.islandSettings;
        this.requiredPrestigePoints = builder.requiredPrestigePoints;
        this.prestigeLevel= builder.prestigeLevel;
        this.blueprintName = builder.blueprintName;
        this.locationStrategy = new DefaultNewIslandLocationStrategy();

        // Check if the required data is invalid
        if(user == null || world == null || gameModeAddon == null || oldIsland == null
                || islandData == null || islandSettings == null) {
            throw new RuntimeException("The IslandCreator lacks required data to create a new island.");
        }

        // Create the new island
        createIsland();
    }

    /**
     * Get the created island.
     * @return The created island or null.
     */
    public @Nullable Island getCreatedIsland() {
        return newIsland;
    }

    /**
     * Create the new island.
     */
    public void createIsland() {
        if(user == null || oldIsland == null || islandData == null || islandSettings == null) return;

        @Nullable Location newIslandLocation = getReservedIslandCenter();
        if(newIslandLocation == null) {
            createIslandAtNextAvailableLocation();
        }

        if(newIsland == null) return;

        // Clean up old user data
        cleanUpUser();

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

        // Process IslandSettings
        if(requiredPrestigePoints != null && prestigeLevel != null) {
            islandSettingsProcessor.processIslandSettings(islandSettings, oldIsland, newIsland, islandData, requiredPrestigePoints, prestigeLevel);
        } else {
            islandSettingsProcessor.processIslandSettings(islandSettings, oldIsland, newIsland, islandData);
        }

        // Should NMS be used to paste the island's blueprint?
        boolean useNMS = !user.getWorld().equals(newIsland.getWorld())
                || (user.getLocation().distance(newIsland.getCenter()) >= Bukkit.getViewDistance() * 16D);
        // Generate the island blocks from the blueprint, then execute post-creation tasks
        blueprintsManager.paste(gameModeAddon, newIsland, blueprintName, this::postCreation, useNMS);

        // Save island
        IslandsManager.updateIsland(newIsland);
    }

    /**
     * Get the {@link Location} for the center of the user's old island.
     * @return A {@link Location} or null.
     */
    private @Nullable Location getReservedIslandCenter() {
        if(world == null || user == null) return null;

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
     * @throws RuntimeException If there is no next location for an island or the island creation fails.
     */
    private void createIslandAtNextAvailableLocation() throws RuntimeException {
        if(user == null) return;

        @Nullable Location islandLocation = this.locationStrategy.getNextLocation(world);
        if(islandLocation == null) {
            logger.error(AdventureUtil.deserialize("No unoccupied location was found to create an island at."));
            throw new RuntimeException("No unoccupied location was found to create an island at.");
        }

        // Create the new island
        newIsland = islandsManager.createIsland(islandLocation, user.getUniqueId());
        if(newIsland == null) {
            logger.error(AdventureUtil.deserialize("Failed to create a new island at unoccupied location."));
            throw new RuntimeException("Failed to create a new island at unoccupied location.");
        }
    }

    /**
     * Clean up any user data.
     */
    private void cleanUpUser() {
        if(user == null || oldIsland == null) return;

        oldIsland.getMemberSet().stream()
                .map(User::getInstance)
                .filter(User::isPlayer)
                .forEach(user -> {
                    // Reset Deaths
                    playersManager.setDeaths(world, user.getUniqueId(), 0);
                });
    }

    /**
     * This method handles updating island members homes, teleporting online players to the new island, and queuing teleports for offline players.
     */
    private void postCreation() {
        if(user == null || world == null || newIsland == null || oldIsland == null) return;
        List<User> islandMembers = newIsland.getMemberSet().stream().map(User::getInstance).filter(User::isPlayer).toList();
        List<User> onlineIslandMembers = islandMembers.stream().filter(user -> user.getPlayer().isOnline() && user.getPlayer().isConnected()).toList();

        // Update the home location for each island member.
        @Nullable Location newIslandSpawnPoint = newIsland.getSpawnPoint(World.Environment.NORMAL);
        if(newIslandSpawnPoint != null) {
            // Set the home location for each island member
            islandMembers.forEach(islandMemberUser -> newIsland.addHome("", newIslandSpawnPoint));

            // Old island bounds
            String worldName = oldIsland.getWorld().getName();
            int minX = Math.min(oldIsland.getMinX(), oldIsland.getMaxX());
            int maxX = Math.max(oldIsland.getMinX(), oldIsland.getMaxX());
            int minZ = Math.min(oldIsland.getMinZ(), oldIsland.getMaxZ());
            int maxZ = Math.max(oldIsland.getMinZ(), oldIsland.getMaxZ());

            // Teleport online island members to the new island that are on the old island
            onlineIslandMembers.forEach(islandMemberUser -> {
                Player islandMemberPlayer = islandMemberUser.getPlayer();

                if(isInBounds(islandMemberPlayer, worldName, minX, maxX, minZ, maxZ)) {
                    islandMemberPlayer.setVelocity(new Vector(0, 0, 0));
                    islandMemberPlayer.setFallDistance(0F);

                    islandMemberPlayer.teleportAsync(newIslandSpawnPoint, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            });

            @NotNull CompletableFuture<List<UUID>> playerIdsOnOldIslandFuture = databaseManager.getPlayerLogoutLocationsTables()
                    .getPlayerIdsWithinByBounds(worldName, minX, maxX, minZ, maxZ);

            // Queue any offline players within the old island's bounds to be teleported on login.
            playerIdsOnOldIslandFuture.thenAccept(playerIdsOnOldIsland -> playerIdsOnOldIsland.forEach(playerId -> {
                if(onlineIslandMembers.stream().noneMatch(user -> user.getUniqueId().equals(playerId))) {
                    databaseManager.getPlayerTeleportTable().insertPlayerIdAndIslandId(playerId, newIsland.getUniqueId());
                }
            }));
        }

        // Delete the old island
        islandsManager.deleteIsland(oldIsland, true, user.getUniqueId());
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
    private boolean isInBounds(@NotNull Player player, @NotNull String worldName, int minX, int maxX, int minZ, int maxZ) {
        @NotNull Location playerLocation = player.getLocation();
        @NotNull String playerWorldName = playerLocation.getWorld().getName();
        int playerX = playerLocation.getBlockX();
        int playerZ = playerLocation.getBlockZ();

        return playerWorldName.equals(worldName) && playerX >= minX && playerX <= maxX && playerZ >= minZ && playerZ <= maxZ;
    }

    /**
     * Create a new {@link IslandCreator.Builder}.
     * @param plugin A {@link SkyPlugin} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @return An {@link IslandCreator.Builder}.
     */
    public static @NotNull Builder builder(
            @NotNull SkyPlugin plugin,
            @NotNull DatabaseManager databaseManager,
            @NotNull HookManager hookManager,
            @NotNull IslandSettingsProcessor islandSettingsProcessor) {
        return new Builder(plugin, databaseManager, hookManager, islandSettingsProcessor);
    }

    /**
     * This class is used to build a new {@link Island}.
     */
    public static class Builder {
        private final @NotNull SkyPlugin plugin;
        private final @NotNull DatabaseManager databaseManager;
        private final @NotNull HookManager hookManager;
        private final @NotNull IslandSettingsProcessor islandSettingsProcessor;

        private @Nullable Island oldIsland;
        private @Nullable IslandData oldIslandData;
        private @Nullable User user;
        private @Nullable World world;
        private @NotNull String blueprintName = BlueprintsManager.DEFAULT_BUNDLE_NAME;
        private @Nullable GameModeAddon gameModeAddon;
        private @Nullable IslandSettingsInterface islandSettings;
        private @Nullable Double requiredPrestigePoints;
        private @Nullable Integer prestigeLevel;

        /**
         * Constructor
         * @param plugin A {@link SkyPlugin} instance.
         * @param databaseManager A {@link DatabaseManager} instance.
         * @param hookManager A {@link HookManager} instance.
         * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
         */
        public Builder(
                @NotNull SkyPlugin plugin,
                @NotNull DatabaseManager databaseManager,
                @NotNull HookManager hookManager,
                @NotNull IslandSettingsProcessor islandSettingsProcessor) {
            this.plugin = plugin;
            this.databaseManager = databaseManager;
            this.hookManager = hookManager;
            this.islandSettingsProcessor = islandSettingsProcessor;
        }

        /**
         * Set the old island.
         * @param oldIsland The old {@link Island}.
         * @return The {@link IslandCreator.Builder}.
         */
        public @NotNull Builder oldIsland(@NotNull Island oldIsland) {
            this.oldIsland = oldIsland;
            this.world = oldIsland.getWorld();
            return this;
        }

        /**
         * Set the old island data.
         * @param oldIslandData The old {@link IslandData}.
         * @return The {@link IslandCreator.Builder}.
         */
        public @NotNull Builder islandData(@NotNull IslandData oldIslandData) {
            this.oldIslandData = oldIslandData;
            return this;
        }

        /**
         * Set the user creating the island.
         * @param user The {@link User}.
         * @return The {@link IslandCreator.Builder}.
         */
        public @NotNull Builder user(@NotNull User user) {
            this.user = user;
            return this;
        }

        /**
         * Set the {@link GameModeAddon} to create the island with.
         * @param gameModeAddon The {@link GameModeAddon}.
         * @return The {@link IslandCreator.Builder}.
         */
        public @NotNull Builder gameModeAddon(@NotNull GameModeAddon gameModeAddon) {
            this.gameModeAddon = gameModeAddon;
            this.world = gameModeAddon.getOverWorld();
            return this;
        }

        /**
         * Set the blueprint name to create the island with.
         * @param blueprintName The blueprint name.
         * @return The {@link IslandCreator.Builder}.
         */
        public @NotNull Builder name(@NotNull String blueprintName) {
            this.blueprintName = blueprintName;
            return this;
        }

        /**
         * Set the {@link IslandSettingsInterface} to process for this island.
         * @param islandSettings The {@link IslandSettingsInterface}.
         * @return The {@link IslandCreator.Builder}.
         */
        public @NotNull Builder islandSettings(@NotNull IslandSettingsInterface islandSettings) {
            this.islandSettings = islandSettings;
            return this;
        }

        /**
         * Set the prestige points that were required to create the new island.
         * @param requiredPrestigePoints The required prestige points.
         * @return The {@link IslandCreator.Builder}.
         */
        public @NotNull Builder requiredPrestigePoints(double requiredPrestigePoints) {
            this.requiredPrestigePoints = requiredPrestigePoints;
            return this;
        }

        /**
         * Set the prestige level. This level will replace the existing level in the IslandData.
         * @param prestigeLevel The prestige level.
         * @return The {@link IslandCreator.Builder}.
         */
        public @NotNull Builder prestigeLevel(int prestigeLevel) {
            this.prestigeLevel = prestigeLevel;
            return this;
        }

        /**
         * Attempt to create the new island.
         * @return The created {@link Island} or null if creation failed.
         */
        public @Nullable Island build() {
            IslandCreator islandCreator = new IslandCreator(plugin, databaseManager, hookManager, islandSettingsProcessor, this);

            return islandCreator.getCreatedIsland();
        }
    }
}