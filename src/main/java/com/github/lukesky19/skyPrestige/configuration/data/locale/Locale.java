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
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The plugin's locale configuration.
 * @param version The config version of the locale.
 * @param prefix The plugin's prefix.
 * @param help The list of messages to send for the plugin's help message.
 * @param reload The plugin's reload message.
 * @param guiOpenError The message sent to a player when a GUI fails to open.
 * @param islandDataNotFound The message sent when the island's data were not found for an island.
 * @param prestigeLevelUpdated The message sent when an island has its prestige level updated.
 * @param prestigeMessages The messages related to prestiging.
 * @param optInMessages The messages related to opting into prestige.
 * @param optOutMessages The messages related to opting out of prestige.
 * @param rewardMessages The messages related to rewards.
 * @param exchangeMessages The messages related to exchanging prestige points.
 * @param vaultMessages The messages related to the vault.
 * @param requirementMessages The messages related to viewing prestige requirements.
 * @param leaderboardMessages The messages related to the prestige leaderboard.
 * @param protectionOrbMessages The messages related to the protection orb.
 * @param multiplierMessages The messages related to multipliers.
 * @param delimiter The deliminator used for listing enchantments.
 * @param finalDelimiter The final deliminator used for listing enchantments.
 */
@ConfigSerializable
public record Locale(
        int version,
        String prefix,
        @NonNull List<String> help,
        String reload,
        String guiOpenError,
        String islandDataNotFound,
        String prestigeLevelUpdated,
        PrestigeMessages prestigeMessages,
        OptInMessages optInMessages,
        OptOutMessages optOutMessages,
        RewardMessages rewardMessages,
        ExchangeMessages exchangeMessages,
        VaultMessages vaultMessages,
        RequirementMessages requirementMessages,
        LeaderboardMessages leaderboardMessages,
        ProtectionOrbMessages protectionOrbMessages,
        MultiplierMessages multiplierMessages,
        String delimiter,
        String finalDelimiter) {
    /**
     * The messages related to prestiging an island.
     * @param prestigePlayerOnly The message sent when a non-player tries to prestige.
     * @param prestigeLevelMax The message sent when a player tries to prestige their island, but they are at the max level.
     * @param playerInWrongWorld The message sent when a player tries to prestige in a non-BentoBox world.
     * @param playerNotOnIsland The message sent when a player tries to prestige while not on an Island.
     * @param islandNotOwned The message sent when a player tries to prestige an island that is not owned.
     * @param playerNotMemberOrOwner The message sent when a player tries to prestige an island they are not the owner or a member of.
     * @param prestigeInProgress The message sent when a player tries to prestige an island, but another player initiated the prestige process.
     * @param notEnoughItems The message sent when a player tries to prestige an island, but collectively, the online members lack the required items.
     * @param notEnoughMoney The message sent when a player tries to prestige an island, but collectively, the online members lack the required money.
     * @param notEnoughPrestigePoints The message sent when the island doesn't have enough prestige points to prestige.
     * @param questIncomplete The message sent when the island members have not completed the required quests to prestige.
     * @param islandOptedOut The message sent when the island is opted out of prestige, but a player tries to prestige.
     * @param prestigeConfigError The message sent when a player tries to prestige their island, but it fails due to a config error.
     * @param requirementError The message sent when a player tries to prestige their island, but it fails due to a requirement config error.
     * @param prestigeAnnouncement The message sent to the server when a player prestiges their island.
     * @param prestigeIslandMemberMessage The message sent to online island members when their island is prestiged.
     */
    @ConfigSerializable
    public record PrestigeMessages(
            String prestigePlayerOnly,
            String prestigeLevelMax,
            String playerInWrongWorld,
            String playerNotOnIsland,
            String islandNotOwned,
            String playerNotMemberOrOwner,
            String prestigeInProgress,
            String notEnoughItems,
            String notEnoughMoney,
            String notEnoughPrestigePoints,
            String questIncomplete,
            String islandOptedOut,
            String prestigeConfigError,
            String requirementError,
            String prestigeAnnouncement,
            String prestigeIslandMemberMessage) {}

    /**
     * The messages related to opting into prestige.
     * @param playerInWrongWorld The message sent when a player tries to opt into prestige in a non-BentoBox world.
     * @param playerNotOnIsland The message sent when a player tries to opt into  prestige while not on an Island.
     * @param islandNotOwned The message sent when a player tries to opt into  prestige and that island is not owned.
     * @param playerNotMemberOrOwner The message sent when a player tries to opt into prestige an island they are not the owner or a member of.
     * @param islandAlreadyOptedIn The message sent when a player tries to opt into prestige when their island is already opted in.
     * @param islandMemberMessage The message sent to online island members when their island is opted into prestige.
     * @param optInInProgress The message sent when a player tries to opt into prestige, but another player has initiated the process.
     */
    @ConfigSerializable
    public record OptInMessages(
            String playerInWrongWorld,
            String playerNotOnIsland,
            String islandNotOwned,
            String playerNotMemberOrOwner,
            String islandAlreadyOptedIn,
            String islandMemberMessage,
            String optInInProgress) {}

    /**
     * The messages related to opting out of prestige.
     * @param playerInWrongWorld The message sent when a player tries to opt out of prestige in a non-BentoBox world.
     * @param playerNotOnIsland The message sent when a player tries to opt out of prestige while not on an Island.
     * @param islandNotOwned The message sent when a player tries to opt out of prestige and that island is not owned.
     * @param playerNotMemberOrOwner The message sent when a player tries to opt out of prestige an island they are not the owner or a member of.
     * @param islandAlreadyOptedOut The message sent when a player tries to opt out of prestige when their island is already opted out.
     * @param islandMemberMessage The message sent to online island members when their island is opted out of prestige.
     * @param optOutInProgress The message sent when a player tries to opt out of prestige, but another player has initiated the process.
     */
    @ConfigSerializable
    public record OptOutMessages(
            String playerInWrongWorld,
            String playerNotOnIsland,
            String islandNotOwned,
            String playerNotMemberOrOwner,
            String islandAlreadyOptedOut,
            String islandMemberMessage,
            String optOutInProgress) {}

    /**
     * The messages related to rewards.
     * @param playerNotOnIsland The message sent when a player tries to view their next prestige level's rewards while not on an island.
     * @param prestigeExempt The message sent when a player tries to view the rewards for next prestige level, but their island is opted out of prestige.
     * @param maxPrestigeLevel The message sent when a player tries to view their next prestige level's rewards, but their island is at the max prestige level.
     * @param prestigeConfigError The message sent to a player when there is no prestige level configured for the level number provided.
     */
    @ConfigSerializable
    public record RewardMessages(
            String playerNotOnIsland,
            String prestigeExempt,
            String maxPrestigeLevel,
            String prestigeConfigError) {}

    /**
     * The messages related to exchanging prestige points.
     * @param playerNotOnIsland The message sent when a player tries to exchange their prestige points, while not on an island.
     * @param prestigeLevelNotMet The message sent when a player tries to exchange their prestige points, but don't meet the required prestige level.
     * @param prestigeExempt The message sent when a player tries to exchange prestige points, but their island is opted out of prestige.
     * @param prestigeInProgress The message sent when a player tries to exchange prestige points, but their island is in the process of being prestiged.
     * @param optOutInProgress The message sent when a player tries to exchange prestige points, but their island is in the process of opting out of prestige.
     * @param notEnoughPrestigePoints The message sent when a player tries to exchange their prestige points, but doesn't have enough prestige points for an exchange.
     */
    @ConfigSerializable
    public record ExchangeMessages(
            String playerNotOnIsland,
            String prestigeLevelNotMet,
            String prestigeExempt,
            String prestigeInProgress,
            String optOutInProgress,
            String notEnoughPrestigePoints) {}

    /**
     * The messages related to the vault.
     * @param playerNotOnIsland The message sent when a player tries to open their island vault while not on an island.
     * @param itemNotAllowed The message sent when a player tries to put an item in their island vault that isn't allowed.
     * @param prestigeExempt The message sent when a player tries to open their island vault, but their island is opted out of prestige.
     * @param prestigeInProgress The message sent when a player tries to access the island vault, but their island is in the process of being prestiged.
     * @param optOutInProgress The message sent when a player tries to access the island vault, but their island is in the process of opting out of prestige.
     */
    @ConfigSerializable
    public record VaultMessages(
            String playerNotOnIsland,
            String itemNotAllowed,
            String prestigeExempt,
            String prestigeInProgress,
            String optOutInProgress) {}

    /**
     * The messages related to viewing prestige requirements.
     * @param playerNotOnIsland The message sent to a player when they are not on an island when attempting to view prestige requirements.
     * @param islandNotOwned The message sent when a player tries to view prestige requirements and the island is not owned.
     * @param playerNotMemberOrOwner The message sent when a player tries to view prestige requirements for an island they are not the owner or a member of.
     * @param prestigeExempt The message sent when a player tries to view prestige requirements for an island that is exempt from prestige.
     * @param maxPrestigeLevel The message sent to a player when their island is at the max prestige level.
     * @param prestigeConfigError The message sent to a player when there is no prestige level configured for the level number provided.
     * @param requirementsConfigError The message sent to a player when there is a config error when viewing prestige level requirements.
     */
    @ConfigSerializable
    public record RequirementMessages(
            String playerNotOnIsland,
            String islandNotOwned,
            String playerNotMemberOrOwner,
            String prestigeExempt,
            String maxPrestigeLevel,
            String prestigeConfigError,
            String requirementsConfigError) {}

    /**
     * The messages related to the prestige leaderboard.
     * @param islandExempt The message sent when an island is marked as exempt from the top leaderboard placeholders.
     * @param islandUnexempt The message sent when an island is marked as not exempt from the top leaderboard placeholders.
     * @param leaderboardTitle The leaderboard title.
     * @param leaderboardPosition The leaderboard positon text.
     * @param leaderboardPositionEmpty The leaderboard positon empty text.
     */
    @ConfigSerializable
    public record LeaderboardMessages(
            String islandExempt,
            String islandUnexempt,
            String leaderboardTitle,
            String leaderboardPosition,
            String leaderboardPositionEmpty) {}

    /**
     * The messages related to the protection orb item.
     * @param protectionOrbAlreadyProtected The message sent when an item is already protected.
     * @param protectionOrbNotAllowed The message sent when an item is not allowed to be protected.
     * @param protectionOrbProtected The message sent when an item is protected.
     */
    @ConfigSerializable
    public record ProtectionOrbMessages(
            String protectionOrbNotAllowed,
            String protectionOrbAlreadyProtected,
            String protectionOrbProtected) {}

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