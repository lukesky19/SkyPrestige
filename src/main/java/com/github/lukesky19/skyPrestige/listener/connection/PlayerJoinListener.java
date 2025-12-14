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
package com.github.lukesky19.skyPrestige.listener.connection;

import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeExemptionManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeManager;
import com.github.lukesky19.skyPrestige.teleportation.TeleportationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Listens for when a player joins and creates or loads any data necessary for their islands.
 */
public class PlayerJoinListener implements Listener {
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull PrestigeManager prestigeManager;
    private final @NotNull PrestigeExemptionManager prestigeStatusManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull TeleportationManager teleportationManager;

    /**
     * Constructor
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param prestigeManager A {@link PrestigeManager} instance.
     * @param prestigeStatusManager A {@link PrestigeExemptionManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param teleportationManager A {@link TeleportationManager} instance.
     */
    public PlayerJoinListener(
            @NotNull DatabaseManager databaseManager,
            @NotNull PrestigeManager prestigeManager,
            @NotNull PrestigeExemptionManager prestigeStatusManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull TeleportationManager teleportationManager) {
        this.databaseManager = databaseManager;
        this.prestigeManager = prestigeManager;
        this.prestigeStatusManager = prestigeStatusManager;
        this.islandDataManager = islandDataManager;
        this.teleportationManager = teleportationManager;
    }

    /**
     * Listens for when a player joins and creates or loads any data necessary for their islands.
     * @param playerJoinEvent A {@link PlayerJoinEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        // Insert the player's uuid into the database if it doesn't exist
        databaseManager.getPlayerIdsTable().insertPlayerId(uuid);

        // Handle any queued teleports for the player.
        teleportationManager.handleQueuedTeleports(player);

        @NotNull CompletableFuture<Void> loadFuture = islandDataManager.loadDataByPlayerIdentifier(uuid);
        loadFuture.thenAccept(v -> {
            // Handle any prestiges that occurred while the player was offline
            prestigeManager.handleOfflinePrestiges(player);

            // Handle any prestige exemption changes that occurred while the player was offline
            prestigeStatusManager.handleOfflineStatusChanges(player);
        });
    }
}
