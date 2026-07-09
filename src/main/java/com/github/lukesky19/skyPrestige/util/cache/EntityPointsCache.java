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
import com.github.lukesky19.skyPrestige.configuration.data.points.points.EntityPoints;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import com.github.lukesky19.skyPrestige.util.key.EntityKey;
import org.bukkit.entity.EntityType;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class caches the prestige points for specific actions related to entities.
 */
public class EntityPointsCache {
    private final @NonNull Map<ActionType, LRUCache<EntityKey, Double>> entityMap = new HashMap<>();

    /**
     * Constructor
     */
    public EntityPointsCache() {}

    /**
     * Get the prestige points for the action and entity.
     * @param actionType The {@link ActionType}.
     * @param config The {@link PrestigePointsMapping.Entity} config.
     * @param entityType The {@link EntityType}.
     * @return The prestige points earned or 0.
     */
    public double getPoints(
            @NonNull ActionType actionType,
            PrestigePointsMapping.@NonNull Entity config,
            @NonNull EntityType entityType) {
        EntityKey entityKey = new EntityKey(entityType);
        LRUCache<EntityKey, Double> cache = entityMap.computeIfAbsent(actionType, _ -> new LRUCache<>(200));

        Double cachedPoints = cache.get(entityKey);
        if(cachedPoints != null) {
            return cachedPoints;
        }

        double points = getFromConfig(config, entityType);
        cache.put(entityKey, points);

        return points;
    }

    /**
     * Get the prestige points from the config.
     * @param config The {@link PrestigePointsMapping.Entity} config.
     * @param entityType The {@link EntityType}.
     * @return The prestige points earned or 0.
     */
    private double getFromConfig(
            PrestigePointsMapping.@NonNull Entity config,
            @NonNull EntityType entityType) {
        Stream<EntityPoints> stream = config.overrides().stream();

        // Filtering logic
        stream = stream.filter(entityPoints ->
                entityPoints.entityData().entityType() != null && entityPoints.entityData().entityType().equals(entityType));

        // Find the first matching entry
        Double points = stream.findFirst().map(EntityPoints::getPoints).orElse(null);

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
        entityMap.clear();
    }
}