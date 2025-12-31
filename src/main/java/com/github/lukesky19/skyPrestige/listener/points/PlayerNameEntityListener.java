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

import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigePointsConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import io.papermc.paper.event.player.PlayerNameEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player names an entity on an island and increments prestige points.
 */
public class PlayerNameEntityListener extends PrestigePointsListener<PlayerNameEntityEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerNameEntityListener(
            @NotNull SkyPlugin plugin,
            @NotNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, prestigePointsConfigManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player names an entity on an island and increments prestige points.
     * @param playerNameEntityEvent A {@link PlayerNameEntityEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerNameEntity(PlayerNameEntityEvent playerNameEntityEvent) {
        process(playerNameEntityEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<PlayerNameEntityEvent> extractor() {
        return playerNameEntityEvent -> {
            Player player = playerNameEntityEvent.getPlayer();
            Entity entity = playerNameEntityEvent.getEntity();
            EntityType entityType = entity.getType();

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setEntityType(entityType);
            eventContext.setAmount(1);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull PrestigePointsConfig prestigePointsConfig, @NotNull IslandData islandData, @NotNull PlayerNameEntityEvent playerNameEntityEvent, @NotNull EventContext eventContext) {
        @Nullable EntityType entityType = eventContext.getEntityType();
        if(entityType == null) return;

        @Nullable Double prestigePoints = prestigePointsConfig.prestigePointsMapping().getNamePrestigePoints(entityType);
        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}