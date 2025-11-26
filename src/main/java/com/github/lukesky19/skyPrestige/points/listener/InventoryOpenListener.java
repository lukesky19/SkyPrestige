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
import org.bukkit.block.BlockType;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player opens an Inventory attached to a block on an Island and increments prestige points.
 */
public class InventoryOpenListener extends PrestigePointsListener<InventoryOpenEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public InventoryOpenListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player opens a Block's Inventory on an Island and increments prestige points.
     * @param inventoryOpenEvent An {@link InventoryOpenEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpened(InventoryOpenEvent inventoryOpenEvent) {
        process(inventoryOpenEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<InventoryOpenEvent> extractor() {
        return inventoryOpenEvent -> {
            if(!(inventoryOpenEvent.getPlayer() instanceof Player player)) return null;
            @Nullable InventoryHolder inventoryHolder = inventoryOpenEvent.getInventory().getHolder(false);
            if(inventoryHolder == null) return null;
            if(!(inventoryHolder instanceof Container container)) return null;
            BlockType blockType = container.getType().asBlockType();
            if(blockType == null) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setBlockType(blockType);
            eventContext.setAmount(1);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull InventoryOpenEvent inventoryOpenEvent, @NotNull EventContext eventContext) {
        @Nullable BlockType blockType = eventContext.getBlockType();
        if(blockType == null) return;

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getOpenPrestigePoints(blockType);
        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}