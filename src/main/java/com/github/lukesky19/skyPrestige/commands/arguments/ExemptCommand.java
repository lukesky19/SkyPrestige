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

import com.github.lukesky19.skyPrestige.commands.util.IslandArgumentType;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;

/**
 * This class creates the exempt command argument for the skyprestige command.
 */
public class ExemptCommand {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public ExemptCommand(
            @NonNull SkyPlugin plugin,
            @NonNull LocaleManager localeManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager) {
        this.plugin = plugin;
        this.localeManager = localeManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the exempt command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the exempt command argument for the /skyprestige command.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("exempt")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.exempt"))
                .then(Commands.argument("island_id", new IslandArgumentType(plugin, hookManager))
                        .executes(ctx -> {
                            Locale locale = localeManager.getConfiguration();
                            Locale.LeaderboardMessages leaderboardMessages = locale.leaderboardMessages();

                            CommandSender sender = ctx.getSource().getSender();
                            Island island = ctx.getArgument("island_id", Island.class);
                            String islandId = island.getUniqueId();
                            IslandData islandData = islandDataManager.getData(islandId);
                            if(islandData == null) {
                                if(sender instanceof Player) {
                                    sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.islandDataNotFound()));
                                } else {
                                    sender.sendMessage(AdventureUtility.deserialize(locale.islandDataNotFound()));
                                }

                                return 0;
                            }

                            islandData.setLeaderboardExempt(true);

                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + leaderboardMessages.islandExempt(), List.of(Placeholder.parsed("island_id", islandId))));
                            } else {
                                sender.sendMessage(AdventureUtility.deserialize(leaderboardMessages.islandExempt(), List.of(Placeholder.parsed("island_id", islandId))));
                            }

                            return 1;
                        })
                ).build();
    }
}
