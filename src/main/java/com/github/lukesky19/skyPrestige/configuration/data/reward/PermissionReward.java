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

import com.github.lukesky19.skyPrestige.configuration.data.common.Context;
import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The configuration for a permission reward.
 * @param displayItem The {@link ItemStackConfig} to display inside the rewards GUI.
 * @param giveToAllIslandMembers Whether to run the commands for to all island members.
 * @param giveOnIslandJoin Should the reward be retroactively given on island join?
 * @param beforeIslandReset Should the permission be given before the island is reset?
 * @param removePermission Should the permission be removed?
 * @param negatePermission Should the permission be added, but set as false?
 * @param contexts The permission contexts.
 * @param permission The permission.
 */
@ConfigSerializable
public record PermissionReward(
        @NonNull ItemStackConfig displayItem,
        boolean giveToAllIslandMembers,
        boolean giveOnIslandJoin,
        boolean beforeIslandReset,
        boolean removePermission,
        boolean negatePermission,
        @NonNull List<Context> contexts,
        @Nullable String permission) implements IReward {
    @Override
    public boolean giveOnIslandJoin() {
        return giveOnIslandJoin;
    }
}