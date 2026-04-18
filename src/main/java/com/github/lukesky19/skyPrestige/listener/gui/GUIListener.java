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
package com.github.lukesky19.skyPrestige.listener.gui;

import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.paper.api.gui.interfaces.BaseGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * Listens when a GUI is open, clicked, dragged, or closed and runs the appropriate methods for the GUI.
 */
public class GUIListener implements Listener {
    private final @NonNull GUIManager guiManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param guiManager A {@link GUIManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public GUIListener(@NonNull GUIManager guiManager, @NonNull HookManager hookManager) {
        this.guiManager = guiManager;
        this.hookManager = hookManager;
    }

    /**
     * When an inventory is clicked, check if the Inventory is a GUI created by the plugin.
     * If so, call the handleClick method for the specific GUI.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        Inventory inventory = inventoryClickEvent.getClickedInventory();

        String islandId = null;
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
        if(island != null) islandId = island.getUniqueId();

        IslandIdUUIDKey islandIdUUIDKey = new IslandIdUUIDKey(islandId, uuid);

        BaseGUI<IslandIdUUIDKey> baseGUI = guiManager.getOpenGUI(islandIdUUIDKey);
        if(baseGUI == null) return;

        baseGUI.handleGlobalClick(inventoryClickEvent);

        if(inventory instanceof PlayerInventory) {
            baseGUI.handleBottomClick(inventoryClickEvent);
        } else {
            baseGUI.handleTopClick(inventoryClickEvent);
        }
    }

    /**
     * When an inventory is dragged, check if the Inventory is a GUI created by the plugin.
     * If so, call the handleDrag method for the specific GUI.
     * @param inventoryDragEvent {@link InventoryDragEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        if(!(inventoryDragEvent.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        Inventory inventory = inventoryDragEvent.getInventory();

        String islandId = null;
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
        if(island != null) islandId = island.getUniqueId();

        IslandIdUUIDKey islandIdUUIDKey = new IslandIdUUIDKey(islandId, uuid);

        BaseGUI<IslandIdUUIDKey> baseGUI = guiManager.getOpenGUI(islandIdUUIDKey);
        if(baseGUI == null) return;

        baseGUI.handleGlobalDrag(inventoryDragEvent);

        if (inventory instanceof PlayerInventory) {
            baseGUI.handleBottomDrag(inventoryDragEvent);
        } else {
            baseGUI.handleTopDrag(inventoryDragEvent);
        }
    }

    /**
     * When an inventory is closed, check if the inventory is a GUI created by the plugin.
     * If so, call the handleClose method for the specific GUI.
     * @param inventoryCloseEvent {@link InventoryCloseEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        if(!(inventoryCloseEvent.getPlayer() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        String islandId = null;
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
        if(island != null) islandId = island.getUniqueId();

        IslandIdUUIDKey islandIdUUIDKey = new IslandIdUUIDKey(islandId, uuid);

        BaseGUI<IslandIdUUIDKey> baseGUI = guiManager.getOpenGUI(islandIdUUIDKey);
        if(baseGUI == null) return;

        baseGUI.handleClose(inventoryCloseEvent);
    }
}
