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
package com.github.lukesky19.skyPrestige.configuration.data.reset.island;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * Settings related to keeping or resetting island data on island reset.
 * @param keepIslandSize Whether to keep island size on island reset or not.
 * @param keepGeneratorUpgrades Whether to keep generator upgrades on island reset or not.
 * @param keepIslandFlags Whether to keep island flags on island reset or not.
 * @param resetPrestigePoints Whether to reset prestige points on island reset or not.
 * @param removeRequiredPrestigePoints Whether to remove the required prestige points or not from the island's total.
 * @param resetPrestigeLevel Whether to reset the island's prestige level or not.
 * @param clearVault Whether to clear the island's vault or not.
 */
@ConfigSerializable
public record IslandSettings(
        boolean keepIslandSize,
        boolean keepGeneratorUpgrades,
        boolean keepIslandFlags,
        boolean resetPrestigePoints,
        boolean removeRequiredPrestigePoints,
        boolean resetPrestigeLevel,
        boolean clearVault) {}
