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
package com.github.lukesky19.skyPrestige.configuration.data.points.data;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * This record holds the configuration to represent an item.
 * @param itemType The {@link BlockType}.
 * @param entityType The optional {@link EntityType}, such as for spawners.
 * @param potionType The optional {@link PotionType}.
 * @param enchantments The {@link Map} mapping {@link Enchantment}s to levels as an {@link Integer}.
 */
@ConfigSerializable
public record ItemData(
        @Nullable ItemType itemType,
        @Nullable EntityType entityType,
        @Nullable PotionType potionType,
        @NotNull Map<Enchantment, Integer> enchantments) {}