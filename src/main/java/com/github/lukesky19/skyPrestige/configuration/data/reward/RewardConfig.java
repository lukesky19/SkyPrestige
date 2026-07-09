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
package com.github.lukesky19.skyPrestige.configuration.data.reward;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The rewards to give when the island is reset is reached.
 * @param itemRewards The {@link List} of {@link ItemReward}s.
 * @param commandRewards The {@link List} of {@link CommandReward}s.
 * @param permissionRewards The {@link List} of {@link PermissionReward}s.
 * @param groupRewards The {@link List} of {@link GroupReward}s.
 * @param moneyRewards The {@link List} of {@link MoneyReward}s.
 * @param islandSizeReward The {@link IslandRangeReward}.
 */
@ConfigSerializable
public record RewardConfig(
        @NonNull List<ItemReward> itemRewards,
        @NonNull List<CommandReward> commandRewards,
        @NonNull List<PermissionReward> permissionRewards,
        @NonNull List<GroupReward> groupRewards,
        @NonNull List<MoneyReward> moneyRewards,
        @NonNull IslandRangeReward islandSizeReward) {}