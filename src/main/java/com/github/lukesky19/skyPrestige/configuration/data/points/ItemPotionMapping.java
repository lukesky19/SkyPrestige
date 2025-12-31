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
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This record contains the mapping of items and potions to prestige points.
 * @param item The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
 * @param potion The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping {@link PotionType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
 */
@ConfigSerializable
public record ItemPotionMapping(
        @NotNull Map<String, Double> item,
        @NotNull Map<String, Map<String, Double>> potion) {
    /**
     * Get the prestige points for the {@link ItemType}.
     * @param itemType A {@link ItemType}.
     * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
     */
    public @Nullable Double getItemPrestigePoints(@NotNull ItemType itemType) {
        return item.getOrDefault(itemType.getKey().toString(), item.get("default"));
    }

    /**
     * Get the prestige points for the {@link ItemType} and {@link PotionType}.
     * @param itemType A {@link ItemType}.
     * @param potionType A {@link PotionType}.
     * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
     */
    public @Nullable Double getPotionPrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
        @Nullable Map<String, Double> potionMap = potion.getOrDefault(itemType.getKey().toString(), potion.get("default"));
        if(potionMap == null) return null;

        return potionMap.getOrDefault(potionType.getKey().toString(), potionMap.get("default"));
    }
}
