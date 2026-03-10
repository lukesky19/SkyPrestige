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
package com.github.lukesky19.skyPrestige.configuration.data.vault;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This record holds the configuration related to the vault.
 * @param version The config version.
 * @param vaultDisallowedItems The {@link List} of {@link ItemType} {@link NamespacedKey}s as a {@link String} that are not allowed in the vault.
 */
@ConfigSerializable
public record VaultConfig(
        int version,
        @NonNull List<String> vaultDisallowedItems) {}
