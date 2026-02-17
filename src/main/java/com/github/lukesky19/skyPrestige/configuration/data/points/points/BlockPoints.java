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

import com.github.lukesky19.skyPrestige.configuration.data.points.data.BlockData;
import com.github.lukesky19.skyPrestige.util.item.ItemUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.format.FormatUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This record holds the configuration for the prestige points for a particular block.
 * @param points The points.
 * @param blockData The {@link BlockData}.
 * @param displayItem The {@link ItemStackConfig} to optionally override the automatic display item with.
 */
@ConfigSerializable
public record BlockPoints(
        double points,
        @NotNull BlockData blockData,
        @NotNull ItemStackConfig displayItem) implements Points {
    @Override
    public double getPoints() {
        return points;
    }

    @Override
    public @NotNull Object getData() {
        return blockData;
    }

    @Override
    public @NotNull ItemStackConfig getDisplayItemStackConfig() {
        return displayItem;
    }

    @Override
    public @Nullable ItemStack createDisplayItemStack(
            @NotNull ComponentLogger logger,
            @NotNull ItemType fallback,
            @NotNull String name,
            @NotNull List<String> lore) {
        lore = new ArrayList<>(lore);

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("entity_type", blockData.entityType() != null ? FormatUtil.formatEntityName(blockData.entityType()) : "Any"));
        placeholders.add(Placeholder.parsed("age", blockData.age() != null ? String.valueOf(blockData.age()) : "Any"));
        placeholders.add(Placeholder.parsed("water_logged", blockData.waterLogged() != null ? String.valueOf(blockData.waterLogged()).toLowerCase() : "Either"));

        if(displayItem.itemType() != null) {
            ItemStackBuilder defaultBuilder = new ItemStackBuilder(logger);
            defaultBuilder.fromItemStackConfig(displayItem, null, null, placeholders);
            Optional<ItemStack> optional = defaultBuilder.buildItemStack();
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        @Nullable ItemType itemType = null;
        @Nullable BlockType blockType = blockData.blockType();
        if(blockType != null) {
            itemType = ItemUtils.getItemTypeFromBlockType(blockType);
        }
        if(itemType == null) itemType = fallback;

        if(blockData.entityType() != null) {
            lore.add("<white>Entity type: <entity_type>");
        }

        if(blockData.age() != null) {
            lore.add("<white>Age: <age>");
        }

        if(blockData.waterLogged() != null) {
            lore.add("<white>Waterlogged: <water_logged>");
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.setItemType(itemType);
        if(blockData.entityType() != null) itemStackBuilder.setEntityType(blockData.entityType());
        itemStackBuilder.setName(AdventureUtil.deserialize(name, placeholders));
        itemStackBuilder.setLore(lore.stream().map(line -> AdventureUtil.deserialize(line, placeholders)).toList());

        return itemStackBuilder.buildItemStack().orElse(null);
    }
}