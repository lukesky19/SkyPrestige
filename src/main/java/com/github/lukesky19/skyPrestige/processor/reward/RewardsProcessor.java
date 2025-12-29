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
package com.github.lukesky19.skyPrestige.processor.reward;

import com.github.lukesky19.skyPrestige.configuration.data.reward.CommandReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.ItemReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.MoneyReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skyPrestige.integration.hooks.EconomyHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class handles the processing of rewards.
 */
public class RewardsProcessor {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull HookManager hookManager;

    private final @NotNull LuckPermsRewardsProcessor luckPermsRewardsProcessor;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public RewardsProcessor(@NotNull SkyPlugin plugin, @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.hookManager = hookManager;

        luckPermsRewardsProcessor = new LuckPermsRewardsProcessor(plugin, logger, hookManager);
    }

    /**
     * Process rewards to be given before the island is reset.
     * @param initiatingPlayer The {@link Player} that initiated the rewards.
     * @param onlineIslandMembers The {@link List} of {@link Player}s that are online and on the island's team.
     * @param offlineIslandMembers The {@link List} of {@link UUID}s that are offline and on the island's team.
     * @param rewardConfig The {@link RewardConfig} to process.
     */
    public void processEarlyRewards(
            @NotNull Player initiatingPlayer,
            @NotNull List<Player> onlineIslandMembers,
            @NotNull List<UUID> offlineIslandMembers,
            @NotNull RewardConfig rewardConfig) {
        luckPermsRewardsProcessor.processEarlyRewards(initiatingPlayer, onlineIslandMembers, offlineIslandMembers, rewardConfig);
    }

    /**
     * Process rewards to be given after the island is reset.
     * @param initiatingPlayer The {@link Player} that initiated the rewards.
     * @param island The new {@link Island}.
     * @param onlineIslandMembers The {@link List} of {@link Player}s that are online and on the island's team.
     * @param offlineIslandMembers The {@link List} of {@link UUID}s that are offline and on the island's team.
     * @param rewardConfig The {@link RewardConfig} to process.
     * @param prestigeLevel The prestige level being processed. Or -1 for non-prestige.
     */
    public void processPostRewards(
            @NotNull Player initiatingPlayer,
            @NotNull Island island,
            @NotNull List<Player> onlineIslandMembers,
            @NotNull List<UUID> offlineIslandMembers,
            @NotNull RewardConfig rewardConfig,
            int prestigeLevel) {
        Server server = plugin.getServer();
        ConsoleCommandSender commandSender = server.getConsoleSender();

        // Item Rewards
        rewardConfig.itemRewards().forEach(itemReward -> {
            if(itemReward.rewardItem().itemType() != null) {
                ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                itemStackBuilder.fromItemStackConfig(itemReward.rewardItem(), null, null, List.of());
                Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

                if(optionalItemStack.isPresent()) {
                    ItemStack itemStack = optionalItemStack.get();

                    // Give the item reward to all island members or only the player who triggered the prestige
                    if(itemReward.giveToAllIslandMembers()) {
                        onlineIslandMembers.forEach(memberPlayer ->
                                PlayerUtil.giveItem(memberPlayer.getInventory(), itemStack, itemStack.getAmount(), memberPlayer.getLocation()));
                    } else {
                        PlayerUtil.giveItem(initiatingPlayer.getInventory(), itemStack, itemStack.getAmount(), initiatingPlayer.getLocation());
                    }
                } else {
                    if(prestigeLevel == -1) {
                        initiatingPlayer.sendMessage(AdventureUtil.deserialize("<red>Failed to give an ItemStack reward for prestige opt out due to a configuration error. Contact your server's system administrator.</red>"));
                        logger.warn(AdventureUtil.deserialize("Unable to process an ItemStack reward due to an invalid ItemStack on prestige opt out."));
                    } else {
                        initiatingPlayer.sendMessage(AdventureUtil.deserialize("<red>Failed to give an ItemStack reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
                        logger.warn(AdventureUtil.deserialize("Unable to process an ItemStack reward due to an invalid ItemStack. Prestige level: " + prestigeLevel));
                    }
                }
            }
        });

        // Command Rewards
        rewardConfig.commandRewards().forEach(commandReward -> {
            if(!commandReward.commands().isEmpty()) {
                // Run command rewards for all island members or only the player prestiging the island.
                if(commandReward.giveToAllIslandMembers()) {
                    onlineIslandMembers.forEach(islandMember ->
                            commandReward.commands().stream()
                                    .map(command -> PlaceholderAPIUtil.parsePlaceholders(islandMember, command))
                                    .forEach(parsedCommand -> server.dispatchCommand(commandSender, parsedCommand)));
                } else {
                    commandReward.commands().stream()
                            .map(command -> PlaceholderAPIUtil.parsePlaceholders(initiatingPlayer, command))
                            .forEach(parsedCommand -> server.dispatchCommand(commandSender, parsedCommand));
                }
            }
        });

        // Permission/Group Rewards
        luckPermsRewardsProcessor.processPostRewards(initiatingPlayer, onlineIslandMembers, offlineIslandMembers, rewardConfig);

        // Money Rewards
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        if(economyHook.isHooked()) {
            rewardConfig.moneyRewards().forEach(moneyReward -> {
                if(moneyReward.money() > 0) {
                    if (moneyReward.giveToAllIslandMembers()) {
                        onlineIslandMembers.forEach(memberPlayer -> economyHook.addToBalance(memberPlayer, moneyReward.money()));
                    } else {
                        economyHook.addToBalance(initiatingPlayer, moneyReward.money());
                    }
                }
            });
        } else {
            if(prestigeLevel == -1) {
                initiatingPlayer.sendMessage(AdventureUtil.deserialize("<red>Failed to give a money reward for prestige opt out due to a configuration error. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.deserialize("Unable to process an ItemStack reward due to an invalid ItemStack on prestige opt out."));
            } else {
                initiatingPlayer.sendMessage(AdventureUtil.deserialize("<red>Failed to give a money reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.deserialize("Unable to give money rewards due to no economy hooked into."));
            }
        }

        // Island Range Reward
        if(rewardConfig.islandRangeReward().rangeToAdd() > 0) {
            island.setProtectionRange(island.getProtectionRange() + rewardConfig.islandRangeReward().rangeToAdd());
        }
    }

    /**
     * Process the reward config for the player.
     * This is meant for use when the player's island was prestiged while they were offline.
     * @param player The {@link Player} to give rewards to.
     * @param rewardConfig The {@link RewardConfig} to process.
     * @param prestigeLevel The prestige level the rewards are being processed for. Or -1 if not a prestige.
     */
    public void processRewardsOnLogin(
            @NotNull Player player,
            @NotNull RewardConfig rewardConfig,
            int prestigeLevel) {
        rewardConfig.itemRewards()
                .stream()
                .filter(ItemReward::giveToAllIslandMembers)
                .forEach(itemReward -> {
                    if(itemReward.rewardItem().itemType() != null) {
                        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                        itemStackBuilder.fromItemStackConfig(itemReward.rewardItem(), null, null, List.of());
                        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

                        if(optionalItemStack.isPresent()) {
                            ItemStack itemStack = optionalItemStack.get();

                            PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
                        } else {
                            if(prestigeLevel == -1) {
                                player.sendMessage(AdventureUtil.deserialize("<red>Failed to give an ItemStack reward for prestige opt in/out due to a configuration error. Contact your server's system administrator.</red>"));
                                logger.warn(AdventureUtil.deserialize("Unable to process an ItemStack reward due to an invalid ItemStack on prestige opt out."));
                            } else {
                                player.sendMessage(AdventureUtil.deserialize("<red>Failed to give an ItemStack reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
                                logger.warn(AdventureUtil.deserialize("Unable to process an ItemStack reward due to an invalid ItemStack. Prestige level: " + prestigeLevel));
                            }
                        }
                    }
                });

        Server server = plugin.getServer();
        CommandSender commandSender = server.getConsoleSender();
        rewardConfig.commandRewards()
                .stream()
                .filter(CommandReward::giveToAllIslandMembers)
                .forEach(commandReward ->
                        commandReward.commands().stream()
                                .map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command))
                                .forEach(parsedCommand -> server.dispatchCommand(commandSender, parsedCommand)));

        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        if(economyHook.isHooked()) {
            rewardConfig.moneyRewards()
                    .stream()
                    .filter(MoneyReward::giveToAllIslandMembers)
                    .forEach(moneyReward -> {
                        if(moneyReward.money() > 0) {
                            economyHook.addToBalance(player, moneyReward.money());
                        }
                    });
        } else {
            if(prestigeLevel == -1) {
                player.sendMessage(AdventureUtil.deserialize("<red>Failed to give a money reward for prestige opt in/out due to a configuration error. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.deserialize("Unable to process an ItemStack reward due to an invalid ItemStack on prestige opt out."));
            } else {
                player.sendMessage(AdventureUtil.deserialize("<red>Failed to give a money reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.deserialize("Unable to give money rewards due to no economy hooked into."));
            }
        }
    }

    /**
     * Revert any early rewards given.
     * @param uniqueIds The {@link ImmutableSet} of {@link UUID}s to remove early rewards from.
     */
    public void revertEarlyRewards(@NotNull ImmutableSet<UUID> uniqueIds) {
        luckPermsRewardsProcessor.revertEarlyRewards(uniqueIds);
    }
}