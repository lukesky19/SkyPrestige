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
package com.github.lukesky19.skyPrestige.points.listener.brewing;

import com.github.lukesky19.skyPrestige.util.enums.SkyPrestigeKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

/**
 * Listens for when a brewing stands completes brewing and marks the slots as freshly brewed.
 */
public class FreshBrewListener implements Listener {
    /**
     * Constructor
     */
    public FreshBrewListener() {}

    /**
     * Marks a brewing stand as freshly brewed so that when potions are removed, island data can be updated.
     * @param brewEvent A {@link BrewEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFreshBrew(BrewEvent brewEvent) {
        Block block = brewEvent.getBlock();
        if(!(block.getState(false) instanceof BrewingStand brewingStand)) return;

        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(brewEvent.getBlock().getLocation());
        if(optionalIsland.isEmpty()) return;

        PersistentDataContainer pdc = brewingStand.getPersistentDataContainer();
        BrewerInventory brewerInventory = brewingStand.getInventory();

        ItemStack bottle1 = brewerInventory.getItem(0);
        ItemStack bottle2 = brewerInventory.getItem(1);
        ItemStack bottle3 = brewerInventory.getItem(2);

        if(bottle1 != null && !bottle1.isEmpty()) {
            @Nullable NamespacedKey key = SkyPrestigeKeys.getFreshlyBrewedKey(0);
            if(key != null) {
                pdc.set(key, PersistentDataType.BOOLEAN, true);
            }
        }

        if(bottle2 != null && !bottle2.isEmpty()) {
            @Nullable NamespacedKey key = SkyPrestigeKeys.getFreshlyBrewedKey(1);
            if(key != null) {
                pdc.set(key, PersistentDataType.BOOLEAN, true);
            }
        }

        if(bottle3 != null && !bottle3.isEmpty()) {
            @Nullable NamespacedKey key = SkyPrestigeKeys.getFreshlyBrewedKey(2);
            if(key != null) {
                pdc.set(key, PersistentDataType.BOOLEAN, true);
            }
        }

        brewingStand.update();
    }
}
