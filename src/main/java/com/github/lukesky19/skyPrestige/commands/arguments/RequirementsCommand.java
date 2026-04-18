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
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.gui.gui.RequirementsGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.requirements.RequirementsManager;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;

/**
 * This class creates the requirements command argument for the skyprestige command.
 */
public class RequirementsCommand {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull ComponentLogger logger;

    private final @NonNull LocaleManager localeManager;
    private final @NonNull PrestigeConfigManager prestigeConfigManager;
    private final @NonNull GUIConfigManager guiConfigManager;

    private final @NonNull GUIManager guiManager;
    private final @NonNull RequirementsManager requirementsManager;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param requirementsManager A {@link RequirementsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public RequirementsCommand(
            @NonNull SkyPlugin plugin,
            @NonNull LocaleManager localeManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull PrestigeConfigManager prestigeConfigManager,
            @NonNull GUIManager guiManager,
            @NonNull RequirementsManager requirementsManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull HookManager hookManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.localeManager = localeManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.guiConfigManager = guiConfigManager;
        this.guiManager = guiManager;
        this.requirementsManager = requirementsManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the requirements command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the requirements command argument for the /skyprestige command.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("requirements")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.requirements") && ctx.getSender() instanceof Player)
                .then(Commands.argument("level", IntegerArgumentType.integer())
                        .suggests((ctx, suggestionsBuilder) -> {
                            prestigeConfigManager.getPrestigeLevels().forEach(suggestionsBuilder::suggest);

                            return suggestionsBuilder.buildFuture();
                        })
                        .executes(ctx -> {
                            Locale locale = localeManager.getConfiguration();

                            Player player = (Player) ctx.getSource().getSender();
                            UUID uuid = player.getUniqueId();
                            int level = ctx.getArgument("level", int.class);

                            PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(level);
                            if(prestigeConfig == null) {
                                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().prestigeConfigError(), List.of(Placeholder.parsed("level", String.valueOf(level)))));
                                return 0;
                            }

                            BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
                            Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
                            if(island == null) {
                                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().playerNotOnIsland()));
                                return 0;
                            }

                            if(island.getOwner() == null) {
                                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().islandNotOwned()));
                                return 0;
                            }

                            if(island.getOwner() != uuid && !island.getMemberSet().contains(uuid)) {
                                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().playerNotMemberOrOwner()));
                                return 0;
                            }

                            IslandData islandData = islandDataManager.getData(island.getUniqueId());
                            if(islandData == null) {
                                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.islandDataNotFound()));
                                logger.warn(AdventureUtility.plain("No island data found for the island " + island.getUniqueId() + "."));
                                return 0;
                            }

                            if(islandData.isPrestigeExempt()) {
                                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().prestigeExempt()));
                                return 0;
                            }

                            openRequirementsGUI(locale, player, uuid, prestigeConfig, island, islandData);

                            return 1;
                        })
                )
                .executes(ctx -> {
                    Locale locale = localeManager.getConfiguration();

                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();

                    BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
                    Island island = bentoBoxHook.getIsland(player.getWorld(), uuid);
                    if(island == null) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().playerNotOnIsland()));
                        return 0;
                    }

                    if(island.getOwner() == null) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().islandNotOwned()));
                        return 0;
                    }

                    if(island.getOwner() != uuid && !island.getMemberSet().contains(uuid)) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().playerNotMemberOrOwner()));
                        return 0;
                    }

                    IslandData islandData = islandDataManager.getData(island.getUniqueId());
                    if(islandData == null) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.islandDataNotFound()));
                        logger.warn(AdventureUtility.plain("No island data found for the island " + island.getUniqueId() + "."));
                        return 0;
                    }

                    if(islandData.isPrestigeExempt()) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().prestigeExempt()));
                        return 0;
                    }

                    int nextLevel = islandData.getPrestigeLevel() + 1;
                    if(nextLevel > prestigeConfigManager.getMaxLevel()) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().maxPrestigeLevel()));
                        return 0;
                    }

                    PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(nextLevel);
                    if(prestigeConfig == null) {
                        player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.requirementMessages().prestigeConfigError()));
                        return 0;
                    }

                    openRequirementsGUI(locale, player, uuid, prestigeConfig, island, islandData);

                    return 1;
                }).build();
    }

    private void openRequirementsGUI(
            @NonNull Locale locale,
            @NonNull Player player,
            @NonNull UUID playerId,
            @NonNull PrestigeConfig prestigeConfig,
            @NonNull Island island,
            @NonNull IslandData islandData) {
        IslandIdUUIDKey identifier = new IslandIdUUIDKey(island.getUniqueId(), playerId);
        RequirementsGUI gui = new RequirementsGUI(plugin, guiManager, identifier, player, guiConfigManager, requirementsManager, hookManager, prestigeConfig, island, islandData);

        boolean creationResult = gui.create();
        if(!creationResult) {
            logger.error(AdventureUtility.plain("Unable to create the InventoryView for the requirements GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        boolean updateResult = gui.update();
        if(!updateResult) {
            logger.error(AdventureUtility.plain("Unable to decorate the requirements GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        boolean openResult = gui.open();
        if(!openResult) {
            logger.error(AdventureUtility.plain("Unable to open the requirements for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
        }
    }
}
