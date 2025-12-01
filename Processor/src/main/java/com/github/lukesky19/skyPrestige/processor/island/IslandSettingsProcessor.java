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
package com.github.lukesky19.skyPrestige.processor.island;

import com.github.lukesky19.skyPrestige.configuration.data.interfaces.IslandSettings;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.hook.hooks.MagicCobblestoneGeneratorHook;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;

/**
 * This class manages the processing of {@link IslandSettings}.
 */
public class IslandSettingsProcessor {
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param hookManager A {@link HookManager} instance.
     */
    public IslandSettingsProcessor(@NotNull HookManager hookManager) {
        this.hookManager = hookManager;
    }

    /**
     * Process the island settings.
     * @param islandSettings The {@link IslandSettings} to process.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}.
     * @param islandData The {@link IslandData}.
     */
    public void processIslandSettings(
            @NotNull IslandSettings islandSettings,
            @NotNull Island oldIsland,
            @NotNull Island newIsland,
            @NotNull IslandData islandData) {
        MagicCobblestoneGeneratorHook magicCobblestoneGeneratorHook = hookManager.getHook(MagicCobblestoneGeneratorHook.class);

        // Copy island size if configured to do so
        if(islandSettings.keepIslandSize()) {
            newIsland.setProtectionRange(oldIsland.getProtectionRange());
        }

        // Copy island generator upgrades if configured to do so
        if(islandSettings.keepGeneratorUpgrades() && magicCobblestoneGeneratorHook.isHooked()) {
            magicCobblestoneGeneratorHook.copyGeneratorData(oldIsland, newIsland);
        }

        // Copy Island Flags if configured to do so
        if(islandSettings.keepIslandFlags()) {
            newIsland.setFlags(new HashMap<>(oldIsland.getFlags()));
        }

        // Reset prestige points if configured to do so
        if(islandSettings.resetPrestigePoints()) {
            islandData.setPrestigePoints(0);
        }

        // Reset prestige level if configured to do so
        if(islandSettings.resetPrestigeLevel()) {
            islandData.setPrestigeLevel(0);
        }

        // Clear vault if configured to do so
        if(islandSettings.clearVault()) {
            islandData.clearVaultItems();
        }
    }

    /**
     * Process the island settings.
     * @param islandSettings The {@link IslandSettings} to process.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}.
     * @param islandData The {@link IslandData}.
     * @param requiredPrestigePoints The prestige points that were required to trigger the processing of the island settings.
     */
    public void processIslandSettings(
            @NotNull IslandSettings islandSettings,
            @NotNull Island oldIsland,
            @NotNull Island newIsland,
            @NotNull IslandData islandData,
            double requiredPrestigePoints) {
        // Remove required prestige points if configured to do so.
        if(islandSettings.removeRequiredPrestigePoints()) {
            islandData.setPrestigePoints(Math.max(0, islandData.getPrestigePoints() - requiredPrestigePoints));
        }

        processIslandSettings(islandSettings, oldIsland, newIsland, islandData);
    }
}
