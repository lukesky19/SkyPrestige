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
package com.github.lukesky19.skyPrestige.configuration.data.points.points;

import com.github.lukesky19.skyPrestige.gui.gui.ValuesGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This interface can be used for a prestige points configuration.
 */
public interface Points {
    /**
     * Get the prestige points.
     * @return The prestige points.
     */
    double getPoints();

    /**
     * Get the data tied to the prestige points.
     * @return The data or null.
     */
    @Nullable Object getData();

    /**
     * Get the {@link ItemStackConfig} used to create the display item.
     * @return The {@link ItemStackConfig} or null.
     */
    @Nullable ItemStackConfig getDisplayItemStackConfig();

    /**
     * Create the {@link ItemStack} to display in the {@link ValuesGUI}.
     * @param logger A {@link ComponentLogger} instance.
     * @param fallback The fallback {@link ItemType} to use.
     * @param name The name of the item.
     * @param lore The lore of the item.
     * @return The {@link ItemStack} or null.
     */
    @Nullable ItemStack createDisplayItemStack(
            @NonNull ComponentLogger logger,
            @NonNull ItemType fallback,
            @NonNull String name,
            @NonNull List<String> lore);
}
