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

import com.github.lukesky19.skyPrestige.configuration.data.requirement.InventoryRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.MoneyRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.PrestigePointsRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.QuestRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.reset.PrestigeResetSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration for a prestige level.
 * @param configVersion The config version of the file.
 * @param prestigeLevel The prestige level this configuration is for.
 * @param prestigePointsRequirement The {@link PrestigePointsRequirement}.
 * @param moneyRequirement The {@link MoneyRequirement}.
 * @param inventoryRequirements The {@link List} of {@link InventoryRequirement}.
 * @param questRequirements The {@link List} of {@link QuestRequirement}.
 * @param prestigeSettings The {@link PrestigeResetSettings} for this level.
 * @param rewardConfig The {@link RewardConfig} for this level.
 */
@ConfigSerializable
public record PrestigeConfig(
        @Nullable String configVersion,
        int prestigeLevel,
        @NonNull PrestigePointsRequirement prestigePointsRequirement,
        @NonNull MoneyRequirement moneyRequirement,
        @NonNull List<InventoryRequirement> inventoryRequirements,
        @NonNull List<QuestRequirement> questRequirements,
        @NonNull PrestigeResetSettings prestigeSettings,
        @NonNull RewardConfig rewardConfig) {}
