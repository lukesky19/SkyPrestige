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
package com.github.lukesky19.skyPrestige.configuration.data.interfaces;

import com.github.lukesky19.skyPrestige.configuration.data.playtime.PlayTimeSettings;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to create different settings to apply to a player when an island is reset.
 */
public interface PlayerSettings {
    /**
     * Get the {@link InventorySettings} for the player's inventory.
     * @return The {@link InventorySettings}.
     */
    @NotNull InventorySettings playerInventorySettings();

    /**
     * Get the {@link InventorySettings} for the player's ender chest.
     * @return The {@link InventorySettings}.
     */
    @NotNull InventorySettings playerEnderChestSettings();

    /**
     * Should the player's experience be reset?
     * @return true if reset, false if not.
     */
    boolean resetExp();

    /**
     * Should the player's money be reset?
     * @return true if reset, false if not.
     */
    boolean resetMoney();

    /**
     * Should the player's auction house items be reset?
     * @return true if reset, false if not.
     */
    boolean resetAuctionItems();

    /**
     * Get the {@link PlayTimeSettings}.
     * @return The {@link PlayTimeSettings}
     */
    @NotNull PlayTimeSettings playTimeSettings();
}
