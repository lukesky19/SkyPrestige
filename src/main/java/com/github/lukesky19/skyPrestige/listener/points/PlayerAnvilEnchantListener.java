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
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for when a player enchants an item using an anvil on an island and increments prestige points.
 */
public class PlayerAnvilEnchantListener extends PointsListener {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerAnvilEnchantListener(
            @NonNull SkyPlugin plugin,
            @NonNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NonNull PrestigePointsManager prestigePointsManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager,
            @NonNull MultiplierManager multiplierManager) {
        super(plugin, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player enchants an item using an anvil on an island and increments prestige points.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAnvilEnchant(InventoryClickEvent inventoryClickEvent) {
        // Config
        PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
        if(prestigePointsConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to process prestige points due to invalid prestige points config."));
            return;
        }

        // Anvil Check
        if(!(inventoryClickEvent.getClickedInventory() instanceof AnvilInventory anvil)) return;

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

        // Item
        ItemStack firstItem = anvil.getFirstItem();
        ItemStack resultItem = anvil.getResult();
        if(firstItem == null || firstItem.isEmpty() || resultItem == null || resultItem.isEmpty()) return;
        if(!firstItem.hasItemMeta() || !resultItem.hasItemMeta()) return;
        ItemType firstItemItemType = firstItem.getType().asItemType();
        if(firstItemItemType == null) return;
        int amount = resultItem.getAmount();

        Map<Enchantment, Integer> firstItemEnchantments = firstItem.getItemMeta().getEnchants();
        Map<Enchantment, Integer> resultItemEnchantments = resultItem.getItemMeta().getEnchants();
        Map<Enchantment, Integer> uniqueEnchantments = getUniqueEnchantments(firstItemEnchantments, resultItemEnchantments);
        if(uniqueEnchantments.isEmpty()) return;

        // Points
        double points = prestigePointsManager.getItemPoints(ActionType.ENCHANT, prestigePointsConfig.prestigePointsMapping().enchant(), firstItemItemType, null, null, uniqueEnchantments);
        if(points <= 0) return;

        // Add points
        islandData.addPrestigePoints((points * amount) * multiplierManager.getMultiplier(islandData));
    }

    /**
     * Get the unique enchantments between the two {@link Map}s provided.
     * @param originalEnchantments The original item's enchantments.
     * @param updatedEnchantments The updated item's enchantments.
     * @return A {@link Map} mapping {@link Enchantment}s to enchantment levels.
     */
    private @NonNull Map<Enchantment, Integer> getUniqueEnchantments(@NonNull Map<Enchantment, Integer> originalEnchantments, @NonNull Map<Enchantment, Integer> updatedEnchantments) {
        Map<Enchantment, Integer> uniqueEnchantments = new HashMap<>();

        for(Map.Entry<Enchantment, Integer> enchantmentEntry : updatedEnchantments.entrySet()) {
            if(!originalEnchantments.containsKey(enchantmentEntry.getKey())) {
                uniqueEnchantments.put(enchantmentEntry.getKey(), enchantmentEntry.getValue());
            }
        }

        return uniqueEnchantments;
    }
}