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
package com.github.lukesky19.skyPrestige.configuration.data.points;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This record contains the mapping of blocks and spawners to prestige points.
 * @param block The {@link Map} mapping {@link BlockType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
 * @param spawner The {@link Map} mapping {@link BlockType}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping {@link EntityType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
 */
@ConfigSerializable
public record BlockSpawnerMapping(
        @NotNull Map<String, Double> block,
        @NotNull Map<String, Map<String, Double>> spawner) {
    /**
     * Get the prestige points for the {@link BlockType}.
     * @param blockType A {@link BlockType}.
     * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
     */
    public @Nullable Double getBlockPrestigePoints(@NotNull BlockType blockType) {
        return block.getOrDefault(blockType.getKey().toString(), block.get("default"));
    }

    /**
     * Get the prestige points for the {@link BlockType} and {@link EntityType}.
     * @param blockType A {@link BlockType}.
     * @param entityType An {@link EntityType}.
     * @return The prestige points or null if no mapping exists for the {@link BlockType} and {@link EntityType} and there is no default value configured.
     */
    public @Nullable Double getSpawnerPrestigePoints(@NotNull BlockType blockType, @NotNull EntityType entityType) {
        @Nullable Map<String, Double> entityMap = spawner.getOrDefault(blockType.getKey().toString(), spawner.get("default"));
        if(entityMap == null) return null;

        return entityMap.getOrDefault(entityType.getKey().toString(), entityMap.get("default"));
    }
}
