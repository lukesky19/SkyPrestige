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
package com.github.lukesky19.skyPrestige.util.key;

import com.github.lukesky19.skyPrestige.util.cache.ItemPointsCache;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * This record is used as a key in the {@link ItemPointsCache} to cache frequently retrieved prestige point values.
 * @param itemType The {@link ItemType}.
 * @param entityType The optional {@link EntityType}, such as for spawners.
 * @param potionType The optional {@link PotionType}, such as for potions.
 * @param enchantments The optional {@link Map} mapping {@link Enchantment}s to levels as {@link Integer}s.
 */
public record ItemKey(
        @NonNull ItemType itemType,
        @Nullable EntityType entityType,
        @Nullable PotionType potionType,
        @Nullable Map<Enchantment, Integer> enchantments) {}