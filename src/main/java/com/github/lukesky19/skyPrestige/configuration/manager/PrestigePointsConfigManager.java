/*
    SkyPrestige allows players to prestige or reset their Island to unlock rewards after obtaining the required prestige points.
    Copyright (C) 2025 lukeskywlker19

    This program is free software: <white>You can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at <white>Your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    <white>You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyPrestige.configuration.manager;

import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsMapping;
import com.github.lukesky19.skyPrestige.util.number.NumberUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.paper.api.format.FormatUtil;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * This class manages the prestige points configuration.
 */
public class PrestigePointsConfigManager extends SimpleConfigManager<PrestigePointsConfig> {
    private final @NonNull List<ItemStack> displayItemStacks = new LinkedList<>();

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     */
    public PrestigePointsConfigManager(@NonNull SkyPlugin plugin) {
        super(plugin, Path.of(plugin.getDataFolder() + File.separator + "points.yml"), PrestigePointsConfig.class);
    }

    /**
     * Get the number of {@link ItemStack}s that can be displayed.
     * @return The count.
     */
    public int getDisplayItemStackCount() {
        return displayItemStacks.size();
    }

    /**
     * Get the {@link ItemStack} for the index.
     * @param index The index.
     * @return The {@link ItemStack} or null if out of bounds.
     */
    public @Nullable ItemStack getItemStackAtIndex(int index) {
        if(index < 0 || index >= displayItemStacks.size()) return null;

        return displayItemStacks.get(index);
    }

    @Override
    public void loadConfiguration() {
        displayItemStacks.clear();

        super.loadConfiguration();

        createDisplayItemStacks();
    }

    private void createDisplayItemStacks() {
        if(configuration == null) return;
        PrestigePointsMapping points = configuration.prestigePointsMapping();

        // Play Time
        ItemStack playTimeStack = points.playTime().createDisplayItemStack(
                logger,
                ItemType.CLOCK,
                "Play Time",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.playTime().getPoints()) + " per 1 second of play time."));
        if(playTimeStack != null) displayItemStacks.add(playTimeStack);

        // Block Break
        ItemStack blockBreakDefaultStack = points.blockBreak().base().createDisplayItemStack(
                logger,
                ItemType.GOLDEN_PICKAXE,
                "Block Break",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.blockBreak().base().getPoints()) + " for breaking blocks."));
        if(blockBreakDefaultStack != null) displayItemStacks.add(blockBreakDefaultStack);
        points.blockBreak().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.GOLDEN_PICKAXE,
                        "Block Break",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for breaking " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Block Place
        ItemStack blockPlaceDefaultStack = points.blockPlace().base().createDisplayItemStack(
                logger,
                ItemType.GRASS_BLOCK,
                "Block Place",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.blockPlace().base().getPoints()) + " for placing blocks."));
        if(blockPlaceDefaultStack != null) displayItemStacks.add(blockPlaceDefaultStack);
        points.blockPlace().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.GRASS_BLOCK,
                        "Block Place",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for placing " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Bone Meal
        ItemStack boneMealDefaultStack = points.boneMeal().base().createDisplayItemStack(
                logger,
                ItemType.BONE_MEAL,
                "Bone Meal",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.boneMeal().base().getPoints()) + " for bone mealing blocks."));
        if(boneMealDefaultStack != null) displayItemStacks.add(boneMealDefaultStack);
        points.boneMeal().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.GRASS_BLOCK,
                        "Bone Meal",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for bone mealing " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Bottle
        ItemStack bottleDefaultStack = points.bottle().base().createDisplayItemStack(
                logger,
                ItemType.GLASS_BOTTLE,
                "Bottling",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.bottle().base().getPoints()) + " for bottling items."));
        if(bottleDefaultStack != null) displayItemStacks.add(bottleDefaultStack);
        points.bottle().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.GLASS_BOTTLE,
                        "Bottling",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for bottling " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Breed
        ItemStack breedDefaultStack = points.breed().base().createDisplayItemStack(
                logger,
                ItemType.WHEAT_SEEDS,
                "Breeding",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.breed().base().getPoints()) + " for breeding entities."));
        if(breedDefaultStack != null) displayItemStacks.add(breedDefaultStack);
        points.breed().overrides().forEach(entityPoints -> {
            if(entityPoints.entityData().entityType() != null) {
                ItemStack overrideStack = entityPoints.createDisplayItemStack(
                        logger,
                        ItemType.WHEAT_SEEDS,
                        "Breeding",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(entityPoints.getPoints()) + " for breeding " + FormatUtil.formatEntityName(entityPoints.entityData().entityType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Brew
        ItemStack brewDefaultStack = points.brew().base().createDisplayItemStack(
                logger,
                ItemType.BREWING_STAND,
                "Brewing",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.brew().base().getPoints()) + " for brewing items."));
        if(brewDefaultStack != null) displayItemStacks.add(brewDefaultStack);
        points.brew().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.BREWING_STAND,
                        "Brewing",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for brewing " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Brush
        ItemStack brushDefaultStack = points.brush().base().createDisplayItemStack(
                logger,
                ItemType.SUSPICIOUS_SAND,
                "Brushing",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.brush().base().getPoints()) + " for brushing blocks."));
        if(brushDefaultStack != null) displayItemStacks.add(brushDefaultStack);
        points.brush().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.SUSPICIOUS_SAND,
                        "Brushing",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for brushing " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Close
        ItemStack closeDefaultStack = points.close().base().createDisplayItemStack(
                logger,
                ItemType.CHEST,
                "Close",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.close().base().getPoints()) + " for closing blocks."));
        if(closeDefaultStack != null) displayItemStacks.add(closeDefaultStack);
        points.close().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.CHEST,
                        "Close",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for closing " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Compost
        ItemStack compostDefaultStack = points.compost().base().createDisplayItemStack(
                logger,
                ItemType.COMPOSTER,
                "Compost",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.compost().base().getPoints()) + " for composting items."));
        if(compostDefaultStack != null) displayItemStacks.add(compostDefaultStack);
        points.compost().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.COMPOSTER,
                        "Compost",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for composting " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Consume
        ItemStack consumeDefaultStack = points.compost().base().createDisplayItemStack(
                logger,
                ItemType.APPLE,
                "Consume",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.consume().base().getPoints()) + " for consuming items."));
        if(consumeDefaultStack != null) displayItemStacks.add(consumeDefaultStack);
        points.consume().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.APPLE,
                        "Consume",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for consuming " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Craft
        ItemStack craftDefaultStack = points.craft().base().createDisplayItemStack(
                logger,
                ItemType.CRAFTING_TABLE,
                "Crafting",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.craft().base().getPoints()) + " for crafting items."));
        if(craftDefaultStack != null) displayItemStacks.add(craftDefaultStack);
        points.craft().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.CRAFTING_TABLE,
                        "Crafting",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for crafting " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Empty
        ItemStack emptyDefaultStack = points.empty().base().createDisplayItemStack(
                logger,
                ItemType.BUCKET,
                "Emptying",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.empty().base().getPoints()) + " for emptying items."));
        if(emptyDefaultStack != null) displayItemStacks.add(emptyDefaultStack);
        points.empty().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.BUCKET,
                        "Emptying",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for emptying " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Enchanting
        ItemStack enchantDefaultStack = points.enchant().base().createDisplayItemStack(
                logger,
                ItemType.ENCHANTING_TABLE,
                "Enchanting",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.enchant().base().getPoints()) + " for enchanting items."));
        if(enchantDefaultStack != null) displayItemStacks.add(enchantDefaultStack);
        points.enchant().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.ENCHANTING_TABLE,
                        "Enchanting",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for enchanting " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Filling
        ItemStack fillDefaultStack = points.fill().base().createDisplayItemStack(
                logger,
                ItemType.WATER_BUCKET,
                "Filling",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.fill().base().getPoints()) + " for filling items."));
        if(fillDefaultStack != null) displayItemStacks.add(fillDefaultStack);
        points.fill().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.WATER_BUCKET,
                        "Filling",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for filling " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Fishing
        ItemStack fishDefaultStack = points.fish().base().createDisplayItemStack(
                logger,
                ItemType.FISHING_ROD,
                "Fishing",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.fish().base().getPoints()) + " for fishing up items."));
        if(fishDefaultStack != null) displayItemStacks.add(fishDefaultStack);
        points.fish().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.FISHING_ROD,
                        "Fishing",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for fishing up " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Harvesting
        ItemStack harvestDefaultStack = points.harvest().base().createDisplayItemStack(
                logger,
                ItemType.SWEET_BERRIES,
                "Harvesting",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.harvest().base().getPoints()) + " for harvesting blocks."));
        if(harvestDefaultStack != null) displayItemStacks.add(harvestDefaultStack);
        points.harvest().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.SWEET_BERRIES,
                        "Harvesting",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for fishing up " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Pickup
        ItemStack pickupDefaultStack = points.pickup().base().createDisplayItemStack(
                logger,
                ItemType.HOPPER,
                "Item Pickup",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.pickup().base().getPoints()) + " for picking up items."));
        if(pickupDefaultStack != null) displayItemStacks.add(pickupDefaultStack);
        points.pickup().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.HOPPER,
                        "Item Pickup",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for picking up " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Drop
        ItemStack dropDefaultStack = points.drop().base().createDisplayItemStack(
                logger,
                ItemType.DROPPER,
                "Item Drop",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.drop().base().getPoints()) + " for dropping items."));
        if(dropDefaultStack != null) displayItemStacks.add(dropDefaultStack);
        points.drop().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.DROPPER,
                        "Item Drop",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for dropping " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Kill
        ItemStack killDefaultStack = points.kill().base().createDisplayItemStack(
                logger,
                ItemType.GOLDEN_SWORD,
                "Killing",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.kill().base().getPoints()) + " for killing entities."));
        if(killDefaultStack != null) displayItemStacks.add(killDefaultStack);
        points.kill().overrides().forEach(entityPoints -> {
            if(entityPoints.entityData().entityType() != null) {
                ItemStack overrideStack = entityPoints.createDisplayItemStack(
                        logger,
                        ItemType.GOLDEN_SWORD,
                        "Killing",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(entityPoints.getPoints()) + " for killing " + FormatUtil.formatEntityName(entityPoints.entityData().entityType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Milk
        ItemStack milkDefaultStack = points.milk().base().createDisplayItemStack(
                logger,
                ItemType.MILK_BUCKET,
                "Milking",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.milk().base().getPoints()) + " for milking entities."));
        if(milkDefaultStack != null) displayItemStacks.add(milkDefaultStack);
        points.milk().overrides().forEach(entityPoints -> {
            if(entityPoints.entityData().entityType() != null) {
                ItemStack overrideStack = entityPoints.createDisplayItemStack(
                        logger,
                        ItemType.MILK_BUCKET,
                        "Milking",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(entityPoints.getPoints()) + " for milking " + FormatUtil.formatEntityName(entityPoints.entityData().entityType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Name Items
        ItemStack itemNameDefaultStack = points.nameItem().base().createDisplayItemStack(
                logger,
                ItemType.ANVIL,
                "Name",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.nameItem().base().getPoints()) + " for naming items."));
        if(itemNameDefaultStack != null) displayItemStacks.add(itemNameDefaultStack);
        points.nameItem().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.ANVIL,
                        "Name Items",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for naming " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Name Entities
        ItemStack nameEntitiesDefaultStack = points.nameEntity().base().createDisplayItemStack(
                logger,
                ItemType.NAME_TAG,
                "Name",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.nameEntity().base().getPoints()) + " for naming entities."));
        if(nameEntitiesDefaultStack != null) displayItemStacks.add(nameEntitiesDefaultStack);
        points.nameEntity().overrides().forEach(entityPoints -> {
            if(entityPoints.entityData().entityType() != null) {
                ItemStack overrideStack = entityPoints.createDisplayItemStack(
                        logger,
                        ItemType.MILK_BUCKET,
                        "Milking",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(entityPoints.getPoints()) + " for naming  " + FormatUtil.formatEntityName(entityPoints.entityData().entityType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Open
        ItemStack openDefaultStack = points.open().base().createDisplayItemStack(
                logger,
                ItemType.CHEST,
                "Open",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.open().base().getPoints()) + " for opening blocks."));
        if(openDefaultStack != null) displayItemStacks.add(openDefaultStack);
        points.open().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.CHEST,
                        "Open",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for opening " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Shear Block
        ItemStack shearBlockDefaultStack = points.shearBlock().base().createDisplayItemStack(
                logger,
                ItemType.SHEARS,
                "Shear",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.shearBlock().base().getPoints()) + " for shearing blocks."));
        if(shearBlockDefaultStack != null) displayItemStacks.add(shearBlockDefaultStack);
        points.shearBlock().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.SHEARS,
                        "Shear",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for shearing " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Shear Entities
        ItemStack shearEntityDefaultStack = points.shearEntity().base().createDisplayItemStack(
                logger,
                ItemType.SHEARS,
                "Shear",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.shearEntity().base().getPoints()) + " for shearing entities."));
        if(shearEntityDefaultStack != null) displayItemStacks.add(shearEntityDefaultStack);
        points.shearEntity().overrides().forEach(entityPoints -> {
            if(entityPoints.entityData().entityType() != null) {
                ItemStack overrideStack = entityPoints.createDisplayItemStack(
                        logger,
                        ItemType.SHEARS,
                        "Shear",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(entityPoints.getPoints()) + " for shearing  " + FormatUtil.formatEntityName(entityPoints.entityData().entityType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Sleep
        ItemStack sleepDefaultStack = points.sleep().base().createDisplayItemStack(
                logger,
                ItemType.RED_BED,
                "Sleep",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.sleep().base().getPoints()) + " for sleeping."));
        if(sleepDefaultStack != null) displayItemStacks.add(sleepDefaultStack);
        points.sleep().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.RED_BED,
                        "Sleep",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for sleeping in " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Smelt
        ItemStack smeltDefaultStack = points.smelt().base().createDisplayItemStack(
                logger,
                ItemType.FURNACE,
                "Smelting",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.smelt().base().getPoints()) + " for smelting items."));
        if(smeltDefaultStack != null) displayItemStacks.add(smeltDefaultStack);
        points.smelt().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.FURNACE,
                        "Smelting",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for smelting " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Strip
        ItemStack stripDefaultStack = points.strip().base().createDisplayItemStack(
                logger,
                ItemType.GOLDEN_AXE,
                "Strip",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.strip().base().getPoints()) + " for stripping blocks."));
        if(stripDefaultStack != null) displayItemStacks.add(stripDefaultStack);
        points.strip().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.GOLDEN_AXE,
                        "Strip",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for stripping " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Tame
        ItemStack tameDefaultStack = points.tame().base().createDisplayItemStack(
                logger,
                ItemType.GOLDEN_CARROT,
                "Tame",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.tame().base().getPoints()) + " for taming entities."));
        if(tameDefaultStack != null) displayItemStacks.add(tameDefaultStack);
        points.tame().overrides().forEach(entityPoints -> {
            if(entityPoints.entityData().entityType() != null) {
                ItemStack overrideStack = entityPoints.createDisplayItemStack(
                        logger,
                        ItemType.GOLDEN_CARROT,
                        "Tame",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(entityPoints.getPoints()) + " for taming  " + FormatUtil.formatEntityName(entityPoints.entityData().entityType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Throw
        ItemStack throwDefaultStack = points.thrown().base().createDisplayItemStack(
                logger,
                ItemType.ENDER_PEARL,
                "Throw",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.thrown().base().getPoints()) + " for throwing items."));
        if(throwDefaultStack != null) displayItemStacks.add(throwDefaultStack);
        points.thrown().overrides().forEach(itemPoints -> {
            if(itemPoints.itemData().itemType() != null) {
                ItemStack overrideStack = itemPoints.createDisplayItemStack(
                        logger,
                        ItemType.ENDER_PEARL,
                        "Throw",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(itemPoints.getPoints()) + " for throwing " + FormatUtil.formatItemTypeName(itemPoints.itemData().itemType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Unwax Block
        ItemStack unWaxBlockDefaultStack = points.unWaxBlock().base().createDisplayItemStack(
                logger,
                ItemType.DIAMOND_AXE,
                "Remove Wax",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.unWaxBlock().base().getPoints()) + " for removing wax from blocks."));
        if(unWaxBlockDefaultStack != null) displayItemStacks.add(unWaxBlockDefaultStack);
        points.unWaxBlock().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.DIAMOND_AXE,
                        "Remove Wax",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for removing wax from " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Unwax Entity
        ItemStack unWaxEntityDefaultStack = points.unWaxEntity().base().createDisplayItemStack(
                logger,
                ItemType.DIAMOND_AXE,
                "Tame",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.unWaxEntity().base().getPoints()) + " for removing wax from entities."));
        if(unWaxEntityDefaultStack != null) displayItemStacks.add(unWaxEntityDefaultStack);
        points.unWaxEntity().overrides().forEach(entityPoints -> {
            if(entityPoints.entityData().entityType() != null) {
                ItemStack overrideStack = entityPoints.createDisplayItemStack(
                        logger,
                        ItemType.DIAMOND_AXE,
                        "Tame",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(entityPoints.getPoints()) + " for removing wax from  " + FormatUtil.formatEntityName(entityPoints.entityData().entityType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Water Log
        ItemStack waterLogDefaultStack = points.waterLog().base().createDisplayItemStack(
                logger,
                ItemType.WATER_BUCKET,
                "Water Log",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.waterLog().base().getPoints()) + " for water logging blocks."));
        if(waterLogDefaultStack != null) displayItemStacks.add(waterLogDefaultStack);
        points.waterLog().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.WATER_BUCKET,
                        "Water Log",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for water logging " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Wax Block
        ItemStack waxBlockDefaultStack = points.waxBlock().base().createDisplayItemStack(
                logger,
                ItemType.HONEYCOMB,
                "Wax",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.waxBlock().base().getPoints()) + " for waxing blocks."));
        if(waxBlockDefaultStack != null) displayItemStacks.add(waxBlockDefaultStack);
        points.waxBlock().overrides().forEach(blockPoints -> {
            if(blockPoints.blockData().blockType() != null) {
                ItemStack overrideStack = blockPoints.createDisplayItemStack(
                        logger,
                        ItemType.HONEYCOMB,
                        "Wax",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(blockPoints.getPoints()) + " for waxing " + FormatUtil.formatBlockTypeName(blockPoints.blockData().blockType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });

        // Wax Entity
        ItemStack waxEntityDefaultStack = points.waxEntity().base().createDisplayItemStack(
                logger,
                ItemType.HONEYCOMB,
                "Wax",
                List.of("<white>You earn " + NumberUtils.formatDecimal(points.waxEntity().base().getPoints()) + " for waxing entities."));
        if(waxEntityDefaultStack != null) displayItemStacks.add(waxEntityDefaultStack);
        points.waxEntity().overrides().forEach(entityPoints -> {
            if(entityPoints.entityData().entityType() != null) {
                ItemStack overrideStack = entityPoints.createDisplayItemStack(
                        logger,
                        ItemType.HONEYCOMB,
                        "Wax",
                        List.of("<white>You earn " + NumberUtils.formatDecimal(entityPoints.getPoints()) + " for removing wax from  " + FormatUtil.formatEntityName(entityPoints.entityData().entityType()) + "."));
                if(overrideStack != null) {
                    displayItemStacks.add(overrideStack);
                }
            }
        });
    }

    @Override
    public @Nullable PrestigePointsConfig migrateConfiguration(@NonNull PrestigePointsConfig prestigePointsConfig) {
        switch(prestigePointsConfig.version()) {
            case 1 -> {
                // latest version, do nothing
                return prestigePointsConfig;
            }

            default -> {
                logger.warn(AdventureUtility.plain("Unknown config version for the prestige points config. Unable to update config."));
                return null;
            }
        }
    }

    @Override
    public boolean validateConfiguration(@Nullable PrestigePointsConfig configuration) {
        return configuration != null;
    }

    @Override
    public void saveDefaultConfiguration() {
        plugin.saveResource("points.yml", false);
    }
}