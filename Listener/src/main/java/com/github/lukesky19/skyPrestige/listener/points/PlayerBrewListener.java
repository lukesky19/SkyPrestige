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
import com.github.lukesky19.skyPrestige.core.util.enums.SkyPrestigeNamespacedKeys;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.dataHandler.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.dataHandler.manager.MultiplierManager;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContextExtractor;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Container;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player brews a potion on an island and increments prestige points.
 */
public class PlayerBrewListener extends PrestigePointsListener<InventoryClickEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerBrewListener(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player removes a freshly brewed potion from a brewing stand on an island and increments prestige points.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRemoveBrewedPotions(InventoryClickEvent inventoryClickEvent) {
        process(inventoryClickEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<InventoryClickEvent> extractor() {
        return inventoryClickEvent -> {
            EventContext eventContext = new EventContext();

            // Player Check
            if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return null;
            eventContext.setPlayer(player);

            // Brewing Stand Check
            Inventory inventory = inventoryClickEvent.getClickedInventory();
            if(inventory == null) return null;
            if(!inventory.getType().equals(InventoryType.BREWING)) return null;
            if(!(inventory.getHolder(false) instanceof BrewingStand brewingStand)) return null;

            // Slot Number Check
            int clickedSlot = inventoryClickEvent.getSlot();
            if(clickedSlot != 0 && clickedSlot != 1 && clickedSlot != 2) return null;

            // Container / PersistentDataContainer
            eventContext.setContainer(brewingStand);

            // Namespaced Key
            @Nullable NamespacedKey key = SkyPrestigeNamespacedKeys.getFreshlyBrewedKey(clickedSlot);
            if(key == null) return null;
            eventContext.setNamespacedKey(key);

            // Freshly Brewed Check
            PersistentDataContainer pdc = brewingStand.getPersistentDataContainer();
            Boolean freshlyBrewed = pdc.get(key, PersistentDataType.BOOLEAN);
            if(freshlyBrewed == null) return null;
            if(!freshlyBrewed) return null;

            // ItemStack and ItemType
            ItemStack itemStack = inventoryClickEvent.getCurrentItem();
            if(itemStack == null) {
                pdc.set(key, PersistentDataType.BOOLEAN, false);
                brewingStand.update();

                return null;
            }
            ItemType itemType = itemStack.getType().asItemType();
            if(itemType == null) return null;
            eventContext.setItemType(itemType);

            // Amount
            int amount = itemStack.getAmount();
            eventContext.setAmount(amount);

            // PotionType
            if(itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
                eventContext.setPotionType(potionMeta.getBasePotionType());
            }

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull InventoryClickEvent inventoryClickEvent, @NotNull EventContext eventContext) {
        @Nullable ItemType itemType = eventContext.getItemType();
        if(itemType == null) return;
        @Nullable PotionType potionType = eventContext.getPotionType();

        @Nullable Double prestigePoints;
        if(potionType != null) {
            prestigePoints = settings.prestigePointsMapping().getBrewPrestigePoints(itemType, potionType);
        } else {
            prestigePoints = settings.prestigePointsMapping().getBrewPrestigePoints(itemType);
        }

        if(prestigePoints == null) return;

        @Nullable Container container = eventContext.getContainer();
        @Nullable PersistentDataContainer pdc = eventContext.getPersistentDataContainer();
        @Nullable NamespacedKey namespacedKey = eventContext.getNamespacedKey();
        if(container != null && pdc != null && namespacedKey != null) {
            pdc.set(namespacedKey, PersistentDataType.BOOLEAN, false);

            container.update();
        }

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}