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
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player uses a cauldron on an island and increments prestige points.
 */
public class CauldronListener extends PrestigePointsListener<CauldronLevelChangeEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public CauldronListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager);
    }

    /**
     * Listens for when a player changes a cauldron's level on an island and increments prestige points.
     * @param cauldronLevelChangeEvent A {@link CauldronLevelChangeEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCauldronLevelChange(CauldronLevelChangeEvent cauldronLevelChangeEvent) {
        process(cauldronLevelChangeEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<CauldronLevelChangeEvent> extractor() {
        return cauldronLevelChangeEvent -> {
            Entity entity = cauldronLevelChangeEvent.getEntity();
            if(!(entity instanceof Player player)) return null;
            Block block = cauldronLevelChangeEvent.getBlock();
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setBlockType(blockType);
            eventContext.setAmount(1);

            switch (cauldronLevelChangeEvent.getReason()) {
                case BOTTLE_FILL -> {
                    eventContext.setItemType(ItemType.POTION);
                    eventContext.setPotionType(PotionType.WATER);
                }

                case BUCKET_FILL, BUCKET_EMPTY -> {
                    if(cauldronLevelChangeEvent.getBlock().getBlockData() instanceof Levelled levelled) {
                        ItemType itemType = levelled.getMaterial().asItemType();
                        if(itemType == null) return null;

                        eventContext.setItemType(itemType);
                    }
                }
            }

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull CauldronLevelChangeEvent cauldronLevelChangeEvent, @NotNull EventContext eventContext) {
        @Nullable ItemType itemType = eventContext.getItemType();
        if(itemType == null) return;
        @Nullable PotionType potionType = eventContext.getPotionType();

        @Nullable Double prestigePoints = null;
        switch(cauldronLevelChangeEvent.getReason()) {
            case BOTTLE_FILL -> {
                if(potionType != null) {
                    prestigePoints = settings.prestigePointsMapping().getBottlePrestigePoints(itemType, potionType);
                }
            }

            case BUCKET_FILL -> prestigePoints = settings.prestigePointsMapping().getFillPrestigePoints(itemType);

            case BUCKET_EMPTY -> prestigePoints = settings.prestigePointsMapping().getEmptyPrestigePoints(itemType);
        }

        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * eventContext.getAmount());
    }
}