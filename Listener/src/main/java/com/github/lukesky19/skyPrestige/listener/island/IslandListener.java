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
package com.github.lukesky19.skyPrestige.listener.island;

import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.interfaces.island.IslandSettingsInterface;
import com.github.lukesky19.skyPrestige.configuration.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.dataHandler.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandResetEvent;
import world.bentobox.bentobox.database.objects.Island;

/**
 * Listens for when an island is created or reset to store or update the island id stored in the database.
 */
public class IslandListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull IslandSettingsProcessor islandSettingsProcessor;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     */
    public IslandListener(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull IslandSettingsProcessor islandSettingsProcessor) {
        this.logger = plugin.getComponentLogger();
        this.settingsManager = settingsManager;
        this.databaseManager = databaseManager;
        this.islandDataManager = islandDataManager;
        this.islandSettingsProcessor = islandSettingsProcessor;
    }

    /**
     * Listens to when an island is created and if the island was not a part of a prestige, adds the island id to the database.
     * @param islandCreatedEvent An {@link IslandCreatedEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandCreation(IslandCreatedEvent islandCreatedEvent) {
        Island island = islandCreatedEvent.getIsland();
        String islandId = island.getUniqueId();

        databaseManager.getIslandIdsTable().insertIslandId(island.getUniqueId());

        IslandData islandData = new IslandData();
        islandDataManager.setData(islandId, islandData);
    }

    /**
     * Listens to when an island is reset and if the island was not a part of a prestige, and resets the island's prestige points and prestige level if configured.
     * @param islandResetEvent An {@link IslandResetEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandReset(IslandResetEvent islandResetEvent) {
        Island oldIsland = islandResetEvent.getOldIsland();
        String oldIslandId = oldIsland.getUniqueId();
        Island newIsland = islandResetEvent.getIsland();

        // Retrieve the IslandData for the old island.
        @Nullable IslandData islandData = islandDataManager.getData(oldIslandId);
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for the island id " + oldIslandId + "."));
            return;
        }

        @Nullable Settings settings = settingsManager.getConfiguration();
        if(settings != null) {
            IslandSettingsInterface islandSettings = settings.islandResetSettings();

            islandSettingsProcessor.processIslandSettings(islandSettings, oldIsland, newIsland, islandData);
        }
    }
}
