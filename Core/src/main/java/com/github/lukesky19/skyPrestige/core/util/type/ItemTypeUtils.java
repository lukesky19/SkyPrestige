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
package com.github.lukesky19.skyPrestige.core.util.type;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is used to check {@link ItemType}s.
 */
public class ItemTypeUtils {
    private static final @NotNull Set<ItemType> AXES = new LinkedHashSet<>();

    static {
        Registry<@NotNull ItemType> itemTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM);
        itemTypeRegistry.forEach(itemType -> {
            String name = itemType.getKey().toString().toLowerCase();

            if(name.endsWith("_axe")) {
                AXES.add(itemType);
            }
        });
    }

    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public ItemTypeUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Checks if the {@link ItemType} is an axe.
     * @param itemType The {@link ItemType} to check.
     * @return true if an axe, otherwise false.
     */
    public static boolean isItemTypeAxe(@NotNull ItemType itemType) {
        return AXES.contains(itemType);
    }
}
