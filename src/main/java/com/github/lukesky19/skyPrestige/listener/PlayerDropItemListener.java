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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
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
 * Listens for when a player drops an item on an island and increments prestige points.
 */
public class PlayerDropItemListener implements Listener {
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
    public PlayerDropItemListener(
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
     * Listens for when a player drops an item on an island and increments prestige points.
     * @param entityDropItemEvent An {@link EntityDropItemEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(EntityDropItemEvent entityDropItemEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Entity entity = entityDropItemEvent.getEntity();
        if(!(entity instanceof Player player)) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        @NotNull Item itemEntity = entityDropItemEvent.getItemDrop();
        @NotNull ItemStack itemStack = itemEntity.getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;
        int amount = itemStack.getAmount();

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

        if(itemMeta instanceof PotionMeta potionMeta) {
            @Nullable PotionType potionType = potionMeta.getBasePotionType();
            if(potionType != null) {
                @Nullable Double prestigePoints = settings.prestigePointsMapping().getItemDropPrestigePoints(itemType, potionType);
                if(prestigePoints != null) {
                    islandData.addPrestigePoints(prestigePoints * amount);
                    return;
                }
            }

            @Nullable Double prestigePoints = settings.prestigePointsMapping().getItemDropPrestigePoints(itemType);
            if(prestigePoints == null) return;

            islandData.addPrestigePoints(prestigePoints * amount);
        } else if(itemMeta instanceof EnchantmentStorageMeta enchantMeta) {
            long totalPrestigePoints = 0;

            for(Map.Entry<Enchantment, Integer> enchantmentEntry : enchantMeta.getStoredEnchants().entrySet()) {
                Enchantment enchantment = enchantmentEntry.getKey();
                int level = enchantmentEntry.getValue();

                @Nullable Double prestigePoints = settings.prestigePointsMapping().getItemDropPrestigePoints(itemType, enchantment, level);
                if(prestigePoints != null) {
                    totalPrestigePoints += prestigePoints;
                }
            }

            islandData.addPrestigePoints(totalPrestigePoints * amount);
        } else if(!itemStack.getEnchantments().isEmpty()) {
            long totalPrestigePoints = 0;

            for(Map.Entry<Enchantment, Integer> enchantmentEntry : itemStack.getEnchantments().entrySet()) {
                Enchantment enchantment = enchantmentEntry.getKey();
                int level = enchantmentEntry.getValue();

                @Nullable Double prestigePoints = settings.prestigePointsMapping().getItemDropPrestigePoints(itemType, enchantment, level);
                if(prestigePoints != null) {
                    totalPrestigePoints += prestigePoints;
                }
            }

            islandData.addPrestigePoints(totalPrestigePoints * amount);
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getItemDropPrestigePoints(itemType);
        if(prestigePoints != null) {
            islandData.addPrestigePoints(prestigePoints * amount);
        }
    }
}
