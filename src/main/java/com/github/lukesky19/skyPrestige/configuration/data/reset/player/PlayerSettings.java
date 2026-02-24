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
package com.github.lukesky19.skyPrestige.configuration.data.reset.player;

import com.github.lukesky19.skyPrestige.configuration.data.reset.inventory.InventorySettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.playtime.PlayTimeSettings;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;

/**
 * Settings related to resetting player data.
 * @param inventorySettings The {@link InventorySettings} for the player's inventory.
 * @param enderChestSettings The {@link InventorySettings} for ender chest.
 * @param resetExp Whether to reset player experience or not.
 * @param resetMoney Whether to reset the player's money or not.
 * @param resetAuctionItems Whether to reset the player's auction house items or not.
 * @param playTimeSettings The {@link PlayTimeSettings}.
 */
@ConfigSerializable
public record PlayerSettings(
        @NonNull InventorySettings inventorySettings,
        @NonNull InventorySettings enderChestSettings,
        boolean resetExp,
        boolean resetMoney,
        boolean resetAuctionItems,
        @NonNull PlayTimeSettings playTimeSettings) {}