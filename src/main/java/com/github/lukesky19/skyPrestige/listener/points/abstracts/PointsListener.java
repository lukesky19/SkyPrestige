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
package com.github.lukesky19.skyPrestige.listener.points.abstracts;

import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigePointsConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.hooks.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigePointsManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

/**
 * This class can be extended to create a generic prestige points listener.
 */
public abstract class PointsListener implements Listener {
    /**
     * A {@link JavaPlugin} instance.
     */
    protected final @NonNull SkyPlugin plugin;
    /**
     * A {@link ComponentLogger} instance.
     */
    protected final @NonNull ComponentLogger logger;
    /**
     * A {@link PrestigePointsConfigManager} instance.
     */
    protected final @NonNull PrestigePointsConfigManager prestigePointsConfigManager;
    /**
     * A {@link PrestigePointsManager} instance.
     */
    protected final @NonNull PrestigePointsManager prestigePointsManager;
    /**
     * An {@link IslandDataManager} instance.
     */
    protected final @NonNull IslandDataManager islandDataManager;
    /**
     * A {@link HookManager} instance.
     */
    protected final @NonNull HookManager hookManager;
    /**
     * A {@link MultiplierManager} instance.
     */
    protected final @NonNull MultiplierManager multiplierManager;


    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PointsListener(
            @NonNull SkyPlugin plugin,
            @NonNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NonNull PrestigePointsManager prestigePointsManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager,
            @NonNull MultiplierManager multiplierManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.prestigePointsConfigManager = prestigePointsConfigManager;
        this.prestigePointsManager = prestigePointsManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
        this.multiplierManager = multiplierManager;
    }

    /**
     * Is the player invalid? This is where points should NOT be awarded.
     * Checks creative mode and SkyPlayTime AFK status if hooked.
     * @param player The {@link Player}.
     * @param prestigePointsConfig The {@link PrestigePointsConfig}.
     * @return true if invalid, false if not.
     */
    protected boolean isPlayerInvalid(
            @NonNull Player player,
            @NonNull PrestigePointsConfig prestigePointsConfig) {
        if(player.getGameMode().equals(GameMode.CREATIVE)) return true;

        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        return skyPlayTimeHook.isHooked() && prestigePointsConfig.awardPointsWhileAfk() && skyPlayTimeHook.isPlayerAFK(player);
    }

    /**
     * Is the island valid?
     * The island must be non-null and the player must be a member of the island to be valid.
     * @param player The {@link Player}.
     * @param playerId The player's {@link UUID}.
     * @return The {@link Island} if valid, or null if invalid.
     */
    protected @Nullable Island checkIsland(@NonNull Player player, @NonNull UUID playerId) {
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        Optional<Island> optionalIsland = bentoBoxHook.getIslandAtLocation(player.getLocation());
        if(optionalIsland.isEmpty()) return null;
        Island island = optionalIsland.get();

        // Island Member Check
        if(!island.getMemberSet().contains(playerId)) return null;

        return island;
    }

    /**
     * Is the island's data valid?
     * The {@link IslandData} must be non-null and not exempt from prestige to be valid.
     * @param island The {@link Island}.
     * @return The {@link IslandData} or null.
     */
    protected @Nullable IslandData checkIslandData(@NonNull Island island) {
        // Island Data check.
        IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            logger.error(AdventureUtility.plain("No island data found for island id " + island.getUniqueId() + "."));
            return null;
        }

        // Check for prestige exemption
        if(islandData.isPrestigeExempt()) return null;

        return islandData;
    }
}