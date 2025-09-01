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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a player enchants an item on an island and increments prestige points.
 */
public class PlayerEnchantListener implements Listener {
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
    public PlayerEnchantListener(
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
     * Listens for when a player enchants an item using an enchantment table on an island and increments prestige points.
     * @param enchantItemEvent An {@link EnchantItemEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEnchant(EnchantItemEvent enchantItemEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = enchantItemEvent.getEnchanter();
        UUID uuid = player.getUniqueId();
        ItemStack itemStack = enchantItemEvent.getItem();
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;
        int amount = itemStack.getAmount();
        Map<Enchantment, Integer> enchantments = enchantItemEvent.getEnchantsToAdd();

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
            logger.error(AdventureUtil.serialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        long totalPrestigePoints = 0;

        for(Map.Entry<Enchantment, Integer> enchantmentEntry : enchantments.entrySet()) {
            Enchantment enchantment = enchantmentEntry.getKey();
            int level = enchantmentEntry.getValue();

            @Nullable Double prestigePoints = settings.prestigePointsMapping().getEnchantmentPrestigePoints(itemType, enchantment, level);
            if(prestigePoints != null) totalPrestigePoints += prestigePoints;
        }

        totalPrestigePoints = totalPrestigePoints * amount;
        islandData.addPrestigePoints(totalPrestigePoints);
    }

    /**
     * Listens for when a player enchants an item using an anvil on an island and increments prestige points.
     * @param inventoryClickEvent An {@link InventoryClickEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAnvilEnchant(InventoryClickEvent inventoryClickEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        if(!(inventoryClickEvent.getWhoClicked() instanceof Player player)) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        if(!(inventoryClickEvent.getClickedInventory() instanceof AnvilInventory anvil)) return;
        @Nullable ItemStack firstItem = anvil.getFirstItem();
        @Nullable ItemStack resultItem = anvil.getResult();
        if(firstItem == null || firstItem.isEmpty() || resultItem == null || resultItem.isEmpty()) return;
        if(!firstItem.hasItemMeta() || !resultItem.hasItemMeta()) return;
        @Nullable ItemType firstItemItemType = firstItem.getType().asItemType();
        if(firstItemItemType == null) return;
        UUID uuid = player.getUniqueId();
        int amount = resultItem.getAmount();

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

        Map<Enchantment, Integer> firstItemEnchantments = firstItem.getItemMeta().getEnchants();
        Map<Enchantment, Integer> resultItemEnchantments = resultItem.getItemMeta().getEnchants();

        Map<Enchantment, Integer> uniqueEnchantments = getUniqueEnchantments(firstItemEnchantments, resultItemEnchantments);
        if(uniqueEnchantments.isEmpty()) return;

        long totalPrestigePoints = 0;

        for(Map.Entry<Enchantment, Integer> enchantmentEntry : uniqueEnchantments.entrySet()) {
            Enchantment enchantment = enchantmentEntry.getKey();
            int level = enchantmentEntry.getValue();

            @Nullable Double prestigePoints = settings.prestigePointsMapping().getEnchantmentPrestigePoints(firstItemItemType, enchantment, level);
            if(prestigePoints != null) totalPrestigePoints += prestigePoints;
        }

        totalPrestigePoints = totalPrestigePoints * amount;
        islandData.addPrestigePoints(totalPrestigePoints);
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
