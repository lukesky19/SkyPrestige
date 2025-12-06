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
 * This interface is used to create different settings to apply to an island when it is reset.
 */
public interface IslandSettingsInterface {
    /**
     * Should the island size be kept on reset?
     * @return true if kept, or false if not.
     */
    boolean keepIslandSize();

    /**
     * Should the generator upgrades be kept on reset?
     * @return true if kept, or false if not.
     */
    boolean keepGeneratorUpgrades();

    /**
     * Should the island flags be kept on reset?
     * @return true if kept, or false if not.
     */
    boolean keepIslandFlags();

    /**
     * Should the prestige points be reset?
     * @return true if reset, or false if not.
     */
    boolean resetPrestigePoints();
    /**
     * Should the required prestige points be removed?
     * @return true if removed, or false if not.
     */
    boolean removeRequiredPrestigePoints();

    /**
     * Should the prestige level be reset?
     * @return true if reset, or false if not.
     */
    boolean resetPrestigeLevel();

    /**
     * Should the island's vault be cleared on reset?
     * @return true if cleared, false if not.
     */
    boolean clearVault();
}