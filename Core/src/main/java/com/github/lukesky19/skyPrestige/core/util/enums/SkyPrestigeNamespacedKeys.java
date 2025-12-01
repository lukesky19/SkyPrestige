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
package com.github.lukesky19.skyPrestige.core.util.enums;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This enum contains the NamespacedKeys used by the plugin.
 */
public enum SkyPrestigeNamespacedKeys {
    /**
     * This enum is for slot 0 of a brewing stand. The NamespacedKey is used to mark the slot as freshly brewed.
     */
    FRESHLY_BREWED_SLOT_0,
    /**
     * This enum is for slot 1 of a brewing stand. The NamespacedKey is used to mark the slot as freshly brewed.
     */
    FRESHLY_BREWED_SLOT_1,
    /**
     * This enum is for slot 2 of a brewing stand. The NamespacedKey is used to mark the slot as freshly brewed.
     */
    FRESHLY_BREWED_SLOT_2,
    /**
     * This enum is for a protection orb. The NamespacedKey is used to mark an ItemStack as a protection orb.
     */
    PROTECTION_ORB,
    /**
     * This enum is for an item protected by a protection orb. The NamespacedKey is used to identify such item.
     */
    PROTECTED;

    /**
     * The {@link NamespacedKey} for the enum.
     */
    private final @NotNull NamespacedKey key;

    /**
     * Creates a new {@link NamespacedKey} for the enum.
     */
    SkyPrestigeNamespacedKeys() {
        this.key = new NamespacedKey("skyprestige", this.toString().toLowerCase());
    }

    /**
     * Gets the {@link NamespacedKey} for the setting.
     * @return A {@link NamespacedKey}.
     */
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    /**
     * Get the {@link NamespacedKey} for the slot number. Will return null if the slot number is {@literal <=} 0 or {@literal >} 3.
     * @param slot The slot number.
     * @return A {@link NamespacedKey} or null.
     */
    public static @Nullable NamespacedKey getFreshlyBrewedKey(int slot) {
        return switch(slot) {
            case 0 -> FRESHLY_BREWED_SLOT_0.getKey();

            case 1 -> FRESHLY_BREWED_SLOT_1.getKey();

            case 2 -> FRESHLY_BREWED_SLOT_2.getKey();

            default -> null;
        };
    }
}
