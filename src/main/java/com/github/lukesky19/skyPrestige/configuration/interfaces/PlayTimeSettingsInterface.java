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
package com.github.lukesky19.skyPrestige.configuration.interfaces;

/**
 * This interface can be used to create play time settings.
 */
public interface PlayTimeSettingsInterface {
    /**
     * Should session play time be reset?
     * @return true or false.
     */
    boolean resetSession();
    /**
     * Should daily play time be reset?
     * @return true or false.
     */
    boolean resetDaily();
    /**
     * Should weekly play time be reset?
     * @return true or false.
     */
    boolean resetWeekly();
    /**
     * Should monthly play time be reset?
     * @return true or false.
     */
    boolean resetMonthly();
    /**
     * Should yearly play time be reset?
     * @return true or false.
     */
    boolean resetYearly();
    /**
     * Should total play time be reset?
     * @return true or false.
     */
    boolean resetTotal();
}
