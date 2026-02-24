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
package com.github.lukesky19.skyPrestige.data.data.reset;

import com.github.lukesky19.skyPrestige.util.enums.SettingsType;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * This record contains the data required to apply queued settings to a player on login.
 * @param playerId The player's {@link UUID}.
 * @param islandId The player's {@link Island}'s id.
 * @param settingsType The {@link SettingsType} to apply.
 * @param prestigeLevel The prestige level achieved. Only applicable for {@link SettingsType#PRESTIGE}.
 * @param timestamp The timestamp.
 */
public record QueuedSettings(
        @NonNull UUID playerId,
        @NonNull String islandId,
        @NonNull SettingsType settingsType,
        int prestigeLevel,
        long timestamp) {}