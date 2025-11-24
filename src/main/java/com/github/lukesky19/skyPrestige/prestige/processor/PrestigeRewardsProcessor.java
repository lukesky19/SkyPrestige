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
package com.github.lukesky19.skyPrestige.prestige.processor;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.EconomyHook;
import com.github.lukesky19.skyPrestige.prestige.config.PrestigeConfig;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.Optional;

/**
 * This class handles the processing of prestige rewards.
 */
public class PrestigeRewardsProcessor {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PrestigeRewardsProcessor(@NotNull SkyPrestige skyPrestige, @NotNull HookManager hookManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.hookManager = hookManager;
    }

    /**
     * Process the {@link PrestigeConfig.RewardConfig} for the prestige level.
     * @param prestigingPlayer The {@link Player} prestiging the island.
     * @param island The new {@link Island}.
     * @param onlineIslandMembers The {@link List} of {@link Player}s that are online and on the player's team.
     * @param rewardConfig The {@link PrestigeConfig.RewardConfig} to process.
     * @param prestigeLevel The prestige level being processed.
     */
    public void processPrestigeRewards(
            @NotNull Player prestigingPlayer,
            @NotNull Island island,
            @NotNull List<Player> onlineIslandMembers,
            @NotNull PrestigeConfig.RewardConfig rewardConfig,
            int prestigeLevel) {
        Server server = skyPrestige.getServer();
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
                        PlayerUtil.giveItem(prestigingPlayer.getInventory(), itemStack, itemStack.getAmount(), prestigingPlayer.getLocation());
                    }
                } else {
                    prestigingPlayer.sendMessage(AdventureUtil.deserialize("<red>Failed to give an ItemStack reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
                    logger.warn(AdventureUtil.deserialize("Unable to process an ItemStack reward due to an invalid ItemStack. Prestige level: " + prestigeLevel));
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
                            .map(command -> PlaceholderAPIUtil.parsePlaceholders(prestigingPlayer, command))
                            .forEach(parsedCommand -> server.dispatchCommand(commandSender, parsedCommand));
                }
            }
        });

        // Money Rewards
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        if(economyHook.isHooked()) {
            rewardConfig.moneyRewards().forEach(moneyReward -> {
                if(moneyReward.money() > 0) {
                    if (moneyReward.giveToAllIslandMembers()) {
                        onlineIslandMembers.forEach(memberPlayer -> economyHook.addToBalance(memberPlayer, moneyReward.money()));
                    } else {
                        economyHook.addToBalance(prestigingPlayer, moneyReward.money());
                    }
                }
            });
        } else {
            prestigingPlayer.sendMessage(AdventureUtil.deserialize("<red>Failed to give a money reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
            logger.warn(AdventureUtil.deserialize("Unable to give money rewards due to no economy hooked into."));
        }

        // Island Range Reward
        if(rewardConfig.islandRangeReward().rangeToAdd() > 0) {
            island.setProtectionRange(island.getProtectionRange() + rewardConfig.islandRangeReward().rangeToAdd());
        }
    }
}
