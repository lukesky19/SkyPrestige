package com.github.lukesky19.skyPrestige.configuration.data.reward;

import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * The configuration for an item reward.
 * @param displayItem The {@link ItemStackConfig} to display inside the rewards GUI.
 * @param giveToAllIslandMembers Whether to give this item to all island members.
 * @param rewardItem The {@link ItemStackConfig} to give to the player.
 */
@ConfigSerializable
public record ItemReward(
        @NotNull ItemStackConfig displayItem,
        boolean giveToAllIslandMembers,
        @NotNull ItemStackConfig rewardItem) implements IReward {
    @Override
    public @NotNull ItemStackConfig displayItem() {
        return displayItem;
    }
}
