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
package com.github.lukesky19.skyPrestige.configuration.data.points;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This record contains the configuration for prestige points obtained for different actions.
 * @param configVersion The file's config version.
 * @param scaleFormula The formula to scale prestige points.
 * @param awardPointsWhileAfk Whether to award prestige points if AFK.
 * @param accurateRoseStacker Whether to consider stacked blocks and entities for prestige points.
 * @param skyEnchantsMultiBreak Whether to listen to SkyEnchant's multibreak events.
 * @param prestigePointsMapping The {@link PrestigePointsMapping}.
 */
@ConfigSerializable
public record PrestigePointsConfig(
        @Nullable String configVersion,
        @Nullable String scaleFormula,
        boolean awardPointsWhileAfk,
        boolean accurateRoseStacker,
        boolean skyEnchantsMultiBreak,
        @NotNull PrestigePointsMapping prestigePointsMapping) {}