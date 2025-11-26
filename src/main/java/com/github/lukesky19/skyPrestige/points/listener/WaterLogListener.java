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
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player water logs a b lock on an island and increments prestige points.
 */
public class WaterLogListener extends PrestigePointsListener<PlayerBucketEmptyEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public WaterLogListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player water logs a block on an island and increments prestige points.
     * @param playerBucketEmptyEvent A {@link PlayerBucketEmptyEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockWaterLogged(PlayerBucketEmptyEvent playerBucketEmptyEvent) {
        skyPrestige.getServer().getScheduler().runTaskLater(skyPrestige, () -> process(playerBucketEmptyEvent), 1L);
    }

    @Override
    protected @NotNull EventContextExtractor<PlayerBucketEmptyEvent> extractor() {
        return playerBucketEmptyEvent -> {
            Player player = playerBucketEmptyEvent.getPlayer();
            Block block = playerBucketEmptyEvent.getBlock();
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) return null;
            if(!(block.getBlockData() instanceof Waterlogged waterlogged)) return null;
            if(waterlogged.isWaterlogged()) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setBlockType(blockType);
            eventContext.setAmount(1);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull PlayerBucketEmptyEvent playerBucketEmptyEvent, @NotNull EventContext eventContext) {
        @Nullable BlockType blockType = eventContext.getBlockType();
        if(blockType == null) return;

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getWaterLogPrestigePoints(blockType);
        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}
