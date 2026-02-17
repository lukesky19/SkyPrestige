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

import com.github.lukesky19.skyPrestige.util.cache.BlockPointsCache;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This record is used as a key in the {@link BlockPointsCache} to cache frequently retrieved prestige point values.
 * @param blockType The {@link BlockType}.
 * @param entityType The optional {@link EntityType}, such as for spawners.
 * @param age The optional age of the block, such as for crops.
 * @param waterLogged The optional water logged state of the block.
 */
public record BlockKey(
        @NotNull BlockType blockType,
        @Nullable EntityType entityType,
        @Nullable Integer age,
        @Nullable Boolean waterLogged) {}