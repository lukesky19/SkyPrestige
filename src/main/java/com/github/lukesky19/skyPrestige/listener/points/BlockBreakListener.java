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
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player breaks a block on an island and increments prestige points.
 */
public class BlockBreakListener extends PrestigePointsListener<BlockBreakEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public BlockBreakListener(
            @NotNull SkyPlugin plugin,
            @NotNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player breaks a block on an island and increments prestige points.
     * @param blockBreakEvent A {@link BlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        process(blockBreakEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<BlockBreakEvent> extractor() {
        return blockBreakEvent -> {
            Block block = blockBreakEvent.getBlock();
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(blockBreakEvent.getPlayer());
            eventContext.setBlockType(blockType);
            eventContext.setAmount(1);

            RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);
            if(roseStackerHook.isHooked()) {
                if(!roseStackerHook.isBlockNotStacked(block)) return null;

                if(block.getState(false) instanceof Spawner spawner) {
                    @Nullable EntityType entityType = spawner.getSpawnedType();

                    if(entityType != null) {
                        eventContext.setEntityType(entityType);
                    }
                }
            } else {
                if(block.getState(false) instanceof Spawner spawner) {
                    if(spawner.getSpawnedType() != null) {
                        @Nullable EntityType entityType = spawner.getSpawnedType();

                        if(entityType != null) {
                            eventContext.setEntityType(entityType);
                        }
                    }
                }
            }

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull PrestigePointsConfig prestigePointsConfig, @NotNull IslandData islandData, @NotNull BlockBreakEvent blockBreakEvent, @NotNull EventContext eventContext) {
        @Nullable BlockType blockType = eventContext.getBlockType();
        if(blockType == null) return;
        @Nullable EntityType entityType = eventContext.getEntityType();

        @Nullable Double prestigePoints;
        if(entityType != null) {
            prestigePoints = prestigePointsConfig.prestigePointsMapping().getBlockBreakPrestigePoints(blockType, entityType);
        } else {
            prestigePoints = prestigePointsConfig.prestigePointsMapping().getBlockBreakPrestigePoints(blockType);
        }

        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}