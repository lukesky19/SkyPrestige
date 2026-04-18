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
import com.github.lukesky19.skyPrestige.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PointsListener;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigePointsManager;
import com.github.lukesky19.skyPrestige.util.block.BlockUtils;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * Listens for when a player water logs a b lock on an island and increments prestige points.
 */
public class WaterLogListener extends PointsListener {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public WaterLogListener(
            @NonNull SkyPlugin plugin,
            @NonNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NonNull PrestigePointsManager prestigePointsManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager,
            @NonNull MultiplierManager multiplierManager) {
        super(plugin, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player water logs a block on an island and increments prestige points.
     * @param playerBucketEmptyEvent A {@link PlayerBucketEmptyEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockWaterLogged(PlayerBucketEmptyEvent playerBucketEmptyEvent) {
        // Process 1 tick later to let the block be water logged first (if water logged at all)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Config
            PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
            if(prestigePointsConfig == null) {
                logger.warn(AdventureUtility.plain("Unable to process prestige points due to invalid prestige points config."));
                return;
            }

            // Player
            Player player = playerBucketEmptyEvent.getPlayer();
            if(!player.isOnline() || !player.isConnected()) return;
            UUID playerId = player.getUniqueId();
            if(isPlayerInvalid(player, prestigePointsConfig)) return;

            // Island Check
            Island island = checkIsland(player, playerId);
            if(island == null) return;

            // IslandData check.
            IslandData islandData = checkIslandData(island);
            if(islandData == null) return;

            // Block
            Block block = playerBucketEmptyEvent.getBlock();
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) return;

            // Block Data
            BlockData blockData = block.getBlockData();
            Boolean waterLogged = BlockUtils.getWaterLogged(blockData);
            if(waterLogged == null || !waterLogged) return;
            EntityType entityType = BlockUtils.getEntityType(hookManager.getHook(RoseStackerHook.class), block);
            Integer age = BlockUtils.getAge(blockData);

            // Points
            double points = prestigePointsManager.getBlockPoints(ActionType.WATER_LOG, prestigePointsConfig.prestigePointsMapping().waterLog(), blockType, entityType, age, true);
            if(points <= 0) return;

            // Add points
            islandData.addPrestigePoints((points * 1) * multiplierManager.getMultiplier(islandData));
        }, 1L);
    }
}