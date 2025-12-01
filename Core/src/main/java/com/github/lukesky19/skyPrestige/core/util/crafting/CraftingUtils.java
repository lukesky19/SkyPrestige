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
package com.github.lukesky19.skyPrestige.core.util.crafting;

import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * This class is used to calculate how many items were crafted from a {@link CraftItemEvent}.
 */
public class CraftingUtils {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public CraftingUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Calculate the number of items crafted from a {@link CraftItemEvent}.
     * @param event A {@link CraftItemEvent}.
     * @return The amount of items crafted.
     */
    public static int calculateCraftedAmount(@NotNull CraftItemEvent event) {
        // Get the ItemStack in the output slot. This is the result of the recipe and not always the ItemStack given to the player.
        @Nullable ItemStack craftStack = event.getInventory().getResult();
        // Return 0 if there is no ItemStack in the output slot.
        if(craftStack == null) return 0;

        // Get the player's inventory.
        @NotNull PlayerInventory inventory = event.getWhoClicked().getInventory();
        // Get the ItemStacks in the crafting matrix.
        @Nullable ItemStack @NotNull[] ingredients = event.getInventory().getMatrix();

        // Return the amount crafted based on the click type.
        return switch (event.getClick()) {
            case SHIFT_LEFT, SHIFT_RIGHT -> getShiftCraftAmount(craftStack, inventory, ingredients);
            case CONTROL_DROP -> getMaxCraftAmount(craftStack, ingredients);
            case NUMBER_KEY -> getSwapCraftAmount(craftStack, inventory.getItem(event.getHotbarButton()));
            case SWAP_OFFHAND -> getSwapCraftAmount(craftStack, inventory.getItemInOffHand());
            case DROP -> getDropCraftAmount(craftStack, event.getCursor());
            case LEFT, RIGHT -> getClickCraftAmount(craftStack, event.getCursor());
            default -> 0; // Return 0 for any other click types.
        };
    }

    /**
     * Get the amount of items crafted due to a shift click.
     * @param craftStack The {@link ItemStack} produced per craft.
     * @param inventory The {@link Inventory} of the player.
     * @param ingredients The array of {@link ItemStack}s inside the crafting matrix.
     * @return The amount of items crafted due to a shift click.
     */
    private static int getShiftCraftAmount(
            @NotNull ItemStack craftStack,
            @NotNull Inventory inventory,
            @Nullable ItemStack @NotNull [] ingredients) {
        // Get the available space inside the Inventory for the provided ItemStack
        int availableSpace = getAvailableSpace(inventory, craftStack);
        // Get the amount of items per craft
        int itemsPerCraft = craftStack.getAmount();
        // Calculate the maximum number of crafts
        int maxCrafts = Math.min(getCraftingCount(ingredients), availableSpace / itemsPerCraft);

        // Return the maximum number of crafts times the number of items per craft.
        return maxCrafts * itemsPerCraft;
    }

    /**
     * Get the amount of items crafted due to a control drop (ctrl key + drop key).
     * @param craftStack The {@link ItemStack} produced per craft.
     * @param ingredients The array of {@link ItemStack}s inside the crafting matrix.
     * @return The amount of items crafted due to a control drop (ctrl key + drop key).
     */
    private static int getMaxCraftAmount(@NotNull ItemStack craftStack, @Nullable ItemStack @NotNull [] ingredients) {
        // Return the maximum amount of items crafted by multiplying the amount of times crafting occurs by the craft stack's amount.
        return getCraftingCount(ingredients) * craftStack.getAmount();
    }

    /**
     * Get the amount of items crafted due to a swap (offhand or hotbar hotkeys).
     * @param craftStack The {@link ItemStack} produced per craft.
     * @param swapStack The {@link ItemStack} in the slot being swapped to.
     * @return The amount of items crafted.
     */
    private static int getSwapCraftAmount(@NotNull ItemStack craftStack, @Nullable ItemStack swapStack) {
        return (swapStack != null && swapStack.getType().asItemType() != null
                && swapStack.getType().asItemType() != ItemType.AIR) ? craftStack.getAmount() : 0;
    }

    /**
     * Get the amount of items crafted due to just clicking normally.
     * @param craftStack The {@link ItemStack} produced per craft.
     * @param cursorStack The {@link ItemStack} in the player's cursor.
     * @return The amount of items crafted.
     */
    private static int getClickCraftAmount(@NotNull ItemStack craftStack, @Nullable ItemStack cursorStack) {
        if(cursorStack == null
                || cursorStack.getType().asItemType() == null
                || cursorStack.getType().asItemType() == ItemType.AIR
                || !cursorStack.isSimilar(craftStack)
                || cursorStack.getAmount() + craftStack.getAmount() > cursorStack.getMaxStackSize()) {
            return 0;
        }

        return craftStack.getAmount();
    }

    /**
     * Get the amount of items crafted due to dropping the result.<br>
     * This is only for a single press of the drop hotkey, not ctrl + drop.
     * See {@link #getMaxCraftAmount(ItemStack, ItemStack[])} for that.
     * @param craftStack The {@link ItemStack} produced per craft.
     * @param cursorStack The {@link ItemStack} in the player's cursor.
     * @return The amount of items crafted.
     */
    private static int getDropCraftAmount(@NotNull ItemStack craftStack, @Nullable ItemStack cursorStack) {
        return (cursorStack != null && cursorStack.getType().asItemType() != null
                && cursorStack.getType().asItemType() != ItemType.AIR) ? craftStack.getAmount() : 0;
    }

    /**
     * Get the maximum amount for the {@link ItemStack} provided that can fit inside the provided {@link Inventory}.
     * @param inventory The {@link Inventory} to check.
     * @param craftStack The {@link ItemStack} to compare.
     * @return The maximum amount for the {@link ItemStack} provided that can fit inside the provided {@link Inventory}.
     */
    private static int getAvailableSpace(@NotNull Inventory inventory, @NotNull ItemStack craftStack) {
        int availableSpace = 0;

        for(@Nullable ItemStack inventoryStack : inventory.getStorageContents()) {
            if(inventoryStack == null || inventoryStack.isEmpty()) continue;

            if(inventoryStack.isSimilar(craftStack)) {
                availableSpace += craftStack.getMaxStackSize() - inventoryStack.getAmount();
            } else {
                availableSpace += craftStack.getMaxStackSize();
            }
        }

        return availableSpace;
    }

    /**
     * Calculate the number of times that crafting occurs based on the ingredient with the smallest stack size.
     * Ingredients that are null or empty (AIR) are ignored.
     * @param ingredients The array of {@link ItemStack}s inside the crafting matrix.
     * @return The number of times that crafting occurs based on the ingredient with the smallest stack size.
     */
    private static int getCraftingCount(@Nullable ItemStack @NotNull [] ingredients) {
        return Arrays.stream(ingredients)
                .filter(item -> item != null && !item.isEmpty())
                .mapToInt(ItemStack::getAmount)
                .min()
                .orElse(0);
    }
}
