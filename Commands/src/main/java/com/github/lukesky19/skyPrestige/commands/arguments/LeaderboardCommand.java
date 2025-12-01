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
package com.github.lukesky19.skyPrestige.commands.arguments;

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.data.leaderboard.Position;
import com.github.lukesky19.skyPrestige.data.leaderboard.TopTen;
import com.github.lukesky19.skyPrestige.leaderboard.LeaderboardManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

/**
 * This class creates the leaderboard command argument for the skyprestige command.
 */
public class LeaderboardCommand {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull LeaderboardManager leaderboardManager;
    private final @NotNull DecimalFormat decimalFormat = new DecimalFormat("#.##");

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param leaderboardManager A {@link LeaderboardManager} instance.
     */
    public LeaderboardCommand(
            @NotNull LocaleManager localeManager,
            @NotNull LeaderboardManager leaderboardManager) {
        this.localeManager = localeManager;
        this.leaderboardManager = leaderboardManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the leaderboard command argument.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the leaderboard command argument.
     */
    public LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("leaderboard")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.leaderboard"))
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    CommandSender sender = ctx.getSource().getSender();
                    @NotNull TopTen topTen = leaderboardManager.getTopTenNotExempt();

                    sender.sendMessage(AdventureUtil.deserialize(locale.leaderboardTitle()));

                    int positionNumber = 1;
                    for(Position position : topTen.getPositions()) {
                        List<TagResolver.Single> placeholders = List.of(
                                Placeholder.parsed("position", String.valueOf(positionNumber)),
                                Placeholder.parsed("player_name", Objects.requireNonNullElse(position.ownerName(), "Unknown Player")),
                                Placeholder.parsed("prestige_level", String.valueOf(position.prestigeLevel())),
                                Placeholder.parsed("prestige_points", String.valueOf(decimalFormat.format(position.prestigePoints()))));

                        sender.sendMessage(AdventureUtil.deserialize(locale.leaderboardPosition(), placeholders));

                        positionNumber++;
                    }

                    while(positionNumber <= 10) {
                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("position", String.valueOf(positionNumber)));

                        sender.sendMessage(AdventureUtil.deserialize(locale.leaderboardPositionEmpty(), placeholders));

                        positionNumber++;
                    }

                    return 1;
                }).build();
    }
}