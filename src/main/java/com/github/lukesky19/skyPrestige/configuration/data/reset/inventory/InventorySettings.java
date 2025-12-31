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
package com.github.lukesky19.skyPrestige.configuration.data.reset.inventory;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * Settings related to resetting a player's inventory.
 * @param resetInventory Whether to reset an island member's inventory on island reset.
 * @param keepProtectedItems Whether to keep items protected by protection orbs or not.
 * @param keepInfiniteSellWands Whether to keep infinite sell wands from SkySellWands or not.
 */
@ConfigSerializable
public record InventorySettings(
        boolean resetInventory,
        boolean keepProtectedItems,
        boolean keepInfiniteSellWands) {}
