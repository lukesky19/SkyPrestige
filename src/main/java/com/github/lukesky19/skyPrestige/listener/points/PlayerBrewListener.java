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
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player brews a potion on an island and increments prestige points.
 */
public class PlayerBrewListener extends PrestigePointsListener<InventoryClickEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PlayerBrewListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager);
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
            eventContext.setSlot(clickedSlot);

            // Container / PersistentDataContainer
            eventContext.setContainer(brewingStand);

            // Freshly Brewed Check
            PersistentDataContainer pdc = brewingStand.getPersistentDataContainer();
            NamespacedKey key = getFreshlyBrewedKey(clickedSlot);
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
        int slot = eventContext.getSlot();
        if(container != null && pdc != null && slot > -1) {
            NamespacedKey key = getFreshlyBrewedKey(slot);

            pdc.set(key, PersistentDataType.BOOLEAN, false);

            container.update();
        }

        islandData.addPrestigePoints(prestigePoints * eventContext.getAmount());
    }

    /**
     * Creates the {@link NamespacedKey} for the slot provided that stores whether the potion in that slot was freshly brewed or not.
     * @param slot The slot to create the {@link NamespacedKey} for.
     * @return A {@link NamespacedKey}.
     */
    private @NotNull NamespacedKey getFreshlyBrewedKey(int slot) {
        return new NamespacedKey("skyprestige", "freshly_brewed_slot_" + slot);
    }
}