package com.github.lukesky19.skyPrestige.configuration.data.reward;

import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * The configuration for a money reward.
 * @param displayItem The {@link ItemStackConfig} to display inside the rewards GUI.
 * @param giveToAllIslandMembers Whether to give the money to all island members.
 * @param money The amount of money to give.
 */
@ConfigSerializable
public record MoneyReward(
        @NotNull ItemStackConfig displayItem,
        boolean giveToAllIslandMembers,
        double money) implements IReward {
    @Override
    public @NotNull ItemStackConfig displayItem() {
        return displayItem;
    }
}
