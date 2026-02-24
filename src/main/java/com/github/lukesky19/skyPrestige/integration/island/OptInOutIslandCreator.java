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
package com.github.lukesky19.skyPrestige.integration.island;

import com.github.lukesky19.skyPrestige.configuration.data.reset.island.IslandSettings;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;

/**
 * This class can be used to create a new {@link Island} from an old {@link Island}.
 */
public class OptInOutIslandCreator extends AbstractIslandCreator {
    private final @NonNull IslandSettings islandSettings;
    private final boolean prestigeExemptStatus;

    private final @NonNull IslandData islandData;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param player The {@link Player} creating the island.
     * @param world The {@link World} the island is being created in.
     * @param gameModeAddon the {@link GameModeAddon} the island is being created for.
     * @param blueprintName The blueprint name to use.
     * @param oldIsland The old {@link Island}.
     * @param islandData The old {@link Island}'s {@link IslandData}.
     * @param islandSettings The {@link IslandSettings} to process.
     * @param prestigeExemptStatus The new prestige exemption status. false for opt-in, true for opt-out
     */
    public OptInOutIslandCreator(
            @NonNull SkyPlugin plugin,
            @NonNull DatabaseManager databaseManager,
            @NonNull HookManager hookManager,
            @NonNull IslandSettingsProcessor islandSettingsProcessor,
            @NonNull Player player,
            @NonNull World world,
            @NonNull GameModeAddon gameModeAddon,
            @NonNull String blueprintName,
            @NonNull Island oldIsland,
            @NonNull IslandData islandData,
            @NonNull IslandSettings islandSettings,
            boolean prestigeExemptStatus) {
        super(plugin, databaseManager, hookManager, islandSettingsProcessor, player,  world, gameModeAddon,  blueprintName, oldIsland);

        this.islandSettings = islandSettings;
        this.prestigeExemptStatus = prestigeExemptStatus;

        this.islandData = islandData.clone();
    }

    @Override
    protected void beforeBlueprintPaste() {
        if(newIsland == null) return;

        islandData.setPrestigeExempt(prestigeExemptStatus);
        islandData.setLeaderboardExempt(prestigeExemptStatus);

        // Process IslandSettings
        islandSettingsProcessor.processIslandSettings(player, islandSettings, oldIsland, newIsland, islandData);
    }

    @Override
    protected void beforeDeletion() {}

    @Override
    protected void afterDeletion() {}
}