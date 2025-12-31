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
package com.github.lukesky19.skyPrestige.configuration.data.prestige;

import com.github.lukesky19.skyPrestige.configuration.data.reset.ResetSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This record contains the configuration for a prestige level.
 * @param configVersion The config version of the file.
 * @param prestigeLevel The prestige level this configuration is for.
 * @param scaleFactor The scale factor used in conjunction with the formula in settings.yml to scale the required prestige points.
 * @param requiredPrestigePoints The base number of prestige points required to prestige. This value will be scaled to the number of players on the island.
 * @param prestigeSettings The {@link ResetSettings} for this level.
 * @param rewardConfig The {@link RewardConfig} for this level.
 */
@ConfigSerializable
public record PrestigeConfig(
        @Nullable String configVersion,
        int prestigeLevel,
        @Nullable Double scaleFactor,
        @Nullable Double requiredPrestigePoints,
        @NotNull ResetSettings prestigeSettings,
        @NotNull RewardConfig rewardConfig) {}
