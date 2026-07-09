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
package com.github.lukesky19.skyPrestige.prestige;

import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsMapping;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigePointsConfigManager;
import com.github.lukesky19.skyPrestige.util.cache.BlockPointsCache;
import com.github.lukesky19.skyPrestige.util.cache.EntityPointsCache;
import com.github.lukesky19.skyPrestige.util.cache.ItemPointsCache;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * This class contains a method related to calculating required prestige points.
 */
public class PrestigePointsManager {
    private final @NonNull PrestigePointsConfigManager prestigePointsConfigManager;

    private final @NonNull BlockPointsCache blockPointsCache = new BlockPointsCache();
    private final @NonNull ItemPointsCache itemPointsCache = new ItemPointsCache();
    private final @NonNull EntityPointsCache entityPointsCache = new EntityPointsCache();

    /**
     * Constructor
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     */
    public PrestigePointsManager(@NonNull PrestigePointsConfigManager prestigePointsConfigManager) {
        this.prestigePointsConfigManager = prestigePointsConfigManager;
    }

    /**
     * Clear the caches.
     */
    public void clearCaches() {
        blockPointsCache.clear();
        itemPointsCache.clear();
        entityPointsCache.clear();
    }

    /**
     * Get the prestige points to award for 1 second of play time.
     * @return The prestige points.
     */
    public double getPlayTimePoints() {
        PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
        if(prestigePointsConfig == null) return 0;

        return prestigePointsConfig.prestigePointsMapping().playTime().points();
    }

    /**
     * Get the prestige points to award for the action and block data.
     * @param actionType The {@link ActionType}.
     * @param config The {@link PrestigePointsMapping.Block} config.
     * @param blockType The {@link BlockType} or null.
     * @param entityType The {@link EntityType} or null.
     * @param age The age or null.
     * @param waterLogged The water logged status or null.
     * @return The prestige points.
     */
    public double getBlockPoints(
            @NonNull ActionType actionType,
            PrestigePointsMapping.@NonNull Block config,
            @NonNull BlockType blockType,
            @Nullable EntityType entityType,
            @Nullable Integer age,
            @Nullable Boolean waterLogged) {
        return blockPointsCache.getPoints(actionType, config, blockType, entityType, age, waterLogged);
    }

    /**
     * Get the prestige points to award for the action and item data.
     * @param actionType The {@link ActionType}.
     * @param config The {@link PrestigePointsMapping.Item} config.
     * @param itemType The {@link ItemType}.
     * @param entityType The {@link EntityType} or null.
     * @param potionType The {@link PotionType} or null.
     * @param enchantments The {@link Map} mapping {@link Enchantment}s to levels as {@link Integer}s.
     * @return The prestige points.
     */
    public double getItemPoints(
            @NonNull ActionType actionType,
            PrestigePointsMapping.@NonNull Item config,
            @NonNull ItemType itemType,
            @Nullable EntityType entityType,
            @Nullable PotionType potionType,
            @Nullable Map<Enchantment, Integer> enchantments) {
        return itemPointsCache.getPoints(actionType, config, itemType, entityType, potionType, enchantments);
    }

    /**
     * Get the prestige points to award for the action and entity data.
     * @param actionType The {@link ActionType}.
     * @param config The {@link PrestigePointsMapping.Item} config.
     * @param entityType The {@link EntityType}.
     * @return The prestige points.
     */
    public double getEntityPoints(
            @NonNull ActionType actionType,
            PrestigePointsMapping.@NonNull Entity config,
            @NonNull EntityType entityType) {
        return entityPointsCache.getPoints(actionType, config, entityType);
    }
}