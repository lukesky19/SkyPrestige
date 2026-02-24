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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.configuration.data.reward.CommandReward;
import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This class handles the processing of {@link CommandReward}s.
 */
public class CommandRewardsProcessor {
    private final @NonNull Server server;
    private final @NonNull ConsoleCommandSender sender;

    /**
     * Constructor
     * @param plugin A {@link SkyPrestige} instance.
     */
    public CommandRewardsProcessor(@NonNull SkyPrestige plugin) {
        this.server = plugin.getServer();
        this.sender = server.getConsoleSender();
    }

    /**
     * Process all command rewards for the initiator and player list provided.
     * @param initiator The initiating {@link Player}.
     * @param playerList The {@link List} of {@link Player}s to give rewards to.
     * @param commandRewardList The {@link List} of {@link CommandReward}s.
     */
    public void process(
            @NonNull Player initiator,
            @NonNull List<Player> playerList,
            @NonNull List<CommandReward> commandRewardList) {
        if(commandRewardList.isEmpty()) return;

        commandRewardList.stream()
                .filter(commandReward -> !commandReward.commands().isEmpty())
                .forEach(commandReward -> {
                    if(commandReward.giveToAllIslandMembers()) {
                        if(!playerList.isEmpty()) {
                            playerList.forEach(player ->
                                    commandReward.commands().stream()
                                            .map(command -> parse(player, command))
                                            .forEach(this::execute));
                        }
                    } else {
                        commandReward.commands().stream()
                                .map(command -> parse(initiator, command))
                                .forEach(this::execute);
                    }
                });
    }

    /**
     * Process all command rewards for the player that logged on.
     * @apiNote Only commands configured for all island members will be executed.
     * This is meant for when an island member was offline when rewards were initially processed.
     * Excluding joining an island, for that, see {@link #processRetroactive(Player, List)}.
     * @param player The {@link Player}.
     * @param commandRewardList The {@link List} of {@link CommandReward}s.
     */
    public void processDeferred(@NonNull Player player, @NonNull List<CommandReward> commandRewardList) {
        if(commandRewardList.isEmpty()) return;

        commandRewardList.stream()
                .filter(CommandReward::giveToAllIslandMembers)
                .forEach(commandReward ->
                        commandReward.commands().stream()
                                .map(command -> parse(player, command))
                                .forEach(this::execute));
    }

    /**
     * Process all command rewards for the player that joined an island.
     * @apiNote Only commands configured for all island members and on island join will be executed.
     * @param player The {@link Player}.
     * @param commandRewardList The {@link List} of {@link CommandReward}s.
     */
    public void processRetroactive(@NonNull Player player, @NonNull List<CommandReward> commandRewardList) {
        if(commandRewardList.isEmpty()) return;

        commandRewardList.stream()
                .filter(CommandReward::giveToAllIslandMembers)
                .filter(IReward::giveOnIslandJoin)
                .forEach(commandReward ->
                        commandReward.commands().stream()
                                .map(command -> parse(player, command))
                                .forEach(this::execute));
    }

    /**
     * Parse any placeholders inside the command.
     * @param player The {@link Player} to parse placeholders for.
     * @param command The command.
     * @return The parsed command.
     */
    private @NonNull String parse(@NonNull Player player, @NonNull String command) {
        return PlaceholderAPIUtil.parsePlaceholders(player, command);
    }

    /**
     * Execute the command.
     * @param command The command.
     */
    private void execute(@NonNull String command) {
        server.dispatchCommand(sender, command);
    }
}