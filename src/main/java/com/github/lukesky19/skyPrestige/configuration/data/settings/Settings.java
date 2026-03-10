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

import com.github.lukesky19.skyPrestige.configuration.data.reset.OtherResetSettings;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This record contains the plugin's configuration settings.
 * @param version The config version.
 * @param locale The locale to use.
 * @param saveFrequencySeconds How frequently island data is periodically saved.
 * @param exchangePrestigeLevel The required prestige level to be able to exchange prestige points.
 * @param progressBarSize The number of pipe symbols (|) to use in the progress bar placeholders.
 * @param startOptedOut Whether islands should start opted out or not.
 * @param applyOptInRewardsForInitialIslands Whether the opt-in reward settings should be run on initial (first) island creation.
 * @param applyOptOutRewardsForInitialIslands Whether the opt-out reward settings should be run on initial (first) island creation.
 * @param islandResetSettings The {@link OtherResetSettings} for normal island resets.
 * @param teamJoinSettings The {@link OtherResetSettings} for team joins.
 * @param teamLeaveSettings The {@link OtherResetSettings} for team leaves.
 * @param teamKickSettings The {@link OtherResetSettings} for team kicks.
 */
@ConfigSerializable
public record Settings(
        int version,
        @Nullable String locale,
        @Nullable Integer saveFrequencySeconds,
        int exchangePrestigeLevel,
        int progressBarSize,
        boolean startOptedOut,
        boolean applyOptInRewardsForInitialIslands,
        boolean applyOptOutRewardsForInitialIslands,
        @NonNull OtherResetSettings islandResetSettings,
        @NonNull OtherResetSettings teamJoinSettings,
        @NonNull OtherResetSettings teamLeaveSettings,
        @NonNull OtherResetSettings teamKickSettings) {}