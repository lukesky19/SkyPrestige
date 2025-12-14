package com.github.lukesky19.skyPrestige.configuration.data.reward;

import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * The configuration for the island range to add.
 * @param displayItem The {@link ItemStackConfig} to display inside the rewards GUI.
 * @param rangeToAdd The range to add to the island.
 */
@ConfigSerializable
public record IslandRangeReward(
        @NotNull ItemStackConfig displayItem,
        int rangeToAdd) implements IReward {
    @Override
    public @NotNull ItemStackConfig displayItem() {
        return displayItem;
    }
}
