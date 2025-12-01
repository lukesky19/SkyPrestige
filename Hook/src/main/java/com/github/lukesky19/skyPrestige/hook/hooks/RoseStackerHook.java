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
package com.github.lukesky19.skyPrestige.hook.hooks;

import com.github.lukesky19.skyPrestige.core.abstracts.SkyPlugin;
import com.github.lukesky19.skyPrestige.hook.interfaces.Hook;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages interfacing with RoseStacker.
 */
public class RoseStackerHook implements Hook {
    private final @NotNull SkyPlugin plugin;
    private @Nullable RoseStackerAPI roseStackerAPI;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public RoseStackerHook(@NotNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link RoseStackerAPI} from RoseStacker.
     */
    @Override
    public void initialize() {
        @Nullable Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("RoseStacker");
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
    public boolean isBlockNotStacked(@NotNull Block block) {
        if(roseStackerAPI == null) return true;

        return !roseStackerAPI.isSpawnerStacked(block) && !roseStackerAPI.isBlockStacked(block);
    }

    /**
     * Get the stack size of the {@link LivingEntity} provided.
     * @apiNote If the provided entity is not stacked or RoseStacker was not hooked into, the method will always return 1.
     * @param entity The {@link LivingEntity} to get the stack size for.
     * @return The entity's stack size or 1.
     */
    public int getStackSize(@NotNull LivingEntity entity) {
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
    public @Nullable StackedEntity getStackedEntity(@NotNull LivingEntity entity) {
        if(roseStackerAPI == null) return null;

        return roseStackerAPI.getStackedEntity(entity);
    }
}
