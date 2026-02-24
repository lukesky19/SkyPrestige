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
package com.github.lukesky19.skyPrestige.configuration.data.requirement;

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This record holds the configuration for the prestige points requirement.
 * @param scaleFormula The formula to scale prestige points.
 * @param scaleFactor The scale factor used in conjunction with the formula to scale the required prestige points.
 * @param prestigePoints The required prestige points.
 * @param removePrestigePoints Whether to remove the required prestige points or not from the island's total.
 * @param incompleteStack The {@link ItemStackConfig} to display in the requirements GUI if the requirement is not met.
 * @param completedStack The {@link ItemStackConfig} to display in the requirements GUI if the requirement is met.
 */
@ConfigSerializable
public record PrestigePointsRequirement(
        @Nullable String scaleFormula,
        double scaleFactor,
        double prestigePoints,
        boolean removePrestigePoints,
        @NonNull ItemStackConfig incompleteStack,
        @NonNull ItemStackConfig completedStack) {}