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
package com.github.lukesky19.skyPrestige.prestige.config;

import com.github.lukesky19.skyPrestige.gui.gui.RewardsGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration for a prestige level.
 * @param configVersion The config version of the file.
 * @param prestigeLevel The prestige level this configuration is for.
 * @param scaleFactor The scale factor used in conjunction with the formula in settings.yml to scale the required prestige points.
 * @param requiredPrestigePoints The base number of prestige points required to prestige. This value will be scaled to the number of players on the island.
 * @param prestigeSettings The {@link PrestigeSettings} for this level.
 * @param rewardConfig The {@link RewardConfig} for this level.
 * @param rewards The legacy {@link List} of {@link Reward}s for this level. Migration purposes only.
 */
@ConfigSerializable
public record PrestigeConfig(
        @Nullable String configVersion,
        int prestigeLevel,
        @Nullable Double scaleFactor,
        @Nullable Double requiredPrestigePoints,
        @NotNull PrestigeSettings prestigeSettings,
        @NotNull RewardConfig rewardConfig,
        @Deprecated @NotNull List<LegacyReward> rewards) {
    /**
     * This record contains settings for when an island is prestiged.
     * @param playerSettings The {@link PlayerSettings}.
     * @param islandSettings The {@link IslandSettings}.
     * @param giveStartingMoneyToAllIslandMembers Whether to give the starting money to all island members or not.
     * @param startingMoney The starting amount of money to give the player.
     * Legacy options below for migration purposes only.
     * @param resetInventory Legacy reset inventory setting. Migration purposes only.
     * @param resetEnderChest Legacy reset ender chest setting. Migration purposes only.
     * @param resetExp Legacy reset experience setting. Migration purposes only.
     * @param resetMoney Legacy reset money setting. Migration purposes only.
     * @param playTimeSettings Legacy {@link PlayTimeSettings}. Migration purposes only.
     * @param resetPrestigePoints Legacy reset prestige points setting. Migration purposes only.
     */
    @ConfigSerializable
    public record PrestigeSettings(
            @NotNull PlayerSettings playerSettings,
            @NotNull IslandSettings islandSettings,
            boolean giveStartingMoneyToAllIslandMembers,
            double startingMoney,
            // Legacy options below for migration purposes only.
            @Deprecated(since = "1.1.0.0") boolean resetInventory,
            @Deprecated(since = "1.1.0.0") boolean resetEnderChest,
            @Deprecated(since = "1.1.0.0") boolean resetExp,
            @Deprecated(since = "1.1.0.0") boolean resetMoney,
            @Deprecated(since = "1.1.0.0") @NotNull PrestigeConfig.PlayTimeSettings playTimeSettings,
            @Deprecated(since = "1.1.0.0") boolean resetPrestigePoints) {}

    /**
     * Settings related to resetting player data.
     * @param inventorySettings The {@link InventorySettings}.
     * @param enderChestSettings The {@link EnderChestSettings}.
     * @param resetExp Whether to reset player experience or not.
     * @param resetMoney Whether to reset the player's money or not.
     * @param resetAuctionItems Whether to reset the player's auction house items or not.
     * @param playTimeSettings The {@link PlayTimeSettings}.
     */
    @ConfigSerializable
    public record PlayerSettings(
            @NotNull InventorySettings inventorySettings,
            @NotNull EnderChestSettings enderChestSettings,
            boolean resetExp,
            boolean resetMoney,
            boolean resetAuctionItems,
            @NotNull PrestigeConfig.PlayTimeSettings playTimeSettings) {}

    /**
     * Settings related to keeping or resetting island data.
     * @param keepIslandSize Whether to keep island size on prestige or not.
     * @param keepGeneratorUpgrades Whether to keep generator upgrades on prestige or not.
     * @param resetPrestigePoints Whether to reset prestige points on prestige or not.
     * @param removeRequiredPrestigePoints Whether to remove the required prestige points or not from the island's total.
     */
    @ConfigSerializable
    public record IslandSettings(
            boolean keepIslandSize,
            boolean keepGeneratorUpgrades,
            boolean resetPrestigePoints,
            boolean removeRequiredPrestigePoints) {}

    /**
     * Settings related to resetting a player's inventory.
     * @param resetInventory Whether to reset an island member's inventory on island prestige.
     * @param keepInfiniteSellWands Whether to keep infinite sell wands from SkySellWands or not.
     */
    @ConfigSerializable
    public record InventorySettings(
            boolean resetInventory,
            boolean keepInfiniteSellWands) {}

    /**
     * Settings related to resetting a player's ender chest.
     * @param resetEnderChest Whether to reset an island member's ender chest on island prestige.
     * @param keepInfiniteSellWands Whether to keep infinite sell wands from SkySellWands or not.
     */
    @ConfigSerializable
    public record EnderChestSettings(
            boolean resetEnderChest,
            boolean keepInfiniteSellWands) {}

    /**
     * This record contains settings for resetting play time.
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
     * The rewards to give when the prestige level is reached.
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

    /**
     * This interface is used to create reward configurations.
     */
    public interface Reward {
        /**
         * Get the {@link ItemStackConfig} to create an {@link ItemStack} that is displayed inside the {@link RewardsGUI}.
         * @return An {@link ItemStackConfig}.
         */
        @NotNull ItemStackConfig displayItem();
    }

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
            @NotNull ItemStackConfig rewardItem) implements Reward {
        @Override
        public @NotNull ItemStackConfig displayItem() {
            return displayItem;
        }
    }

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
            @NotNull List<String> commands) implements Reward {
        @Override
        public @NotNull ItemStackConfig displayItem() {
            return displayItem;
        }
    }

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
            double money) implements Reward {
        @Override
        public @NotNull ItemStackConfig displayItem() {
            return displayItem;
        }
    }

    /**
     * The configuration for the island range to add.
     * @param displayItem The {@link ItemStackConfig} to display inside the rewards GUI.
     * @param rangeToAdd The range to add to the island.
     */
    @ConfigSerializable
    public record IslandRangeReward(
            @NotNull ItemStackConfig displayItem,
            int rangeToAdd) implements Reward {
        @Override
        public @NotNull ItemStackConfig displayItem() {
            return displayItem;
        }
    }

    /**
     * This records contains an individual reward configuration to give on successful prestige.
     * @param giveToAllIslandMembers Whether to this reward to all island members or not.
     * @param displayItem The {@link ItemStackConfig} to display inside the rewards GUI.
     * @param rewardItem The {@link ItemStackConfig} to give as a reward.
     * @param commands The {@link List} of {@link String}s for the commands to execute in console.
     * @deprecated Replaced by {@link RewardConfig}.
     */
    @Deprecated(since = "1.1.0.0")
    @ConfigSerializable
    public record LegacyReward(
            @Deprecated(since = "1.1.0.0") boolean giveToAllIslandMembers,
            @Deprecated(since = "1.1.0.0") @NotNull ItemStackConfig displayItem,
            @Deprecated(since = "1.1.0.0") @NotNull ItemStackConfig rewardItem,
            @Deprecated(since = "1.1.0.0") @NotNull List<String> commands) {}
}
