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
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeExemptionManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

/**
 * This class creates the opt-in command argument for the skyprestige command.
 */
public class OptInCommand {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull PrestigeExemptionManager prestigeExemptionManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param prestigeExemptionManager A {@link PrestigeExemptionManager} instance.
     */
    public OptInCommand(
            @NotNull LocaleManager localeManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull PrestigeExemptionManager prestigeExemptionManager) {
        this.localeManager = localeManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
        this.prestigeExemptionManager = prestigeExemptionManager;
    }

    /**
     * Creates the {@link LiteralCommandNode} of type {@link CommandSourceStack} for the opt-in command argument for the /skyprestige command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the opt-in command argument for the /skyprestige command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("opt-in")
                .requires(ctx -> ctx.getSender().hasPermission("skyprestige.commands.skyprestige.opt-in"))
                .executes(ctx -> {
                    System.out.println("Opt-in");

                    @NotNull Locale locale = localeManager.getConfiguration();
                    CommandSender sender = ctx.getSource().getSender();
                    if(!(sender instanceof Player player)) return 0;
                    @NotNull BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);

                    @NotNull Optional<Island> optionalIsland = bentoBoxHook.getIslandAtLocation(player.getLocation());
                    if(optionalIsland.isEmpty()) {
                        sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeStatusPlayerNotOnIsland()));
                        return 0;
                    }
                    Island island = optionalIsland.get();

                    @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
                    if(islandData == null) {
                        sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
                        return 0;
                    }

                    if(!islandData.isPrestigeExempt()) {
                        sender.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandAlreadyOptedIn()));
                        return 0;
                    }

                    prestigeExemptionManager.toggleIslandPrestigeStatus(player, island, islandData);

                    return 1;
                }).build();
    }
}
