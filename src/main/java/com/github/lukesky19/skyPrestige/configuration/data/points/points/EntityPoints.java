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

import com.github.lukesky19.skyPrestige.configuration.data.points.data.EntityData;
import com.github.lukesky19.skyPrestige.util.item.ItemUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.format.FormatUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This record holds the configuration for the prestige points for a particular entity.
 * @param points The points.
 * @param entityData The {@link EntityData}.
 * @param displayItem The {@link ItemStackConfig} to optionally override the automatic display item with.
 */
@ConfigSerializable
public record EntityPoints(
        double points,
        @NonNull EntityData entityData,
        @NonNull ItemStackConfig displayItem) implements Points {
    @Override
    public double getPoints() {
        return points;
    }

    @Override
    public @NonNull Object getData() {
        return entityData;
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
        if(entityData.entityType() == null) return null;
        lore = new ArrayList<>(lore);

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("entity_type", FormatUtil.formatEntityName(entityData.entityType())));

        if(displayItem.itemType() != null) {
            ItemStackBuilder defaultBuilder = new ItemStackBuilder(logger);
            defaultBuilder.fromItemStackConfig(displayItem, null, placeholders);
            Optional<ItemStack> optional = defaultBuilder.buildItemStack();
            if(optional.isPresent()) {
                return optional.get();
            }
        }

        ItemType itemType = ItemUtils.getItemTypeFromEntityType(logger, entityData.entityType());
        if(itemType == null) itemType = fallback;

        lore.add("<white>Entity type: <entity_type>");

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.setItemType(itemType);
        itemStackBuilder.setEntityType(entityData.entityType());
        itemStackBuilder.setName(AdventureUtil.deserialize(name, placeholders));
        itemStackBuilder.setLore(lore.stream().map(line -> AdventureUtil.deserialize(line, placeholders)).toList());

        return itemStackBuilder.buildItemStack().orElse(null);
    }
}