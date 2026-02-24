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
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.hooks.LMBQuestHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.processor.queued.QueuedSettingsProcessor;
import com.github.lukesky19.skyPrestige.teleportation.TeleportationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Listens for when a player joins and creates or loads any data necessary for their islands.
 */
public class PlayerJoinListener implements Listener {
    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull TeleportationManager teleportationManager;
    private final @NonNull HookManager hookManager;
    private final @NonNull QueuedSettingsProcessor queuedSettingsProcessor;

    /**
     * Constructor
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param teleportationManager A {@link TeleportationManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param queuedSettingsProcessor A {@link QueuedSettingsProcessor} instance.
     */
    public PlayerJoinListener(
            @NonNull DatabaseManager databaseManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull TeleportationManager teleportationManager,
            @NonNull HookManager hookManager,
            @NonNull QueuedSettingsProcessor queuedSettingsProcessor) {
        this.databaseManager = databaseManager;
        this.islandDataManager = islandDataManager;
        this.teleportationManager = teleportationManager;
        this.hookManager = hookManager;
        this.queuedSettingsProcessor = queuedSettingsProcessor;
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

        CompletableFuture<Void> loadFuture = islandDataManager.loadDataByPlayerIdentifier(uuid);
        loadFuture.thenAccept(v -> queuedSettingsProcessor.processQueuedSettings(player));

        // Load player quest data
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        LMBQuestHook lmbQuestHook = hookManager.getHook(LMBQuestHook.class);
        if(lmbQuestHook.isHooked()) {
            List<Island> playerIslands = bentoBoxHook.getIslands(uuid);
            playerIslands.forEach(island -> lmbQuestHook.loadPlayerData(island.getMemberSet()));
        }
    }
}