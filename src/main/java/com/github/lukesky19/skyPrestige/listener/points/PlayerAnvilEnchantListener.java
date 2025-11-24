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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.settings.Settings;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.listener.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.context.EventContextExtractor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for when a player enchants an item using an anvil on an island and increments prestige points.
 */
public class PlayerAnvilEnchantListener extends PrestigePointsListener<InventoryClickEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PlayerAnvilEnchantListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager);
    }

    /**
     * Listens for when a player enchants an item using an anvil on an island and increments prestige points.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAnvilEnchant(InventoryClickEvent inventoryClickEvent) {
        process(inventoryClickEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<InventoryClickEvent> extractor() {
        return inventoryClickEvent -> {
            if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return null;
            if(!(inventoryClickEvent.getClickedInventory() instanceof AnvilInventory anvil)) return null;
            @Nullable ItemStack firstItem = anvil.getFirstItem();
            @Nullable ItemStack resultItem = anvil.getResult();
            if(firstItem == null || firstItem.isEmpty() || resultItem == null || resultItem.isEmpty()) return null;
            if(!firstItem.hasItemMeta() || !resultItem.hasItemMeta()) return null;
            @Nullable ItemType firstItemItemType = firstItem.getType().asItemType();
            if(firstItemItemType == null) return null;
            int amount = resultItem.getAmount();

            Map<Enchantment, Integer> firstItemEnchantments = firstItem.getItemMeta().getEnchants();
            Map<Enchantment, Integer> resultItemEnchantments = resultItem.getItemMeta().getEnchants();
            Map<Enchantment, Integer> uniqueEnchantments = getUniqueEnchantments(firstItemEnchantments, resultItemEnchantments);
            if(uniqueEnchantments.isEmpty()) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setItemType(firstItemItemType);
            eventContext.setEnchantments(uniqueEnchantments);
            eventContext.setAmount(amount);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull InventoryClickEvent inventoryClickEvent, @NotNull EventContext eventContext) {
        @Nullable ItemType itemType = eventContext.getItemType();
        if(itemType == null) return;
        @Nullable Map<Enchantment, Integer> enchantments = eventContext.getEnchantments();

        @Nullable Double prestigePoints = null;
        if(enchantments != null && !enchantments.isEmpty()) {
            double totalPoints = 0;
            for(Map.Entry<Enchantment, Integer> enchantmentEntry : enchantments.entrySet()) {
                @Nullable Double points = settings.prestigePointsMapping().getEnchantmentPrestigePoints(itemType, enchantmentEntry.getKey(), enchantmentEntry.getValue());
                if(points != null) totalPoints += points;
            }

            if(totalPoints > 0) prestigePoints = totalPoints;
        }

        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * eventContext.getAmount());
    }

    /**
     * Get the unique enchantments between the two {@link Map}s provided.
     * @param originalEnchantments The original item's enchantments.
     * @param updatedEnchantments The updated item's enchantments.
     * @return A {@link Map} mapping {@link Enchantment}s to enchantment levels.
     */
    private @NotNull Map<Enchantment, Integer> getUniqueEnchantments(@NotNull Map<Enchantment, Integer> originalEnchantments, @NotNull Map<Enchantment, Integer> updatedEnchantments) {
        @NotNull Map<Enchantment, Integer> uniqueEnchantments = new HashMap<>();

        for(Map.Entry<Enchantment, Integer> enchantmentEntry : updatedEnchantments.entrySet()) {
            if(!originalEnchantments.containsKey(enchantmentEntry.getKey())) {
                uniqueEnchantments.put(enchantmentEntry.getKey(), enchantmentEntry.getValue());
            }
        }

        return uniqueEnchantments;
    }
}
