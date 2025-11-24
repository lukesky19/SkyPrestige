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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.settings.Settings;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.RoseStackerHook;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.listener.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.context.EventContextExtractor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player breaks a block on an island and increments prestige points.
 */
public class BlockBreakListener extends PrestigePointsListener<BlockBreakEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public BlockBreakListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager);
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
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull BlockBreakEvent blockBreakEvent, @NotNull EventContext eventContext) {
        @Nullable BlockType blockType = eventContext.getBlockType();
        if(blockType == null) return;
        @Nullable EntityType entityType = eventContext.getEntityType();

        @Nullable Double prestigePoints = null;
        if(entityType != null) {
            prestigePoints = settings.prestigePointsMapping().getBlockBreakPrestigePoints(blockType, entityType);
        } else {
            settings.prestigePointsMapping().getBlockBreakPrestigePoints(blockType);
        }

        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * eventContext.getAmount());
    }
}