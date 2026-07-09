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

import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigePointsConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PointsListener;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigePointsManager;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import com.github.lukesky19.skyPrestige.util.enums.SkyPrestigeNamespacedKeys;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * Listens for when a player brews a potion on an island and increments prestige points.
 */
public class PlayerBrewListener extends PointsListener {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerBrewListener(
            @NonNull SkyPlugin plugin,
            @NonNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NonNull PrestigePointsManager prestigePointsManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager,
            @NonNull MultiplierManager multiplierManager) {
        super(plugin, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player removes a freshly brewed potion from a brewing stand on an island and increments prestige points.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRemoveBrewedPotions(InventoryClickEvent inventoryClickEvent) {
        // Config
        PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
        if(prestigePointsConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to process prestige points due to invalid prestige points config."));
            return;
        }

        // Player
        if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return;
        UUID playerId = player.getUniqueId();
        if(isPlayerInvalid(player, prestigePointsConfig)) return;

        // Island Check
        Island island = checkIsland(player, playerId);
        if(island == null) return;

        // IslandData check.
        IslandData islandData = checkIslandData(island);
        if(islandData == null) return;

        // Brewing Stand
        Inventory inventory = inventoryClickEvent.getClickedInventory();
        if(inventory == null) return;
        if(!inventory.getType().equals(InventoryType.BREWING)) return;
        if(!(inventory.getHolder(false) instanceof BrewingStand brewingStand)) return;

        // Slot Number Check
        int clickedSlot = inventoryClickEvent.getSlot();
        if(clickedSlot != 0 && clickedSlot != 1 && clickedSlot != 2) return;

        // Namespaced Key
        NamespacedKey key = SkyPrestigeNamespacedKeys.getFreshlyBrewedKey(clickedSlot);
        if(key == null) return;

        // Freshly Brewed Check
        PersistentDataContainer pdc = brewingStand.getPersistentDataContainer();
        Boolean freshlyBrewed = pdc.get(key, PersistentDataType.BOOLEAN);
        if(freshlyBrewed == null) return;
        if(!freshlyBrewed) return;

        // Item
        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        if(itemStack == null) {
            pdc.set(key, PersistentDataType.BOOLEAN, false);
            brewingStand.update();
            return;
        }
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;

        // Potion
        PotionType potionType = null;
        if(itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
            potionType = potionMeta.getBasePotionType();
        }

        // Amount
        int amount = itemStack.getAmount();

        // Points
        double points = prestigePointsManager.getItemPoints(ActionType.BREW, prestigePointsConfig.prestigePointsMapping().brew(), itemType, null, potionType, null);
        if(points <= 0) return;

        // Add points
        islandData.addPrestigePoints((points * amount) * multiplierManager.getMultiplier(islandData));
    }
}