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
import com.github.lukesky19.skyPrestige.config.data.settings.Settings;
import com.github.lukesky19.skyPrestige.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.config.manager.prestige.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.EconomyHook;
import com.github.lukesky19.skyPrestige.hook.impl.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.hook.impl.SkySellWandsHook;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages applying prestige settings to an offline player.
 */
public class OfflinePrestigeProcessor {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public OfflinePrestigeProcessor(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull HookManager hookManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;
    }

    /**
     * Process the prestige settings for the player's offline prestiges.
     * @param player The {@link Player}
     */
    public void processOfflinePrestiges(@NotNull Player player) {
        UUID playerId = player.getUniqueId();

        CompletableFuture<List<Integer>> future = databaseManager.getOfflinePrestigeTable().getPrestigeLevels(playerId);
        future.thenAccept(prestigeLevels -> {
            if(prestigeLevels.isEmpty()) return;
            if(!player.isOnline() || !player.isConnected()) return;

            @NotNull Map<Integer, PrestigeConfig> prestigeConfigMap = prestigeConfigManager.getPrestigeConfigMap(prestigeLevels);
            if(prestigeConfigMap.isEmpty()) return;

            Locale locale = localeManager.getLocale();
            @Nullable Settings settings = settingsManager.getSettings();
            if(settings == null) {
                logger.error(AdventureUtil.deserialize("Unable to process offline prestige for player " + player.getName() + " due to invalid plugin settings."));
                return;
            }

            Server server = skyPrestige.getServer();
            ConsoleCommandSender commandSender = server.getConsoleSender();
            EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
            SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
            SkySellWandsHook skySellWandsHook = hookManager.getHook(SkySellWandsHook.class);

            boolean invReset = false;
            boolean enderChestReset = false;
            boolean expReset = false;
            boolean moneyReset = false;
            boolean auctionHouseMessageSent = false;

            // Process prestige settings for all prestige levels that occurred while the player was offline
            for(Map.Entry<Integer, PrestigeConfig> prestigeConfigEntry  : prestigeConfigMap.entrySet()) {
                int prestigeLevel = prestigeConfigEntry.getKey();
                PrestigeConfig prestigeConfig = prestigeConfigEntry.getValue();
                PrestigeConfig.PrestigeSettings prestigeSettings = prestigeConfig.prestigeSettings();
                PrestigeConfig.PlayerSettings playerSettings = prestigeSettings.playerSettings();
                PrestigeConfig.InventorySettings inventorySettings = playerSettings.inventorySettings();
                PrestigeConfig.EnderChestSettings enderChestSettings = playerSettings.enderChestSettings();
                PrestigeConfig.RewardConfig rewardConfig = prestigeConfig.rewardConfig();

                if(playerSettings.resetAuctionItems() && !auctionHouseMessageSent) {
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeAuctionHouseItemsReset()));
                    auctionHouseMessageSent = true;
                }

                // Reset the player's inventory if configured to do so, and it hasn't been done so already
                if(inventorySettings.resetInventory() && !invReset) {
                    ItemStack emptyStack = ItemType.AIR.createItemStack();

                    Inventory playerInventory = player.getInventory();
                    int inventorySize = playerInventory.getSize();
                    for(int i = 0; i < inventorySize; i++) {
                        ItemStack itemStack = playerInventory.getItem(i);
                        if(itemStack == null || itemStack.isEmpty()) continue;
                        if(inventorySettings.keepInfiniteSellWands() && skySellWandsHook.isInfiniteSellWand(itemStack)) continue;

                        playerInventory.setItem(i, emptyStack);
                    }

                    invReset = true;

                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeInventoryReset()));
                }

                // Reset the player's ender chest if configured to do so, and it hasn't been done so already
                if(enderChestSettings.resetEnderChest() && !enderChestReset) {
                    ItemStack emptyStack = ItemType.AIR.createItemStack();

                    Inventory playerEnderChest = player.getEnderChest();
                    int inventorySize = playerEnderChest.getSize();
                    for(int i = 0; i < inventorySize; i++) {
                        ItemStack itemStack = playerEnderChest.getItem(i);
                        if(itemStack == null || itemStack.isEmpty()) continue;
                        if(inventorySettings.keepInfiniteSellWands() && skySellWandsHook.isInfiniteSellWand(itemStack)) continue;

                        playerEnderChest.setItem(i, emptyStack);
                    }

                    enderChestReset = true;

                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeEnderChestReset()));
                }

                // Reset the player's experience if configured to do so, and it hasn't been done so already
                if(playerSettings.resetExp() && !expReset) {
                    player.setTotalExperience(0);
                    expReset = true;

                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeExperienceReset()));
                }

                // Check if an economy is hooked into
                if(economyHook.isHooked()) {
                    // Reset the player's balance if configured to do so, and it hasn't been done so already
                    if(playerSettings.resetMoney() && !moneyReset) {
                        double balance = economyHook.getBalance(player);

                        economyHook.removeFromBalance(player, balance);

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

                // Check if SkyPlayTime is hooked into.
                if(skyPlayTimeHook.isHooked()) {
                    PrestigeConfig.PlayTimeSettings playTimeSettings = playerSettings.playTimeSettings();

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
                    player.sendMessage(AdventureUtil.deserialize("<red>Failed to apply reset your play time due to SkyPlayTime not being hooked into. Contact your server's system administrator.</red>"));
                    logger.warn(AdventureUtil.deserialize("<red>Failed to apply reset a player's play time due to SkyPlayTime not being hooked into.</red>"));
                }

                // Rewards
                // Item Rewards
                rewardConfig.itemRewards().forEach(itemReward -> {
                    // Give the item reward the player if the reward is configured to be given to all island members
                    if(itemReward.giveToAllIslandMembers()) {
                        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                        itemStackBuilder.fromItemStackConfig(itemReward.rewardItem(), null, null, List.of());
                        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

                        if(optionalItemStack.isPresent()) {
                            ItemStack itemStack = optionalItemStack.get();

                            PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
                        } else {
                            player.sendMessage(AdventureUtil.deserialize("<red>Failed to give an ItemStack reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
                            logger.warn(AdventureUtil.deserialize("Unable to process an ItemStack reward due to an invalid ItemStack. Prestige level: " + prestigeLevel));
                        }
                    }
                });

                // Command Rewards
                rewardConfig.commandRewards().forEach(commandReward -> {
                    if(!commandReward.commands().isEmpty()) {
                        // Run command rewards for all island members or only the player prestiging the island.
                        if(commandReward.giveToAllIslandMembers()) {
                            commandReward.commands().stream()
                                    .map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command))
                                    .forEach(parsedCommand -> server.dispatchCommand(commandSender, parsedCommand));
                        }
                    }
                });

                // Money Rewards
                if(economyHook.isHooked()) {
                    rewardConfig.moneyRewards().forEach(moneyReward -> {
                        if(moneyReward.money() > 0) {
                            if(moneyReward.giveToAllIslandMembers()) {
                                economyHook.addToBalance(player, moneyReward.money());
                            }
                        }
                    });
                } else {
                    player.sendMessage(AdventureUtil.deserialize("<red>Failed to give a money reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
                    logger.warn(AdventureUtil.deserialize("Unable to give money rewards due to no economy hooked into."));
                }
            }

            // Remove any offline prestiges from the database for the player
            databaseManager.getOfflinePrestigeTable().removeOfflinePrestige(playerId);
        });
    }
}