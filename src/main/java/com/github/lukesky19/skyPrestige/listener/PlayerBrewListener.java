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
import com.github.lukesky19.skyPrestige.config.Settings;
import com.github.lukesky19.skyPrestige.data.IslandData;
import com.github.lukesky19.skyPrestige.hook.impl.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.manager.config.SettingsManager;
import com.github.lukesky19.skyPrestige.manager.hook.HookManager;
import com.github.lukesky19.skyPrestige.manager.island.IslandDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a player brews a potion on an island and increments prestige points.
 */
public class PlayerBrewListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;

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
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens for when a player removes a potion from a brewing stand on an island and increments prestige points.
     * Prestige points are only awarded if the potions were freshly brewed. See {@link #onBrew(BrewEvent)}.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRemoveBrewedPotions(InventoryClickEvent inventoryClickEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        Inventory inventory = inventoryClickEvent.getClickedInventory();
        if(inventory == null) return;
        if(!inventory.getType().equals(InventoryType.BREWING)) return;
        if(!(inventory.getHolder(false) instanceof BrewingStand brewingStand)) return;

        int clickedSlot = inventoryClickEvent.getSlot();
        if(!(clickedSlot == 0) && !(clickedSlot == 1) && !(clickedSlot == 2)) return;

        PersistentDataContainer pdc = brewingStand.getPersistentDataContainer();
        NamespacedKey key = getFreshlyBrewedKey(clickedSlot);
        Boolean freshlyBrewed = pdc.get(key, PersistentDataType.BOOLEAN);
        if(freshlyBrewed == null) return;
        if(!freshlyBrewed) return;

        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        if(itemStack == null) {
            pdc.set(key, PersistentDataType.BOOLEAN, false);
            brewingStand.update();
            return;
        }
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;
        int amount = itemStack.getAmount();

        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
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

        if(itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
            @Nullable PotionType potionType = potionMeta.getBasePotionType();

            if(potionType != null) {
                @Nullable Double prestigePoints = settings.prestigePointsMapping().getBrewPrestigePoints(itemType, potionType);
                if(prestigePoints != null) {
                    islandData.addPrestigePoints(prestigePoints * amount);

                    pdc.set(key, PersistentDataType.BOOLEAN, false);
                    brewingStand.update();

                    return;
                }
            }
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getBrewPrestigePoints(itemType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * amount);

        pdc.set(key, PersistentDataType.BOOLEAN, false);
        brewingStand.update();
    }

    /**
     * Marks a brewing stand as freshly brewed so that when potions are removed, island data can be updated.
     * See {@link #onPlayerRemoveBrewedPotions(InventoryClickEvent)}.
     * @param brewEvent A {@link BrewEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrew(BrewEvent brewEvent) {
        Block block = brewEvent.getBlock();
        if(!(block.getState(false) instanceof BrewingStand brewingStand)) return;

        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(brewEvent.getBlock().getLocation());
        if(optionalIsland.isEmpty()) return;

        PersistentDataContainer pdc = brewingStand.getPersistentDataContainer();
        BrewerInventory brewerInventory = brewingStand.getInventory();

        ItemStack bottle1 = brewerInventory.getItem(0);
        ItemStack bottle2 = brewerInventory.getItem(1);
        ItemStack bottle3 = brewerInventory.getItem(2);

        if(bottle1 != null && !bottle1.isEmpty()) {
            NamespacedKey key = getFreshlyBrewedKey(0);
            pdc.set(key, PersistentDataType.BOOLEAN, true);
        }

        if(bottle2 != null && !bottle2.isEmpty()) {
            NamespacedKey key = getFreshlyBrewedKey(1);
            pdc.set(key, PersistentDataType.BOOLEAN, true);
        }

        if(bottle3 != null && !bottle3.isEmpty()) {
            NamespacedKey key = getFreshlyBrewedKey(2);
            pdc.set(key, PersistentDataType.BOOLEAN, true);
        }

        brewingStand.update();
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
