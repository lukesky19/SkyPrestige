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
package com.github.lukesky19.skyPrestige.processor.prestige;

import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.*;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.player.PlayerSettingsProcessor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;

/**
 * This class handles the processing of prestige settings.
 */
public class PrestigeSettingsProcessor {
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull DatabaseManager databaseManager;

    private final @NotNull IslandSettingsProcessor islandSettingsProcessor;
    private final @NotNull PlayerSettingsProcessor playerSettingsProcessor;

    /**
     * Constructor
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param playerSettingsProcessor A {@link PlayerSettingsProcessor} instance.
     */
    public PrestigeSettingsProcessor(
            @NotNull IslandDataManager islandDataManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull IslandSettingsProcessor islandSettingsProcessor,
            @NotNull PlayerSettingsProcessor playerSettingsProcessor) {
        this.islandDataManager = islandDataManager;
        this.databaseManager = databaseManager;

        this.islandSettingsProcessor = islandSettingsProcessor;
        this.playerSettingsProcessor = playerSettingsProcessor;
    }

    /**
     * Process and apply {@link PrestigeConfig.PrestigeSettings}.
     * @param prestigingPlayer The {@link Player} prestiging their island.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}
     * @param onlineIslandMembers A {@link List} of {@link Player}s for online members.
     * @param offlineIslandMembers A {@link List} of {@link OfflinePlayer} for offline members.
     * @param islandData The new island's {@link IslandData}.
     * @param requiredPrestigePoints The prestige points that were required to prestige.
     * @param prestigeSettings The {@link PrestigeConfig.PrestigeSettings}.
     */
    public void processPrestigeSettings(
            @NotNull Player prestigingPlayer,
            @NotNull Island oldIsland,
            @NotNull Island newIsland,
            @NotNull List<Player> onlineIslandMembers,
            @NotNull List<OfflinePlayer> offlineIslandMembers,
            @NotNull IslandData islandData,
            double requiredPrestigePoints,
            @NotNull PrestigeConfig.PrestigeSettings prestigeSettings) {
        // Process Island Settings
        islandSettingsProcessor.processIslandSettings(
                prestigeSettings.islandSettings(),
                oldIsland,
                newIsland,
                islandData,
                requiredPrestigePoints);

        // Process Player Settings
        playerSettingsProcessor.processPlayerSettings(
                prestigeSettings.playerSettings(),
                prestigingPlayer,
                onlineIslandMembers,
                offlineIslandMembers,
                prestigeSettings.startingMoney(),
                prestigeSettings.giveStartingMoneyToAllIslandMembers());

        // Update the island id the island data is stored under
        updateIslandData(oldIsland.getUniqueId(), newIsland.getUniqueId(), islandData);

        // Update the database
        updateDatabase(
                newIsland.getWorld(),
                oldIsland.getMinX(),
                oldIsland.getMaxX(),
                oldIsland.getMinZ(),
                oldIsland.getMaxZ(),
                oldIsland.getUniqueId(),
                newIsland.getUniqueId(),
                islandData,
                offlineIslandMembers);
    }

    /**
     * Update the prestige level and which island id the island data is stored under.
     * @param oldIslandId The old island id.
     * @param newIslandId The new island id.
     * @param islandData The {@link IslandData} to store.
     */
    private void updateIslandData(
            @NotNull String oldIslandId,
            @NotNull String newIslandId,
            @NotNull IslandData islandData) {
        // Update the prestige level
        islandData.setPrestigeLevel(islandData.getPrestigeLevel() + 1);

        // Store the island data under the new island id.
        islandDataManager.setIslandData(newIslandId, islandData);

        // Remove the old island data.
        islandDataManager.removeIslandData(oldIslandId);
    }

    /**
     * Flush changes to the database.
     * @param islandWorld The {@link World} the old island is in.
     * @param minX The min X coordinate of the old island.
     * @param maxX The max X coordinate of the old island.
     * @param minZ The min Z coordinate of the old island.
     * @param maxZ The max Z coordinate of the old island.
     * @param oldIslandId The old island id.
     * @param newIslandId The new island data.
     * @param islandData The {@link IslandData} to save.
     * @param offlineMemberIds A {@link List} of {@link UUID} for offline island members.
     */
    private void updateDatabase(
            @NotNull World islandWorld,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            @NotNull String oldIslandId,
            @NotNull String newIslandId,
            @NotNull IslandData islandData,
            @NotNull List<OfflinePlayer> offlineMemberIds) {
        IslandIdsTable islandIdsTable = databaseManager.getIslandIdsTable();
        OfflinePrestigeTable offlinePrestigeTable = databaseManager.getOfflinePrestigeTable();
        PlayerLogoutLocationsTables playerLogoutLocationsTables = databaseManager.getPlayerLogoutLocationsTables();
        PlayerTeleportTable playerTeleportTable = databaseManager.getPlayerTeleportTable();
        IslandDataTable islandDataTable = databaseManager.getIslandDataTable();

        islandIdsTable.updateIslandId(oldIslandId, newIslandId).thenAccept(v1 -> {
            // For offline island members, store the data necessary to process the prestige settings for them when they come online.
            offlineMemberIds.forEach(offlinePlayer -> offlinePrestigeTable.insertOfflinePrestige(offlinePlayer.getUniqueId(), newIslandId, islandData.getPrestigeLevel()));

            // Mark players for teleport if their logout location was within the island's bounds.
            playerLogoutLocationsTables.getPlayerIdsWithinByBounds(islandWorld.getName(), minX, maxX, minZ, maxZ)
                    .thenAccept(list -> list.forEach(uuid ->
                            playerTeleportTable.insertPlayerIdAndIslandId(uuid, newIslandId)));

            // Save Island Data
            islandDataTable.saveIslandData(newIslandId, islandData);
        });
    }
}