package com.github.lukesky19.skyPrestige.configuration.data.reward;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The rewards to give when the island is reset is reached.
 * @param itemRewards The {@link List} of {@link ItemReward}s.
 * @param commandRewards The {@link List} of {@link CommandReward}s.
 * @param moneyRewards The {@link List} of {@link MoneyReward}s.
 * @param islandRangeReward The {@link IslandRangeReward}.
 */
@ConfigSerializable
public record RewardConfig(
        @NotNull List<ItemReward> itemRewards,
        @NotNull List<CommandReward> commandRewards,
        @NotNull List<MoneyReward> moneyRewards,
        @NotNull IslandRangeReward islandRangeReward) {}
