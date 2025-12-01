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

import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.core.abstracts.SkyPlugin;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Listens for when a player renames an item on an island and increments prestige points.
 */
public class PlayerRenameItemListener extends PrestigePointsListener<InventoryClickEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerRenameItemListener(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player renames an item on an island and increments prestige points.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRenameItem(InventoryClickEvent inventoryClickEvent) {
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
            @Nullable ItemType resultItemType = resultItem.getType().asItemType();
            if(resultItemType == null) return null;
            int amount = resultItem.getAmount();

            if(!firstItem.hasItemMeta() || !resultItem.hasItemMeta()) return null;
            @NotNull ItemMeta firstItemMeta = firstItem.getItemMeta();
            @NotNull ItemMeta resultItemMeta = resultItem.getItemMeta();
            if(isItemNameSimilar(firstItemMeta, resultItemMeta)) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setItemType(resultItemType);
            eventContext.setEnchantments(resultItem.getEnchantments());
            eventContext.setAmount(amount);

            switch(resultItemMeta) {
                case PotionMeta potionMeta -> eventContext.setPotionType(potionMeta.getBasePotionType());

                case EnchantmentStorageMeta enchantmentStorageMeta -> eventContext.setEnchantments(enchantmentStorageMeta.getStoredEnchants());

                default -> {}
            }

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull InventoryClickEvent inventoryClickEvent, @NotNull EventContext eventContext) {
        @Nullable ItemType itemType = eventContext.getItemType();
        if(itemType == null) return;
        @Nullable PotionType potionType = eventContext.getPotionType();
        @Nullable Map<Enchantment, Integer> enchantments = eventContext.getEnchantments();

        @Nullable Double prestigePoints = null;
        if(enchantments != null && !enchantments.isEmpty()) {
            double totalPoints = 0;
            for(Map.Entry<Enchantment, Integer> enchantmentEntry : enchantments.entrySet()) {
                @Nullable Double points = settings.prestigePointsMapping().getNamePrestigePoints(itemType, enchantmentEntry.getKey(), enchantmentEntry.getValue());
                if(points != null) totalPoints += points;
            }

            if(totalPoints > 0) prestigePoints = totalPoints;
        } else if(potionType != null) {
            prestigePoints = settings.prestigePointsMapping().getNamePrestigePoints(itemType, potionType);
        } else {
            prestigePoints = settings.prestigePointsMapping().getNamePrestigePoints(itemType);
        }

        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }

    /**
     * Check if two items have the same or similar names.
     * @param firstItemMeta The first item's {@link ItemMeta}.
     * @param resultItemMeta The second item's {@link ItemMeta}.
     * @return true if the item name is the same, otherwise false.
     */
    private boolean isItemNameSimilar(@NotNull ItemMeta firstItemMeta, @NotNull ItemMeta resultItemMeta) {
        @Nullable Component firstItemName = firstItemMeta.customName();
        @Nullable Component resultItemName = resultItemMeta.customName();
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
