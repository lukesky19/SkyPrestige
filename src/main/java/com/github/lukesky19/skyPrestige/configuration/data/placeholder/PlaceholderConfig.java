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
package com.github.lukesky19.skyPrestige.configuration.data.placeholder;

import com.github.lukesky19.skyPrestige.configuration.data.common.TimeFormat;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * This record contains the configuration for PlaceholderAPI placeholders.
 * @param configVersion The config version.
 * @param prestigeLevel The configuration for the prestige level placeholder.
 * @param prestigePoints The configuration for the prestige points placeholder.
 * @param requiredPrestigePoints The configuration for the required prestige points placeholder.
 * @param progressBar The configuration for the progress bar.
 * @param legacyProgressBar The configuration for the progress bar that uses legacy color codes.
 * @param multiplier The configuration for multiplier placeholders.
 */
@ConfigSerializable
public record PlaceholderConfig(
        String configVersion,
        PrestigeNumberConfig prestigeLevel,
        PrestigeNumberConfig prestigePoints,
        PrestigeNumberConfig requiredPrestigePoints,
        ProgressBarConfig progressBar,
        ProgressBarConfig legacyProgressBar,
        MultiplierConfig multiplier) {
    /**
     * This record contains configuration related to prestige level and prestige points placeholders.
     * @param noIslandText The text to display when there is no island.
     * @param optedOutText The text to display when the island is opted out of prestige.
     */
    @ConfigSerializable
    public record PrestigeNumberConfig(
            String noIslandText,
            String optedOutText) {}
    /**
     * This record contains configuration related to the progress bar placeholder.
     * @param noIslandText The text to display when there is no island.
     * @param optedOutText The text to display when the island is opted out of prestige.
     * @param filledBarText The text to use for when a progress bar is filled, i.e., {@literal <green>|}
     * @param emptyBarText The text to use for when a progress bar is not filled, i.e., {@literal <red>|}
     */
    @ConfigSerializable
    public record ProgressBarConfig(
            String noIslandText,
            String optedOutText,
            String filledBarText,
            String emptyBarText) {}
    /**
     * This record contains the configuration for multiplier placeholders.
     * @param noIslandText The text to display when there is no island.
     * @param optedOutText The text to display when the island is opted out of prestige.
     * @param timeFormat The {@link TimeFormat} to use for time placeholders.
     */
    @ConfigSerializable
    public record MultiplierConfig(
            String noIslandText,
            String optedOutText,
            TimeFormat timeFormat) {}
}