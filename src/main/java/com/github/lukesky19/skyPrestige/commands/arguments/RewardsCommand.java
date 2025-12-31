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
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.*;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.gui.gui.OptInRewardsGUI;
import com.github.lukesky19.skyPrestige.gui.gui.OptOutRewardsGUI;
import com.github.lukesky19.skyPrestige.gui.gui.PrestigeRewardsGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * This class creates the rewards command argument for the skyprestige command.
 */
public class RewardsCommand {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull OptInConfigManager optInConfigManager;
    private final @NotNull OptOutConfigManager optOutConfigManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param optInConfigManager An {@link OptInConfigManager} instance.
     * @param optOutConfigManager An {@link OptOutConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public RewardsCommand(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull OptInConfigManager optInConfigManager,
            @NotNull OptOutConfigManager optOutConfigManager,
            @NotNull GUIManager guiManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.optInConfigManager = optInConfigManager;
        this.optOutConfigManager = optOutConfigManager;
        this.guiManager = guiManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the rewards command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the rewards command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("rewards")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.rewards"))

                .then(Commands.literal("prestige")
                        .then(Commands.argument("level", IntegerArgumentType.integer())
                                .suggests((ctx, suggestionsBuilder) -> {
                                    prestigeConfigManager.getPrestigeLevels().forEach(suggestionsBuilder::suggest);

                                    return suggestionsBuilder.buildFuture();
                                })
                                .executes(ctx ->
                                        showPrestigeRewardsGUI(
                                                (Player) ctx.getSource().getSender(),
                                                ctx.getArgument("level", int.class)))
                        .executes(ctx ->
                                showPrestigeRewardsGUI((Player) ctx.getSource().getSender()))))

                .then(Commands.literal("opt-in")
                        .executes(ctx ->
                                showOptInRewardsGUI((Player) ctx.getSource().getSender())))

                .then(Commands.literal("opt-out")
                        .executes(ctx ->
                                showOptOutRewardsGUI((Player) ctx.getSource().getSender())))

                .executes(ctx ->
                        showPrestigeRewardsGUI((Player) ctx.getSource().getSender())).build();
    }

    /**
     * Open the prestige rewards GUI using the island the player is on.
     * @param player The {@link Player} to open the GUI for.
     * @return 1 if successful, or 0 if not.
     */
    private int showPrestigeRewardsGUI(@NotNull Player player) {
        Locale locale = localeManager.getConfiguration();
        UUID uuid = player.getUniqueId();

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsPlayerNotOnIsland()));
            return 0;
        }

        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
            logger.warn(AdventureUtil.deserialize("No island data found for the island " + island.getUniqueId() + "."));
            return 0;
        }

        if(islandData.isPrestigeExempt()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsPrestigeExempt()));
            return 0;
        }

        PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(islandData.getPrestigeLevel() + 1);
        if(prestigeConfig == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsMaxPrestigeLevel()));
            return 0;
        }

        return showPrestigeRewardsGUI(player, island, prestigeConfig);
    }

    /**
     * Open the prestige rewards GUI for the given prestige level.
     * @param player The {@link Player} to open the GUI for.
     * @param prestigeLevel The prestige level.
     * @return 1 if successful, or 0 if not.
     */
    private int showPrestigeRewardsGUI(@NotNull Player player, int prestigeLevel) {
        Locale locale = localeManager.getConfiguration();
        UUID uuid = player.getUniqueId();

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsPlayerNotOnIsland()));
            return 0;
        }

        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
            logger.warn(AdventureUtil.deserialize("No island data found for the island " + island.getUniqueId() + "."));
            return 0;
        }

        if(islandData.isPrestigeExempt()) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsPrestigeExempt()));
            return 0;
        }

        PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(prestigeLevel);
        if(prestigeConfig == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsMaxPrestigeLevel()));
            return 0;
        }

        return showPrestigeRewardsGUI(player, island, prestigeConfig);
    }

    /**
     * Open the prestige rewards GUI using the provided island and prestige config.
     * @param player The {@link Player} to open the GUI for.
     * @param island The {@link Island} to open the GUI for.
     * @param prestigeConfig The prestige config.
     * @return 1 if successful, or 0 if not.
     */
    private int showPrestigeRewardsGUI(
            @NotNull Player player,
            @NotNull Island island,
            @NotNull PrestigeConfig prestigeConfig) {
        Locale locale = localeManager.getConfiguration();
        UUID uuid = player.getUniqueId();

        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), uuid);

        // Create the RewardsGUI
        PrestigeRewardsGUI gui = new PrestigeRewardsGUI(plugin, guiManager, identifier, player, guiConfigManager, prestigeConfig, null);

        boolean creationResult = gui.create();
        if(!creationResult) {
            logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the prestige rewards GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return 0;
        }

        boolean updateResult = gui.update();
        if(!updateResult) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the prestige rewards GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return 0;
        }

        boolean openResult = gui.open();
        if(!openResult) {
            logger.error(AdventureUtil.deserialize("Unable to open the prestige rewards GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return 0;
        }

        return 1;
    }

    /**
     * Open the opt-in rewards GUI using the island the player is on.
     * @param player The {@link Player} to open the GUI for.
     * @return 1 if successful, or 0 if not.
     */
    private int showOptInRewardsGUI(@NotNull Player player) {
        Locale locale = localeManager.getConfiguration();
        UUID uuid = player.getUniqueId();

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsPlayerNotOnIsland()));
            return 0;
        }

        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
            logger.warn(AdventureUtil.deserialize("No island data found for the island " + island.getUniqueId() + "."));
            return 0;
        }

        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), uuid);

        // Create the RewardsGUI
        OptInRewardsGUI gui = new OptInRewardsGUI(plugin, guiManager, identifier, player, guiConfigManager, optInConfigManager, null);

        boolean creationResult = gui.create();
        if(!creationResult) {
            logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the opt-in rewards GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return 0;
        }

        boolean updateResult = gui.update();
        if(!updateResult) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the opt-in rewards GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return 0;
        }

        boolean openResult = gui.open();
        if(!openResult) {
            logger.error(AdventureUtil.deserialize("Unable to open the opt-in rewards GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return 0;
        }

        return 1;
    }

    /**
     * Open the opt-out rewards GUI using the island the player is on.
     * @param player The {@link Player} to open the GUI for.
     * @return 1 if successful, or 0 if not.
     */
    private int showOptOutRewardsGUI(@NotNull Player player) {
        Locale locale = localeManager.getConfiguration();
        UUID uuid = player.getUniqueId();

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
        if(island == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.rewardsPlayerNotOnIsland()));
            return 0;
        }

        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
            logger.warn(AdventureUtil.deserialize("No island data found for the island " + island.getUniqueId() + "."));
            return 0;
        }

        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), uuid);

        // Create the RewardsGUI
        OptOutRewardsGUI gui = new OptOutRewardsGUI(plugin, guiManager, identifier, player, guiConfigManager, optOutConfigManager, null);

        boolean creationResult = gui.create();
        if(!creationResult) {
            logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the opt-in rewards GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return 0;
        }

        boolean updateResult = gui.update();
        if(!updateResult) {
            logger.error(AdventureUtil.deserialize("Unable to decorate the opt-in rewards GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return 0;
        }

        boolean openResult = gui.open();
        if(!openResult) {
            logger.error(AdventureUtil.deserialize("Unable to open the opt-in rewards GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            return 0;
        }

        return 1;
    }
}