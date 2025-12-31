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

import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class handles teleporting offline players who logged out on an island that was prestiged.
 */
public class TeleportationManager {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public TeleportationManager(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull HookManager hookManager) {
        this.logger = plugin.getComponentLogger();
        this.settingsManager = settingsManager;
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;
    }

    /**
     * If the player's UUID is stored for teleportation, teleport them to either their new island or the fallback location.
     * @param player The {@link Player} to teleport.
     */
    public void handleQueuedTeleports(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        @NotNull CompletableFuture<@Nullable String> islandIdFuture = databaseManager.getPlayerTeleportTable().getIslandId(playerId);
        islandIdFuture.thenAccept(islandId -> {
            if(!player.isOnline() || !player.isConnected()) return;
            if(islandId == null) return;
            @Nullable Settings settings = settingsManager.getConfiguration();
            if(settings == null) {
                logger.error(AdventureUtil.deserialize("Unable to teleport player " + player.getName() + " due to invalid plugin settings."));
                return;
            }

            BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
            if(!bentoBoxHook.isHooked()) return;
            Optional<Island> optionalIsland = bentoBoxHook.getIslandById(islandId);
            if(optionalIsland.isEmpty()) {
                logger.error(AdventureUtil.deserialize("Unable to teleport player " + player.getName() + " due to no island found for island id " + islandId + "."));
                return;
            }

            Island island = optionalIsland.get();
            @Nullable Location spawnPoint = island.getSpawnPoint(World.Environment.NORMAL);
            if(spawnPoint != null) {
                player.teleportAsync(spawnPoint);
            } else {
                logger.warn(AdventureUtil.deserialize("Unable to teleport player " + player.getName() + " due to now island spawn point set for island id " + islandId + "."));
            }

            databaseManager.getPlayerTeleportTable().deletePlayerIdAndIslandId(playerId);
        });
    }
}