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
package com.github.lukesky19.skyPrestige.util.enums;

/**
 * This enum contains the different actions that can earn prestige points.
 */
public enum ActionType {
    /**
     * For play time.
     */
    PLAY_TIME,
    /**
     * For breaking blocks.
     */
    BLOCK_BREAK,
    /**
     * For placing blocks.
     */
    BLOCK_PLACE,
    /**
     * For bone mealing blocks.
     */
    BONE_MEAL,
    /**
     * For bottling items
     */
    BOTTLE,
    /**
     * For breeding entities.
     */
    BREED,
    /**
     * Brew brewing potions (items).
     */
    BREW,
    /**
     * For brushing blocks.
     */
    BRUSH,
    /**
     * For closing containers. I.e., chests
     */
    CLOSE,
    /**
     * For composting items.
     */
    COMPOST,
    /**
     * For consuming items.
     */
    CONSUME,
    /**
     * For crafting items.
     */
    CRAFT,
    /**
     * For emptying buckets.
     */
    EMPTY,
    /**
     * For enchanting items. Includes anvils and enchantment tables.
     */
    ENCHANT,
    /**
     * For filling buckets.
     */
    FILL,
    /**
     * For fishing up items.
     */
    FISH,
    /**
     * For harvest blocks. I.e., sweet berry bushes.
     */
    HARVEST,
    /**
     * For picking up items.
     */
    PICKUP,
    /**
     * For dropping items.
     */
    DROP,
    /**
     * For killing entities.
     */
    KILL,
    /**
     * For milking entities.
     */
    MILK,
    /**
     * For naming items in an anvil.
     */
    NAME_ITEM,
    /**
     * For naming entities with a name tag.
     */
    NAME_ENTITY,
    /**
     * For opening containers. I.e., chests.
     */
    OPEN,
    /**
     * For shearing blocks.
     */
    SHEAR_BLOCK,
    /**
     * For shearing entities.
     */
    SHEAR_ENTITY,
    /**
     * For sleeping in a bed.
     */
    SLEEP,
    /**
     * For smelting items.
     */
    SMELT,
    /**
     * For stripping blocks.
     * @apiNote Excludes removing wax. See {@link #UNWAX_BLOCK} and {@link #UNWAX_ENTITY}.
     */
    STRIP,
    /**
     * For taming entities.
     */
    TAME,
    /**
     * For throwing items.
     */
    THROW,
    /**
     * For unwaxing blocks.
     */
    UNWAX_BLOCK,
    /**
     * For unwaxing entities.
     */
    UNWAX_ENTITY,
    /**
     * For water logging blocks.
     */
    WATER_LOG,
    /**
     * For waxing blocks.
     */
    WAX_BLOCK,
    /**
     * For waxing entities.
     */
    WAX_ENTITY
}