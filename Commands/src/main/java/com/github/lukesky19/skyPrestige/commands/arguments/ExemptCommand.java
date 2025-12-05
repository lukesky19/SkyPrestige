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
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.dataHandler.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.hook.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class creates the exempt command argument for the skyprestige command.
 */
public class ExemptCommand {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public ExemptCommand(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.localeManager = localeManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the exempt command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the exempt command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("exempt")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.exempt"))
                .then(Commands.argument("island_id", StringArgumentType.word())
                        .suggests((ctx, suggestionsBuilder) -> {
                            BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
                            Map<String, Message> suggestionsMap = new HashMap<>();

                            plugin.getServer().getOnlinePlayers().forEach(player -> {
                                @NotNull List<Island> islands = bentoBoxHook.getIslands(player.getUniqueId());
                                islands.forEach(island -> {
                                    String islandId = island.getUniqueId();
                                    if (!suggestionsMap.containsKey(islandId)) {
                                        String islandMembersNames = island.getMemberSet().stream().map(memberId ->
                                                        plugin.getServer().getOfflinePlayer(memberId).getName())
                                                .collect(Collectors.joining(","));
                                        Message toolTip = MessageComponentSerializer.message().serialize(AdventureUtil.deserialize("Members: " + islandMembersNames));

                                        suggestionsMap.put(islandId, toolTip);
                                    }
                                });
                            });

                            suggestionsMap.forEach(suggestionsBuilder::suggest);

                            return suggestionsBuilder.buildFuture();
                        })
                        .executes(ctx -> {
                            Locale locale = localeManager.getConfiguration();
                            CommandSender sender = ctx.getSource().getSender();
                            String islandId = ctx.getArgument("island_id", String.class);
                            @Nullable IslandData islandData = islandDataManager.getData(islandId);
                            if(islandData == null) {
                                if(sender instanceof Player) {
                                    sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
                                } else {
                                    sender.sendMessage(AdventureUtil.deserialize(locale.islandDataNotFound()));
                                }

                                return 0;
                            }

                            islandData.setExempt(true);

                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandExempt(), List.of(Placeholder.parsed("island_id", islandId))));
                            } else {
                                sender.sendMessage(AdventureUtil.deserialize(locale.islandExempt(), List.of(Placeholder.parsed("island_id", islandId))));
                            }

                            return 1;
                        })
                ).build();
    }
}
