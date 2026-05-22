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
package com.github.lukesky19.skyPrestige.commands.admin;

import com.github.lukesky19.skyPrestige.commands.util.IslandArgumentType;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.dialog.Dialog;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * This class creates the manage command argument for the skyprestige command.
 */
public class ManageIslandCommand {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull ComponentLogger logger;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull PrestigeConfigManager prestigeConfigManager;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull MultiplierManager multiplierManager;
    private final @NonNull HookManager hookManager;
    private final @NonNull DialogManager dialogManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param dialogManager A {@link DialogManager} instance.
     */
    public ManageIslandCommand(
            @NonNull SkyPlugin plugin,
            @NonNull LocaleManager localeManager,
            @NonNull PrestigeConfigManager prestigeConfigManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull HookManager hookManager,
            @NonNull DialogManager dialogManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.localeManager = localeManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.islandDataManager = islandDataManager;
        this.multiplierManager = multiplierManager;
        this.hookManager = hookManager;
        this.dialogManager = dialogManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the manage command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the manage command argument for the /skyprestige command.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("manage")
            .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.manage") && ctx.getSender() instanceof Player)
            .then(Commands.argument("island_id", new IslandArgumentType(plugin, hookManager))
                .executes(ctx -> {
                    Locale locale = localeManager.getConfiguration();
                    Player player = (Player) ctx.getSource().getSender();
                    Island island = ctx.getArgument("island_id", Island.class);
                    String islandId = island.getUniqueId();
                    UUID islandOwnerId = island.getOwner();
                    if(islandOwnerId == null) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + "Island is not owned. Unable to manage island data."));
                        return 0;
                    }

                    IslandData islandData = islandDataManager.getData(islandId);
                    if(islandData == null) {
                        logger.error(AdventureUtility.plain("No island data found for the island " + islandId + "."));

                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.islandDataNotFound()));

                        return 0;
                    }

                    IslandDialog islandDialog = new IslandDialog(plugin, islandDataManager, multiplierManager, dialogManager, prestigeConfigManager, island, islandOwnerId, islandData, player);

                    Dialog dialog = islandDialog.createDialog();

                    player.showDialog(dialog);

                    dialogManager.addOpenDialog(player.getUniqueId(), dialog);

                    return 1;
                })).build();
    }
}