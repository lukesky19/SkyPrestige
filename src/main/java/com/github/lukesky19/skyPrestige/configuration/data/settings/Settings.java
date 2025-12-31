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
package com.github.lukesky19.skyPrestige.configuration.data.settings;

import com.github.lukesky19.skyPrestige.configuration.data.reset.ResetSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.island.IslandSettings;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This record contains the plugin's configuration settings.
 * @param configVersion The file's config version.
 * @param locale The locale to use.
 * @param saveFrequencySeconds How frequently island data is periodically saved.
 * @param exchangePrestigeLevel The required prestige level to be able to exchange prestige points.
 * @param islandResetSettings The {@link ResetSettings} for normal island resets.
 */
@ConfigSerializable
public record Settings(
        @Nullable String configVersion,
        @Nullable String locale,
        @Nullable Integer saveFrequencySeconds,
        int exchangePrestigeLevel,
        @NotNull IslandSettings islandResetSettings) {}
