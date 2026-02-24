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

import com.github.lukesky19.skyPrestige.configuration.data.reset.island.IslandSettings;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.hooks.MagicCobblestoneGeneratorHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.HashMap;

/**
 * This class manages the processing of {@link IslandSettings}.
 */
public class IslandSettingsProcessor {
    private final @NonNull ComponentLogger logger;
    private final @NonNull HookManager hookManager;
    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull IslandDataManager islandDataManager;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     */
    public IslandSettingsProcessor(
            @NonNull SkyPlugin plugin,
            @NonNull HookManager hookManager,
            @NonNull DatabaseManager databaseManager,
            @NonNull IslandDataManager islandDataManager) {
        this.logger = plugin.getComponentLogger();
        this.hookManager = hookManager;
        this.databaseManager = databaseManager;
        this.islandDataManager = islandDataManager;
    }

    /**
     * Process the island settings. This method is used to process general island settings, regardless of source.
     * @param player The {@link Player} who initiated the settings being applied.
     * @param islandSettings The {@link IslandSettings} to process.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}.
     * @param islandData The {@link IslandData}.
     */
    public void processIslandSettings(
            @NonNull Player player,
            @NonNull IslandSettings islandSettings,
            @NonNull Island oldIsland,
            @NonNull Island newIsland,
            @NonNull IslandData islandData) {
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        IslandWorldManager islandWorldManager = bentoBoxHook.getIslandWorldManager();
        MagicCobblestoneGeneratorHook magicCobblestoneGeneratorHook = hookManager.getHook(MagicCobblestoneGeneratorHook.class);

        // Copy island size if configured to do so
        if(islandSettings.keepIslandSize()) {
            bentoBoxHook.setIslandSize(player.getUniqueId(), newIsland, newIsland.getProtectionRange(), oldIsland.getProtectionRange());
        } else {
            bentoBoxHook.setIslandSize(player.getUniqueId(), newIsland, newIsland.getProtectionRange(), bentoBoxHook.getDefaultProtectionRange(newIsland.getWorld()));
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
                bentoBoxHook.setIslandSize(player.getUniqueId(), newIsland, islandValue, permissionValue);
            }
        });

        // Copy island generator upgrades if configured to do so
        if(magicCobblestoneGeneratorHook.isHooked()) {
            if(islandSettings.keepGeneratorUpgrades()) {
                magicCobblestoneGeneratorHook.copyGeneratorData(oldIsland, newIsland);
            } else {
                magicCobblestoneGeneratorHook.resetGeneratorData(newIsland);
            }
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

        islandDataManager.removeDataByIdentifier(oldIsland.getUniqueId());
        islandDataManager.setData(newIsland.getUniqueId(), islandData);

        databaseManager.getIslandIdsTable().updateIslandId(oldIsland.getUniqueId(), newIsland.getUniqueId())
                .thenAccept(v1 ->
                        databaseManager.getIslandDataTable().saveIslandData(newIsland.getUniqueId(), islandData).exceptionally(ex -> {
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
}