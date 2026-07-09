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
package com.github.lukesky19.skyPrestige.integration.hooks;

import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.nms.spawner.SpawnerType;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This class manages interfacing with RoseStacker.
 */
public class RoseStackerHook implements Hook {
    private final @NonNull SkyPlugin plugin;
    private @Nullable RoseStackerAPI roseStackerAPI;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public RoseStackerHook(@NonNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link RoseStackerAPI} from RoseStacker.
     */
    @Override
    public void initialize() {
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("RoseStacker");
        if(plugin != null && plugin.isEnabled()) {
            roseStackerAPI = RoseStackerAPI.getInstance();
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return roseStackerAPI != null;
    }

    /**
     * Checks if the {@link Block} provided is not stacked.
     * @apiNote This method will always return true if not hooked into RoseStacker.
     * @param block The {@link Block} to check.
     * @return true if not stacked or not hooked into RoseStacker, otherwise false.
     */
    public boolean isBlockNotStacked(@NonNull Block block) {
        if(roseStackerAPI == null) return true;

        return !roseStackerAPI.isSpawnerStacked(block) && !roseStackerAPI.isBlockStacked(block);
    }

    /**
     * Get the stack size of the {@link LivingEntity} provided.
     * @apiNote If the provided entity is not stacked or RoseStacker was not hooked into, the method will always return 1.
     * @param entity The {@link LivingEntity} to get the stack size for.
     * @return The entity's stack size or 1.
     */
    public int getStackSize(@NonNull LivingEntity entity) {
        if(roseStackerAPI == null) return 1;

        StackedEntity stackedEntity = roseStackerAPI.getStackedEntity(entity);
        if(stackedEntity != null) {
            return stackedEntity.getStackSize();
        } else {
            return 1;
        }
    }

    /**
     * Get the {@link StackedEntity} for the {@link LivingEntity} provided.
     * @apiNote The {@link StackedEntity} will always be null if not stacked or RoseStacker was not hooked into.
     * @param entity The {@link LivingEntity} to check.
     * @return The {@link StackedEntity} or null if not stacked or RoseStacker was not hooked into.
     */
    public @Nullable StackedEntity getStackedEntity(@NonNull LivingEntity entity) {
        if(roseStackerAPI == null) return null;

        return roseStackerAPI.getStackedEntity(entity);
    }

    /**
     * Get a new {@link ItemStack} created from the provided ItemStack and amount.
     * @apiNote If RoseStacker is not hooked into, it will just update the provided ItemStack's amount and return the same ItemStack.
     * @param itemStack The {@link ItemStack} to update the stack size for.
     * @param amount The updated stack size.
     * @return The updated {@link ItemStack}.
     */
    public @NonNull ItemStack setStackSize(@NonNull ItemStack itemStack, int amount) {
        if(!isHooked()) {
            itemStack.setAmount(amount);
            return itemStack;
        }

        Material material = itemStack.getType();
        if(material.equals(Material.SPAWNER)) {
            SpawnerType spawnerType = ItemUtils.getStackedItemSpawnerType(itemStack);
            return ItemUtils.getSpawnerAsStackedItemStack(spawnerType, amount);
        } else {
            return ItemUtils.getBlockAsStackedItemStack(material, amount);
        }
    }

    /**
     * Get the stack size of the {@link ItemStack} provided.
     * @apiNote If RoseStacker was not hooked into, the {@link ItemStack}'s amount will be returned instead.
     * @param itemStack The {@link ItemStack} to get the stack size for.
     * @return The amount of items in the stack size or the {@link ItemStack} amount.
     */
    public int getStackSize(@NonNull ItemStack itemStack) {
        int stackSize = itemStack.getAmount();
        if(roseStackerAPI == null) return stackSize;

        return Math.max(stackSize, ItemUtils.getStackedItemStackAmount(itemStack));
    }
}