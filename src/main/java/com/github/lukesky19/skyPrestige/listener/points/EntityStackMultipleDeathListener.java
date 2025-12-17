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
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import dev.rosewood.rosestacker.event.EntityStackMultipleDeathEvent;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for multiple entities from a stack are killed on an island and increments prestige points.
 */
public class EntityStackMultipleDeathListener extends PrestigePointsListener<EntityStackMultipleDeathEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public EntityStackMultipleDeathListener(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a stacked entity died on an island and increments prestige points.
     * @param entityStackMultipleDeathEvent A {@link EntityStackMultipleDeathEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStackedEntityDeath(EntityStackMultipleDeathEvent entityStackMultipleDeathEvent) {
        process(entityStackMultipleDeathEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<EntityStackMultipleDeathEvent> extractor() {
        return entityStackMultipleDeathEvent -> {
            @Nullable Settings settings = settingsManager.getConfiguration();
            if(settings == null) return null;
            StackedEntity stackedEntity = entityStackMultipleDeathEvent.getStack();
            Player player = stackedEntity.getEntity().getKiller();
            if(player == null) return null;
            EntityType entityType = stackedEntity.getEntity().getType();
            int amountKilled = settings.accurateRoseStacker() ? entityStackMultipleDeathEvent.getEntityKillCount() : 1;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setEntityType(entityType);
            eventContext.setAmount(amountKilled);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull EntityStackMultipleDeathEvent entityStackMultipleDeathEvent, @NotNull EventContext eventContext) {
        @Nullable EntityType entityType = eventContext.getEntityType();
        if(entityType == null) return;

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getKillPrestigePoints(entityType);
        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}
