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
package com.github.lukesky19.skyPrestige.commands.util;

import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This class handles island arguments.
 */
public class IslandArgumentType implements CustomArgumentType.Converted<Island, String> {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull BentoBoxHook bentoBoxHook;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public IslandArgumentType(@NonNull SkyPlugin plugin, @NonNull HookManager hookManager) {
        this.plugin = plugin;
        bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
    }

    /**
     * The error to use when no island is found for an island id.
     */
    private static final DynamicCommandExceptionType ERROR_NO_ISLAND_FOUND = new DynamicCommandExceptionType(islandId ->
            MessageComponentSerializer.message().serialize(Component.text("No island found for island id " + islandId + ".")));

    /**
     * Convert the island id to an island.
     * @param islandId The island id.
     * @return The {@link Island}.
     * @throws CommandSyntaxException If no island was found for the island id.
     */
    @Override
    public @NonNull Island convert(@NonNull String islandId) throws CommandSyntaxException {
        Optional<Island> optionalIsland = bentoBoxHook.getIslandById(islandId);
        if(optionalIsland.isPresent()) {
            return optionalIsland.get();
        } else {
            throw ERROR_NO_ISLAND_FOUND.create(islandId);
        }
    }

    /**
     * List all island ids in suggestions.
     * @param context The context.
     * @param builder The suggestions builder.
     * @return A {@link CompletableFuture} of type {@link Suggestions}.
     * @param <S> The context type.
     */
    @Override
    public <S> @NonNull CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, @NonNull SuggestionsBuilder builder) {
        bentoBoxHook.getIslandsManager().getIslands().stream()
                .filter(island -> island.getUniqueId().startsWith(builder.getRemaining()))
                .forEach(island -> {
                    String islandMembersNames = island.getMemberSet().stream().map(memberId ->
                                    plugin.getServer().getOfflinePlayer(memberId).getName())
                            .collect(Collectors.joining(","));
                    Message toolTip = MessageComponentSerializer.message().serialize(AdventureUtil.deserialize("Members: " + islandMembersNames));
                    builder.suggest(island.getUniqueId(), toolTip);
                });

        return builder.buildFuture();
    }

    /**
     * Get the native type.
     * @return The native type.
     */
    @Override
    public @NonNull ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}