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
package com.github.lukesky19.skyPrestige.configuration.data.multiplier;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.Nullable;

/**
 * This record contains the configuration related to the scheduled multiplier event.
 * @param configVersion The file's config version.
 * @param enabled Is the multiplier event enabled?
 * @param timezone The timezone to use.
 * @param day What day should the multiplier event activate on?
 * @param hour What hour should the multiplier event activate at?
 * @param durationSeconds How long the event in seconds should last.
 * @param multiplier The multiplier to add for the duration of the event.
 */
@ConfigSerializable
public record MultiplierConfig(
        @Nullable String configVersion,
        boolean enabled,
        @Nullable String timezone,
        @Nullable String day,
        int hour,
        int durationSeconds,
        int multiplier) {}
