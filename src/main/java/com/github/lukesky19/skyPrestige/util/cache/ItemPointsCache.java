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
package com.github.lukesky19.skyPrestige.util.cache;

import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsMapping;
import com.github.lukesky19.skyPrestige.configuration.data.points.points.ItemPoints;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import com.github.lukesky19.skyPrestige.util.key.ItemKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class caches the prestige points for specific actions related to items.
 */
public class ItemPointsCache {
    private final @NotNull Map<ActionType, LRUCache<ItemKey, Double>> itemMap = new HashMap<>();

    /**
     * Constructor
     */
    public ItemPointsCache() {}

    /**
     * Get the prestige points for the action and item.
     * @param actionType The {@link ActionType}.
     * @param config The {@link PrestigePointsMapping.Item} config.
     * @param itemType The {@link ItemType}.
     * @param entityType The optional {@link EntityType}.
     * @param potionType The optional {@link PotionType}.
     * @param enchantments The optional {@link Map} mapping {@link Enchantment}s to levels as {@link Integer}s.
     * @return The prestige points earned or 0.
     */
    public double getPoints(
            @NotNull ActionType actionType,
            @NotNull PrestigePointsMapping.Item config,
            @NotNull ItemType itemType,
            @Nullable EntityType entityType,
            @Nullable PotionType potionType,
            @Nullable Map<Enchantment, Integer> enchantments) {
        ItemKey itemKey = new ItemKey(itemType, entityType, potionType, enchantments);
        LRUCache<ItemKey, Double> cache = itemMap.computeIfAbsent(actionType, k -> new LRUCache<>(1000));

        @Nullable Double cachedPoints = cache.get(itemKey);
        if(cachedPoints != null) {
            return cachedPoints;
        }

        double points = getFromConfig(config, itemType, entityType, potionType, enchantments);
        cache.put(itemKey, points);

        return points;
    }

    /**
     * Get the prestige points from the config.
     * @param config The {@link PrestigePointsMapping.Item} config.
     * @param itemType The {@link ItemType}.
     * @param entityType The optional {@link EntityType}.
     * @param potionType The optional {@link PotionType}.
     * @param enchantments The optional {@link Map} mapping {@link Enchantment}s to levels as {@link Integer}s.
     * @return The prestige points earned or 0.
     */
    private double getFromConfig(
            @NotNull PrestigePointsMapping.Item config,
            @NotNull ItemType itemType,
            @Nullable EntityType entityType,
            @Nullable PotionType potionType,
            @Nullable Map<Enchantment, Integer> enchantments) {
        Stream<ItemPoints> stream = config.overrides().stream();

        // Filtering logic
        stream = stream.filter(blockPoints ->
                blockPoints.itemData().itemType() != null && blockPoints.itemData().itemType().equals(itemType));

        if(entityType != null) {
            stream = stream.filter(blockPoints -> {
                if(blockPoints.itemData().entityType() != null) {
                    return blockPoints.itemData().entityType().equals(entityType);
                } else {
                    // Include entries without a defined entity type
                    return true;
                }
            });
        }

        if(potionType != null) {
            stream = stream.filter(blockPoints -> {
                if(blockPoints.itemData().potionType() != null) {
                    return blockPoints.itemData().potionType().equals(potionType);
                } else {
                    // Include entries without a defined potion type
                    return true;
                }
            });
        } else {
            stream = stream.filter(blockPoints -> blockPoints.itemData().potionType() == null);
        }

        if(enchantments != null) {
            stream = stream.filter(blockPoints -> {
                if(!blockPoints.itemData().enchantments().isEmpty()) {
                    return blockPoints.itemData().enchantments().equals(enchantments);
                } else {
                    // Include entries without defined enchantments
                    return true;
                }
            });
        }

        // Find the first matching entry
        @Nullable Double points = stream.findFirst().map(ItemPoints::getPoints).orElse(null);

        // If null, get the default points
        if(points == null) {
            points = config.base().getPoints();
        }

        return points;
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        itemMap.clear();
    }
}