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
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.listener.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.context.EventContextExtractor;
import dev.rosewood.rosestacker.event.BlockStackEvent;
import dev.rosewood.rosestacker.stack.StackedBlock;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a block is stacked on an island and increments prestige points.
 */
public class BlockStackListener extends PrestigePointsListener<BlockStackEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public BlockStackListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager);
    }
    /**
     * Listens for when a block is stacked on an island and increments prestige points.
     * @param blockStackEvent A {@link BlockStackEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockStack(BlockStackEvent blockStackEvent) {
        process(blockStackEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<BlockStackEvent> extractor() {
        return blockStackEvent -> {
            Player player = blockStackEvent.getPlayer();
            StackedBlock stackedBlock = blockStackEvent.getStack();
            BlockType blockType = stackedBlock.getBlock().getType().asBlockType();
            if(blockType == null) return null;
            int amount = blockStackEvent.getIncreaseAmount();

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setBlockType(blockType);
            eventContext.setAmount(amount);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull BlockStackEvent blockStackEvent, @NotNull EventContext eventContext) {
        @Nullable BlockType blockType = eventContext.getBlockType();
        if(blockType == null) return;

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getBlockPlacePrestigePoints(blockType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * eventContext.getAmount());
    }
}