package com.github.lukesky19.skyPrestige.configuration.data.reward;

import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The configuration for a command reward.
 * @param displayItem The {@link ItemStackConfig} to display inside the rewards GUI.
 * @param giveToAllIslandMembers Whether to run the commands for to all island members.
 * @param commands The {@link List} of commands as a {@link String}.
 */
@ConfigSerializable
public record CommandReward(
        @NotNull ItemStackConfig displayItem,
        boolean giveToAllIslandMembers,
        @NotNull List<String> commands) implements IReward {
    @Override
    public @NotNull ItemStackConfig displayItem() {
        return displayItem;
    }
}
