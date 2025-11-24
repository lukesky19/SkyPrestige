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
package com.github.lukesky19.skyPrestige.locale;

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
 * @param islandNotFound The message sent when an island cannot be found.
 * @param islandDataNotFound The message sent when the island's data were not found for an island.
 * @param islandPrestigeLevelUpdated The message sent when an island has its prestige level updated.
 * @param prestigePlayerOnly The message sent when a non-player tries to prestige.
 * @param islandPrestigeLevelMax The message sent when a player tries to prestige their island, but they are at the max level.
 * @param prestigePlayerInWrongWorld The message sent when a player tries to prestige in a non-BentoBox world.
 * @param prestigePlayerNotOnIsland The message sent when a player tries to prestige while not on an Island.
 * @param prestigeIslandNotOwned The message sent when a player tries to prestige an island that is not owned.
 * @param prestigePlayerNotMemberOrOwner The message sent when a player tries to prestige an island they are not the owner or a member of.
 * @param prestigeNotEnoughPrestigePoints The message sent when the island doesn't have enough prestige points to prestige.
 * @param prestigeInventoryReset The message sent when a player has their inventory reset as part of prestiging their island.
 * @param prestigeEnderChestReset The message sent when a player has their ender chest reset as part of prestiging their island.
 * @param prestigeExperienceReset The message sent when a player has their experience reset as part of prestiging their island.
 * @param prestigeBalanceReset The message sent when a player has their balance reset as part of prestiging their island.
 * @param prestigeAuctionHouseItemsReset The message sent when a player has their auction house items reset as a part of prestiging their island.
 * @param prestigeStartingMoneyGiven The message sent when a player has been given starting money as part of prestiging their island.
 * @param islandMemberPrestigeNotice The message sent to island members when their island was prestiged.
 * @param otherPrestigeNotice The message sent to non-island members when an island they are cooped or trusted on was prestiged.
 * @param islandMemberIslandTeleportNotice The message sent when an island member is being teleported to their new island due to the island being prestiged.
 * @param islandMemberFallbackTeleportNotice The message sent when an island member is being teleported to the fallback location due to their island being prestiged and the spawn point being null.
 * @param otherIslandTeleportNotice The message sent when any other player is being teleported to a new island due to the island being prestiged.
 * @param otherFallbackTeleportNotice The message sent when any other player is being teleported to the fallback location due to the island being prestiged and the spawn point being null.
 * @param prestigeConfigError The message sent when a player tries to prestige their island, but it fails due to a config error.
 * @param prestigeConfigRequirementError The message sent when a player tries to prestige their island, but it fails due to a requirement config error.
 * @param progressPlayerNotOnIsland The message sent when a player tries to view their progress towards the next prestige level while not on an island.
 * @param progressMaxPrestigeLevel The message sent when a player tries to view their progress towards the next prestige level, but their island is at the max prestige level.
 * @param rewardsPlayerNotOnIsland The message sent when a player tries to view their next prestige level's rewards while not on an island.
 * @param rewardsMaxPrestigeLevel The message sent when a player tries to view their next prestige level's rewards, but their island is at the max prestige level.
 * @param exchangePlayerNotOnIsland The message sent when a player tries to exchange their prestige points, while not on an island.
 * @param exchangePrestigeLevelNotMet The message sent when a player tries to exchange their prestige points, but don't meet the required prestige level.
 * @param exchangeNotEnoughPrestigePoints The message sent when a player tries to exchange their prestige points, but doesn't have enough prestige points for an exchange.
 * @param vaultPlayerNotOnIsland The message sent when a player tries to open their island vault while not on an island.
 * @param vaultItemNotAllowed The message sent when a player tries to put an item in their island vault that isn't allowed.
 * @param requirementsConfigError The message sent to a player when there is a config error when viewing prestige level requirements.
 * @param requirementsLevelNotFound The message sent to a player when there is no prestige level configured for the level number provided.
 * @param requirementsPointsForLevel The message sent to a player to display prestige points required for a level.
 * @param islandExempt The message sent when an island is marked as exempt from the top leaderboard placeholders.
 * @param islandUnexempt The message sent when an island is marked as not exempt from the top leaderboard placeholders.
 * @param leaderboardTitle The leaderboard title.
 * @param leaderboardPosition The leaderboard positon text.
 * @param leaderboardPositionEmpty The leaderboard positon empty text.
 * @param protectionOrbAlreadyProtected The message sent when an item is already protected.
 * @param protectionOrbNotAllowed The message sent when an item is not allowed to be protected.
 * @param protectionOrbProtected The message sent when an item is protected.
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
        String islandNotFound,
        String islandDataNotFound,
        String islandPrestigeLevelUpdated,
        String prestigePlayerOnly,
        String islandPrestigeLevelMax,
        String prestigePlayerInWrongWorld,
        String prestigePlayerNotOnIsland,
        String prestigeIslandNotOwned,
        String prestigePlayerNotMemberOrOwner,
        String prestigeNotEnoughPrestigePoints,
        String prestigeInventoryReset,
        String prestigeEnderChestReset,
        String prestigeExperienceReset,
        String prestigeBalanceReset,
        String prestigeAuctionHouseItemsReset,
        String prestigeStartingMoneyGiven,
        String islandMemberPrestigeNotice,
        String otherPrestigeNotice,
        String islandMemberIslandTeleportNotice,
        String islandMemberFallbackTeleportNotice,
        String otherIslandTeleportNotice,
        String otherFallbackTeleportNotice,
        String prestigeConfigError,
        String prestigeConfigRequirementError,
        String progressPlayerNotOnIsland,
        String progressMaxPrestigeLevel,
        String rewardsPlayerNotOnIsland,
        String rewardsMaxPrestigeLevel,
        String exchangePlayerNotOnIsland,
        String exchangePrestigeLevelNotMet,
        String exchangeNotEnoughPrestigePoints,
        String vaultPlayerNotOnIsland,
        String vaultItemNotAllowed,
        String requirementsLevelNotFound,
        String requirementsConfigError,
        String requirementsPointsForLevel,
        String islandExempt,
        String islandUnexempt,
        String leaderboardTitle,
        String leaderboardPosition,
        String leaderboardPositionEmpty,
        String protectionOrbNotAllowed,
        String protectionOrbAlreadyProtected,
        String protectionOrbProtected,
        String delimiter,
        String finalDelimiter) {
}
