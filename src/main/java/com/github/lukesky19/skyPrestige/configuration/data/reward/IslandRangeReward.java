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

import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import org.jspecify.annotations.NonNull;

/**
 * The configuration for the island range to add.
 * @param displayItem The {@link ItemStackConfig} to display inside the rewards GUI.
 * @param setSize Should the island size be set instead of added?
 * @param islandSize The island size to add or set for island.
 */
@ConfigSerializable
public record IslandRangeReward(
        @NonNull ItemStackConfig displayItem,
        boolean setSize,
        int islandSize) implements IReward {
    @Override
    public @NonNull ItemStackConfig displayItem() {
        return displayItem;
    }

    /**
     * Always false. This is only applicable to player-based rewards.
     * @return Always false.
     */
    @Override
    public boolean giveOnIslandJoin() {
        return false;
    }
}