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

import com.github.lukesky19.skyPrestige.locale.Locale;
import com.github.lukesky19.skyPrestige.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.protection.manager.ProtectionOrbManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class creates the protectionorb command argument for the skyprestige command.
 */
public class ProtectionOrbCommand {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull ProtectionOrbManager protectionOrbManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param protectionOrbManager A {@link ProtectionOrbManager} instance.
     */
    public ProtectionOrbCommand(
            @NotNull LocaleManager localeManager,
            @NotNull ProtectionOrbManager protectionOrbManager) {
        this.localeManager = localeManager;
        this.protectionOrbManager = protectionOrbManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the protectionorb command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the protectionorb command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("protectionorb")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.protectionorb"))
                .then(Commands.literal("give")
                        .then(Commands.argument("player_name", ArgumentTypes.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            Locale locale = localeManager.getLocale();
                                            CommandSender commandSender = ctx.getSource().getSender();
                                            Player player = ctx.getArgument("player_name", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                            int amount = ctx.getArgument("amount", Integer.class);
                                            @Nullable ItemStack protectionOrbStack = protectionOrbManager.getProtectionOrb();

                                            if(protectionOrbStack == null) {
                                                if(commandSender instanceof Player) {
                                                    commandSender.sendMessage(AdventureUtil.deserialize(locale.prefix() + "<red>Unable to give player " + player.getName() + " a protection orb due to invalid plugin settings."));
                                                } else {
                                                    commandSender.sendMessage(AdventureUtil.deserialize("<red>Unable to give player " + player.getName() + " a protection orb due to invalid plugin settings."));
                                                }

                                                return 0;
                                            }

                                            // Give the Protection Orb
                                            PlayerUtil.giveItem(player.getInventory(), protectionOrbStack, amount, player.getLocation());
                                            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + "<green>You have been given a protection orb.</green>"));

                                            if(commandSender instanceof Player) {
                                                commandSender.sendMessage(AdventureUtil.deserialize(locale.prefix() + "<green>Gave player " + player.getName() + " " + amount + " protection orb(s)."));
                                            } else {
                                                commandSender.sendMessage(AdventureUtil.deserialize("<green>Gave player " + player.getName() + " " + amount + " protection orb(s)."));
                                            }

                                            return 1;
                                        })
                                )
                        ))
                .build();
    }
}
