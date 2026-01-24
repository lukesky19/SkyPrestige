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
package com.github.lukesky19.skyPrestige.configuration.data.locale;

import com.github.lukesky19.skyPrestige.configuration.data.common.TimeFormat;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The plugin's locale configuration.
 * @param configVersion The config version of the locale.
 * @param prefix The plugin's prefix.
 * @param help The list of messages to send for the plugin's help message.
 * @param reload The plugin's reload message.
 * @param guiOpenError The message sent to a player when a GUI fails to open.
 * @param islandDataNotFound The message sent when the island's data were not found for an island.
 * @param islandPrestigeLevelUpdated The message sent when an island has its prestige level updated.
 * @param prestigePlayerOnly The message sent when a non-player tries to prestige.
 * @param islandPrestigeLevelMax The message sent when a player tries to prestige their island, but they are at the max level.
 * @param prestigePlayerInWrongWorld The message sent when a player tries to prestige in a non-BentoBox world.
 * @param prestigePlayerNotOnIsland The message sent when a player tries to prestige while not on an Island.
 * @param prestigeIslandNotOwned The message sent when a player tries to prestige an island that is not owned.
 * @param prestigePlayerNotMemberOrOwner The message sent when a player tries to prestige an island they are not the owner or a member of.
 * @param prestigeNotEnoughPrestigePoints The message sent when the island doesn't have enough prestige points to prestige.
 * @param prestigeIslandOptedOut The message sent when the island is opted out of prestige, but a player tries to prestige.
 * @param prestigeConfigError The message sent when a player tries to prestige their island, but it fails due to a config error.
 * @param prestigeConfigRequirementError The message sent when a player tries to prestige their island, but it fails due to a requirement config error.
 * @param progressPlayerNotOnIsland The message sent when a player tries to view their progress towards the next prestige level while not on an island.
 * @param progressMaxPrestigeLevel The message sent when a player tries to view their progress towards the next prestige level, but their island is at the max prestige level.
 * @param progressPrestigeExempt The message sent when a player tries to view their progress towards the next prestige level, but their island is opted out of prestige.
 * @param rewardsPlayerNotOnIsland The message sent when a player tries to view their next prestige level's rewards while not on an island.
 * @param rewardsMaxPrestigeLevel The message sent when a player tries to view their next prestige level's rewards, but their island is at the max prestige level.
 * @param rewardsPrestigeExempt The message sent when a player tries to view the rewards for next prestige level, but their island is opted out of prestige.
 * @param exchangePlayerNotOnIsland The message sent when a player tries to exchange their prestige points, while not on an island.
 * @param exchangePrestigeLevelNotMet The message sent when a player tries to exchange their prestige points, but don't meet the required prestige level.
 * @param exchangeNotEnoughPrestigePoints The message sent when a player tries to exchange their prestige points, but doesn't have enough prestige points for an exchange.
 * @param exchangePrestigeExempt The message sent when a player tries to exchange prestige points, but their island is opted out of prestige.
 * @param vaultPlayerNotOnIsland The message sent when a player tries to open their island vault while not on an island.
 * @param vaultItemNotAllowed The message sent when a player tries to put an item in their island vault that isn't allowed.
 * @param vaultPrestigeExempt The message sent when a player tries to open their island vault, but their island is opted out of prestige.
 * @param requirementsConfigError The message sent to a player when there is a config error when viewing prestige level requirements.
 * @param requirementsLevelNotFound The message sent to a player when there is no prestige level configured for the level number provided.
 * @param requirementsPointsForLevel The message sent to a player to display prestige points required for a level.
 * @param islandExempt The message sent when an island is marked as exempt from the top leaderboard placeholders.
 * @param islandUnexempt The message sent when an island is marked as not exempt from the top leaderboard placeholders.
 * @param islandAlreadyOptedIn The message sent when a player tries to opt into prestige, but they are already opted in.
 * @param islandAlreadyOptedOut The message sent when a player tries to opt-out of prestige, but they are opted out.
 * @param leaderboardTitle The leaderboard title.
 * @param leaderboardPosition The leaderboard positon text.
 * @param leaderboardPositionEmpty The leaderboard positon empty text.
 * @param protectionOrbAlreadyProtected The message sent when an item is already protected.
 * @param protectionOrbNotAllowed The message sent when an item is not allowed to be protected.
 * @param protectionOrbProtected The message sent when an item is protected.
 * @param multiplier The messages related to multipliers.
 * @param prestigeStatusPlayerInWrongWorld The message sent when a player tries to opt in or out of prestige in a non-BentoBox world.
 * @param prestigeStatusPlayerNotOnIsland The message sent when a player tries to opt in or out of prestige while not on an Island.
 * @param prestigeStatusIslandNotOwned The message sent when a player tries to opt in or out of prestige and that island is not owned.
 * @param prestigeStatusPlayerNotMemberOrOwner The message sent when a player tries to opt in or out of prestige an island they are not the owner or a member of.
 * @param delimiter The deliminator used for listing enchantments.
 * @param finalDelimiter The final deliminator used for listing enchantments.
 */
@ConfigSerializable
public record Locale(
        @Nullable String configVersion,
        String prefix,
        @NotNull List<String> help,
        String reload,
        String guiOpenError,
        String islandDataNotFound,
        String islandPrestigeLevelUpdated,
        String prestigePlayerOnly,
        String islandPrestigeLevelMax,
        String prestigePlayerInWrongWorld,
        String prestigePlayerNotOnIsland,
        String prestigeIslandNotOwned,
        String prestigePlayerNotMemberOrOwner,
        String prestigeNotEnoughPrestigePoints,
        String prestigeIslandOptedOut,
        String prestigeConfigError,
        String prestigeConfigRequirementError,
        String progressPlayerNotOnIsland,
        String progressMaxPrestigeLevel,
        String progressPrestigeExempt,
        String rewardsPlayerNotOnIsland,
        String rewardsMaxPrestigeLevel,
        String rewardsPrestigeExempt,
        String exchangePlayerNotOnIsland,
        String exchangePrestigeLevelNotMet,
        String exchangeNotEnoughPrestigePoints,
        String exchangePrestigeExempt,
        String vaultPlayerNotOnIsland,
        String vaultItemNotAllowed,
        String vaultPrestigeExempt,
        String requirementsLevelNotFound,
        String requirementsConfigError,
        String requirementsPointsForLevel,
        String islandExempt,
        String islandUnexempt,
        String islandAlreadyOptedIn,
        String islandAlreadyOptedOut,
        String leaderboardTitle,
        String leaderboardPosition,
        String leaderboardPositionEmpty,
        String protectionOrbNotAllowed,
        String protectionOrbAlreadyProtected,
        String protectionOrbProtected,
        MultiplierMessages multiplier,
        String prestigeStatusPlayerInWrongWorld,
        String prestigeStatusPlayerNotOnIsland,
        String prestigeStatusIslandNotOwned,
        String prestigeStatusPlayerNotMemberOrOwner,
        String delimiter,
        String finalDelimiter) {
    /**
     * This record contains the messages related to the multiplier.
     * @param serverMultiplierChangedTimeLimit The message sent when the server multiplier is updated with a time limit.
     * @param serverMultiplierChangedNoTimeLimit The message sent when the server multiplier is updated with no time limit.
     * @param islandMultiplierChangedTimeLimit The message sent when the island multiplier is updated with a time limit.
     * @param islandMultiplierChangedNoTimeLimit The message sent when the island multiplier is updated with no time limit.
     * @param serverMultiplierExpiredNotice The message sent when the server's multiplier has expired.
     * @param islandMultiplierExpiredNotice The message sent when an island's multiplier has expired.
     * @param serverMultiplierClearedNotice The message sent when the server's multiplier is cleared.
     * @param islandMultiplierClearedNotice The message sent when the island's multiplier is cleared.
     * @param serverMultiplierTimeLimit The message sent to the command sender that updated the server multiplier with a time limit.
     * @param serverMultiplierNoTimeLimit The message sent to the command sender that updated the server multiplier with no time limit.
     * @param islandMultiplierTimeLimit The message sent to the command sender that updated an island multiplier with a time limit.
     * @param islandMultiplierNoTimeLimit The message sent to the command sender that updated an island multiplier with no time limit.
     * @param serverMultiplierCleared The message sent to the command sender that cleared the server multiplier.
     * @param islandMultiplierCleared The message sent to the command sender that cleared the island multiplier.
     * @param effectiveMultiplier The message sent to a player viewing their effective multiplier.
     * @param multiplierNotOnIsland The message sent to a player when not on an island when trying to view the island multiplier.
     * @param multiplierTimePlaceholder The {@link TimeFormat} to use for {@literal <time>} placeholders.
     * @param shopMultiplierMessages The {@link ShopMultiplierMessages}.
     */
    @ConfigSerializable
    public record MultiplierMessages(
            String serverMultiplierChangedTimeLimit,
            String serverMultiplierChangedNoTimeLimit,
            String islandMultiplierChangedTimeLimit,
            String islandMultiplierChangedNoTimeLimit,
            String serverMultiplierExpiredNotice,
            String islandMultiplierExpiredNotice,
            String serverMultiplierClearedNotice,
            String islandMultiplierClearedNotice,
            String serverMultiplierTimeLimit,
            String serverMultiplierNoTimeLimit,
            String islandMultiplierTimeLimit,
            String islandMultiplierNoTimeLimit,
            String serverMultiplierCleared,
            String islandMultiplierCleared,
            String effectiveMultiplier,
            String multiplierNotOnIsland,
            TimeFormat multiplierTimePlaceholder,
            ShopMultiplierMessages shopMultiplierMessages) {
        /**
         * This record contains messages related to buying a prestige point multiplier.
         * @param notOnIsland The message sent to a player when they are not on their island.
         * @param multiplierActive The message sent to a player when a multiplier is active and the player isn't allowed to buy another because of it.
         * @param higherMultiplierActive The message sent to a player when a higher multiplier is active and the player isn't allowed to buy another because of it.
         * @param multiplierTimeMax The message sent to a player when a multiplier is at the maximum time allowed.
         */
        @ConfigSerializable
        public record ShopMultiplierMessages(
                String notOnIsland,
                String multiplierActive,
                String higherMultiplierActive,
                String multiplierTimeMax) {}
    }
}
