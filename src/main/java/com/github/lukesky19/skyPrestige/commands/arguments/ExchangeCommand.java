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
import com.github.lukesky19.skyPrestige.gui.gui.ExchangeGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.locale.Locale;
import com.github.lukesky19.skyPrestige.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.settings.Settings;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * This class creates the exchange command argument for the skyprestige command.
 */
public class ExchangeCommand {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull IslandDataManager islandDataManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     */
    public ExchangeCommand(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull GUIManager guiManager,
            @NotNull IslandDataManager islandDataManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.guiManager = guiManager;
        this.islandDataManager = islandDataManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the exchange command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the exchange command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("exchange")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.exchange"))
                .executes(ctx -> {
                    @Nullable Settings settings = settingsManager.getSettings();
                    Locale locale = localeManager.getLocale();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();

                    Island island = BentoBox.getInstance().getIslandsManager().getIsland(player.getWorld(), uuid);
                    if(island == null) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.exchangePlayerNotOnIsland()));
                        return 0;
                    }

                    String islandId = island.getUniqueId();

                    IslandData islandData = islandDataManager.getIslandData(island.getUniqueId());
                    if(islandData == null) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
                        logger.warn(AdventureUtil.deserialize("No island data found for the island " + island.getUniqueId() + "."));
                        return 0;
                    }

                    if(settings == null) {
                        logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the exchange GUI for player " + player.getName() + " due to invalid plugin settings."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    if(islandData.getPrestigeLevel() < settings.exchangePrestigeLevel()) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.exchangePrestigeLevelNotMet()));
                        return 0;
                    }

                    // Create the ExchangeGUI
                    ExchangeGUI gui = new ExchangeGUI(skyPrestige, localeManager, guiConfigManager, guiManager, player, islandId, islandData);

                    boolean creationResult = gui.create();
                    if(!creationResult) {
                        logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the exchange GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean updateResult = gui.update();
                    if(!updateResult) {
                        logger.error(AdventureUtil.deserialize("Unable to decorate the exchange GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean openResult = gui.open();
                    if(!openResult) {
                        logger.error(AdventureUtil.deserialize("Unable to open the exchange GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                }).build();
    }
}