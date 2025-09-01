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
package com.github.lukesky19.skyPrestige.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is used to check {@link BlockType}s.
 */
public class BlockTypeUtils {
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
    public BlockTypeUtils() {
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
}
