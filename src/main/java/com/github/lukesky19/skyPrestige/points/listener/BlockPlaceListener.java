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
package com.github.lukesky19.skyPrestige.points.listener;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.RoseStackerHook;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.points.context.EventContext;
import com.github.lukesky19.skyPrestige.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.points.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.settings.Settings;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player places a block on an island and increments prestige points.
 */
public class BlockPlaceListener extends PrestigePointsListener<BlockPlaceEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public BlockPlaceListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player places a block on an island and increments prestige points.
     * @param blockPlaceEvent A {@link BlockPlaceEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        skyPrestige.getServer().getScheduler().runTaskLater(skyPrestige, () -> process(blockPlaceEvent), 1L);
    }

    @Override
    protected @NotNull EventContextExtractor<BlockPlaceEvent> extractor() {
        return blockPlaceEvent -> {
            Player player = blockPlaceEvent.getPlayer();
            if(!player.isOnline() || !player.isConnected()) return null;
            Block block = blockPlaceEvent.getBlock();
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setBlockType(blockType);
            eventContext.setAmount(1);

            RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);
            if(roseStackerHook.isHooked()) {
                if(!roseStackerHook.isBlockNotStacked(block)) return null;

                if(block.getState(false) instanceof Spawner spawner) {
                    if(spawner.getSpawnedType() != null) {
                        @Nullable EntityType entityType = spawner.getSpawnedType();

                        if(entityType != null) {
                            eventContext.setEntityType(entityType);
                        }
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
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull BlockPlaceEvent blockPlaceEvent, @NotNull EventContext eventContext) {
        @Nullable BlockType blockType = eventContext.getBlockType();
        if(blockType == null) return;
        @Nullable EntityType entityType = eventContext.getEntityType();

        @Nullable Double prestigePoints;
        if(entityType != null) {
            prestigePoints = settings.prestigePointsMapping().getBlockPlacePrestigePoints(blockType, entityType);
        } else {
            prestigePoints = settings.prestigePointsMapping().getBlockPlacePrestigePoints(blockType);
        }

        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}