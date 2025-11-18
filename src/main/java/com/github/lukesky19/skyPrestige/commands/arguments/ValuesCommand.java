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
import com.github.lukesky19.skyPrestige.config.Locale;
import com.github.lukesky19.skyPrestige.gui.ValuesGUI;
import com.github.lukesky19.skyPrestige.manager.config.GUIConfigManager;
import com.github.lukesky19.skyPrestige.manager.config.LocaleManager;
import com.github.lukesky19.skyPrestige.manager.gui.GUIManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This class creates the values command argument for the skyprestige command.
 */
public class ValuesCommand {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull GUIManager guiManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     */
    public ValuesCommand(
            @NotNull SkyPrestige skyPrestige,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull GUIManager guiManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.guiManager = guiManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the values command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the values command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("values")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.values") && ctx.getSender() instanceof Player)
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    Player player = (Player) ctx.getSource().getSender();

                    // Create the ValuesGUI
                    ValuesGUI gui = new ValuesGUI(skyPrestige, guiConfigManager, guiManager, player);

                    boolean creationResult = gui.create();
                    if(!creationResult) {
                        logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the values GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean updateResult = gui.update();
                    if(!updateResult) {
                        logger.error(AdventureUtil.deserialize("Unable to decorate the values GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean openResult = gui.open();
                    if(!openResult) {
                        logger.error(AdventureUtil.deserialize("Unable to open the values GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                }).build();
    }
}