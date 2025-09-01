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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a player renames an item on an island and increments prestige points.
 */
public class PlayerRenameItemListener implements Listener {
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
    public PlayerRenameItemListener(
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
     * Listens for when a player renames an item on an island and increments prestige points.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRenameItem(InventoryClickEvent inventoryClickEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        if(!(inventoryClickEvent.getClickedInventory() instanceof AnvilInventory anvil)) return;

        @Nullable ItemStack firstItem = anvil.getFirstItem();
        @Nullable ItemStack resultItem = anvil.getResult();
        if(firstItem == null || firstItem.isEmpty() || resultItem == null || resultItem.isEmpty()) return;
        @Nullable ItemType resultItemType = resultItem.getType().asItemType();
        if(resultItemType == null) return;
        int amount = resultItem.getAmount();

        if(!firstItem.hasItemMeta() || !resultItem.hasItemMeta()) return;
        @NotNull ItemMeta firstItemMeta = firstItem.getItemMeta();
        @NotNull ItemMeta resultItemMeta = resultItem.getItemMeta();
        if(!isItemNameDifferent(firstItemMeta, resultItemMeta)) return;

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
            logger.error(AdventureUtil.serialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        if(resultItemMeta instanceof PotionMeta potionMeta) {
            @Nullable PotionType potionType = potionMeta.getBasePotionType();
            if(potionType != null) {
                @Nullable Double prestigePoints = settings.prestigePointsMapping().getNamePrestigePoints(resultItemType, potionType);
                if(prestigePoints != null) {
                    islandData.addPrestigePoints(prestigePoints * amount);
                    return;
                }
            }

            @Nullable Double prestigePoints = settings.prestigePointsMapping().getNamePrestigePoints(resultItemType);
            if(prestigePoints == null) return;

            islandData.addPrestigePoints(prestigePoints * amount);
        } else if(resultItemMeta instanceof EnchantmentStorageMeta enchantMeta) {
            long totalPrestigePoints = 0;

            for(Map.Entry<Enchantment, Integer> enchantmentEntry : enchantMeta.getStoredEnchants().entrySet()) {
                Enchantment enchantment = enchantmentEntry.getKey();
                int level = enchantmentEntry.getValue();

                @Nullable Double prestigePoints = settings.prestigePointsMapping().getNamePrestigePoints(resultItemType, enchantment, level);
                if(prestigePoints != null) {
                    totalPrestigePoints += prestigePoints;
                }
            }

            islandData.addPrestigePoints(totalPrestigePoints * amount);
        } else if(!resultItem.getEnchantments().isEmpty()) {
            long totalPrestigePoints = 0;

            for(Map.Entry<Enchantment, Integer> enchantmentEntry : resultItem.getEnchantments().entrySet()) {
                Enchantment enchantment = enchantmentEntry.getKey();
                int level = enchantmentEntry.getValue();

                @Nullable Double prestigePoints = settings.prestigePointsMapping().getNamePrestigePoints(resultItemType, enchantment, level);
                if(prestigePoints != null) {
                    totalPrestigePoints += prestigePoints;
                }
            }

            islandData.addPrestigePoints(totalPrestigePoints * amount);
        } else {
            @Nullable Double prestigePoints = settings.prestigePointsMapping().getNamePrestigePoints(resultItemType);
            if (prestigePoints != null) {
                islandData.addPrestigePoints(prestigePoints * amount);
            }
        }
    }

    /**
     * Check if two items have different names.
     * @param firstItemMeta The first item's {@link ItemMeta}.
     * @param resultItemMeta The second item's {@link ItemMeta}.
     * @return true if the item name is different, otherwise false.
     */
    private boolean isItemNameDifferent(@NotNull ItemMeta firstItemMeta, @NotNull ItemMeta resultItemMeta) {
        @Nullable Component firstItemName = firstItemMeta.customName();
        @Nullable Component resultItemName = resultItemMeta.customName();
        // If neither item has a custom name, no item name was updated. Return false.
        if(firstItemName == null && resultItemName == null) return false;

        // If the first item has no custom name, but the resulting item does, the item name was updated. Return true.
        if(firstItemName == null) return true;
        // If the first item has a custom name, but the resulting item does not, the item name was updated. Return true.
        if(resultItemName == null) return true;

        // Return if the two items names are not equal, such as a scenario where the item name was updated.
        return !firstItemName.equals(resultItemName);
    }
}
