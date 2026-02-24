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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.hooks.LuckPermsHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.api.events.team.TeamJoinEvent;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class handles the processing of rewards.
 */
public class RewardsProcessor {
    private final @NonNull HookManager hookManager;

    private final @NonNull ItemRewardsProcessor itemRewardsProcessor;
    private final @NonNull CommandRewardsProcessor commandRewardsProcessor;
    private final @NonNull LuckPermsRewardsProcessor luckPermsRewardsProcessor;
    private final @NonNull MoneyRewardProcessor moneyRewardProcessor;

    /**
     * Constructor
     * @param plugin A {@link SkyPrestige} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public RewardsProcessor(
            @NonNull SkyPrestige plugin,
            @NonNull HookManager hookManager) {
        ComponentLogger logger = plugin.getComponentLogger();
        this.hookManager = hookManager;

        this.itemRewardsProcessor = new ItemRewardsProcessor(logger);
        this.commandRewardsProcessor = new CommandRewardsProcessor(plugin);
        this.luckPermsRewardsProcessor = new LuckPermsRewardsProcessor(plugin, logger, hookManager);
        this.moneyRewardProcessor = new MoneyRewardProcessor(hookManager);
    }

    /**
     * Process rewards to be given before the island is reset.
     * @param initiatingPlayer The {@link Player} that initiated the rewards.
     * @param onlineIslandMembers The {@link List} of {@link Player}s that are online and on the island's team.
     * @param offlineIslandMembers The {@link List} of {@link UUID}s that are offline and on the island's team.
     * @param rewardConfig The {@link RewardConfig} to process.
     */
    public void processEarlyRewards(
            @NonNull Player initiatingPlayer,
            @NonNull List<Player> onlineIslandMembers,
            @NonNull List<UUID> offlineIslandMembers,
            @NonNull RewardConfig rewardConfig) {
        luckPermsRewardsProcessor.processEarlyRewards(initiatingPlayer, onlineIslandMembers, offlineIslandMembers, rewardConfig);
    }

    /**
     * Process rewards to be given after the island is reset.
     * @param initiatingPlayer The {@link Player} that initiated the rewards.
     * @param island The new {@link Island}.
     * @param onlineIslandMembers The {@link List} of {@link Player}s that are online and on the island's team.
     * @param offlineIslandMembers The {@link List} of {@link UUID}s that are offline and on the island's team.
     * @param rewardConfig The {@link RewardConfig} to process.
     */
    public void processPostRewards(
            @NonNull Player initiatingPlayer,
            @NonNull Island island,
            @NonNull List<Player> onlineIslandMembers,
            @NonNull List<UUID> offlineIslandMembers,
            @NonNull RewardConfig rewardConfig) {
        // Item Rewards
        itemRewardsProcessor.process(initiatingPlayer, onlineIslandMembers, rewardConfig.itemRewards());

        // Command Rewards
        commandRewardsProcessor.process(initiatingPlayer, onlineIslandMembers, rewardConfig.commandRewards());

        // Permission/Group Rewards
        luckPermsRewardsProcessor.processPostRewards(initiatingPlayer, onlineIslandMembers, offlineIslandMembers, rewardConfig);

        // Money Rewards
        moneyRewardProcessor.process(initiatingPlayer, onlineIslandMembers, rewardConfig.moneyRewards());

        // Island Range Reward
        if(rewardConfig.islandSizeReward().islandSize() > 0) {
            BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);

            if(rewardConfig.islandSizeReward().setSize()) {
                bentoBoxHook.setIslandSize(initiatingPlayer.getUniqueId(), island, island.getProtectionRange(),
                        rewardConfig.islandSizeReward().islandSize());
            } else {
                bentoBoxHook.setIslandSize(initiatingPlayer.getUniqueId(), island, island.getProtectionRange(),
                        island.getProtectionRange() + rewardConfig.islandSizeReward().islandSize());
            }
        }
    }

    /**
     * Process the reward config for the player on login.
     * @param player The {@link Player} to give rewards to.
     * @param rewardConfig The {@link RewardConfig} to process.
     */
    public void processRewardsOnLogin(@NonNull Player player, @NonNull RewardConfig rewardConfig) {
        // Item Rewards
        itemRewardsProcessor.processDeferred(player, rewardConfig.itemRewards());

        // Command Rewards
        commandRewardsProcessor.processDeferred(player, rewardConfig.commandRewards());

        // Money Rewards
        moneyRewardProcessor.processDeferred(player, rewardConfig.moneyRewards());
    }

    /**
     * Process the reward config for the player that joined an island.
     * @apiNote This should only be called for when a {@link TeamJoinEvent} occurs.
     * @param player The {@link Player}. The player may be null, not online, or not connected.
     * @param playerId The {@link UUID} of the player.
     * @param rewardConfig The {@link RewardConfig}.
     */
    public void processRetroactive(
            @Nullable Player player,
            @NonNull UUID playerId,
            @NonNull RewardConfig rewardConfig) {
        if(player != null && player.isOnline() && player.isConnected()) {
            // Item Rewards
            itemRewardsProcessor.processRetroactive(player, rewardConfig.itemRewards());

            // Command Rewards
            commandRewardsProcessor.processRetroactive(player, rewardConfig.commandRewards());

            // Money Rewards
            moneyRewardProcessor.processRetroactive(player, rewardConfig.moneyRewards());
        }

        // Permission / Group Rewards
        LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
        if(!luckPermsHook.isHooked()) return;
        CompletableFuture<@Nullable User> future = luckPermsHook.getOrLoadUser(playerId);
        future.thenAccept(user -> {
            if(user != null) {
                luckPermsRewardsProcessor.processRetroactive(user, rewardConfig.permissionRewards(), rewardConfig.groupRewards());
            }
        });
    }

    /**
     * Process the reward config for the player that joined an island while offline.
     * @param player The {@link Player}.
     * @param rewardConfig The {@link RewardConfig}.
     */
    public void processRetroactiveOnLogin(@NonNull Player player, @NonNull RewardConfig rewardConfig) {
        // Item Rewards
        itemRewardsProcessor.processRetroactive(player, rewardConfig.itemRewards());

        // Command Rewards
        commandRewardsProcessor.processRetroactive(player, rewardConfig.commandRewards());

        // Money Rewards
        moneyRewardProcessor.processRetroactive(player, rewardConfig.moneyRewards());
    }

    /**
     * Revert any early rewards given.
     * @param playerIds The {@link ImmutableSet} of {@link UUID}s to remove early rewards from.
     */
    public void revertEarlyRewards(@NonNull ImmutableSet<UUID> playerIds) {
        luckPermsRewardsProcessor.revertEarlyRewards(playerIds);
    }

    /**
     * Revert any early rewards given.
     * @param playerId The {@link UUID} to remove early rewards from.
     */
    public void revertEarlyRewards(@NonNull UUID playerId) {
        luckPermsRewardsProcessor.revertEarlyRewards(playerId);
    }
}