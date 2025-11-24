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
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player produces a bottle of water on an island and increments prestige points.
 */
public class PlayerBottleWaterListener extends PrestigePointsListener<PlayerInteractEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PlayerBottleWaterListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager);
    }

    /**
     * Listens for when a player produces a bottle of water from a cauldron on an island and increments prestige points.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR) // Cancelled events are purposely not ignored here.
    public void onPlayerBottleWater(PlayerInteractEvent playerInteractEvent) {
        process(playerInteractEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<PlayerInteractEvent> extractor() {
        return playerInteractEvent -> {
            Player player = playerInteractEvent.getPlayer();

            Action action = playerInteractEvent.getAction();
            if(!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) return null;

            @Nullable ItemStack itemStack = playerInteractEvent.getItem();
            if(itemStack == null || itemStack.isEmpty()) return null;
            ItemType itemType = itemStack.getType().asItemType();
            if(itemType == null) return null;
            if(!itemType.equals(ItemType.GLASS_BOTTLE)) return null;

            @Nullable RayTraceResult rayTraceResult = player.rayTraceBlocks(5, FluidCollisionMode.SOURCE_ONLY);
            if(rayTraceResult == null) return null;
            @Nullable Block block = rayTraceResult.getHitBlock();
            if(block == null) return null;
            @Nullable BlockType blockType = block.getType().asBlockType();
            if(blockType == null) return null;
            if(!blockType.equals(BlockType.WATER)) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setItemType(itemType);
            eventContext.setPotionType(PotionType.WATER);
            eventContext.setAmount(1);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull PlayerInteractEvent playerInteractEvent, @NotNull EventContext eventContext) {
        @Nullable ItemType itemType = eventContext.getItemType();
        if(itemType == null) return;
        @Nullable PotionType potionType = eventContext.getPotionType();
        if(potionType == null) return;

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getBottlePrestigePoints(itemType, potionType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * eventContext.getAmount());
    }
}