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
package com.github.lukesky19.skyPrestige.processor.reward;

import com.github.lukesky19.skyPrestige.configuration.data.reward.MoneyReward;
import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skyPrestige.integration.hooks.EconomyHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This class handles the processing of {@link MoneyReward}s.
 */
public class MoneyRewardProcessor {
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param hookManager A {@link HookManager} instance.
     */
    public MoneyRewardProcessor(@NonNull HookManager hookManager) {
        this.hookManager = hookManager;
    }

    /**
     * Process all money rewards for the initiator and player list provided.
     * @param initiator The initiating {@link Player}.
     * @param playerList The {@link List} of {@link Player}s to give rewards to.
     * @param moneyRewardList The {@link List} of {@link MoneyReward}s.
     */
    public void process(
            @NonNull Player initiator,
            @NonNull List<Player> playerList,
            @NonNull List<MoneyReward> moneyRewardList) {
        if(moneyRewardList.isEmpty()) return;
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        if(economyHook.isHooked()) return;

        moneyRewardList.stream()
                .filter(moneyReward -> moneyReward.money() > 0)
                .forEach(moneyReward -> {
                    if(moneyReward.giveToAllIslandMembers()) {
                        playerList.forEach(player -> economyHook.addToBalance(player, moneyReward.money()));
                    } else {
                        economyHook.addToBalance(initiator, moneyReward.money());
                    }
                });
    }

    /**
     * Process all money rewards for the player that logged on.
     * @apiNote Only money rewards configured for all island members will be given.
     * @param player The {@link Player}.
     * @param moneyRewardList The {@link List} of {@link MoneyReward}s.
     */
    public void processDeferred(@NonNull Player player, @NonNull List<MoneyReward> moneyRewardList) {
        if(moneyRewardList.isEmpty()) return;
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        if(economyHook.isHooked()) return;

        moneyRewardList.stream()
                .filter(moneyReward -> moneyReward.money() > 0)
                .filter(MoneyReward::giveToAllIslandMembers)
                .forEach(moneyReward -> economyHook.addToBalance(player, moneyReward.money()));
    }

    /**
     * Process all money rewards for the player that joined an island.
     * @apiNote Only money rewards configured for all island members and on island join will be executed.
     * @param player The {@link Player}.
     * @param moneyRewardList The {@link List} of {@link MoneyReward}s.
     */
    public void processRetroactive(@NonNull Player player, @NonNull List<MoneyReward> moneyRewardList) {
        if(moneyRewardList.isEmpty()) return;
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        if(economyHook.isHooked()) return;

        moneyRewardList.stream()
                .filter(moneyReward -> moneyReward.money() > 0)
                .filter(MoneyReward::giveToAllIslandMembers)
                .filter(IReward::giveOnIslandJoin)
                .forEach(moneyReward -> economyHook.addToBalance(player, moneyReward.money()));
    }
}