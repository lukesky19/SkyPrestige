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
import com.github.lukesky19.skyPrestige.config.PrestigeConfig;
import com.github.lukesky19.skyPrestige.data.IslandData;
import com.github.lukesky19.skyPrestige.gui.RewardsGUI;
import com.github.lukesky19.skyPrestige.manager.config.GUIConfigManager;
import com.github.lukesky19.skyPrestige.manager.config.LocaleManager;
import com.github.lukesky19.skyPrestige.manager.config.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.manager.gui.GUIManager;
import com.github.lukesky19.skyPrestige.manager.island.IslandDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * This class creates the rewards command argument for the skyprestige command.
 */
public class RewardsCommand {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull IslandDataManager islandDataManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     */
    public RewardsCommand(
            @NotNull SkyPrestige skyPrestige,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull GUIManager guiManager,
            @NotNull IslandDataManager islandDataManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.guiManager = guiManager;
        this.islandDataManager = islandDataManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the rewards command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the rewards command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("rewards")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.rewards"))
                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();
                    Island island = BentoBox.getInstance().getIslandsManager().getIsland(player.getWorld(), uuid);
                    if(island == null) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsPlayerNotOnIsland()));
                        return 0;
                    }

                    IslandData islandData = islandDataManager.getIslandData(island.getUniqueId());
                    if(islandData == null) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
                        logger.warn(AdventureUtil.deserialize("No island data found for the island " + island.getUniqueId() + "."));
                        return 0;
                    }

                    PrestigeConfig nextPrestigeLevelConfig = prestigeConfigManager.getPrestigeConfig(islandData.getPrestigeLevel() + 1);
                    if(nextPrestigeLevelConfig == null) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsMaxPrestigeLevel()));
                        return 0;
                    }

                    // Create the RewardsGUI
                    RewardsGUI gui = new RewardsGUI(skyPrestige, guiConfigManager, guiManager, player, null, nextPrestigeLevelConfig);

                    boolean creationResult = gui.create();
                    if(!creationResult) {
                        logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the rewards GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean updateResult = gui.update();
                    if(!updateResult) {
                        logger.error(AdventureUtil.deserialize("Unable to decorate the rewards GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean openResult = gui.open();
                    if(!openResult) {
                        logger.error(AdventureUtil.deserialize("Unable to open the rewards GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                }).build();
    }
}
