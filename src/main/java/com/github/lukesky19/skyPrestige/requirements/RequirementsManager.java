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
package com.github.lukesky19.skyPrestige.requirements;

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.InventoryRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.MoneyRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.PrestigePointsRequirement;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.integration.hooks.*;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.util.inventory.InventoryUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.math.EquationUtil;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.leonardobishop.quests.common.player.QPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * This class manages checking if requirements are complete to prestige.
 */
public class RequirementsManager {
    private final @NonNull ComponentLogger logger;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public RequirementsManager(
            @NonNull SkyPlugin plugin,
            @NonNull LocaleManager localeManager,
            @NonNull HookManager hookManager) {
        this.logger = plugin.getComponentLogger();
        this.localeManager = localeManager;
        this.hookManager = hookManager;
    }

    /**
     * Does the island meet the requirements to prestige?
     * @param player The {@link Player} attempting to prestige the island.
     * @param playerList The {@link List} of online island members as {@link QPlayer}s.
     * @param offlinePlayerList The {@link List} of {@link OfflinePlayer} for all island members.
     * @param qPlayerList The {@link List} of {@link QPlayer}s for all island members.
     * @param islandData The {@link IslandData}.
     * @param requiredItems The {@link List} of required {@link ItemStack}s.
     * @param requiredMoney The required money.
     * @param requiredPrestigePoints The required prestige points.
     * @param requiredQuestIds The {@link List} of required quest ids as {@link String}s.
     * @return true if requirements are met, false if not.
     */
    public boolean areRequirementsNotMet(
            @NonNull Player player,
            @NonNull List<Player> playerList,
            @NonNull List<OfflinePlayer> offlinePlayerList,
            @NonNull List<QPlayer> qPlayerList,
            @NonNull IslandData islandData,
            @NonNull Set<ItemStack> requiredItems,
            double requiredMoney,
            double requiredPrestigePoints,
            @NonNull List<String> requiredQuestIds) {
        return !isInventoryItemsMet(player, playerList, requiredItems)
                || !isMoneyMet(player, offlinePlayerList, requiredMoney)
                || !isPrestigePointsMet(player, islandData, requiredPrestigePoints)
                || !isQuestsComplete(player, qPlayerList, requiredQuestIds);
    }

    /**
     * Do all online island members collectively have enough items to prestige?
     * @param player The {@link Player} attempting to prestige the island.
     * @param playerList The {@link List} of {@link Player}s for onlien island members.
     * @param requiredItems The {@link List} of {@link ItemStack}s.
     * @return true if the inventory requirements are met, false if not.
     */
    private boolean isInventoryItemsMet(
            @NonNull Player player,
            @NonNull List<Player> playerList,
            @NonNull Set<ItemStack> requiredItems) {
        RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);
        SkyHoppersHook skyHoppersHook = hookManager.getHook(SkyHoppersHook.class);
        SkySellWandsHook skySellWandsHook = hookManager.getHook(SkySellWandsHook.class);
        ExcellentCratesHook excellentCratesHook = hookManager.getHook(ExcellentCratesHook.class);

        Locale locale = localeManager.getConfiguration();
        for(ItemStack requiredStack : requiredItems) {
            int playersAmount = playerList.stream()
                    .filter(Objects::nonNull)
                    .filter(member -> member.isOnline() && member.isConnected())
                    .map(member -> InventoryUtils.getItemAmountInInventory(roseStackerHook, skyHoppersHook, skySellWandsHook, excellentCratesHook, requiredStack, member.getInventory()))
                    .reduce(Integer::sum)
                    .orElse(0);

            if(playersAmount < requiredStack.getAmount()) {
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.prestigeMessages().notEnoughItems()));
                return false;
            }
        }

        return true;
    }

    /**
     * Do all island members collectively have enough money to prestige?
     * @param player The {@link Player} attempting to prestige the island.
     * @param playerList The {@link List} of {@link OfflinePlayer}s for the island members.
     * @param requiredMoney The money required.
     * @return true if the money requirement is met, false if not.
     */
    private boolean isMoneyMet(
            @NonNull Player player,
            @NonNull List<OfflinePlayer> playerList,
            double requiredMoney) {
        Locale locale = localeManager.getConfiguration();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);

        double balance = playerList.stream()
                .map(economyHook::getBalance)
                .reduce(Double::sum)
                .orElse(0.0);

        if(balance < requiredMoney) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.prestigeMessages().notEnoughMoney()));
            return false;
        }

        return true;
    }

    /**
     * Does the island meet the required prestige points to prestige?
     * @param player The {@link Player} attempting to prestige the island.
     * @param islandData The {@link IslandData}.
     * @param requiredPrestigePoints The required prestige points.
     * @return true if the prestige points requirement is met, false if not.
     */
    private boolean isPrestigePointsMet(
            @NonNull Player player,
            @NonNull IslandData islandData,
            double requiredPrestigePoints) {
        Locale locale = localeManager.getConfiguration();

        if(islandData.getPrestigePoints() < requiredPrestigePoints) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.prestigeMessages().notEnoughPrestigePoints()));
            return false;
        }

        return true;
    }

    /**
     * Does at least one island member have the quest requirements completed?
     * @apiNote Any island member can have a quest completed, as long as all quests are completed.
     * @param player The player attempting to prestige the island.
     * @param playerList The {@link List} of {@link QPlayer}s.
     * @param requiredQuesIds The {@link List} of required quests as a {@link String}.
     * @return true if all quests have been completed, false if not.
     */
    private boolean isQuestsComplete(
            @NonNull Player player,
            @NonNull List<QPlayer> playerList,
            @NonNull List<String> requiredQuesIds) {
        if(requiredQuesIds.isEmpty()) return true;

        Locale locale = localeManager.getConfiguration();
        LMBQuestHook lmbQuestHook = hookManager.getHook(LMBQuestHook.class);
        Component requirementError = AdventureUtility.deserialize(locale.prefix() + locale.prestigeMessages().requirementError());
        if(!lmbQuestHook.isHooked()) {
            player.sendMessage(requirementError);
            logger.error(AdventureUtility.plain("The quest plugin isn't hooked into, but required quests are configured."));
            return false;
        }

        for(String questId : requiredQuesIds) {
            if(!lmbQuestHook.isQuestComplete(playerList, questId)) {
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.prestigeMessages().questIncomplete(),
                        List.of(Placeholder.parsed("quest_id", questId))));
                return false;
            }
        }

        return true;
    }

    /**
     * Remove the required items to prestige from online island members.
     * @param islandMembers The {@link List} of online island members as {@link Player}s.
     * @param requiredItems The {@link Map} of {@link ItemStack}s to {@link Boolean}s required.
     */
    public void removeRequiredItems(
            @NonNull List<Player> islandMembers,
            @NonNull Map<ItemStack, Boolean> requiredItems) {
        RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);
        SkyHoppersHook skyHoppersHook = hookManager.getHook(SkyHoppersHook.class);
        SkySellWandsHook skySellWandsHook = hookManager.getHook(SkySellWandsHook.class);
        ExcellentCratesHook excellentCratesHook = hookManager.getHook(ExcellentCratesHook.class);

        for(Map.Entry<ItemStack, Boolean> entry : requiredItems.entrySet()) {
            if(entry.getValue()) continue;
            ItemStack requiredStack = entry.getKey();

            int amountToRemove = requiredStack.getAmount();

            for(Player member : islandMembers) {
                amountToRemove -= InventoryUtils.removeRequiredItem(logger, roseStackerHook, skyHoppersHook, skySellWandsHook, excellentCratesHook, member, requiredStack, requiredStack.getAmount());

                if(amountToRemove <= 0) break;
            }
        }
    }

    /**
     * Remove the required money to prestige from online island members.
     * @param islandMembers The {@link List} of {@link OfflinePlayer}s for the island members.
     * @param requiredMoney The required money.
     */
    public void removeRequiredMoney(
            @NonNull List<OfflinePlayer> islandMembers,
            double requiredMoney) {
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        if(!economyHook.isHooked() || requiredMoney <= 0) return;

        for(OfflinePlayer member : islandMembers) {
            requiredMoney -= economyHook.removeFromBalance(member, requiredMoney);

            if(requiredMoney <= 0) return;
        }
    }

    /**
     * Remove the required prestige points from the island.
     * @param islandData The {@link IslandData}.
     * @param requiredPrestigePoints The required prestige points.
     */
    public void removeRequiredPrestigePoints(
            @NonNull IslandData islandData,
            double requiredPrestigePoints) {
        islandData.removePrestigePoints(requiredPrestigePoints);
    }

    /**
     * Get the count of the required items all online island members have.
     * @param playerList The {@link List} of online island members as {@link Player}s.
     * @param requiredItems The {@link List} of {@link ItemStack}s required.
     * @return The count of completed item requirements.
     */
    public int getItemCompletedCount(
            @NonNull List<Player> playerList,
            @NonNull List<ItemStack> requiredItems) {
        RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);
        SkyHoppersHook skyHoppersHook = hookManager.getHook(SkyHoppersHook.class);
        SkySellWandsHook skySellWandsHook = hookManager.getHook(SkySellWandsHook.class);
        ExcellentCratesHook excellentCratesHook = hookManager.getHook(ExcellentCratesHook.class);

        int completedCount = 0;
        for(ItemStack requiredStack : requiredItems) {
            int playersAmount = playerList.stream()
                    .filter(Objects::nonNull)
                    .filter(member -> member.isOnline() && member.isConnected())
                    .map(member -> InventoryUtils.getItemAmountInInventory(roseStackerHook, skyHoppersHook, skySellWandsHook, excellentCratesHook, requiredStack, member.getInventory()))
                    .reduce(Integer::sum)
                    .orElse(0);

            if(playersAmount >= requiredStack.getAmount()) {
                completedCount++;
            }
        }

        return completedCount;
    }

    /**
     * Get the amount of money all island members have.
     * @param playerList The {@link List} of {@link OfflinePlayer}s for island members.
     * @return The amount of money they have.
     */
    public double getMoneyCompletedCount(@NonNull List<OfflinePlayer> playerList) {
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);

        return playerList.stream()
                .map(economyHook::getBalance)
                .reduce(Double::sum)
                .orElse(0.0);
    }

    /**
     * Get the number of required quests the island members collectively have completed.
     * @param playerList The {@link List} of {@link QPlayer}s.
     * @param requiredQuestIds The {@link List} of required quest ids as a {@link String}.
     * @return The count of completed quests.
     */
    public int getQuestsCompletedCount(
            @NonNull List<QPlayer> playerList,
            @NonNull List<String> requiredQuestIds) {
        if(requiredQuestIds.isEmpty()) return 0;

        LMBQuestHook lmbQuestHook = hookManager.getHook(LMBQuestHook.class);
        if(!lmbQuestHook.isHooked()) return 0;

        return requiredQuestIds.stream()
                .map(questId -> lmbQuestHook.isQuestComplete(playerList, questId) ? 1 : 0)
                .reduce(Integer::sum)
                .orElse(0);
    }

    /**
     * Calculate the required items required for the island to prestige.
     * @param memberCount The number of island members.
     * @param islandData The island's {@link IslandData}.
     * @param inventoryRequirements The {@link List} of {@link InventoryRequirement}s.
     * @return The {@link Map} of {@link ItemStack}s to {@link Boolean}. The boolean is for whether the item should be removed on prestige.
     */
    public @NonNull Map<ItemStack, Boolean> calculateRequiredItems(
            int memberCount,
            @NonNull IslandData islandData,
            @NonNull List<InventoryRequirement> inventoryRequirements) {
        if(islandData.isPrestigeExempt() || inventoryRequirements.isEmpty()) {
            return new HashMap<>();
        }

        Map<ItemStack, Boolean> itemStackMap = new HashMap<>();
        for(InventoryRequirement inventoryRequirement : inventoryRequirements) {
            int requiredAmount = scale(
                    inventoryRequirement.scaleFormula(),
                    inventoryRequirement.item().amount() != null ? inventoryRequirement.item().amount() : 1,
                    memberCount,
                    inventoryRequirement.scaleFactor());

            ItemStackBuilder requiredItemBuilder = new ItemStackBuilder(logger);
            requiredItemBuilder.fromItemStackConfig(inventoryRequirement.item(), null, List.of());
            requiredItemBuilder.setAmount(requiredAmount);

            Optional<ItemStack> optionalRequiredItem = requiredItemBuilder.buildItemStack();
            if(optionalRequiredItem.isEmpty()) {
                logger.warn(AdventureUtility.plain("Failed to create a required ItemStack for an inventory requirement."));
                continue;
            }

            itemStackMap.put(optionalRequiredItem.get(), inventoryRequirement.removeItem());
        }

        return itemStackMap;
    }

    /**
     * Calculate the money required for the island to prestige.
     * @param memberCount The number of island members.
     * @param islandData The island's {@link IslandData}.
     * @param moneyRequirement The {@link MoneyRequirement}.
     * @return The required money.
     */
    public double calculateRequiredMoney(
            int memberCount,
            @NonNull IslandData islandData,
            @NonNull MoneyRequirement moneyRequirement) {
        if(islandData.isPrestigeExempt() || moneyRequirement.money() <= 0) {
            return 0;
        }

        return scale(
                moneyRequirement.scaleFormula(),
                moneyRequirement.money(),
                memberCount,
                moneyRequirement.scaleFactor());
    }

    /**
     * Calculate the prestige points required for the island to prestige.
     * @param memberCount The number of island members.
     * @param islandData The island's {@link IslandData}.
     * @param prestigePointsRequirement The {@link PrestigePointsRequirement}.
     * @return The required prestige points.
     */
    public double calculateRequiredPrestigePoints(
            int memberCount,
            @NonNull IslandData islandData,
            @NonNull PrestigePointsRequirement prestigePointsRequirement) {
        if(islandData.isPrestigeExempt() || prestigePointsRequirement.prestigePoints() <= 0) {
            return 0;
        }

        return scale(
                prestigePointsRequirement.scaleFormula(),
                prestigePointsRequirement.prestigePoints(),
                memberCount,
                prestigePointsRequirement.scaleFactor());
    }

    /**
     * Scale the amount provided using the formula and data provided.
     * @apiNote Will return the unscaled amount if the formula is null or scale factor is less than or equal to 0.
     * @param formula The formula.
     * @param amount The amount to scale.
     * @param playerCount The player count.
     * @param scaleFactor The scale factor.
     * @return The scaled amount.
     */
    public int scale(@Nullable String formula, int amount, int playerCount, double scaleFactor) {
        if(formula != null && scaleFactor > 0) {
            HashMap<String, String> variables = new HashMap<>();
            variables.put("r", String.valueOf(amount));
            variables.put("p", String.valueOf(playerCount));
            variables.put("k", String.valueOf(scaleFactor));

            try {
                return EquationUtil.evaluateEquation(formula, variables).intValue();
            } catch (RuntimeException e) {
                logger.warn(AdventureUtility.plain("Failed to scale amount. The non-scaled value will be returned. Error: " + e.getMessage()));
                return amount;
            }
        } else {
            return amount;
        }
    }

    /**
     * Scale the amount provided using the formula and data provided.
     * @apiNote Will return the unscaled amount if the formula is null or scale factor is less than or equal to 0.
     * @param formula The formula.
     * @param amount The amount to scale.
     * @param playerCount The player count.
     * @param scaleFactor The scale factor.
     * @return The scaled amount.
     */
    public double scale(@Nullable String formula, double amount, int playerCount, double scaleFactor) {
        if(formula != null && scaleFactor > 0) {
            HashMap<String, String> variables = new HashMap<>();
            variables.put("r", String.valueOf(amount));
            variables.put("p", String.valueOf(playerCount));
            variables.put("k", String.valueOf(scaleFactor));

            try {
                return EquationUtil.evaluateEquation(formula, variables);
            } catch (RuntimeException e) {
                logger.warn(AdventureUtility.plain("Failed to scale amount. The non-scaled value will be returned. Error: " + e.getMessage()));
                return amount;
            }
        } else {
            return amount;
        }
    }
}