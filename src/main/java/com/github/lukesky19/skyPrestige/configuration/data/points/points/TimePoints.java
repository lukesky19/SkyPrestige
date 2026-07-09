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
package com.github.lukesky19.skyPrestige.configuration.data.points.points;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This record holds the configuration for the prestige points for a 1 second of play time.
 * @param points The points.
 * @param displayItem The {@link ItemStackConfig} to optionally override the automatic display item with.
 */
@ConfigSerializable
public record TimePoints(
        double points,
        @NonNull ItemStackConfig displayItem) implements Points {
    @Override
    public double getPoints() {
        return points;
    }

    /**
     * There is no extra data associated with prestige points earned for play time.
     * @return Always null.
     */
    @Override
    public @Nullable Object getData() {
        return null;
    }

    @Override
    public @NonNull ItemStackConfig getDisplayItemStackConfig() {
        return displayItem;
    }

    @Override
    public @Nullable ItemStack createDisplayItemStack(
            @NonNull ComponentLogger logger,
            @NonNull ItemType fallback,
            @NonNull String name,
            @NonNull List<String> lore) {
        if(displayItem.itemType() != null) {
            ItemStackBuilder defaultBuilder = new ItemStackBuilder(logger);
            defaultBuilder.fromItemStackConfig(displayItem, null, List.of());
            Optional<ItemStack> optional = defaultBuilder.buildItemStack();
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.setItemType(fallback);
        itemStackBuilder.setName(AdventureUtility.deserialize(name));
        itemStackBuilder.setLore(lore.stream().map(AdventureUtility::deserialize).toList());

        return itemStackBuilder.buildItemStack().orElse(null);
    }
}