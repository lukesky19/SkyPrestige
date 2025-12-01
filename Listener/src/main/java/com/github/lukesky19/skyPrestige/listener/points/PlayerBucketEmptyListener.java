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
import com.github.lukesky19.skyPrestige.configuration.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.core.abstracts.SkyPlugin;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player empties a bucket on an island and increments prestige points.
 */
public class PlayerBucketEmptyListener extends PrestigePointsListener<PlayerBucketEmptyEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerBucketEmptyListener(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player empties a bucket on an island and increments prestige points.
     * @param playerBucketEmptyEvent A {@link PlayerBucketEmptyEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketEmptyEvent playerBucketEmptyEvent) {
        process(playerBucketEmptyEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<PlayerBucketEmptyEvent> extractor() {
        return playerBucketEmptyEvent -> {
            @Nullable ItemType itemType = playerBucketEmptyEvent.getBucket().asItemType();
            if(itemType == null) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(playerBucketEmptyEvent.getPlayer());
            eventContext.setItemType(itemType);
            eventContext.setAmount(1);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull PlayerBucketEmptyEvent playerBucketEmptyEvent, @NotNull EventContext eventContext) {
        @Nullable ItemType itemType = eventContext.getItemType();
        if(itemType == null) return;

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getEmptyPrestigePoints(itemType);
        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}