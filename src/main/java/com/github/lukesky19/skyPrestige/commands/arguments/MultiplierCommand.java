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
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.ArrayList;
import java.util.List;

/**
 * This class creates the multiplier command argument for the skyprestige command.
 */
public class MultiplierCommand {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull MultiplierManager multiplierManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public MultiplierCommand(
            @NonNull SkyPlugin plugin,
            @NonNull LocaleManager localeManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull HookManager hookManager) {
        this.plugin = plugin;
        this.localeManager = localeManager;
        this.multiplierManager = multiplierManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the multiplier command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the multiplier command argument for the /skyprestige command.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("multiplier");
        builder.requires(ctx -> ctx.getSender() instanceof Player && ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier"));
        
        builder.executes(ctx -> {
            Locale locale = localeManager.getConfiguration();
            Locale.MultiplierMessages multiplierMessages = locale.multiplierMessages();
            Player player = (Player) ctx.getSource().getSender();
            BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
            Island island = bentoBoxHook.getIslandAtLocation(player.getLocation()).orElse(null);
            if(island == null) {
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + multiplierMessages.multiplierNotOnIsland()));
                return 0;
            }

            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("multiplier", String.valueOf(multiplierManager.getMultiplier(island))));
            
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + multiplierMessages.effectiveMultiplier(), placeholders));
            
            return 1;
        });
        
        builder.then(Commands.literal("server")
            .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.server"))
            .then(Commands.literal("add")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.server.add"))
                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.0))
                    .then(Commands.argument("time", LongArgumentType.longArg(0))
                        .then(Commands.argument("notice", BoolArgumentType.bool())
                            .executes(ctx -> {
                                Player initiator = (Player) ctx.getSource().getSender();
                                double multiplier = ctx.getArgument("multiplier", double.class);
                                long time = ctx.getArgument("time", long.class);
                                boolean notice = ctx.getArgument("notice", boolean.class);

                                return multiplierManager.addServerMultiplier(initiator, multiplier, time, notice) ? 1 : 0;
                            })
                        )
                    )
                )
            )
                
            .then(Commands.literal("remove")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.server.remove"))
                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.0))
                    .then(Commands.argument("time", LongArgumentType.longArg(0))
                        .then(Commands.argument("notice", BoolArgumentType.bool())
                            .executes(ctx -> {
                                Player initiator = (Player) ctx.getSource().getSender();
                                double multiplier = ctx.getArgument("multiplier", double.class);
                                long time = ctx.getArgument("time", long.class);
                                boolean notice = ctx.getArgument("notice", boolean.class);

                                return multiplierManager.removeServerMultiplier(initiator, multiplier, time, notice) ? 1 : 0;
                            })
                        )
                    )
                )
            )
                
            .then(Commands.literal("set")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.server.set"))
                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.0))
                    .then(Commands.argument("time", LongArgumentType.longArg(0))
                        .then(Commands.argument("notice", BoolArgumentType.bool())
                            .executes(ctx -> {
                                Player initiator = (Player) ctx.getSource().getSender();
                                double multiplier = ctx.getArgument("multiplier", double.class);
                                long time = ctx.getArgument("time", long.class);
                                boolean notice = ctx.getArgument("notice", boolean.class);

                                return multiplierManager.setServerMultiplier(initiator, multiplier, time, notice) ? 1 : 0;
                            })
                        )
                    )
                )
            )
                
            .then(Commands.literal("clear")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.server.clear"))
                .then(Commands.argument("notice", BoolArgumentType.bool())
                    .executes(ctx -> {
                        Player initiator = (Player) ctx.getSource().getSender();
                        boolean notice = ctx.getArgument("notice", boolean.class);

                        multiplierManager.clearServerMultiplier(initiator, notice);

                        return 1;
                    })
                )
            )
                
            .then(Commands.literal("get")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.server.get"))
                .executes(ctx -> {
                    Locale locale = localeManager.getConfiguration();
                    Locale.MultiplierMessages multiplierMessages = locale.multiplierMessages();
                    Player initiator = (Player) ctx.getSource().getSender();
                    double multiplier = multiplierManager.getServerMultiplier();
                    long time = multiplierManager.getServerMultiplierTime();

                    List<TagResolver.Single> placeholders = new ArrayList<>();
                    placeholders.add(Placeholder.parsed("multiplier", String.valueOf(multiplier)));
                    if(time > -1) placeholders.add(Placeholder.component("time", multiplierManager.getTimePlaceholder(multiplierMessages.multiplierTimePlaceholder(), time)));

                    Component message = time != -1 ?
                            AdventureUtility.deserialize(locale.prefix() + multiplierMessages.serverMultiplierTimeLimit(), placeholders) :
                            AdventureUtility.deserialize(locale.prefix() + multiplierMessages.serverMultiplierNoTimeLimit(), placeholders);

                    initiator.sendMessage(message);

                    return 1;
                })
            )

            .executes(ctx -> {
                Locale locale = localeManager.getConfiguration();
                Locale.MultiplierMessages multiplierMessages = locale.multiplierMessages();
                Player initiator = (Player) ctx.getSource().getSender();
                double multiplier = multiplierManager.getServerMultiplier();
                long time = multiplierManager.getServerMultiplierTime();

                List<TagResolver.Single> placeholders = new ArrayList<>();
                placeholders.add(Placeholder.parsed("multiplier", String.valueOf(multiplier)));
                if(time > -1) placeholders.add(Placeholder.component("time", multiplierManager.getTimePlaceholder(multiplierMessages.multiplierTimePlaceholder(), time)));

                Component message = time != -1 ?
                        AdventureUtility.deserialize(locale.prefix() + multiplierMessages.serverMultiplierTimeLimit(), placeholders) :
                        AdventureUtility.deserialize(locale.prefix() + multiplierMessages.serverMultiplierNoTimeLimit(), placeholders);

                initiator.sendMessage(message);

                return 1;
            })
        );

        builder.then(Commands.literal("island")
            .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.island"))
            .then(Commands.literal("add")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.island.add"))
                .then(Commands.argument("island_id", new IslandArgumentType(plugin, hookManager))
                    .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.0))
                        .then(Commands.argument("time", LongArgumentType.longArg(0))
                            .then(Commands.argument("notice", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    Player initiator = (Player) ctx.getSource().getSender();
                                    Island island = ctx.getArgument("island_id", Island.class);
                                    double multiplier = ctx.getArgument("multiplier", double.class);
                                    long time = ctx.getArgument("time", long.class);
                                    boolean notice = ctx.getArgument("notice", boolean.class);
        
                                    return multiplierManager.addIslandMultiplier(initiator, island, multiplier, time, notice) ? 1 : 0;
                                })
                            )
                        )
                    )
                )
            )
                
            .then(Commands.literal("remove")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.island.remove"))
                .then(Commands.argument("island_id", new IslandArgumentType(plugin, hookManager))
                    .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.0))
                        .then(Commands.argument("time", LongArgumentType.longArg(0))
                            .then(Commands.argument("notice", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    Player initiator = (Player) ctx.getSource().getSender();
                                    Island island = ctx.getArgument("island_id", Island.class);
                                    double multiplier = ctx.getArgument("multiplier", double.class);
                                    long time = ctx.getArgument("time", long.class);
                                    boolean notice = ctx.getArgument("notice", boolean.class);
        
                                    return multiplierManager.removeIslandMultiplier(initiator, island, multiplier, time, notice) ? 1 : 0;
                                })
                            )
                        )
                    )
                )
            )
                
            .then(Commands.literal("set")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.island.set"))
                .then(Commands.argument("island_id", new IslandArgumentType(plugin, hookManager))
                    .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.0))
                        .then(Commands.argument("time", LongArgumentType.longArg(0))
                            .then(Commands.argument("notice", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    Player initiator = (Player) ctx.getSource().getSender();
                                    Island island = ctx.getArgument("island_id", Island.class);
                                    double multiplier = ctx.getArgument("multiplier", double.class);
                                    long time = ctx.getArgument("time", long.class);
                                    boolean notice = ctx.getArgument("notice", boolean.class);
        
                                    return multiplierManager.setIslandMultiplier(initiator, island, multiplier, time, notice) ? 1 : 0;
                                })
                            )
                        )
                    )
                )
            )
                
            .then(Commands.literal("clear")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.island.clear"))
                .then(Commands.argument("notice", BoolArgumentType.bool())
                    .executes(ctx -> {
                        Player initiator = (Player) ctx.getSource().getSender();
                        Island island = ctx.getArgument("island_id", Island.class);
                        boolean notice = ctx.getArgument("notice", boolean.class);

                        return multiplierManager.clearIslandMultiplier(initiator, island, notice) ? 1 : 0;
                    })
                )
            )
                
            .then(Commands.literal("get")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.island.get"))
                .then(Commands.argument("island_id", new IslandArgumentType(plugin, hookManager))
                    .executes(ctx -> {
                        Locale locale = localeManager.getConfiguration();
                        Locale.MultiplierMessages multiplierMessages = locale.multiplierMessages();
                        Player initiator = (Player) ctx.getSource().getSender();
                        Island island = ctx.getArgument("island_id", Island.class);
                        double multiplier = multiplierManager.getIslandMultiplier(island);
                        long time = multiplierManager.getIslandMultiplierTime(island);

                        List<TagResolver.Single> placeholders = new ArrayList<>();
                        placeholders.add(Placeholder.parsed("multiplier", String.valueOf(multiplier)));
                        if(time > -1) placeholders.add(Placeholder.component("time", multiplierManager.getTimePlaceholder(multiplierMessages.multiplierTimePlaceholder(), time)));

                        Component message = time != -1 ?
                                AdventureUtility.deserialize(locale.prefix() + multiplierMessages.islandMultiplierTimeLimit(), placeholders) :
                                AdventureUtility.deserialize(locale.prefix() + multiplierMessages.islandMultiplierNoTimeLimit(), placeholders);

                        initiator.sendMessage(message);

                        return 1;
                    })
                )
            )

            .executes(ctx -> {
                Locale locale = localeManager.getConfiguration();
                Locale.MultiplierMessages multiplierMessages = locale.multiplierMessages();
                Player initiator = (Player) ctx.getSource().getSender();

                BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
                Island island = bentoBoxHook.getIslandAtLocation(initiator.getLocation()).orElse(null);
                if(island == null) {
                    initiator.sendMessage(AdventureUtility.deserialize(locale.prefix() + multiplierMessages.multiplierNotOnIsland()));
                    return 0;
                }
                double multiplier = multiplierManager.getIslandMultiplier(island);
                long time = multiplierManager.getIslandMultiplierTime(island);

                List<TagResolver.Single> placeholders = new ArrayList<>();
                placeholders.add(Placeholder.parsed("multiplier", String.valueOf(multiplier)));
                if(time > -1) placeholders.add(Placeholder.component("time", multiplierManager.getTimePlaceholder(multiplierMessages.multiplierTimePlaceholder(), time)));

                Component message = time != -1 ?
                        AdventureUtility.deserialize(locale.prefix() + multiplierMessages.islandMultiplierTimeLimit(), placeholders) :
                        AdventureUtility.deserialize(locale.prefix() + multiplierMessages.islandMultiplierNoTimeLimit(), placeholders);

                initiator.sendMessage(message);

                return 1;
            })
        );

        return builder.build();
    }
}