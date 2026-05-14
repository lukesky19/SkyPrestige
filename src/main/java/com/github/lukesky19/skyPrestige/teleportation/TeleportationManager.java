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
package com.github.lukesky19.skyPrestige.teleportation;

import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class handles teleporting offline players who logged out on an island that was prestiged.
 */
public class TeleportationManager {
    private final @NonNull ComponentLogger logger;
    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public TeleportationManager(
            @NonNull SkyPlugin plugin,
            @NonNull DatabaseManager databaseManager,
            @NonNull HookManager hookManager) {
        this.logger = plugin.getComponentLogger();
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;
    }

    /**
     * If the player's UUID is stored for teleportation, teleport them to either their new island or the fallback location.
     * @param player The {@link Player} to teleport.
     */
    public void handleQueuedTeleports(@NonNull Player player) {
        UUID playerId = player.getUniqueId();
        CompletableFuture<@Nullable String> islandIdFuture = databaseManager.getPlayerTeleportTable().getIslandId(playerId);
        islandIdFuture.thenAccept(islandId -> {
            if(!player.isOnline() || !player.isConnected()) return;
            if(islandId == null) return;

            BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
            if(!bentoBoxHook.isHooked()) return;
            Optional<Island> optionalIsland = bentoBoxHook.getIslandById(islandId);
            if(optionalIsland.isEmpty()) {
                logger.warn(AdventureUtility.plain("Unable to teleport player " + player.getName() + " due to no island found for island id " + islandId + "."));
                return;
            }

            Island island = optionalIsland.get();
            Location playerLocation = player.getLocation();
            Location spawnPoint = island.getSpawnPoint(World.Environment.NORMAL);
            if(spawnPoint != null) {
                logger.info(AdventureUtility.plain("Teleporting player " + player.getName() + " to their new island's spawn point."));
                logger.info(AdventureUtility.plain("Previous Location: World: " + playerLocation.getWorld().getName() + " X: " + playerLocation.getBlockX() + " Y: " + playerLocation.getBlockY() + " Z: " + playerLocation.getBlockZ()));
                logger.info(AdventureUtility.plain("New Location: World: " + spawnPoint.getWorld().getName() + " X: " + spawnPoint.getBlockX() + " Y: " + spawnPoint.getBlockY() + " Z: " + spawnPoint.getBlockZ()));

                player.teleportAsync(spawnPoint);
            } else {
                logger.warn(AdventureUtility.plain("Unable to teleport player " + player.getName() + " due to now island spawn point set for island id " + islandId + "."));
            }

            databaseManager.getPlayerTeleportTable().deletePlayerIdAndIslandId(playerId);
        });
    }
}