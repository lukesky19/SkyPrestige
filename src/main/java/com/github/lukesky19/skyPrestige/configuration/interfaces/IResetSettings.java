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

import com.github.lukesky19.skyPrestige.configuration.data.reset.common.GroupConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.common.PermissionConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.island.IslandSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.player.PlayerSettings;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This interface is used to define reset settings to process when an island is reset or a player is kicked or leaves an island.
 */
public interface IResetSettings {
    /**
     * The {@link IslandSettings}.
     * @return {@link IslandSettings}.
     */
    @NonNull IslandSettings getIslandSettings();

    /**
     * The {@link PlayerSettings}.
     * @return {@link PlayerSettings}.
     */
    @NonNull PlayerSettings getPlayerSettings();

    /**
     * Whether starting money is given to all island members or not.
     * @return true or false.
     */
    boolean giveStartingMoneyToAllIslandMembers();

    /**
     * The starting money.
     * @return The starting money.
     */
    double getStartingMoney();

    /**
     * The commands to execute on reset.
     * @return A {@link List} of commands.
     */
    @NonNull List<@NonNull String> getCommands();

    /**
     * The permissions to modify on reset.
     * @return A {@link List} of {@link PermissionConfig}.
     */
    @NonNull List<@NonNull PermissionConfig> getPermissions();

    /**
     * The groups to modify on reset.
     * @return A {@link List} of {@link GroupConfig}.
     */
    @NonNull List<@NonNull GroupConfig> getGroups();
}
