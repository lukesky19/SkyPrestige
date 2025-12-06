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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player milks a cow on an island and increments prestige points.
 */
public class PlayerMilkCowListener extends PrestigePointsListener<PlayerInteractEntityEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerMilkCowListener(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player milks a cow on an island and increments prestige points.
     * @param playerInteractEntityEvent A {@link PlayerInteractEntityEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCowMilked(PlayerInteractEntityEvent playerInteractEntityEvent) {
        process(playerInteractEntityEvent);
    }

    @Override
    protected @NotNull EventContextExtractor<PlayerInteractEntityEvent> extractor() {
        return playerInteractEntityEvent -> {
            Player player = playerInteractEntityEvent.getPlayer();
            EntityType entityType = playerInteractEntityEvent.getRightClicked().getType();
            if(!entityType.equals(EntityType.COW) && !entityType.equals(EntityType.MOOSHROOM)) return null;
            ItemType itemTypeUsed = player.getInventory().getItemInMainHand().getType().asItemType();
            if(itemTypeUsed == null) return null;
            if(!itemTypeUsed.equals(ItemType.BUCKET) && !itemTypeUsed.equals(ItemType.BOWL)) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setItemType(itemTypeUsed);
            eventContext.setEntityType(entityType);
            eventContext.setAmount(1);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull PlayerInteractEntityEvent playerInteractEntityEvent, @NotNull EventContext eventContext) {
        @Nullable ItemType itemType = eventContext.getItemType();
        if(itemType == null) return;
        @Nullable EntityType entityType = eventContext.getEntityType();
        if(entityType == null) return;

        double prestigePoints = 0;
        if(itemType.equals(ItemType.BUCKET)) {
            if(entityType.equals(EntityType.COW)) {
                @Nullable Double entityPrestigePoints = settings.prestigePointsMapping().getMilkPrestigePoints(entityType);

                if(entityPrestigePoints != null) prestigePoints += entityPrestigePoints;
            }
        } else if(itemType.equals(ItemType.BOWL)) {
            if(entityType.equals(EntityType.MOOSHROOM)) {
                @Nullable Double entityPrestigePoints = settings.prestigePointsMapping().getMilkPrestigePoints(entityType);
                if(entityPrestigePoints != null) prestigePoints += entityPrestigePoints;

                @Nullable Double itemPrestigePoints = settings.prestigePointsMapping().getFillPrestigePoints(ItemType.MUSHROOM_STEW);
                if(itemPrestigePoints != null) prestigePoints += itemPrestigePoints;
            }
        }

        if(prestigePoints <= 0) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }
}