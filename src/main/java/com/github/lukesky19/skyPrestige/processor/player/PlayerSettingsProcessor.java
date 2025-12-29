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
package com.github.lukesky19.skyPrestige.processor.player;

import com.github.lukesky19.skyPrestige.configuration.data.playtime.PlayTimeSettings;
import com.github.lukesky19.skyPrestige.configuration.interfaces.InventorySettingsInterface;
import com.github.lukesky19.skyPrestige.configuration.interfaces.PlayTimeSettingsInterface;
import com.github.lukesky19.skyPrestige.configuration.interfaces.PlayerSettingsInterface;
import com.github.lukesky19.skyPrestige.integration.hooks.EconomyHook;
import com.github.lukesky19.skyPrestige.integration.hooks.PlayerAuctionsHook;
import com.github.lukesky19.skyPrestige.integration.hooks.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.integration.hooks.SkySellWandsHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.protection.ProtectionOrbManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * This class manages the processing of {@link PlayerSettingsInterface}.
 */
public class PlayerSettingsProcessor {
    private final @NotNull HookManager hookManager;
    private final @NotNull ProtectionOrbManager protectionOrbManager;

    /**
     * Constructor
     * @param hookManager A {@link HookManager} instance.
     * @param protectionOrbManager A {@link ProtectionOrbManager} instance.
     */
    public PlayerSettingsProcessor(
            @NotNull HookManager hookManager,
            @NotNull ProtectionOrbManager protectionOrbManager) {
        this.hookManager = hookManager;
        this.protectionOrbManager = protectionOrbManager;
    }

    /**
     * Process the player settings.
     * @param playerSettings The {@link PlayerSettingsInterface} to process.
     * @param initiatingPlayer The player that initiated the processing of the player settings.
     * @param onlinePlayerList The list of online players to apply the settings to.
     * @param offlinePlayerIds The list of offline player ids to apply the settings to.
     * @param startingMoney The starting money.
     * @param giveToAll Whether to give the starting money to all players.
     */
    public void processPlayerSettings(
            @NotNull PlayerSettingsInterface playerSettings,
            @NotNull Player initiatingPlayer,
            @NotNull List<Player> onlinePlayerList,
            @NotNull List<UUID> offlinePlayerIds,
            double startingMoney,
            boolean giveToAll) {
        // Online Player Settings
        onlinePlayerList.forEach(player -> processPlayerSettings(playerSettings, player, initiatingPlayer.equals(player), startingMoney, giveToAll));

        // Offline Player Settings
        offlinePlayerIds.forEach(this::resetAuctionHouse);
    }

    /**
     * Process the player settings.
     * @param playerSettings The {@link PlayerSettingsInterface} to process.
     * @param player The {@link Player} to apply the settings to.
     * @param startingMoney The starting money.
     * @param giveToAll Whether to give the starting money to all players.
     */
    public void processPlayerSettingsOnLogin(
            @NotNull PlayerSettingsInterface playerSettings,
            @NotNull Player player,
            double startingMoney,
            boolean giveToAll) {
        processInventorySettings(playerSettings.playerInventorySettings(), playerSettings.playerEnderChestSettings(), player);

        if(playerSettings.resetExp()) {
            resetExperience(player);
        }

        processEconomySettings(player, false, playerSettings.resetMoney(), startingMoney, giveToAll);

        resetPlayTime(player, playerSettings.playTimeSettings());
    }

    /**
     * Process the player settings.
     * @param playerSettings The {@link PlayerSettingsInterface} to process.
     * @param player The {@link Player} to process the settings for.
     * @param isPlayerInitiator Is the player the initiator?
     * @param startingMoney The starting money.
     * @param giveToAll Whether to give the starting money to all players.
     */
    private void processPlayerSettings(
            @NotNull PlayerSettingsInterface playerSettings,
            @NotNull Player player,
            boolean isPlayerInitiator,
            double startingMoney,
            boolean giveToAll) {
        processInventorySettings(playerSettings.playerInventorySettings(), playerSettings.playerEnderChestSettings(), player);

        if(playerSettings.resetExp()) {
            resetExperience(player);
        }

        processEconomySettings(player, isPlayerInitiator, playerSettings.resetMoney(), startingMoney, giveToAll);

        if(playerSettings.resetAuctionItems()) {
            resetAuctionHouse(player.getUniqueId());
        }

        resetPlayTime(player, playerSettings.playTimeSettings());
    }

    /**
     * Process the {@link InventorySettingsInterface} provided.
     * @param playerInventorySettings The {@link InventorySettingsInterface} to apply to the player's Inventory.
     * @param playerEnderChestInventorySettings The {@link InventorySettingsInterface} to apply to the player's Ender Chest.
     * @param player The {@link Player} to process settings for.
     */
    private void processInventorySettings(
            @NotNull InventorySettingsInterface playerInventorySettings,
            @NotNull InventorySettingsInterface playerEnderChestInventorySettings,
            @NotNull Player player) {
        processInventorySettings(playerInventorySettings, player.getInventory());
        processInventorySettings(playerEnderChestInventorySettings, player.getEnderChest());
    }

    /**
     * Process the {@link InventorySettingsInterface} for the {@link Inventory} provided.
     * @param inventorySettings The {@link InventorySettingsInterface}.
     * @param inventory The {@link Inventory}.
     */
    private void processInventorySettings(
            @NotNull InventorySettingsInterface inventorySettings,
            @NotNull Inventory inventory) {
        if(inventorySettings.clearInventory()) {
            SkySellWandsHook skySellWandsHook = hookManager.getHook(SkySellWandsHook.class);
            ItemStack emptyStack = ItemType.AIR.createItemStack();

            int inventorySize = inventory.getSize();
            for(int i = 0; i < inventorySize; i++) {
                ItemStack itemStack = inventory.getItem(i);
                if(itemStack == null || itemStack.isEmpty()) continue;
                if(inventorySettings.keepInfiniteSellWands() && skySellWandsHook.isInfiniteSellWand(itemStack)) continue;
                if(inventorySettings.keepProtectedItems() && protectionOrbManager.isItemStackProtected(itemStack)) continue;

                inventory.setItem(i, emptyStack);
            }
        }
    }

    /**
     * Reset the player's experience.
     * @param player The {@link Player} to reset experience for
     */
    private void resetExperience(@NotNull Player player) {
        player.setLevel(0);
        player.setExp(0);
    }

    /**
     * Process the economy settings.
     * @param player The {@link Player}.
     * @param isPlayerInitiator Is the player the initiator?
     * @param resetMoney Should money be reset?
     * @param startingMoney The starting money.
     * @param giveAll Whether to give the starting money to all players.
     */
    private void processEconomySettings(
            @NotNull Player player,
            boolean isPlayerInitiator,
            boolean resetMoney,
            double startingMoney,
            boolean giveAll) {
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);

        if(economyHook.isHooked()) {
            if(resetMoney) {
                economyHook.removeFromBalance(player, economyHook.getBalance(player));
            }

            if(startingMoney > 0) {
                if(giveAll) {
                    economyHook.addToBalance(player, startingMoney);
                } else {
                    if(isPlayerInitiator) {
                        economyHook.addToBalance(player, startingMoney);
                    }
                }
            }
        }
    }

    /**
     * Reset the auction house items for the player id provided.
     * @param playerId The {@link UUID}.
     */
    private void resetAuctionHouse(@NotNull UUID playerId) {
        PlayerAuctionsHook playerAuctionsHook = hookManager.getHook(PlayerAuctionsHook.class);

        if(playerAuctionsHook.isHooked()) {
            playerAuctionsHook.clearPlayerAuctions(playerId);
        }
    }

    /**
     * Reset the play time for the player provided.
     * @param player The {@link Player}
     * @param playTimeSettings The {@link PlayTimeSettings}.
     */
    private void resetPlayTime(@NotNull Player player, @NotNull PlayTimeSettingsInterface playTimeSettings) {
        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);

        if(skyPlayTimeHook.isHooked()) {
            skyPlayTimeHook.resetPlayTime(
                    player.getUniqueId(),
                    playTimeSettings.resetSession(),
                    playTimeSettings.resetDaily(),
                    playTimeSettings.resetWeekly(),
                    playTimeSettings.resetMonthly(),
                    playTimeSettings.resetYearly(),
                    playTimeSettings.resetTotal());
        }
    }
}