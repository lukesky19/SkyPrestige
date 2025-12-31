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
package com.github.lukesky19.skyPrestige.configuration.data.reset;

import com.github.lukesky19.skyPrestige.configuration.data.reset.island.IslandSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.player.PlayerSettings;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * The configuration settings to apply when a player resets an island.
 * @param islandSettings The {@link IslandSettings}.
 * @param playerSettings The {@link PlayerSettings}.
 * @param giveStartingMoneyToAllIslandMembers Whether to give starting money to all island members.
 * @param startingMoney The starting money.
 */
@ConfigSerializable
public record ResetSettings(
        @NotNull IslandSettings islandSettings,
        @NotNull PlayerSettings playerSettings,
        boolean giveStartingMoneyToAllIslandMembers,
        double startingMoney) {
}
