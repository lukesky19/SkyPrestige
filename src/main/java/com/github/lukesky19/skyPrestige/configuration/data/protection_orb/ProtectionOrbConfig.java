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
package com.github.lukesky19.skyPrestige.configuration.data.protection_orb;

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This contains the configuration related to the protection orb that protects an item from being reset on prestige.
 * @param version The config version.
 * @param itemStackConfig The {@link ItemStackConfig} for the protection orb.
 * @param disallowedItems The {@link List} of disallowed item names or namespaced keys.
 * @param protectedLore The lore to add to an item that is protected.
 */
@ConfigSerializable
public record ProtectionOrbConfig(
        int version,
        @NonNull ItemStackConfig itemStackConfig,
        @NonNull List<String> disallowedItems,
        @Nullable String protectedLore) {}
