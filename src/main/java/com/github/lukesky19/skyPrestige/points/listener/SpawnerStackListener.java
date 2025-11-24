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
import com.github.lukesky19.skyPrestige.settings.Settings;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import dev.rosewood.rosestacker.event.SpawnerStackEvent;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a spawner is stacked on an island and increments prestige points.
 */
public class SpawnerStackListener extends PrestigePointsListener<SpawnerStackEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public SpawnerStackListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager);
    }

    /**
     * Listens for when a spawner is stacked on an island and increments prestige points.
     * @param spawnerStackEvent A {@link SpawnerStackEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockStack(SpawnerStackEvent spawnerStackEvent) {
        process(spawnerStackEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<SpawnerStackEvent> extractor() {
        return spawnerStackEvent -> {
            Player player = spawnerStackEvent.getPlayer();
            StackedSpawner stackedSpawner = spawnerStackEvent.getStack();
            Spawner spawner = stackedSpawner.getSpawner();
            @Nullable EntityType entityType = spawner.getSpawnedType();
            BlockType blockType = stackedSpawner.getBlock().getType().asBlockType();
            if(blockType == null) return null;
            int amount = spawnerStackEvent.getIncreaseAmount();

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setBlockType(blockType);
            eventContext.setEntityType(entityType);
            eventContext.setAmount(amount);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull SpawnerStackEvent spawnerStackEvent, @NotNull EventContext eventContext) {
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

        islandData.addPrestigePoints(prestigePoints * eventContext.getAmount());
    }
}