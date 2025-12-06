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
package com.github.lukesky19.skyPrestige.configuration.data.playtime;

import com.github.lukesky19.skyPrestige.configuration.interfaces.PlayTimeSettingsInterface;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * This record contains settings for resetting play time.
 * @param resetSession Whether session play time should be reset or not.
 * @param resetDaily Whether daily play time should be reset or not.
 * @param resetWeekly Whether weekly play time should be reset or not.
 * @param resetMonthly Whether monthly play time should be reset or not.
 * @param resetYearly Whether yearly play time should be reset or not.
 * @param resetTotal Whether total play time should be reset or not.
 */
@ConfigSerializable
public record PlayTimeSettings(
        boolean resetSession,
        boolean resetDaily,
        boolean resetWeekly,
        boolean resetMonthly,
        boolean resetYearly,
        boolean resetTotal) implements PlayTimeSettingsInterface {}