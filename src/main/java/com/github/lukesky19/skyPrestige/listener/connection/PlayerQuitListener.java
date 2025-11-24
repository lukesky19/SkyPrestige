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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;

/**
 * Listens for when a player leaves and then saves and unloads any data necessary for their islands.
 */
public class PlayerQuitListener implements Listener {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull IslandDataManager islandDataManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param islandDataManager An {@link IslandDataManager}.
     */
    public PlayerQuitListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull DatabaseManager databaseManager,
            @NotNull IslandDataManager islandDataManager) {
        this.skyPrestige = skyPrestige;
        this.databaseManager = databaseManager;
        this.islandDataManager = islandDataManager;
    }

    /**
     * Listens for when a player leaves and then saves and unloads any data necessary for their islands.
     * @param playerQuitEvent A {@link PlayerQuitEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        Location playerLocation = player.getLocation();
        BentoBox bentoBox = BentoBox.getInstance();
        List<Island> islandList = bentoBox.getIslandsManager().getIslands(uuid);

        islandList.stream()
                .filter(island -> !isIslandMemberOnline(island))
                .forEach(island -> {
                    String islandId = island.getUniqueId();
                    islandDataManager.saveIslandData(islandId)
                            .whenComplete((v, t) -> islandDataManager.removeIslandData(islandId));
                });

        databaseManager.getPlayerLogoutLocationsTables().setPlayerLogoutLocation(uuid, playerLocation);
    }

    /**
     * Checks if the island has any member online.
     * @param island The {@link Island} to check.
     * @return true if any island member is online, otherwise false.
     */
    private boolean isIslandMemberOnline(@NotNull Island island) {
        return island.getMemberSet().stream()
                .map(skyPrestige.getServer()::getPlayer)
                .anyMatch(memberPlayer -> memberPlayer != null && memberPlayer.isOnline() && memberPlayer.isConnected());
    }
}