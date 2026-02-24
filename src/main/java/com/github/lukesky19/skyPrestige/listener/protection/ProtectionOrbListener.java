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
package com.github.lukesky19.skyPrestige.listener.protection;

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.protection.ProtectionOrbManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NonNull;

/**
 * This class listens to players clicking an item with a protection orb and adds protection to the item to prevent it from being removed on prestige.
 */
public class ProtectionOrbListener implements Listener {
    private final @NonNull LocaleManager localeManager;
    private final @NonNull ProtectionOrbManager protectionOrbManager;

    /**
     * Constructor
     * @param localeManager  A {@link LocaleManager} instance.
     * @param protectionOrbManager A {@link ProtectionOrbManager} instance.
     */
    public ProtectionOrbListener(
            @NonNull LocaleManager localeManager,
            @NonNull ProtectionOrbManager protectionOrbManager) {
        this.localeManager = localeManager;
        this.protectionOrbManager = protectionOrbManager;
    }

    /**
     * Listens for when a player clicks an item in their inventory with a protection orb and attempts to protect it.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemClick(InventoryClickEvent inventoryClickEvent) {
        if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return;
        if(!(inventoryClickEvent.getClickedInventory() instanceof PlayerInventory)) return;

        // Clicked ItemStack
        ItemStack clickedSlotItemStack = inventoryClickEvent.getCurrentItem();
        if(clickedSlotItemStack == null || clickedSlotItemStack.isEmpty()) return;
        ItemType clickedSlotItemType = clickedSlotItemStack.getType().asItemType();
        if(clickedSlotItemType == null) return;

        // ItemStack on cursor
        ItemStack cursorItem = inventoryClickEvent.getCursor();
        if(cursorItem.isEmpty()) return;

        // Check if the cursor item is a protection orb
        if(!protectionOrbManager.isItemStackProtectionOrb(cursorItem)) return;

        // Locale
        Locale locale = localeManager.getConfiguration();
        Locale.ProtectionOrbMessages protectionOrbMessages = locale.protectionOrbMessages();

        // Check if the item is allowed to be protected
        if(protectionOrbManager.isProtectionOrbItemTypeDisallowed(clickedSlotItemType)) {
            inventoryClickEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + protectionOrbMessages.protectionOrbNotAllowed()));
            return;
        }

        // Check if the item is already protected
        if(protectionOrbManager.isItemStackProtected(clickedSlotItemStack)) {
            inventoryClickEvent.setCancelled(true);
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + protectionOrbMessages.protectionOrbAlreadyProtected()));
            return;
        }

        // Cancel the event
        inventoryClickEvent.setCancelled(true);

        // Update the amount or clear the protection orb (item on cursor)
        int cursorAmount = cursorItem.getAmount();
        cursorAmount -= 1;
        if(cursorAmount > 0) {
            cursorItem.setAmount(cursorAmount);
        } else {
            player.setItemOnCursor(ItemType.AIR.createItemStack());
        }

        // Protect the item
        protectionOrbManager.protectItemStack(clickedSlotItemStack);

        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + protectionOrbMessages.protectionOrbProtected()));
    }
}
