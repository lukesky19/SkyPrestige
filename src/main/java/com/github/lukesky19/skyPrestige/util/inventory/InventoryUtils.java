/*
    SkyPrestige allows players to reset their Island after meeting certain requirements to unlock rewards.
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
package com.github.lukesky19.skyPrestige.util.inventory;

import com.github.lukesky19.skyPrestige.integration.hooks.ExcellentCratesHook;
import com.github.lukesky19.skyPrestige.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skyPrestige.integration.hooks.SkyHoppersHook;
import com.github.lukesky19.skyPrestige.integration.hooks.SkySellWandsHook;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/**
 * This method contains utilities related to calculating and or removing items in an inventory.
 */
public class InventoryUtils {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public InventoryUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Gets the amount of items inside an inventory that match the {@link ItemStack} provided.
     * @param roseStackerHook A {@link RoseStackerHook} instance.
     * @param skyHoppersHook A {@link SkyHoppersHook} instance.
     * @param skySellWandsHook A {@link SkySellWandsHook} instance.
     * @param excellentCratesHook An {@link ExcellentCratesHook} instance.
     * @param itemStack The {@link ItemStack} to get the amount inside the inventory for.
     * @param inventory The {@link Inventory} to check for items.
     * @return The amount of items inside the provided {@link Inventory}.
     */
    public static int getItemAmountInInventory(
            @NonNull RoseStackerHook roseStackerHook,
            @NonNull SkyHoppersHook skyHoppersHook,
            @NonNull SkySellWandsHook skySellWandsHook,
            @NonNull ExcellentCratesHook excellentCratesHook,
            @NonNull ItemStack itemStack,
            @NonNull Inventory inventory) {
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return 0;

        return Arrays.stream(inventory.getContents())
                .filter(invItem -> {
                    if(invItem == null || invItem.isEmpty()) return false;
                    if(!itemStack.isSimilar(invItem)) return false;
                    if(skyHoppersHook.isHooked() && skyHoppersHook.isItemStackSkyHopper(invItem)) return false;
                    if(skySellWandsHook.isHooked() && skySellWandsHook.isItemStackSellWand(invItem)) return false;
                    return !excellentCratesHook.isHooked() || !excellentCratesHook.isItemStackKey(invItem);
                })
                .map(roseStackerHook::getStackSize)
                .reduce(Integer::sum)
                .orElse(0);
    }

    /**
     * Removes a specified amount of a required item from the player's inventory.
     * @param logger The plugin's {@link ComponentLogger}.
     * @param roseStackerHook A {@link RoseStackerHook} instance.
     * @param skyHoppersHook A {@link SkyHoppersHook} instance.
     * @param skySellWandsHook A {@link SkySellWandsHook} instance.
     * @param excellentCratesHook An {@link ExcellentCratesHook} instance.
     * @param player The player from whose inventory the item will be removed.
     * @param itemStack The {@link ItemStack} to be removed.
     * @param amount The amount of the item to remove.
     * @return The amount removed.
     */
    public static int removeRequiredItem(
            @NonNull ComponentLogger logger,
            @NonNull RoseStackerHook roseStackerHook,
            @NonNull SkyHoppersHook skyHoppersHook,
            @NonNull SkySellWandsHook skySellWandsHook,
            @NonNull ExcellentCratesHook excellentCratesHook,
            @NonNull Player player,
            @NonNull ItemStack itemStack,
            int amount) {
        Inventory playerInventory = player.getInventory();
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) {
            logger.error(AdventureUtility.plain("Unable to remove a required item due to a null item type name."));
            return 0;
        }

        int totalRemoved = 0;
        for(int i = 0; i <= playerInventory.getSize() - 1; i++) {
            ItemStack invItem = playerInventory.getItem(i);
            if(invItem == null || invItem.isEmpty()) continue;
            if(!invItem.isSimilar(itemStack)) continue;

            if(skyHoppersHook.isHooked()) {
                if(skyHoppersHook.isItemStackSkyHopper(invItem)) continue;
            }

            if(skySellWandsHook.isHooked()) {
                if(skySellWandsHook.isItemStackSellWand(invItem)) continue;
            }

            if(excellentCratesHook.isHooked()) {
                if(excellentCratesHook.isItemStackKey(invItem)) continue;
            }

            int invItemAmount = roseStackerHook.getStackSize(invItem);

            // Calculate how many items to remove from this stack
            int toRemove = Math.min(amount - totalRemoved, invItemAmount);
            int updatedAmount = invItemAmount - toRemove;

            // Delete the item if the updatedAmount is <= 0
            if(updatedAmount > 0) {
                ItemStack updatedStack = roseStackerHook.setStackSize(invItem, updatedAmount);
                playerInventory.setItem(i, updatedStack);
            } else {
                playerInventory.setItem(i, ItemStack.of(Material.AIR));
            }

            totalRemoved += toRemove;

            // If we've removed enough items, we can stop
            if(totalRemoved >= amount) {
                break;
            }
        }

        return totalRemoved;
    }
}