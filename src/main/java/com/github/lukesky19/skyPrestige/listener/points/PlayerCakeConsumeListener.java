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
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player consumes a cake slice on an island and increments prestige points.
 */
public class PlayerCakeConsumeListener extends PrestigePointsListener<EntityChangeBlockEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PlayerCakeConsumeListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager);
    }

    /**
     * Checks when a player eats a cake slice.
     * @param entityChangeBlockEvent An {@link EntityChangeBlockEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCakeConsume(EntityChangeBlockEvent entityChangeBlockEvent) {
        process(entityChangeBlockEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<EntityChangeBlockEvent> extractor() {
        return entityChangeBlockEvent -> {
            if(!(entityChangeBlockEvent.getEntity() instanceof Player player)) return null;
            Block block = entityChangeBlockEvent.getBlock();
            @Nullable BlockType blockType = block.getType().asBlockType();
            if(blockType == null) return null;
            if(!blockType.equals(BlockType.CAKE)) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setItemType(ItemType.CAKE);
            eventContext.setAmount(1);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull EntityChangeBlockEvent entityChangeBlockEvent, @NotNull EventContext eventContext) {
        @Nullable ItemType itemType = eventContext.getItemType();
        if(itemType == null) return;

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getConsumePrestigePoints(itemType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * eventContext.getAmount());
    }
}