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
package com.github.lukesky19.skyPrestige.prestige;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.locale.Locale;
import com.github.lukesky19.skyPrestige.config.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.*;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.*;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;

/**
 * This class handles the processing of prestige settings.
 */
public class PrestigeSettingsProcessor {
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PrestigeSettingsProcessor(
            @NotNull SkyPrestige skyPrestige,
            @NotNull LocaleManager localeManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull HookManager hookManager) {
        this.logger = skyPrestige.getComponentLogger();
        this.localeManager = localeManager;
        this.islandDataManager = islandDataManager;
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;
    }

    /**
     * Process and apply {@link PrestigeConfig.PrestigeSettings}.
     * @param prestigingPlayer The {@link Player} prestiging their island.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}
     * @param onlineIslandMembers A {@link List} of {@link Player}s for online members.
     * @param offlineMemberIds A {@link List} of {@link UUID} for offline members.
     * @param islandData The new island's {@link IslandData}.
     * @param requiredPrestigePoints The prestige points that were required to prestige.
     * @param prestigeSettings The {@link PrestigeConfig.PrestigeSettings}.
     */
    public void processPrestigeSettings(
            @NotNull Player prestigingPlayer,
            @NotNull Island oldIsland,
            @NotNull Island newIsland,
            @NotNull List<Player> onlineIslandMembers,
            @NotNull List<UUID> offlineMemberIds,
            @NotNull IslandData islandData,
            double requiredPrestigePoints,
            @NotNull PrestigeConfig.PrestigeSettings prestigeSettings) {
        // Process Island Settings
        processIslandSettings(oldIsland, newIsland, islandData, requiredPrestigePoints, prestigeSettings.islandSettings());

        // Process Player Settings
        processPlayerSettings(prestigingPlayer, onlineIslandMembers, offlineMemberIds, prestigeSettings);

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
                offlineMemberIds);
    }

    /**
     * Process the {@link PrestigeConfig.IslandSettings} for a prestige level.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}.
     * @param islandData The {@link IslandData} of the new island.
     * @param requiredPrestigePoints The prestige points that were required to prestige.
     * @param islandSettings The {@link PrestigeConfig.IslandSettings} to process.
     */
    private void processIslandSettings(
            @NotNull Island oldIsland,
            @NotNull Island newIsland,
            @NotNull IslandData islandData,
            double requiredPrestigePoints,
            @NotNull PrestigeConfig.IslandSettings islandSettings) {
        MagicCobblestoneGeneratorHook magicCobblestoneGeneratorHook = hookManager.getHook(MagicCobblestoneGeneratorHook.class);

        // Copy island generator upgrades if configured to do so
        if(islandSettings.keepGeneratorUpgrades() && magicCobblestoneGeneratorHook.isHooked()) {
            magicCobblestoneGeneratorHook.copyGeneratorData(oldIsland, newIsland);
        }

        // Copy island size if configured to do so
        if(islandSettings.keepIslandSize()) {
            newIsland.setProtectionRange(oldIsland.getProtectionRange());
        }

        // Reset prestige points if configured to do so
        if(islandSettings.resetPrestigePoints()) {
            islandData.setPrestigePoints(0);
        }

        // Remove required prestige points if configured to do so.
        if(islandSettings.removeRequiredPrestigePoints()) {
            islandData.setPrestigePoints(Math.max(0, islandData.getPrestigePoints() - requiredPrestigePoints));
        }
    }

    /**
     * Process the settings related to players.
     * @param prestigingPlayer The {@link Player} prestiging their island.
     * @param onlineIslandMembers A {@link List} of {@link Player}s that are online and a part of the island's team.
     * @param offlineMemberIds A {@link List} of {@link UUID}s that are offline and a part of the island's team.
     * @param prestigeSettings The {@link PrestigeConfig.PrestigeSettings} to process.
     */
    private void processPlayerSettings(
            @NotNull Player prestigingPlayer,
            @NotNull List<Player> onlineIslandMembers,
            @NotNull List<UUID> offlineMemberIds,
            @NotNull PrestigeConfig.PrestigeSettings prestigeSettings) {
        // Process prestige settings for online players
        processOnlinePrestigeSettings(prestigingPlayer, onlineIslandMembers, prestigeSettings);

        // Process player settings for offline players
        processOfflinePlayerSettings(offlineMemberIds, prestigeSettings.playerSettings());
    }

    /**
     * Process the settings for online players.
     * @param prestigingPlayer The {@link Player} prestiging their island.
     * @param players The {@link List} of {@link Player}s that are a part of the island and are online.
     * @param prestigeSettings The {@link PrestigeConfig.PrestigeSettings} to process.
     */
    private void processOnlinePrestigeSettings(
            @NotNull Player prestigingPlayer,
            @NotNull List<Player> players,
            @NotNull PrestigeConfig.PrestigeSettings prestigeSettings) {
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);

        // Process the prestige settings for each online player
        players.forEach(player -> processPlayerPrestigeSettings(player, player.getUniqueId(), prestigeSettings));

        // Give starting money if configured and the starting money should only be given to the player prestiging the island.
        if(prestigeSettings.startingMoney() > 0 && !prestigeSettings.giveStartingMoneyToAllIslandMembers()) {
            if(economyHook.isHooked()) {
                economyHook.addToBalance(prestigingPlayer, prestigeSettings.startingMoney());
            } else {
                // Display an error if an economy isn't hooked into and starting money is configured.
                prestigingPlayer.sendMessage(AdventureUtil.deserialize("<red>Failed to give a player starting money due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.deserialize("<red>Failed to give a player starting money due to an Economy not being hooked into.</red>"));
            }
        }
    }

    /**
     * Process the {@link PrestigeConfig.PrestigeSettings} for the player provided.
     * @param player The {@link Player} to process settings for.
     * @param playerId The {@link UUID} of the player.
     * @param prestigeSettings The {@link PrestigeConfig.PrestigeSettings} to process.
     */
    private void processPlayerPrestigeSettings(
            @NotNull Player player,
            @NotNull UUID playerId,
            @NotNull PrestigeConfig.PrestigeSettings prestigeSettings) {
        // Locale
        Locale locale = localeManager.getLocale();
        // Settings
        PrestigeConfig.PlayerSettings playerSettings = prestigeSettings.playerSettings();
        PrestigeConfig.InventorySettings inventorySettings = playerSettings.inventorySettings();
        PrestigeConfig.EnderChestSettings enderChestSettings = playerSettings.enderChestSettings();
        PrestigeConfig.PlayTimeSettings playTimeSettings = playerSettings.playTimeSettings();
        // Hooks
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        SkySellWandsHook skySellWandsHook = hookManager.getHook(SkySellWandsHook.class);
        PlayerAuctionsHook playerAuctionsHook = hookManager.getHook(PlayerAuctionsHook.class);

        // Inventory Settings
        if(inventorySettings.resetInventory()) {
            ItemStack emptyStack = ItemType.AIR.createItemStack();

            Inventory memberInventory = player.getInventory();
            int inventorySize = memberInventory.getSize();
            for(int i = 0; i < inventorySize; i++) {
                ItemStack itemStack = memberInventory.getItem(i);
                if(itemStack == null || itemStack.isEmpty()) continue;
                if(inventorySettings.keepInfiniteSellWands() && skySellWandsHook.isInfiniteSellWand(itemStack)) continue;

                memberInventory.setItem(i, emptyStack);
            }

            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeInventoryReset()));
        }

        // Ender Chest Settings
        if(enderChestSettings.resetEnderChest()) {
            ItemStack emptyStack = ItemType.AIR.createItemStack();

            Inventory memberEnderChest = player.getEnderChest();
            int inventorySize = memberEnderChest.getSize();
            for(int i = 0; i < inventorySize; i++) {
                ItemStack itemStack = memberEnderChest.getItem(i);
                if(itemStack == null || itemStack.isEmpty()) continue;
                if(enderChestSettings.keepInfiniteSellWands() && skySellWandsHook.isInfiniteSellWand(itemStack)) continue;

                memberEnderChest.setItem(i, emptyStack);
            }

            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeEnderChestReset()));
        }

        // Exp Settings
        if(playerSettings.resetExp()) {
            player.setLevel(0);
            player.setExp(0);

            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeExperienceReset()));
        }

        // Process Money Settings
        // Check if an economy is hooked into
        if(economyHook.isHooked()) {
            // Reset the island member's balance if configured to do so
            if(playerSettings.resetMoney()) {
                economyHook.removeFromBalance(player, economyHook.getBalance(player));

                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeBalanceReset()));
            }

            // Give starting money if configured and the starting money should be given to all island members
            if(prestigeSettings.startingMoney() > 0 && prestigeSettings.giveStartingMoneyToAllIslandMembers()) {
                economyHook.addToBalance(player, prestigeSettings.startingMoney());

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("money", String.valueOf(prestigeSettings.startingMoney())));

                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeStartingMoneyGiven(), placeholders));
            }
        } else {
            // Display appropriate errors, if any, if no economy was hooked into.
            if(playerSettings.resetMoney() && prestigeSettings.startingMoney() > 0) {
                // Display an error if an economy isn't hooked into and starting money is configured.
                player.sendMessage(AdventureUtil.deserialize("<red>Failed to reset a player's balance or give starting money due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.deserialize("<red>Failed to reset a player's balance or give starting money due to an Economy not being hooked into.</red>"));
            } else if(playerSettings.resetMoney()) {
                // Display an error if the economy isn't hooked into and starting money is configured.
                player.sendMessage(AdventureUtil.deserialize("<red>Failed to reset a player's balance due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.deserialize("<red>Failed to reset a player's balance due to an Economy not being hooked into.</red>"));
            } else if(prestigeSettings.startingMoney() > 0) {
                // Display an error if an economy isn't hooked into and starting money is configured.
                player.sendMessage(AdventureUtil.deserialize("<red>Failed to give a player starting money due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.deserialize("<red>Failed to give a player starting money due to an Economy not being hooked into.</red>"));
            }
        }

        // Process Auction House Settings
        if(playerSettings.resetAuctionItems() && playerAuctionsHook.isHooked()) {
            playerAuctionsHook.clearPlayerAuctions(playerId);

            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeAuctionHouseItemsReset()));
        }

        // SkyPlayTime Settings
        // Check if SkyPlayTime is hooked into.
        if(skyPlayTimeHook.isHooked()) {
            // Reset any play time configured to do so.
            skyPlayTimeHook.resetPlayTime(
                    playerId,
                    playTimeSettings.resetSession(),
                    playTimeSettings.resetDaily(),
                    playTimeSettings.resetWeekly(),
                    playTimeSettings.resetMonthly(),
                    playTimeSettings.resetYearly(),
                    playTimeSettings.resetTotal());
        } else {
            if(playTimeSettings.resetSession()
                    || playTimeSettings.resetDaily()
                    || playTimeSettings.resetWeekly()
                    || playTimeSettings.resetMonthly()
                    || playTimeSettings.resetYearly()
                    || playTimeSettings.resetTotal()) {
                player.sendMessage(AdventureUtil.deserialize("<red>Failed to apply reset a player's play time due to SkyPlayTime not being hooked into. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.deserialize("<red>Failed to apply reset a player's play time due to SkyPlayTime not being hooked into.</red>"));
            }
        }
    }

    /**
     * Process the settings for offline players.
     * @param playerIds The {@link List} of {@link UUID}s that are a part of the island, but are offline.
     * @param playerSettings The {@link PrestigeConfig.PlayerSettings} to process.
     */
    private void processOfflinePlayerSettings(
            @NotNull List<UUID> playerIds,
            @NotNull PrestigeConfig.PlayerSettings playerSettings) {
        PlayerAuctionsHook playerAuctionsHook = hookManager.getHook(PlayerAuctionsHook.class);

        if(playerSettings.resetAuctionItems() && playerAuctionsHook.isHooked()) {
            playerIds.forEach(playerAuctionsHook::clearPlayerAuctions);
        }
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
            @NotNull List<UUID> offlineMemberIds) {
        IslandIdsTable islandIdsTable = databaseManager.getIslandIdsTable();
        OfflinePrestigeTable offlinePrestigeTable = databaseManager.getOfflinePrestigeTable();
        PlayerLogoutLocationsTables playerLogoutLocationsTables = databaseManager.getPlayerLogoutLocationsTables();
        PlayerTeleportTable playerTeleportTable = databaseManager.getPlayerTeleportTable();
        PrestigePointsTable prestigePointsTable = databaseManager.getPrestigePointsTable();
        PrestigeLevelsTable prestigeLevelsTable = databaseManager.getPrestigeLevelsTable();

        islandIdsTable.updateIslandId(oldIslandId, newIslandId).thenAccept(v1 -> {
            // For offline island members, store the data necessary to process the prestige settings for them when they come online.
            offlineMemberIds.forEach(uuid -> offlinePrestigeTable.insertOfflinePrestige(uuid, newIslandId, islandData.getPrestigeLevel()));

            // Mark players for teleport if their logout location was within the island's bounds.
            playerLogoutLocationsTables.getPlayerIdsWithinByBounds(islandWorld.getName(), minX, maxX, minZ, maxZ)
                    .thenAccept(list -> list.forEach(uuid ->
                            playerTeleportTable.insertPlayerIdAndIslandId(uuid, newIslandId)));

            // Save prestige points
            prestigePointsTable.savePrestigePoints(newIslandId, islandData.getPrestigePoints());

            // Save prestige level
            prestigeLevelsTable.setLevel(newIslandId, islandData.getPrestigeLevel());
        });
    }
}