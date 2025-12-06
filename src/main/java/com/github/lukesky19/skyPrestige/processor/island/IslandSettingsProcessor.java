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

import com.github.lukesky19.skyPrestige.configuration.interfaces.IslandSettingsInterface;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.hooks.MagicCobblestoneGeneratorHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.HashMap;

/**
 * This class manages the processing of {@link IslandSettingsInterface}.
 */
public class IslandSettingsProcessor {
    private final @NotNull ComponentLogger logger;
    private final @NotNull HookManager hookManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull IslandDataManager islandDataManager;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     */
    public IslandSettingsProcessor(
            @NotNull SkyPlugin plugin,
            @NotNull HookManager hookManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull IslandDataManager islandDataManager) {
        this.logger = plugin.getComponentLogger();
        this.hookManager = hookManager;
        this.databaseManager = databaseManager;
        this.islandDataManager = islandDataManager;
    }

    /**
     * Process the island settings.
     * @param islandSettings The {@link IslandSettingsInterface} to process.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}.
     * @param oldIslandData The {@link IslandData}.
     */
    public void processIslandSettings(
            @NotNull IslandSettingsInterface islandSettings,
            @NotNull Island oldIsland,
            @NotNull Island newIsland,
            @NotNull IslandData oldIslandData) {
        IslandData newIslandData = oldIslandData.clone();
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        IslandWorldManager islandWorldManager = bentoBoxHook.getIslandWorldManager();
        MagicCobblestoneGeneratorHook magicCobblestoneGeneratorHook = hookManager.getHook(MagicCobblestoneGeneratorHook.class);

        // Copy island size if configured to do so
        if(islandSettings.keepIslandSize()) {
            newIsland.setProtectionRange(oldIsland.getProtectionRange());
        }

        // If an island member has a higher protection range permission, update the island's protection range.
        newIsland.getMemberSet().stream().map(User::getInstance).forEach(islandMemberUser -> {
            int islandValue = newIsland.getProtectionRange();
            int permissionValue = islandMemberUser.getPermissionValue(
                    islandWorldManager.getAddon(newIsland.getWorld())
                            .map(GameModeAddon::getPermissionPrefix)
                            .orElse("")
                            + "island.range",
                    newIsland.getProtectionRange());

            if(permissionValue > islandValue) {
                newIsland.setProtectionRange(permissionValue);
            }
        });

        // Copy island generator upgrades if configured to do so
        if(islandSettings.keepGeneratorUpgrades() && magicCobblestoneGeneratorHook.isHooked()) {
            magicCobblestoneGeneratorHook.copyGeneratorData(oldIsland, newIsland);
        }

        // Copy Island Flags if configured to do so
        if(islandSettings.keepIslandFlags()) {
            newIsland.setFlags(new HashMap<>(oldIsland.getFlags()));
        } else {
            // Set default settings
            newIsland.setFlagsDefaults();
        }

        // Reset prestige points if configured to do so
        if(islandSettings.resetPrestigePoints()) {
            newIslandData.setPrestigePoints(0);
        }

        // Reset prestige level if configured to do so
        if(islandSettings.resetPrestigeLevel()) {
            newIslandData.setPrestigeLevel(0);
        }

        // Clear vault if configured to do so
        if(islandSettings.clearVault()) {
            newIslandData.clearVaultItems();
        }

        islandDataManager.removeDataByIdentifier(oldIsland.getUniqueId());
        islandDataManager.setData(newIsland.getUniqueId(), newIslandData);

        databaseManager.getIslandIdsTable().updateIslandId(oldIsland.getUniqueId(), newIsland.getUniqueId())
                .thenAccept(v1 ->
                        databaseManager.getIslandDataTable().saveIslandData(newIsland.getUniqueId(), newIslandData).exceptionally(ex -> {
                                    logger.error(AdventureUtil.deserialize("Failed to save island data for new island id: " + newIsland.getUniqueId() + ". Error: " + ex.getMessage()));
                                    return null;
                                })
                                .exceptionally(ex -> {
                                    logger.error(AdventureUtil.deserialize("Failed to update old island id " + oldIsland.getUniqueId() + " to new island id " + newIsland.getUniqueId() + ". Error: " + ex.getMessage()));
                                    return null;
                                }));

        // Update island
        IslandsManager.updateIsland(newIsland);
    }

    /**
     * Process the island settings.
     * @param islandSettings The {@link IslandSettingsInterface} to process.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}.
     * @param islandData The {@link IslandData}.
     * @param requiredPrestigePoints The prestige points that were required to trigger the processing of the island settings.
     * @param prestigeLevel The prestige level to update the island data with.
     */
    public void processIslandSettings(
            @NotNull IslandSettingsInterface islandSettings,
            @NotNull Island oldIsland,
            @NotNull Island newIsland,
            @NotNull IslandData islandData,
            double requiredPrestigePoints,
            int prestigeLevel) {
        IslandData newIslandData = islandData.clone();

        // Update the prestige level
        newIslandData.setPrestigeLevel(prestigeLevel);

        // Remove required prestige points if configured to do so.
        if(islandSettings.removeRequiredPrestigePoints()) {
            newIslandData.removePrestigePoints(requiredPrestigePoints);
        }

        processIslandSettings(islandSettings, oldIsland, newIsland, newIslandData);
    }
}
