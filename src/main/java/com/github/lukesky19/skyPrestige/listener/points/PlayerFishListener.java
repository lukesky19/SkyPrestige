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
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
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
 * Listens for when a player fishes up an item on an island and increments prestige points.
 */
public class PlayerFishListener extends PrestigePointsListener<PlayerFishEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerFishListener(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player fishes up an item on an island and increments prestige points.
     * @param playerFishEvent A {@link PlayerFishEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent playerFishEvent) {
        process(playerFishEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<PlayerFishEvent> extractor() {
        return playerFishEvent -> {
            Entity entity = playerFishEvent.getCaught();
            if(!(entity instanceof Item item)) return null;
            ItemStack itemStack = item.getItemStack();
            if(itemStack.isEmpty()) return null;
            if(!itemStack.hasItemMeta()) return null;
            ItemMeta itemMeta = itemStack.getItemMeta();
            ItemType itemType = itemStack.getType().asItemType();
            if(itemType == null) return null;
            int amount = itemStack.getAmount();

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(playerFishEvent.getPlayer());
            eventContext.setItemType(itemType);
            eventContext.setEnchantments(itemStack.getEnchantments());
            eventContext.setAmount(amount);

            switch(itemMeta) {
                case PotionMeta potionMeta -> eventContext.setPotionType(potionMeta.getBasePotionType());

                case EnchantmentStorageMeta enchantmentStorageMeta -> eventContext.setEnchantments(enchantmentStorageMeta.getStoredEnchants());

                default -> {}
            }

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull PlayerFishEvent playerFishEvent, @NotNull EventContext eventContext) {
        @Nullable ItemType itemType = eventContext.getItemType();
        if(itemType == null) return;
        @Nullable PotionType potionType = eventContext.getPotionType();
        @Nullable Map<Enchantment, Integer> enchantments = eventContext.getEnchantments();

        @Nullable Double prestigePoints = null;
        if(enchantments != null && !enchantments.isEmpty()) {
            double totalPoints = 0;
            for(Map.Entry<Enchantment, Integer> enchantmentEntry : enchantments.entrySet()) {
                @Nullable Double points = settings.prestigePointsMapping().getFishPrestigePoints(itemType, enchantmentEntry.getKey(), enchantmentEntry.getValue());
                if(points != null) totalPoints += points;
            }

            if(totalPoints > 0) prestigePoints = totalPoints;
        } else if(potionType != null) {
            prestigePoints = settings.prestigePointsMapping().getFishPrestigePoints(itemType, potionType);
        } else {
            prestigePoints = settings.prestigePointsMapping().getFishPrestigePoints(itemType);
        }

        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}
