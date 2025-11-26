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
package com.github.lukesky19.skyPrestige.points.listener;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.points.context.EventContext;
import com.github.lukesky19.skyPrestige.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.points.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.settings.Settings;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Listens for when a player enchants an item using an enchantment table on an island and increments prestige points.
 */
public class PlayerEnchantmentTableEnchantListener extends PrestigePointsListener<EnchantItemEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerEnchantmentTableEnchantListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player enchants an item using an enchantment table on an island and increments prestige points.
     * @param enchantItemEvent An {@link EnchantItemEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEnchant(EnchantItemEvent enchantItemEvent) {
        process(enchantItemEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<EnchantItemEvent> extractor() {
        return enchantItemEvent -> {
            ItemStack itemStack = enchantItemEvent.getItem();
            ItemType itemType = itemStack.getType().asItemType();
            if(itemType == null) return null;
            Map<Enchantment, Integer> enchantments = enchantItemEvent.getEnchantsToAdd();
            if(enchantments.isEmpty()) return null;
            int amount = itemStack.getAmount();

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(enchantItemEvent.getEnchanter());
            eventContext.setItemType(itemType);
            eventContext.setEnchantments(enchantments);
            eventContext.setAmount(amount);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull EnchantItemEvent enchantItemEvent, @NotNull EventContext eventContext) {
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

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}
