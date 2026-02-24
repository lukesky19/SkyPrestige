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

import com.github.lukesky19.skyPrestige.configuration.data.points.data.ItemData;
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
 * This record holds the configuration for the prestige points for a particular item.
 * @param points The points.
 * @param itemData The {@link ItemData}.
 * @param displayItem The {@link ItemStackConfig} to optionally override the automatic display item with.
 */
@ConfigSerializable
public record ItemPoints(
        double points,
        @NonNull ItemData itemData,
        @NonNull ItemStackConfig displayItem) implements Points {
    @Override
    public double getPoints() {
        return points;
    }

    @Override
    public @NonNull Object getData() {
        return itemData;
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
        lore = new ArrayList<>(lore);

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("entity_type", itemData.entityType() != null ? FormatUtil.formatEntityName(itemData.entityType()) : "Any"));
        placeholders.add(Placeholder.parsed("potion_type", itemData.potionType() != null ? FormatUtil.formatPotionTypeName(itemData.potionType()) : "Any"));

        if(!itemData.enchantments().isEmpty()) {
            List<String> enchantmentNames = new ArrayList<>();
            itemData.enchantments().forEach((enchantment, level) ->
                    enchantmentNames.add(FormatUtil.formatKey(enchantment.getKey()) + " " + level));

            if(!enchantmentNames.isEmpty()) {
                String combined = String.join(", ", enchantmentNames);
                placeholders.add(Placeholder.parsed("enchantments", combined));
            } else {
                placeholders.add(Placeholder.parsed("enchantments", "Any"));
            }
        } else {
            placeholders.add(Placeholder.parsed("enchantments", "Any"));
        }

        if(displayItem.itemType() != null) {
            ItemStackBuilder defaultBuilder = new ItemStackBuilder(logger);
            defaultBuilder.fromItemStackConfig(displayItem, null, placeholders);
            Optional<ItemStack> optional = defaultBuilder.buildItemStack();
            if(optional.isPresent()) {
                return optional.get();
            }
        }

        ItemType itemType = itemData.itemType();
        if(itemType == null) itemType = fallback;

        if(itemData.entityType() != null) {
            lore.add("<white>Entity type: <entity_type>");
        }

        if(itemData.potionType() != null) {
            lore.add("<white>Potion type: <potion_type>");
        }

        if(!itemData.enchantments().isEmpty()) {
            lore.add("<white>Enchantments: <enchantments>");
        }

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        itemStackBuilder.setItemType(itemType);
        if(itemData.entityType() != null) itemStackBuilder.setEntityType(itemData.entityType());
        if(itemData.potionType() != null) itemStackBuilder.setPotionType(itemData.potionType());
        if(!itemData.enchantments().isEmpty()) itemStackBuilder.setEnchantments(itemData.enchantments());
        itemStackBuilder.setName(AdventureUtil.deserialize(name, placeholders));
        itemStackBuilder.setLore(lore.stream().map(line -> AdventureUtil.deserialize(line, placeholders)).toList());

        return itemStackBuilder.buildItemStack().orElse(null);
    }
}