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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.locale.Locale;
import com.github.lukesky19.skyPrestige.locale.LocaleManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class creates the level command argument for the skyprestige command.
 */
public class PrestigeLevelCommand {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull IslandDataManager islandDataManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     */
    public PrestigeLevelCommand(
            @NotNull SkyPrestige skyPrestige,
            @NotNull LocaleManager localeManager,
            @NotNull IslandDataManager islandDataManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.localeManager = localeManager;
        this.islandDataManager = islandDataManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the level command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the level command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("level")
            .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.level"))
            .then(Commands.literal("set")
                .then(Commands.argument("island_id", StringArgumentType.word())
                    .suggests((ctx, suggestionsBuilder) -> {
                        Map<String, Message> suggestionsMap = new HashMap<>();
                        skyPrestige.getServer().getOnlinePlayers().forEach(player -> {
                            List<Island> islands = BentoBox.getInstance().getIslandsManager().getIslands(player.getUniqueId());

                            for(Island island : islands) {
                                String islandId = island.getUniqueId();
                                if(suggestionsMap.containsKey(islandId)) continue;

                                String islandMembersNames = island.getMemberSet().stream().map(memberId ->
                                                skyPrestige.getServer().getOfflinePlayer(memberId).getName())
                                        .collect(Collectors.joining(","));
                                Message toolTip = MessageComponentSerializer.message().serialize(AdventureUtil.deserialize("Members: " + islandMembersNames));

                                suggestionsMap.put(islandId, toolTip);
                            }
                        });

                        suggestionsMap.forEach(suggestionsBuilder::suggest);

                        return suggestionsBuilder.buildFuture();
                    })
                    .then(Commands.argument("level", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            String islandId = ctx.getArgument("island_id", String.class);
                            int prestigeLevel = ctx.getArgument("level", int.class);

                            IslandData islandData = islandDataManager.getIslandData(islandId);
                            if(islandData == null) {
                                logger.error(AdventureUtil.deserialize("No island data found for the island " + islandId + "."));

                                if(sender instanceof Player player) {
                                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
                                } else {
                                    logger.error(AdventureUtil.deserialize(locale.islandDataNotFound()));
                                }

                                return 0;
                            }

                            islandData.setPrestigeLevel(prestigeLevel);

                            List<TagResolver.Single> placeholders = List.of(
                                    Placeholder.parsed("island_id", islandId),
                                    Placeholder.parsed("prestige_level", String.valueOf(islandData.getPrestigeLevel())));

                            if(sender instanceof Player player) {
                                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandPrestigeLevelUpdated(), placeholders));
                            } else {
                                logger.error(AdventureUtil.deserialize(locale.islandPrestigeLevelUpdated(), placeholders));
                            }

                            return 1;
                        })))).build();
    }
}
