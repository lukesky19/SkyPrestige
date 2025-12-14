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
package com.github.lukesky19.skyPrestige.configuration.data.player;

import com.github.lukesky19.skyPrestige.configuration.data.inventory.EnderChestInventorySettings;
import com.github.lukesky19.skyPrestige.configuration.data.inventory.PlayerInventorySettings;
import com.github.lukesky19.skyPrestige.configuration.data.playtime.PlayTimeSettings;
import com.github.lukesky19.skyPrestige.configuration.interfaces.InventorySettingsInterface;
import com.github.lukesky19.skyPrestige.configuration.interfaces.PlayerSettingsInterface;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * Settings related to resetting player data.
 * @param inventorySettings The {@link PlayerInventorySettings}.
 * @param enderChestSettings The {@link EnderChestInventorySettings}.
 * @param resetExp Whether to reset player experience or not.
 * @param resetMoney Whether to reset the player's money or not.
 * @param resetAuctionItems Whether to reset the player's auction house items or not.
 * @param playTimeSettings The {@link PlayTimeSettings}.
 */
@ConfigSerializable
public record PlayerSettings(
        @NotNull PlayerInventorySettings inventorySettings,
        @NotNull EnderChestInventorySettings enderChestSettings,
        boolean resetExp,
        boolean resetMoney,
        boolean resetAuctionItems,
        @NotNull PlayTimeSettings playTimeSettings) implements PlayerSettingsInterface {
    @Override
    public @NotNull InventorySettingsInterface playerInventorySettings() {
        return inventorySettings;
    }

    @Override
    public @NotNull InventorySettingsInterface playerEnderChestSettings() {
        return enderChestSettings;
    }
}
