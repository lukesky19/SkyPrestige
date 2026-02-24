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
package com.github.lukesky19.skyPrestige.listener.points;

import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigePointsConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PointsListener;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigePointsManager;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.version.VersionUtil;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * Listens for when a player waxes an entity on an island and increments prestige points.
 */
public class PlayerWaxEntityListener extends PointsListener {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerWaxEntityListener(
            @NonNull SkyPlugin plugin,
            @NonNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NonNull PrestigePointsManager prestigePointsManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager,
            @NonNull MultiplierManager multiplierManager) {
        super(plugin, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player waxes an entity on an island and increments prestige points.
     * @param playerInteractEntityEvent A {@link PlayerInteractEntityEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWaxEntity(PlayerInteractEntityEvent playerInteractEntityEvent) {
        // Copper Golems were added in 1.21.9 so we ignore older versions.
        if(VersionUtil.getMajorVersion() < 21
                || (VersionUtil.getMajorVersion() == 21 && VersionUtil.getMinorVersion() < 9)) return;

        // Process 1 tick later to let the entity be waxed first (if waxed at all)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Config
            PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
            if(prestigePointsConfig == null) {
                logger.warn(AdventureUtil.deserialize("Unable to process prestige points due to invalid prestige points config."));
                return;
            }

            // Player
            Player player = playerInteractEntityEvent.getPlayer();
            if(!player.isOnline() || !player.isConnected()) return;
            UUID playerId = player.getUniqueId();
            if(isPlayerInvalid(player, playerId, prestigePointsConfig)) return;

            // Island Check
            Island island = checkIsland(player, playerId);
            if(island == null) return;

            // IslandData check.
            IslandData islandData = checkIslandData(island);
            if(islandData == null) return;

            // Entity
            Entity entity = playerInteractEntityEvent.getRightClicked();
            EntityType entityType = entity.getType();
            if(!(entity instanceof CopperGolem copperGolem)) return;
            if(!(copperGolem.getOxidizing().equals(CopperGolem.Oxidizing.waxed()))) return;

            // Item
            ItemType itemTypeUsed = player.getInventory().getItemInMainHand().getType().asItemType();
            if(itemTypeUsed == null) return;
            if(!itemTypeUsed.equals(ItemType.HONEYCOMB)) return;

            // Points
            double points = prestigePointsManager.getEntityPoints(ActionType.WAX_ENTITY, prestigePointsConfig.prestigePointsMapping().waxEntity(), entityType);
            if(points <= 0) return;

            // Add points
            islandData.addPrestigePoints((points * 1) * multiplierManager.getMultiplier(islandData));
        }, 1L);
    }
}