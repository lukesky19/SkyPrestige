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
package com.github.lukesky19.skyPrestige.util.item;

import com.github.lukesky19.skyPrestige.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skylib.paper.api.registry.RegistryUtil;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.spawner.Spawner;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to validate and extract data from {@link ItemStack}s.
 */
public class ItemUtils {
    private static final @NonNull Set<ItemType> AXES = new LinkedHashSet<>();
    private static final HashMap<BlockType, ItemType> BLOCK_TO_ITEM_TYPES = new HashMap<>();

    static {
        Registry<@NonNull ItemType> itemTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM);
        itemTypeRegistry.forEach(itemType -> {
            String name = itemType.getKey().toString().toLowerCase();

            if(name.endsWith("_axe")) {
                AXES.add(itemType);
            }
        });

        BLOCK_TO_ITEM_TYPES.put(BlockType.KELP_PLANT, ItemType.KELP);
        BLOCK_TO_ITEM_TYPES.put(BlockType.CAVE_VINES_PLANT, ItemType.GLOW_BERRIES);
    }

    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public ItemUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Checks if the {@link ItemType} is an axe.
     * @param itemType The {@link ItemType} to check.
     * @return true if an axe, otherwise false.
     */
    public static boolean isNotAxe(@NonNull ItemType itemType) {
        return !AXES.contains(itemType);
    }

    /**
     * Get the amount of items in the {@link ItemStack}
     * @apiNote Handles RoseStacker support.
     * @param roseStackerHook A {@link RoseStackerHook} instance.
     * @param itemStack The {@link ItemStack}.
     * @return The stack size.
     */
    public static int getAmount(@NonNull RoseStackerHook roseStackerHook, @NonNull ItemStack itemStack) {
        if(roseStackerHook.isHooked() && dev.rosewood.rosestacker.utils.ItemUtils.hasStoredStackSize(itemStack)) {
            return dev.rosewood.rosestacker.utils.ItemUtils.getStackedItemStackAmount(itemStack);
        }

        return itemStack.getAmount();
    }

    /**
     * Get the {@link EntityType} from the {@link ItemStack}.
     * @param roseStackerHook A {@link RoseStackerHook} instance.
     * @param itemStack The {@link ItemStack}.
     * @return The {@link EntityType} or null if not a spawner / no associated {@link EntityType}.
     */
    public static @Nullable EntityType getEntityType(@NonNull RoseStackerHook roseStackerHook, @NonNull ItemStack itemStack) {
        if(roseStackerHook.isHooked() && dev.rosewood.rosestacker.utils.ItemUtils.hasStoredStackSize(itemStack)) {
            return dev.rosewood.rosestacker.utils.ItemUtils.getStackedItemEntityType(itemStack);
        } else {
            if(itemStack.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
                if(blockStateMeta.getBlockState() instanceof Spawner spawner) {
                    EntitySnapshot entitySnapshot = spawner.getSpawnedEntity();
                    if(entitySnapshot != null) {
                        entitySnapshot.getEntityType();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get the {@link PotionType} from the {@link ItemStack}.
     * @param itemStack The {@link ItemStack}.
     * @return The {@link PotionType} or null if not a potion / no associated {@link PotionType}.
     */
    public static @Nullable PotionType getPotionType(@NonNull ItemStack itemStack) {
        if(itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
            return potionMeta.getBasePotionType();
        }

        return null;
    }

    /**
     * Get the {@link Map} mapping {@link Enchantment} to levels as {@link Integer}s from the {@link ItemStack}.
     * @param itemStack The {@link ItemStack}.
     * @return The {@link Map} mapping {@link Enchantment} to levels as {@link Integer}s or null if no enchantments.
     */
    public static @Nullable Map<Enchantment, Integer> getEnchantments(@NonNull ItemStack itemStack) {
        if(itemStack.getItemMeta() instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
            if(!enchantmentStorageMeta.getStoredEnchants().isEmpty()) {
                return enchantmentStorageMeta.getStoredEnchants();
            }
        } else {
            if(!itemStack.getEnchantments().isEmpty()) {
                return itemStack.getEnchantments();
            }
        }

        return null;
    }

    /**
     * Get the {@link ItemType} for the spawn egg of the {@link EntityType}.
     * @param logger A {@link ComponentLogger} instance.
     * @param entityType The {@link EntityType}.
     * @return The {@link ItemType} or null if no spawn egg exists for that {@link EntityType}.
     */
    public static @Nullable ItemType getItemTypeFromEntityType(
            @NonNull ComponentLogger logger,
            @NonNull EntityType entityType) {
        return RegistryUtil.getItemType(logger, entityType.getKey().getKey() + "_spawn_egg").orElse(null);
    }

    /**
     * Get the {@link ItemType} associated with the {@link BlockType}.
     * @apiNote This mostly uses {@link BlockType#getItemType()}, but there are some special overrides.
     * @param blockType The {@link BlockType}.
     * @return The {@link ItemType} or null.
     */
    public static @Nullable ItemType getItemTypeFromBlockType(@NonNull BlockType blockType) {
        if(BLOCK_TO_ITEM_TYPES.containsKey(blockType)) {
            return BLOCK_TO_ITEM_TYPES.get(blockType);
        } else if(blockType.hasItemType()) {
            return blockType.getItemType();
        }

        return null;
    }
}