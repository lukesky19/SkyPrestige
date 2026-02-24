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

import com.github.lukesky19.skyPrestige.configuration.data.points.points.BlockPoints;
import com.github.lukesky19.skyPrestige.configuration.data.points.points.EntityPoints;
import com.github.lukesky19.skyPrestige.configuration.data.points.points.ItemPoints;
import com.github.lukesky19.skyPrestige.configuration.data.points.points.TimePoints;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.meta.Setting;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This record contains the configuration for prestige points obtained for different actions.
 * @param playTime The number of prestige points gained for 1 second of play time.
 * @param blockBreak The prestige points for breaking blocks.
 * @param blockPlace The prestige points for placing blocks.
 * @param boneMeal The prestige points for bone mealing blocks.
 * @param bottle The prestige points for bottling items.
 * @param breed The prestige points for breeding.
 * @param brew The prestige points for brewing.
 * @param brush The prestige points for brushing blocks.
 * @param close The prestige points for closing containers.
 * @param compost The prestige points for composting items.
 * @param consume The prestige points for consuming items.
 * @param craft The prestige points for crafting items.
 * @param empty The prestige points for emptying items.
 * @param enchant The prestige points for enchanting items.
 * @param fill The prestige points for filling items.
 * @param fish The prestige points for fishing items or potions.
 * @param harvest The prestige points for harvesting blocks.
 * @param pickup The prestige points for picking up items.
 * @param drop The prestige points for dropping items.
 * @param kill The prestige points for killing entities.
 * @param milk The prestige points for milking entities.
 * @param nameItem The prestige points for naming items.
 * @param nameEntity The prestige points for naming entities.
 * @param open The prestige points for opening containers.
 * @param shearBlock The prestige points for shearing blocks.
 * @param shearEntity The prestige points for shearing entities.
 * @param sleep The prestige points for sleeping in beds.
 * @param smelt The prestige points for smelting items.
 * @param strip The prestige points for stripping blocks.
 * @param tame The prestige points for taming entities.
 * @param thrown The prestige points for throwing items.
 * @param unWaxBlock The prestige points for unwaxing blocks.
 * @param unWaxEntity The prestige points for unwaxing entities.
 * @param waxBlock The prestige points for waxing blocks
 * @param waxEntity The prestige points for waxing entities.
 * @param waterLog The prestige points for water logging blocks.
 */
@ConfigSerializable
public record PrestigePointsMapping(
        @NonNull TimePoints playTime,
        @NonNull Block blockBreak,
        @NonNull Block blockPlace,
        @NonNull Block boneMeal,
        @NonNull Item bottle,
        @NonNull Entity breed,
        @NonNull Item brew,
        @NonNull Block brush,
        @NonNull Block close,
        @NonNull Item compost,
        @NonNull Item consume,
        @NonNull Item craft,
        @NonNull Item empty,
        @NonNull Item enchant,
        @NonNull Item fill,
        @NonNull Item fish,
        @NonNull Block harvest,
        @NonNull Item pickup,
        @NonNull Item drop,
        @NonNull Entity kill,
        @NonNull Entity milk,
        @NonNull Item nameItem,
        @NonNull Entity nameEntity,
        @NonNull Block open,
        @NonNull Block shearBlock,
        @NonNull Entity shearEntity,
        @NonNull Block sleep,
        @NonNull Item smelt,
        @NonNull Block strip,
        @NonNull Entity tame,
        @NonNull Item thrown,
        @NonNull Block unWaxBlock,
        @NonNull Entity unWaxEntity,
        @NonNull Block waterLog,
        @NonNull Block waxBlock,
        @NonNull Entity waxEntity) {
    /**
     * The configuration for block-based prestige points.
     * @param base The default {@link BlockPoints} configuration.
     * @param overrides The {@link List} of {@link BlockPoints} overrides.
     */
    @ConfigSerializable
    public record Block(
            @NonNull @Setting("default") BlockPoints base,
            @NonNull List<BlockPoints> overrides) {}

    /**
     * The configuration for item-based prestige points.
     * @param base The default {@link ItemPoints} configuration.
     * @param overrides The {@link List} of {@link ItemPoints} overrides.
     */
    @ConfigSerializable
    public record Item(
            @NonNull @Setting("default") ItemPoints base,
            @NonNull List<ItemPoints> overrides) {}

    /**
     * The configuration for entity-based prestige points.
     * @param base The default {@link EntityPoints} configuration.
     * @param overrides The {@link List} of {@link EntityPoints} overrides.
     */
    @ConfigSerializable
    public record Entity(
            @NonNull @Setting("default") EntityPoints base,
            @NonNull List<EntityPoints> overrides) {}
}