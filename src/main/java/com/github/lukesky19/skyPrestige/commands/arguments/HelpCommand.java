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
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * This class creates the help command argument for the skyprestige command.
 */
public class HelpCommand {
    private final @NonNull ComponentLogger logger;
    private final @NonNull LocaleManager localeManager;

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}.
     * @param localeManager A {@link LocaleManager} instance.
     */
    public HelpCommand(@NonNull ComponentLogger logger, @NonNull LocaleManager localeManager) {
        this.logger = logger;
        this.localeManager = localeManager;
    }
    
    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the help command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the help command argument for the /skyprestige command.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("help")
            .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.help"))
            .executes(ctx -> {
                Locale locale = localeManager.getConfiguration();

                if(ctx.getSource().getSender() instanceof Player player) {
                    for(String msg : locale.help()) {
                        player.sendMessage(AdventureUtil.deserialize(player, msg));
                    }
                } else {
                    for(String msg : locale.help()) {
                        logger.info(AdventureUtil.deserialize(msg));
                    }
                }

                return 1;
            }).build();
    }
}
