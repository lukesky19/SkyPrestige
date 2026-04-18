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

import com.github.lukesky19.skyPrestige.configuration.data.reward.ItemReward;
import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.player.PlayerUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * This class handles the processing of {@link ItemReward}s.
 */
public class ItemRewardsProcessor {
    private final @NonNull ComponentLogger logger;

    /**
     * Constructor
     * @param logger A {@link ComponentLogger} instance.
     */
    public ItemRewardsProcessor(@NonNull ComponentLogger logger) {
        this.logger = logger;
    }

    /**
     * Process all item rewards for the initiator and player list provided.
     * @param initiator The initiating {@link Player}.
     * @param playerList The {@link List} of {@link Player}s to give rewards to.
     * @param itemRewardList The {@link List} of {@link ItemReward}s.
     */
    public void process(
            @NonNull Player initiator,
            @NonNull List<Player> playerList,
            @NonNull List<ItemReward> itemRewardList) {
        if(itemRewardList.isEmpty()) return;

        itemRewardList.stream()
                .filter(itemReward -> itemReward.rewardItem().itemType() != null)
                .forEach(itemReward -> {
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(itemReward.rewardItem(), null, List.of());
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

                    if(itemReward.giveToAllIslandMembers()) {
                        playerList.forEach(player ->
                                optionalItemStack.ifPresent(itemStack ->
                                        PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation())));
                    } else {
                        optionalItemStack.ifPresent(itemStack ->
                                PlayerUtil.giveItem(initiator.getInventory(), itemStack, itemStack.getAmount(), initiator.getLocation()));
                    }
                });
    }

    /**
     * Process all item rewards for the player that logged on.
     * @apiNote Only items configured for all island members will be given.
     * This is meant for when an island member was offline when rewards were initially processed.
     * Excluding joining an island, for that, see {@link #processRetroactive(Player, List)}.
     * @param player The {@link Player}.
     * @param itemRewardList The {@link List} of {@link ItemReward}s.
     */
    public void processDeferred(@NonNull Player player, @NonNull List<ItemReward> itemRewardList) {
        if(itemRewardList.isEmpty()) return;

        itemRewardList.stream()
                .filter(itemReward -> itemReward.rewardItem().itemType() != null)
                .filter(ItemReward::giveToAllIslandMembers)
                .forEach(itemReward -> {
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(itemReward.rewardItem(), null, List.of());
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

                    optionalItemStack.ifPresent(itemStack ->
                            PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation()));
                });
    }

    /**
     * Process all item rewards player provided.
     * @apiNote Only rewards meant for all island members and configured to be given retroactively are awarded.
     * This is meant for when an island member joins an island.
     * @param player The {@link Player}.
     * @param itemRewardList The {@link List} of {@link ItemReward}s.
     */
    public void processRetroactive(@NonNull Player player, @NonNull List<ItemReward> itemRewardList) {
        if(itemRewardList.isEmpty()) return;

        itemRewardList.stream()
                .filter(itemReward -> itemReward.rewardItem().itemType() != null)
                .filter(ItemReward::giveToAllIslandMembers)
                .filter(IReward::giveOnIslandJoin)
                .forEach(itemReward -> {
                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                    itemStackBuilder.fromItemStackConfig(itemReward.rewardItem(), player, List.of());
                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

                    optionalItemStack.ifPresent(itemStack ->
                            PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation()));
                });
    }
}