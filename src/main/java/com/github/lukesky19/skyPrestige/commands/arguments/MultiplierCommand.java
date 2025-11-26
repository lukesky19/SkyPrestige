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
import com.github.lukesky19.skyPrestige.locale.Locale;
import com.github.lukesky19.skyPrestige.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.points.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.settings.Settings;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This class creates the multiplier command argument for the skyprestige command.
 */
public class MultiplierCommand {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull MultiplierManager multiplierManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public MultiplierCommand(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull MultiplierManager multiplierManager) {
        this.skyPrestige = skyPrestige;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.multiplierManager = multiplierManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the multiplier command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the multiplier command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("multiplier");
        builder.requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier"));

        builder.then(Commands.literal("event")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.event"))
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    CommandSender sender = ctx.getSource().getSender();

                    long eventDurationSeconds = multiplierManager.getEventDuration();
                    long nextEventSeconds = multiplierManager.getTimeUntilNextEvent();
                    double eventMultiplier = multiplierManager.getEventMultiplier() + 1;
                    double currentMultiplier = multiplierManager.getMultiplier();

                    if(eventDurationSeconds > 0) {
                        Component timePlaceholder = multiplierManager.getTimePlaceholder(locale.multiplierTimePlaceholder(), eventDurationSeconds);
                        List<TagResolver.Single> placeholderList = List.of(
                                Placeholder.component("time", timePlaceholder),
                                Placeholder.parsed("event_multiplier", String.valueOf(eventMultiplier)),
                                Placeholder.parsed("current_multiplier", String.valueOf(currentMultiplier)));

                        if(sender instanceof Player) {
                            sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.multiplierEventRemainingTime(), placeholderList));
                        } else {
                            sender.sendMessage(AdventureUtil.deserialize(locale.multiplierEventRemainingTime(), placeholderList));
                        }
                    } else {
                        @Nullable Settings settings = settingsManager.getSettings();
                        if(settings == null) return 0;

                        if(nextEventSeconds > 0) {
                            Component timePlaceholder = multiplierManager.getTimePlaceholder(locale.multiplierTimePlaceholder(), nextEventSeconds);
                            List<TagResolver.Single> placeholderList = List.of(
                                    Placeholder.component("time", timePlaceholder),
                                    Placeholder.parsed("event_multiplier", String.valueOf(settings.multiplierEventSettings().multiplier())));

                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.multiplierEventNextTime(), placeholderList));
                            } else {
                                sender.sendMessage(AdventureUtil.deserialize(locale.multiplierEventNextTime(), placeholderList));
                            }
                        } else {
                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.multiplierEventDisabled()));
                            } else {
                                sender.sendMessage(AdventureUtil.deserialize(locale.multiplierEventDisabled()));
                            }
                        }
                    }

                    return 1;
                }));

        builder.then(Commands.literal("add")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.add"))
                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            double multiplier = ctx.getArgument("multiplier", double.class);

                            multiplierManager.addAdditionalMultiplier(multiplier);
                            double updatedMultiplier = multiplierManager.getMultiplier();

                            List<TagResolver.Single> placeholderList = List.of(
                                    Placeholder.parsed("current_multiplier", String.valueOf(updatedMultiplier)));

                            if(!(sender instanceof Player)) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.multiplierChanged(), placeholderList));
                            }

                            Component playerMessage = AdventureUtil.deserialize(locale.prefix() + locale.multiplierChanged(), placeholderList);
                            skyPrestige.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(playerMessage));

                            return 1;
                        })));

        builder.then(Commands.literal("remove")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.remove"))
                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            double multiplier = ctx.getArgument("multiplier", double.class);

                            multiplierManager.removeAdditionalMultiplier(multiplier);
                            double updatedMultiplier = multiplierManager.getMultiplier();

                            List<TagResolver.Single> placeholderList = List.of(
                                    Placeholder.parsed("current_multiplier", String.valueOf(updatedMultiplier)));

                            if(!(sender instanceof Player)) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.multiplierChanged(), placeholderList));
                            }

                            Component playerMessage = AdventureUtil.deserialize(locale.prefix() + locale.multiplierChanged(), placeholderList);
                            skyPrestige.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(playerMessage));

                            return 1;
                        })));

        builder.then(Commands.literal("set")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.set"))
                .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            double multiplier = ctx.getArgument("multiplier", double.class);

                            multiplierManager.setAdditionalMultiplier(multiplier);
                            double updatedMultiplier = multiplierManager.getMultiplier();

                            List<TagResolver.Single> placeholderList = List.of(
                                    Placeholder.parsed("current_multiplier", String.valueOf(updatedMultiplier)));

                            if(!(sender instanceof Player)) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.multiplierChanged(), placeholderList));
                            }

                            Component playerMessage = AdventureUtil.deserialize(locale.prefix() + locale.multiplierChanged(), placeholderList);
                            skyPrestige.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(playerMessage));

                            return 1;
                        })));

        builder.then(Commands.literal("get")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.get"))
                .then(Commands.literal("additional")
                        .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.get.additional"))
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            double additionalMultiplier = multiplierManager.getAdditionalMultiplier();

                            List<TagResolver.Single> placeholderList = List.of(
                                    Placeholder.parsed("additional_multiplier", String.valueOf(additionalMultiplier)));

                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.additionalMultiplierGet(), placeholderList));
                            } else {
                                sender.sendMessage(AdventureUtil.deserialize(locale.additionalMultiplierGet(), placeholderList));
                            }

                            return 1;
                        }))
                .then(Commands.literal("event")
                        .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.event"))
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            double eventMultiplier = multiplierManager.getEventMultiplier();

                            List<TagResolver.Single> placeholderList = List.of(
                                    Placeholder.parsed("event_multiplier", String.valueOf(eventMultiplier)));

                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.eventMultiplierGet(), placeholderList));
                            } else {
                                sender.sendMessage(AdventureUtil.deserialize(locale.eventMultiplierGet(), placeholderList));
                            }

                            return 1;
                        }))
                .then(Commands.literal("total")
                        .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.multiplier.get.total"))
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            CommandSender sender = ctx.getSource().getSender();
                            double totalMultiplier = multiplierManager.getMultiplier();

                            List<TagResolver.Single> placeholderList = List.of(
                                    Placeholder.parsed("total_multiplier", String.valueOf(totalMultiplier)));

                            if(sender instanceof Player) {
                                sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.totalMultiplierGet(), placeholderList));
                            } else {
                                sender.sendMessage(AdventureUtil.deserialize(locale.totalMultiplierGet(), placeholderList));
                            }

                            return 1;
                        }))
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    CommandSender sender = ctx.getSource().getSender();
                    double totalMultiplier = multiplierManager.getMultiplier();

                    List<TagResolver.Single> placeholderList = List.of(
                            Placeholder.parsed("total_multiplier", String.valueOf(totalMultiplier)));

                    if(sender instanceof Player) {
                        sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.totalMultiplierGet(), placeholderList));
                    } else {
                        sender.sendMessage(AdventureUtil.deserialize(locale.totalMultiplierGet(), placeholderList));
                    }

                    return 1;
                }));

        return builder.build();
    }
}
