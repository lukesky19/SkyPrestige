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
package com.github.lukesky19.skyPrestige.commands.user;

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.gui.gui.ExchangeGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeExemptionManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeManager;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * This class creates the exchange command argument for the skyprestige command.
 */
public class ExchangeCommand {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull ComponentLogger logger;
    private final @NonNull SettingsManager settingsManager;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIConfigManager guiConfigManager;
    private final @NonNull GUIManager guiManager;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull HookManager hookManager;
    private final @NonNull PrestigeManager prestigeManager;
    private final @NonNull PrestigeExemptionManager prestigeExemptionManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param prestigeManager A {@link PrestigeManager} instance.
     * @param prestigeExemptionManager A {@link PrestigeExemptionManager} instance.
     */
    public ExchangeCommand(
            @NonNull SkyPlugin plugin,
            @NonNull SettingsManager settingsManager,
            @NonNull LocaleManager localeManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull GUIManager guiManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager,
            @NonNull PrestigeManager prestigeManager,
            @NonNull PrestigeExemptionManager prestigeExemptionManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.guiManager = guiManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
        this.prestigeManager = prestigeManager;
        this.prestigeExemptionManager = prestigeExemptionManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the exchange command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the exchange command argument for the /skyprestige command.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("exchange")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.exchange"))
                .executes(ctx -> {
                    Settings settings = settingsManager.getConfiguration();
                    Locale locale = localeManager.getConfiguration();
                    Locale.ExchangeMessages exchangeMessages = locale.exchangeMessages();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();
                    BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
                    if(!bentoBoxHook.isHooked()) return 0;

                    Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
                    if(island == null) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + exchangeMessages.playerNotOnIsland()));
                        return 0;
                    }
                    String islandId = island.getUniqueId();

                    IslandData islandData = islandDataManager.getData(islandId);
                    if(islandData == null) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.islandDataNotFound()));
                        logger.warn(AdventureUtility.plain("No island data found for the island " + islandId + "."));
                        return 0;
                    }

                    if(settings == null) {
                        logger.error(AdventureUtility.plain("Unable to create the InventoryView for the exchange GUI for player " + player.getName() + " due to invalid plugin settings."));
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    if(islandData.isPrestigeExempt()) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + exchangeMessages.prestigeExempt()));
                        return 0;
                    }

                    // Prevent exchanging prestige points if the island is in the process of prestiging or opting in/out of prestige.
                    if(prestigeManager.isIslandPrestiging(islandId)) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + exchangeMessages.prestigeInProgress()));
                        return 0;
                    }
                    if(prestigeExemptionManager.isIslandExempting(islandId)) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + exchangeMessages.optOutInProgress()));
                        return 0;
                    }

                    if(islandData.getPrestigeLevel() < settings.exchangePrestigeLevel()) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + exchangeMessages.prestigeLevelNotMet()));
                        return 0;
                    }

                    IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), uuid);
                    // Create the ExchangeGUI
                    ExchangeGUI gui = new ExchangeGUI(plugin, localeManager, guiConfigManager, guiManager, identifier, player, islandData);

                    boolean creationResult = gui.create();
                    if(!creationResult) {
                        logger.error(AdventureUtility.plain("Unable to create the InventoryView for the exchange GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean updateResult = gui.update();
                    if(!updateResult) {
                        logger.error(AdventureUtility.plain("Unable to decorate the exchange GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean openResult = gui.open();
                    if(!openResult) {
                        logger.error(AdventureUtility.plain("Unable to open the exchange GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                }).build();
    }
}