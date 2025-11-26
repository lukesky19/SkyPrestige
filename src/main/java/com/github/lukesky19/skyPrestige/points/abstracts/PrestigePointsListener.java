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
package com.github.lukesky19.skyPrestige.points.abstracts;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.points.context.EventContext;
import com.github.lukesky19.skyPrestige.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.points.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.settings.Settings;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.Optional;
import java.util.UUID;

/**
 * This class is used to create listeners that increment prestige points.
 * @param <E> The {@link Event} to listen for and process.
 */
public abstract class PrestigePointsListener<E extends Event> implements Listener {
    /**
     * A {@link SkyPrestige} instance.
     */
    protected final @NotNull SkyPrestige skyPrestige;
    /**
     * A {@link ComponentLogger} instance.
     */
    protected final @NotNull ComponentLogger logger;
    /**
     * A {@link SettingsManager} instance.
     */
    protected final @NotNull SettingsManager settingsManager;
    /**
     * An {@link IslandDataManager} instance.
     */
    protected final @NotNull IslandDataManager islandDataManager;
    /**
     * A {@link HookManager} instance.
     */
    protected final @NotNull HookManager hookManager;
    /**
     * A {@link MultiplierManager} instance.
     */
    protected final @NotNull MultiplierManager multiplierManager;
    /**
     * An {@link IslandsManager} instance.
     */
    protected final @NotNull IslandsManager islandsManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    protected PrestigePointsListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
        this.multiplierManager = multiplierManager;
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
    }

    /**
     * An {@link EventContextExtractor} that extracts the required data and returns an {@link EventContext}.
     * @return An {@link EventContextExtractor}.
     */
    protected abstract @NotNull EventContextExtractor<E> extractor();

    /**
     * Process the event provided.
     * @param event The event to process.
     */
    protected void process(E event) {
        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        @Nullable EventContext eventContext = extractor().extract(event);
        if(eventContext == null) return;
        @Nullable Player player = eventContext.getPlayer();
        if(player == null) return;
        @Nullable UUID playerId = eventContext.getPlayerId();
        if(playerId == null) return;

        // AFK check
        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        if(skyPlayTimeHook.isHooked() && !settings.awardPointsWhileAfk() && skyPlayTimeHook.isPlayerAFK(playerId)) return;

        // Island Check
        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(player.getLocation());
        if(optionalIsland.isEmpty()) return;
        Island island = optionalIsland.get();

        // Island Member Check
        if(!island.getMemberSet().contains(playerId)) return;

        // Island Data check.
        IslandData islandData = islandDataManager.getIslandData(island.getUniqueId());
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        // Handle the event and event context
        handle(settings, islandData, event, eventContext);
    }

    /**
     * Handles the event and event context to increment prestige points.
     * @param settings The plugin's {@link Settings}.
     * @param islandData The {@link IslandData} to add prestige points to.
     * @param event The {@link E}. The event the class is handling.
     * @param eventContext The {@link EventContext}.
     */
    protected abstract void handle(
            @NotNull Settings settings,
            @NotNull IslandData islandData,
            @NotNull E event,
            @NotNull EventContext eventContext);

    /**
     * Adds prestiges points to the island data.
     * This takes the base prestige points, multiplies it by the amount, and then multiplies it by the modifier.
     * @param islandData The {@link IslandData} to add prestige points to.
     * @param basePrestigePoints The base prestige points.
     * @param amount The amount to multiply the base prestige points to.
     */
    protected void addPrestigePoints(@NotNull IslandData islandData, double basePrestigePoints, int amount) {
        islandData.addPrestigePoints((basePrestigePoints * amount) * multiplierManager.getMultiplier());
    }
}
