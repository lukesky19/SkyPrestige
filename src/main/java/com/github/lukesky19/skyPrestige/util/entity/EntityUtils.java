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
package com.github.lukesky19.skyPrestige.util.entity;

import com.github.lukesky19.skyPrestige.integration.hooks.RoseStackerHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to validate and extract data from {@link Entity}.
 */
public class EntityUtils {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public EntityUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Get the stack size of the entities.
     * @param roseStackerHook A {@link RoseStackerHook} instance.
     * @param entity The {@link LivingEntity}.
     * @return The amount of entities in the stack. Defaults to 1 if RoseStacker isn't hooked into.
     */
    public static int getAmount(@NotNull RoseStackerHook roseStackerHook, @NotNull LivingEntity entity) {
        return roseStackerHook.getStackSize(entity);
    }
}