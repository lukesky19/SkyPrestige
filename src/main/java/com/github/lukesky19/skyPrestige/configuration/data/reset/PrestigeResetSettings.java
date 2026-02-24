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
package com.github.lukesky19.skyPrestige.configuration.data.reset;

import com.github.lukesky19.skyPrestige.configuration.data.reset.common.GroupConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.common.PermissionConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.island.IslandSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.player.PlayerSettings;
import com.github.lukesky19.skyPrestige.configuration.interfaces.IResetSettings;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The configuration settings to apply when a player prestiges or opts in/out an island.
 * @param islandSettings The {@link IslandSettings}.
 * @param playerSettings The {@link PlayerSettings}.
 * @param giveStartingMoneyToAllIslandMembers Whether to give starting money to all island members.
 * @param startingMoney The starting money.
 */
@ConfigSerializable
public record PrestigeResetSettings(
        @NonNull IslandSettings islandSettings,
        @NonNull PlayerSettings playerSettings,
        boolean giveStartingMoneyToAllIslandMembers,
        double startingMoney) implements IResetSettings {
    @Override
    public @NonNull IslandSettings getIslandSettings() {
        return islandSettings;
    }

    @Override
    public @NonNull PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    @Override
    public boolean giveStartingMoneyToAllIslandMembers() {
        return giveStartingMoneyToAllIslandMembers;
    }

    @Override
    public double getStartingMoney() {
        return startingMoney;
    }

    @Override
    public @NonNull List<@NonNull String> getCommands() {
        return List.of();
    }

    @Override
    public @NonNull List<@NonNull PermissionConfig> getPermissions() {
        return List.of();
    }

    @Override
    public @NonNull List<@NonNull GroupConfig> getGroups() {
        return List.of();
    }
}