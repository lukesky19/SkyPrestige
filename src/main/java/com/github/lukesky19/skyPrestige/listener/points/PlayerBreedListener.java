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
package com.github.lukesky19.skyPrestige.listener.points;

import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.hooks.RoseStackerHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player breeds two entities on an island and increments prestige points.
 */
public class PlayerBreedListener extends PrestigePointsListener<EntityBreedEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerBreedListener(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player breeds two entities on an island and increments prestige points.
     * @param entityBreedEvent An {@link EntityBreedEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent entityBreedEvent) {
        process(entityBreedEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<EntityBreedEvent> extractor() {
        return entityBreedEvent -> {
            LivingEntity breeder = entityBreedEvent.getBreeder();
            if(!(breeder instanceof Player player)) return null;
            LivingEntity bredEntity = entityBreedEvent.getEntity();
            EntityType entityType = bredEntity.getType();

            int amount = 1;
            RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);
            if(roseStackerHook.isHooked()) {
                int stackSize = roseStackerHook.getStackSize(bredEntity);
                if(stackSize > 1) {
                    amount = getBredAmount(stackSize);
                }
            }

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setEntityType(entityType);
            eventContext.setAmount(amount);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull EntityBreedEvent entityBreedEvent, @NotNull EventContext eventContext) {
        @Nullable EntityType entityType = eventContext.getEntityType();
        if(entityType == null) return;

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getBreedPrestigePoints(entityType);
        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }

    /**
     * Calculate the amount of entities bred from a stack size.
     * If the stack size is less than or equal to 0, 0 is returned.
     * If the stack size is not divisible by 2, 1 is subtracted from the stack size before dividing by 2.
     * Otherwise, the stack size divided by two is returned.
     * @param stackSize The amount of entities stacked.
     * @return The amount of entities bred.
     */
    private int getBredAmount(int stackSize) {
        if(stackSize <= 0) return 0;

        if (stackSize % 2 != 0) {
            stackSize = stackSize - 1;
            if(stackSize <= 0) return 0;
        }

        return stackSize / 2;
    }
}