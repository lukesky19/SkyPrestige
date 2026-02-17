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
package com.github.lukesky19.skyPrestige.util.block;

import com.github.lukesky19.skyPrestige.integration.hooks.RoseStackerHook;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.EntityType;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is used to validate and extract data from {@link Block}s.
 */
public class BlockUtils {
    private static final @NotNull Set<BlockType> STRIPPED_LOGS = new LinkedHashSet<>();
    private static final @NotNull Set<BlockType> WAXED_BLOCKS = new LinkedHashSet<>();

    static {
        Registry<@NotNull BlockType> blockTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK);
        blockTypeRegistry.forEach(blockType -> {
            String name = blockType.getKey().toString().toLowerCase();

            if(name.contains("waxed")) {
                WAXED_BLOCKS.add(blockType);
            } else if(name.contains("stripped")) {
                STRIPPED_LOGS.add(blockType);
            }
        });
    }

    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public BlockUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Checks if a {@link BlockType} is that of a stripped log, wood, stem, or hyphae.
     * @param blockType The {@link BlockType} to check.
     * @return true if a stripped log, otherwise false.
     */
    public static boolean isBlockTypeStripped(@NotNull BlockType blockType) {
        return STRIPPED_LOGS.contains(blockType);
    }

    /**
     * Checks if a {@link BlockType} is that of a waxed block.
     * @param blockType The {@link BlockType} to check.
     * @return true if a stripped log, otherwise false.
     */
    public static boolean isBlockTypeWaxed(@NotNull BlockType blockType) {
        return WAXED_BLOCKS.contains(blockType);
    }

    /**
     * Checks if a {@link Block} is waxed.
     * @param block The {@link Block} to check.
     * @param blockType The {@link BlockType} of the block to check.
     * @return true if waxed, otherwise false.
     */
    public static boolean isBlockWaxed(@NotNull Block block, @NotNull BlockType blockType) {
        if(isBlockTypeWaxed(blockType)) {
            return true;
        } else if(block.getState(false) instanceof Sign sign) {
            return sign.isWaxed();
        } else {
            return false;
        }
    }

    /**
     * Get the age of the block.
     * @param blockData The {@link BlockData}.
     * @return The age or null.
     */
    public static @Nullable Integer getAge(@NotNull BlockData blockData) {
        return blockData instanceof Ageable ageable ? ageable.getAge() : null;
    }

    /**
     * Get the water logged status of the block.
     * @param blockData The {@link BlockData}.
     * @return The water logged status or null.
     */
    public static @Nullable Boolean getWaterLogged(@NotNull org.bukkit.block.data.BlockData blockData) {
        return blockData instanceof Waterlogged waterlogged ? waterlogged.isWaterlogged() : null;
    }

    /**
     * Get the {@link EntityType} associated with the block.
     * @param roseStackerHook A {@link RoseStackerHook} instance.
     * @param block The {@link Block}
     * @return The {@link EntityType}.
     */
    public static @Nullable EntityType getEntityType(@NotNull RoseStackerHook roseStackerHook, @NotNull Block block) {
        BlockState blockState = block.getState(false);
        if(roseStackerHook.isHooked()) {
            if(!roseStackerHook.isBlockNotStacked(block)) return null;

            if(blockState instanceof Spawner spawner) {
                return spawner.getSpawnedType();
            }
        } else {
            if(blockState instanceof Spawner spawner) {
                return spawner.getSpawnedType();
            }
        }

        return null;
    }
}