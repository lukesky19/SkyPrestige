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
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.VaultConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.gui.VaultGUI;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * This class creates the vault command argument for the skyprestige command.
 */
public class VaultCommand {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull VaultConfigManager vaultConfigManager;

    private final @NotNull GUIManager guiManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull DatabaseManager databaseManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param vaultConfigManager A {@link VaultConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public VaultCommand(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull VaultConfigManager vaultConfigManager,
            @NotNull GUIManager guiManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.vaultConfigManager = vaultConfigManager;
        this.guiManager = guiManager;
        this.islandDataManager = islandDataManager;
        this.databaseManager = databaseManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the vault command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the vault command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("vault")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.vault"))
                .executes(ctx -> {
                    Locale locale = localeManager.getConfiguration();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();

                    Island island = BentoBox.getInstance().getIslandsManager().getIsland(player.getWorld(), uuid);
                    if(island == null) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.vaultPlayerNotOnIsland()));
                        return 0;
                    }

                    String islandId = island.getUniqueId();

                    @Nullable IslandData islandData = islandDataManager.getData(islandId);
                    if(islandData == null) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
                        logger.warn(AdventureUtil.deserialize("No island data found for the island " + islandId + "."));
                        return 0;
                    }

                    if(islandData.isPrestigeExempt()) {
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.vaultPrestigeExempt()));
                        return 0;
                    }

                    IslandIdUUIDKey identifier = new IslandIdUUIDKey(islandId, uuid);
                    // Create the VaultGUI
                    VaultGUI gui = new VaultGUI(plugin, guiConfigManager, guiManager, identifier, databaseManager, localeManager, vaultConfigManager, islandId, islandData, player);

                    boolean creationResult = gui.create();
                    if(!creationResult) {
                        logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the vault GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean updateResult = gui.update();
                    if(!updateResult) {
                        logger.error(AdventureUtil.deserialize("Unable to decorate the vault GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    boolean openResult = gui.open();
                    if(!openResult) {
                        logger.error(AdventureUtil.deserialize("Unable to open the vault GUI for player " + player.getName() + " due to a configuration error."));
                        player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                        return 0;
                    }

                    return 1;
                }).build();
    }
}
