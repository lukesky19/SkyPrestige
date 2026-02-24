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
import com.github.lukesky19.skyPrestige.configuration.data.points.points.BlockPoints;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import com.github.lukesky19.skyPrestige.util.key.BlockKey;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class caches the prestige points for specific actions related to blocks.
 */
public class BlockPointsCache {
    private final @NonNull Map<ActionType, LRUCache<BlockKey, Double>> blockMap = new HashMap<>();

    /**
     * Constructor
     */
    public BlockPointsCache() {}

    /**
     * Get the prestige points for the action and block.
     * @param actionType The {@link ActionType}.
     * @param config The {@link PrestigePointsMapping.Block} config.
     * @param blockType The {@link BlockType}.
     * @param entityType The optional {@link EntityType}.
     * @param age The optional age.
     * @param waterLogged The optional water logged status.
     * @return The prestige points earned or 0.
     */
    public double getPoints(
            @NonNull ActionType actionType,
            PrestigePointsMapping.@NonNull Block config,
            @NonNull BlockType blockType,
            @Nullable EntityType entityType,
            @Nullable Integer age,
            @Nullable Boolean waterLogged) {
        BlockKey blockKey = new BlockKey(blockType, entityType, age, waterLogged);
        LRUCache<BlockKey, Double> cache = blockMap.computeIfAbsent(actionType, k -> new LRUCache<>(250));

        Double cachedPoints = cache.get(blockKey);
        if(cachedPoints != null) {
            return cachedPoints;
        }

        double points = getFromConfig(config, blockType, entityType, age, waterLogged);
        cache.put(blockKey, points);

        return points;
    }

    /**
     * Get the prestige points from the config.
     * @param config The {@link PrestigePointsMapping.Block} config.
     * @param blockType The {@link BlockType}.
     * @param entityType The optional {@link EntityType}.
     * @param age The optional age.
     * @param waterLogged The optional water logged status.
     * @return The prestige points earned or 0.
     */
    private double getFromConfig(
            PrestigePointsMapping.@NonNull Block config,
            @NonNull BlockType blockType,
            @Nullable EntityType entityType,
            @Nullable Integer age,
            @Nullable Boolean waterLogged) {
        Stream<BlockPoints> stream = config.overrides().stream();

        // Filtering logic
        stream = stream.filter(blockPoints ->
                blockPoints.blockData().blockType() != null && blockPoints.blockData().blockType().equals(blockType));

        if(entityType != null) {
            stream = stream.filter(blockPoints -> {
                if(blockPoints.blockData().entityType() != null) {
                    return blockPoints.blockData().entityType().equals(entityType);
                } else {
                    // Include entries without a defined entity type
                    return true;
                }
            });
        }

        if(age != null) {
            stream = stream.filter(blockPoints -> {
                if(blockPoints.blockData().age() != null) {
                    return blockPoints.blockData().age().equals(age);
                } else {
                    // Include entries without a defined age
                    return true;
                }
            });
        }

        if(waterLogged != null) {
            stream = stream.filter(blockPoints -> {
                if(blockPoints.blockData().waterLogged() != null) {
                    return blockPoints.blockData().waterLogged().equals(waterLogged);
                } else {
                    // Include entries without a defined water logged state
                    return true;
                }
            });
        }

        // Find the first matching entry
        Double points = stream.findFirst().map(BlockPoints::getPoints).orElse(null);

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
        blockMap.clear();
    }
}