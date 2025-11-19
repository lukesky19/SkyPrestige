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
package com.github.lukesky19.skyPrestige.listener;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.settings.Settings;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.interfaces.BaseGUI;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.GameMode;
import org.bukkit.block.BlockType;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a player interacts with an Inventory GUI or another non-GUI Inventory on an Island and increments prestige points.
 */
public class InventoryListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public InventoryListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull GUIManager guiManager,
            @NotNull HookManager hookManager) {
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.islandDataManager = islandDataManager;
        this.guiManager = guiManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens for when a player opens a Block's Inventory on an Island and increments prestige points.
     * @param inventoryOpenEvent An {@link InventoryOpenEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpened(InventoryOpenEvent inventoryOpenEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        if(!(inventoryOpenEvent.getPlayer() instanceof Player player)) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        @Nullable InventoryHolder inventoryHolder = inventoryOpenEvent.getInventory().getHolder(false);
        if(inventoryHolder == null) return;
        if(!(inventoryHolder instanceof Container container)) return;
        BlockType blockType = container.getType().asBlockType();
        if(blockType == null) return;

        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        if(skyPlayTimeHook.isHooked()) {
            if(!settings.awardPointsWhileAfk()) {
                if(skyPlayTimeHook.isPlayerAFK(uuid)) return;
            }
        }

        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(player.getLocation());
        if(optionalIsland.isEmpty()) return;
        Island island = optionalIsland.get();
        if(!island.getMemberSet().contains(uuid)) return;

        String islandId = island.getUniqueId();
        IslandData islandData = islandDataManager.getIslandData(islandId);
        if (islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getOpenPrestigePoints(blockType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints);
    }

    /**
     * Listens for when a player closes a Block's Inventory on an Island and increments prestige points.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClosed(InventoryCloseEvent inventoryCloseEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        if(!(inventoryCloseEvent.getPlayer() instanceof Player player)) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        @Nullable InventoryHolder inventoryHolder = inventoryCloseEvent.getInventory().getHolder(false);
        if(inventoryHolder == null) return;
        if(!(inventoryHolder instanceof Container container)) return;
        BlockType blockType = container.getType().asBlockType();
        if(blockType == null) return;

        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        if(skyPlayTimeHook.isHooked()) {
            if(!settings.awardPointsWhileAfk()) {
                if(skyPlayTimeHook.isPlayerAFK(uuid)) return;
            }
        }

        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(player.getLocation());
        if(optionalIsland.isEmpty()) return;
        Island island = optionalIsland.get();
        if(!island.getMemberSet().contains(uuid)) return;

        String islandId = island.getUniqueId();
        IslandData islandData = islandDataManager.getIslandData(islandId);
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getClosePrestigePoints(blockType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints);
    }

    /**
     * When an inventory is clicked, check if the Inventory is a GUI created by the plugin.
     * If so, call the handleClick method for the specific GUI.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(InventoryClickEvent inventoryClickEvent) {
        UUID uuid = inventoryClickEvent.getWhoClicked().getUniqueId();
        Inventory inventory = inventoryClickEvent.getClickedInventory();

        @NotNull Optional<@NotNull BaseGUI> optionalBaseGUI = guiManager.getOpenGUI(uuid);
        if(optionalBaseGUI.isEmpty()) return;
        BaseGUI baseGUI = optionalBaseGUI.get();

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
        UUID uuid = inventoryDragEvent.getWhoClicked().getUniqueId();
        Inventory inventory = inventoryDragEvent.getInventory();

        @NotNull Optional<@NotNull BaseGUI> optionalBaseGUI = guiManager.getOpenGUI(uuid);
        if (optionalBaseGUI.isEmpty()) return;
        BaseGUI baseGUI = optionalBaseGUI.get();

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
        UUID uuid = inventoryCloseEvent.getPlayer().getUniqueId();

        @NotNull Optional<@NotNull BaseGUI> optionalBaseGUI = guiManager.getOpenGUI(uuid);
        if(optionalBaseGUI.isEmpty()) return;
        BaseGUI baseGUI = optionalBaseGUI.get();

        baseGUI.handleClose(inventoryCloseEvent);
    }
}
