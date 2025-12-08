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
package com.github.lukesky19.skyPrestige.placeholderapi;

import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.data.manager.LeaderboardManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages the PlaceholderAPI expansion for the plugin.
 */
public class PlaceholderManager {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull LeaderboardManager leaderboardManager;
    private final @NotNull HookManager hookManager;

    private @Nullable SkyPrestigeExpansion skyPrestigeExpansion;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param leaderboardManager A {@link LeaderboardManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PlaceholderManager(
            @NotNull SkyPlugin plugin,
            @NotNull IslandDataManager islandDataManager,
            @NotNull LeaderboardManager leaderboardManager,
            @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.islandDataManager = islandDataManager;
        this.leaderboardManager = leaderboardManager;
        this.hookManager = hookManager;
    }

    /**
     * Reload the PlaceholderAPI expansion.
     */
    public void reload() {
        unregisterExpansion();

        registerExpansion();
    }

    /**
     * This method registers the PlaceholderAPI expansion if PlaceholderAPI is enabled.
     */
    public void registerExpansion() {
        if(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if(skyPrestigeExpansion == null) {
                skyPrestigeExpansion = new SkyPrestigeExpansion(islandDataManager, leaderboardManager, hookManager);
            }

            skyPrestigeExpansion.register();
        }
    }

    /**
     * This method unregisters the PlaceholderAPI expansion if PlaceholderAPI is enabled.
     */
    public void unregisterExpansion() {
        if(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if(skyPrestigeExpansion != null) {
                skyPrestigeExpansion.unregister();
            }
        }
    }
}
