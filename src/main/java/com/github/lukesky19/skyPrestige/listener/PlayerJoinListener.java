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
package com.github.lukesky19.skyPrestige.listener;

import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.manager.island.IslandDataManager;
import com.github.lukesky19.skyPrestige.manager.prestige.PrestigeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Listens for when a player joins and creates or loads any data necessary for their islands.
 */
public class PlayerJoinListener implements Listener {
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull PrestigeManager prestigeManager;
    private final @NotNull IslandDataManager islandDataManager;

    /**
     * Constructor
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param prestigeManager A {@link PrestigeManager} instance.
     * @param islandDataManager An {@link IslandDataManager}.
     */
    public PlayerJoinListener(
            @NotNull DatabaseManager databaseManager,
            @NotNull PrestigeManager prestigeManager,
            @NotNull IslandDataManager islandDataManager) {
        this.databaseManager = databaseManager;
        this.prestigeManager = prestigeManager;
        this.islandDataManager = islandDataManager;
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

        islandDataManager.loadIslandData(uuid);

        // Handle any prestiges that occurred while the player was offline
        CompletableFuture<List<Integer>> future = databaseManager.getOfflinePrestigeTable().getPrestigeLevels(uuid);
        future.thenAccept(list -> prestigeManager.handleOfflinePrestige(player, uuid, list));

        prestigeManager.handleQueuedTeleports(player, uuid);
    }
}
