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
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import dev.rosewood.rosestacker.event.SpawnerStackEvent;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * Listens for when a spawner is stacked on an island and increments prestige points.
 */
public class SpawnerStackListener extends PointsListener {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public SpawnerStackListener(
            @NonNull SkyPlugin plugin,
            @NonNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NonNull PrestigePointsManager prestigePointsManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager,
            @NonNull MultiplierManager multiplierManager) {
        super(plugin, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a spawner is stacked on an island and increments prestige points.
     * @param spawnerStackEvent A {@link SpawnerStackEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockStack(SpawnerStackEvent spawnerStackEvent) {
        // Config
        PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
        if(prestigePointsConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to process prestige points due to invalid prestige points config."));
            return;
        }

        // Player
        Player player = spawnerStackEvent.getPlayer();
        UUID playerId = player.getUniqueId();
        if(isPlayerInvalid(player, prestigePointsConfig)) return;

        // Island Check
        Island island = checkIsland(player, playerId);
        if(island == null) return;

        // IslandData check.
        IslandData islandData = checkIslandData(island);
        if(islandData == null) return;

        // Block
        StackedSpawner stackedSpawner = spawnerStackEvent.getStack();
        Block block = stackedSpawner.getBlock();
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return;

        // Block Data
        EntityType entityType = BlockUtils.getEntityType(hookManager.getHook(RoseStackerHook.class), block);

        // Amount
        int amount = prestigePointsConfig.accurateRoseStacker() ? spawnerStackEvent.getIncreaseAmount() : 1;

        // Points
        double points = prestigePointsManager.getBlockPoints(ActionType.BLOCK_PLACE, prestigePointsConfig.prestigePointsMapping().blockPlace(), blockType, entityType, null, null);
        if(points <= 0) return;

        // Add points
        islandData.addPrestigePoints((points * amount) * multiplierManager.getMultiplier(islandData));
    }
}