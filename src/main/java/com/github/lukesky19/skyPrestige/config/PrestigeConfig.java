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
package com.github.lukesky19.skyPrestige.config;

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration for a prestige level.
 * @param configVersion The config version of the file.
 * @param prestigeLevel The prestige level this configuration is for.
 * @param scaleFactor The scale factor used in conjunction with the formula in settings.yml to scale the required prestige points.
 * @param prestigeSettings The {@link PrestigeSettings} for this level.
 * @param requiredPrestigePoints The base number of prestige points required to prestige. This value will be scaled to the number of players on the island.
 * @param rewards The {@link List} of {@link Reward}s for this level.
 */
@ConfigSerializable
public record PrestigeConfig(
        @Nullable String configVersion,
        int prestigeLevel,
        @Nullable Double scaleFactor,
        @NotNull PrestigeSettings prestigeSettings,
        @Nullable Long requiredPrestigePoints,
        @NotNull List<Reward> rewards) {
    /**
     * This record contains settings for when an island is prestiged.
     * @param resetInventory Whether to reset an island member's inventory on island prestige.
     * @param resetEnderChest Whether to reset an island member's ender chest on island prestige.
     * @param resetExp Whether to reset an island member's exp on island prestige.
     * @param resetMoney Whether to reset an island member's balance on island prestige.
     * @param giveStartingMoneyToAllIslandMembers Whether to give the starting money to all island members or not.
     * @param startingMoney The starting amount of money to give the player.
     * @param playTimeSettings The {@link PlayTimeSettings}.
     * @param resetPrestigePoints Whether prestige points should be reset on island prestige.
     */
    @ConfigSerializable
    public record PrestigeSettings(
            boolean resetInventory,
            boolean resetEnderChest,
            boolean resetExp,
            boolean resetMoney,
            boolean giveStartingMoneyToAllIslandMembers,
            double startingMoney,
            @NotNull PrestigeConfig.PlayTimeSettings playTimeSettings,
            boolean resetPrestigePoints) {}
    /**
     * This record contains settings for play time for when an island is prestiged.
     * @param resetSession Whether session play time should be reset or not.
     * @param resetDaily Whether daily play time should be reset or not.
     * @param resetWeekly Whether weekly play time should be reset or not.
     * @param resetMonthly Whether monthly play time should be reset or not.
     * @param resetYearly Whether yearly play time should be reset or not.
     * @param resetTotal Whether total play time should be reset or not.
     */
    @ConfigSerializable
    public record PlayTimeSettings(
            boolean resetSession,
            boolean resetDaily,
            boolean resetWeekly,
            boolean resetMonthly,
            boolean resetYearly,
            boolean resetTotal) {}
    /**
     * This records contains an individual reward configuration to give on successful prestige.
     * @param giveToAllIslandMembers Whether to this reward to all island members or not.
     * @param displayItem The {@link ItemStackConfig} to display inside the rewards GUI.
     * @param rewardItem The {@link ItemStackConfig} to give as a reward.
     * @param commands The {@link List} of {@link String}s for the commands to execute in console.
     */
    @ConfigSerializable
    public record Reward(
            boolean giveToAllIslandMembers,
            @NotNull ItemStackConfig displayItem,
            @NotNull ItemStackConfig rewardItem,
            @NotNull List<String> commands) {}
}
