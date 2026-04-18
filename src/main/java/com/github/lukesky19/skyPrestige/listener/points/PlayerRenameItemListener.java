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
import com.github.lukesky19.skyPrestige.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PointsListener;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigePointsManager;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import com.github.lukesky19.skyPrestige.util.item.ItemUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Map;
import java.util.UUID;

/**
 * Listens for when a player renames an item on an island and increments prestige points.
 */
public class PlayerRenameItemListener extends PointsListener {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerRenameItemListener(
            @NonNull SkyPlugin plugin,
            @NonNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NonNull PrestigePointsManager prestigePointsManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager,
            @NonNull MultiplierManager multiplierManager) {
        super(plugin, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player renames an item on an island and increments prestige points.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRenameItem(InventoryClickEvent inventoryClickEvent) {
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

        // Anvil
        if(!(inventoryClickEvent.getClickedInventory() instanceof AnvilInventory anvil)) return;

        // Island Check
        Island island = checkIsland(player, playerId);
        if(island == null) return;

        // IslandData check.
        IslandData islandData = checkIslandData(island);
        if(islandData == null) return;

        // Item
        ItemStack firstItem = anvil.getFirstItem();
        ItemStack resultItem = anvil.getResult();
        if(firstItem == null || firstItem.isEmpty() || resultItem == null || resultItem.isEmpty()) return;
        ItemType resultItemType = resultItem.getType().asItemType();
        if(resultItemType == null) return;

        if(!firstItem.hasItemMeta() || !resultItem.hasItemMeta()) return;
        ItemMeta firstItemMeta = firstItem.getItemMeta();
        ItemMeta resultItemMeta = resultItem.getItemMeta();
        if(isItemNameSimilar(firstItemMeta, resultItemMeta)) return;

        RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);

        // Item Data
        EntityType entityType = ItemUtils.getEntityType(roseStackerHook, resultItem);
        PotionType potionType = ItemUtils.getPotionType(resultItem);
        Map<Enchantment, Integer> enchantments = ItemUtils.getEnchantments(resultItem);

        // Amount
        int amount = ItemUtils.getAmount(roseStackerHook, resultItem);

        // Points
        double points = prestigePointsManager.getItemPoints(ActionType.NAME_ITEM, prestigePointsConfig.prestigePointsMapping().nameItem(), resultItemType, entityType, potionType, enchantments);
        if(points <= 0) return;

        // Add points
        islandData.addPrestigePoints((points * amount) * multiplierManager.getMultiplier(islandData));
    }

    /**
     * Check if two items have the same or similar names.
     * @param firstItemMeta The first item's {@link ItemMeta}.
     * @param resultItemMeta The second item's {@link ItemMeta}.
     * @return true if the item name is the same, otherwise false.
     */
    private boolean isItemNameSimilar(@NonNull ItemMeta firstItemMeta, @NonNull ItemMeta resultItemMeta) {
        Component firstItemName = firstItemMeta.customName();
        Component resultItemName = resultItemMeta.customName();
        // If neither item has a custom name, no item name was updated. Return false.
        if(firstItemName == null && resultItemName == null) return true;

        // If the first item has no custom name, but the resulting item does, the item name was updated. Return true.
        if(firstItemName == null) return false;
        // If the first item has a custom name, but the resulting item does not, the item name was updated. Return true.
        if(resultItemName == null) return false;

        // Return if the two items names are not equal, such as a scenario where the item name was updated.
        return firstItemName.equals(resultItemName);
    }
}