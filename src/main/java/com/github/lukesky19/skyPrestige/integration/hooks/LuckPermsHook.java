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
package com.github.lukesky19.skyPrestige.integration.hooks;

import com.github.lukesky19.skyPrestige.configuration.data.common.Context;
import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages interfacing with the luckperms api.
 */
public class LuckPermsHook implements Hook {
    private final @NonNull SkyPlugin plugin;
    private @Nullable LuckPerms luckPermsAPI;
    private @Nullable UserManager userManager;
    private @Nullable PlayerAdapter<Player> playerAdapter;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public LuckPermsHook(@NonNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link RoseStackerAPI} from RoseStacker.
     */
    @Override
    public void initialize() {
        if(plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<LuckPerms> rsp = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            if(rsp != null) {
                this.luckPermsAPI = rsp.getProvider();
                this.userManager = luckPermsAPI.getUserManager();
                this.playerAdapter = luckPermsAPI.getPlayerAdapter(Player.class);
            }
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return luckPermsAPI != null && userManager != null;
    }

    /**
     * Get or load the {@link User} for the {@link UUID} provided.
     * @param playerId The {@link UUID} of the player.
     * @return A {@link CompletableFuture} with a {@link User}, which may be null.
     */
    public @NonNull CompletableFuture<@Nullable User> getOrLoadUser(@NonNull UUID playerId) {
        if(luckPermsAPI == null || userManager == null || playerAdapter == null) return CompletableFuture.completedFuture(null);

        Player player = plugin.getServer().getPlayer(playerId);
        return getOrLoadUser(player, playerId);
    }

    /**
     * Get or load the {@link User} for the {@link Player} and {@link UUID} provided.
     * @param player The {@link Player}.  The player may be null, not online, or not connected.
     * @param playerId The {@link UUID} of the player.
     * @return A {@link CompletableFuture} with a {@link User}, which may be null.
     */
    public @NonNull CompletableFuture<@Nullable User> getOrLoadUser(@Nullable Player player, @NonNull UUID playerId) {
        if(luckPermsAPI == null || userManager == null || playerAdapter == null) return CompletableFuture.completedFuture(null);

        CompletableFuture<@Nullable User> future = new CompletableFuture<>();

        if(player != null && player.isOnline() && player.isConnected()) {
            future.complete(playerAdapter.getUser(player));
        } else {
            userManager.loadUser(playerId).thenAccept(future::complete);
        }

        return future;
    }

    /**
     * Save the {@link User}.
     * @param user The {@link User} to save.
     */
    public void saveUser(@NonNull User user) {
        if(userManager == null) return;

        userManager.saveUser(user);
    }

    /**
     * Add a node to the user. Make sure to call {@link #saveUser(User)} afterward.
     * @param user The user.
     * @param node The node to add.
     * @return The {@link DataMutateResult} or null if LuckPerms was not hooked into.
     */
    public @Nullable DataMutateResult addNode(@NonNull User user, @NonNull Node node) {
        if(userManager == null) return null;

        return user.data().add(node);
    }

    /**
     * Remove a node from the user. Make sure to call {@link #saveUser(User)} afterward.
     * @param user The user.
     * @param node The node to remove.
     * @return The {@link DataMutateResult} or null if LuckPerms was not hooked into.
     */
    public @Nullable DataMutateResult removeNode(@NonNull User user, @NonNull Node node) {
        if(userManager == null) return null;

        return user.data().remove(node);
    }

    /**
     * Get a {@link List} of {@link PermissionNode}s for the user, permission, and contexts provided.
     * @param user The {@link User}
     * @param permission The permission.
     * @param contexts The permission contexts.
     * @return A {@link List} of {@link PermissionNode}s. Will be empty if the user has no permissions that matched.
     */
    public @NonNull List<PermissionNode> getPermissionNodes(@NonNull User user, @NonNull String permission, @NonNull List<Context> contexts) {
        List<PermissionNode> permissionNodeList = new ArrayList<>();

        for(Node node : user.getDistinctNodes()) {
            if(!(node instanceof PermissionNode permissionNode)) continue;
            if(!permissionNode.getPermission().equals(permission)) continue;

            if(contexts.isEmpty()) {
                permissionNodeList.add(permissionNode);
            } else {
                ImmutableContextSet contextSet = permissionNode.getContexts();
                boolean contextsMatch = contexts.stream()
                        .filter(e -> e.type() != null && e.value() != null)
                        .allMatch(e -> contextSet.contains(e.type(), e.value()));
                if(contextsMatch) {
                    permissionNodeList.add(permissionNode);
                }
            }
        }

        return permissionNodeList;
    }

    /**
     * Create a permission node for the permission and value provided.
     * @param permission The permission.
     * @param value The value of the permission. true allows the player to access the permission, false doesn't (negated).
     * @param contexts The permission contexts.
     * @return A {@link PermissionNode}.
     */
    public @NonNull PermissionNode createPermissionNode(
            @NonNull String permission,
            boolean value,
            @NonNull List<Context> contexts) {
        PermissionNode.Builder nodeBuilder = PermissionNode.builder(permission);
        nodeBuilder.value(value);

        if(!contexts.isEmpty()) {
            ImmutableContextSet.Builder contextBuilder = ImmutableContextSet.builder();
            contexts.stream()
                    .filter(e -> e.type() != null && e.value() != null)
                    .forEach(e -> contextBuilder.add(e.type(), e.value()));
            ContextSet contextSet = contextBuilder.build();

            nodeBuilder.context(contextSet);
        }

        return nodeBuilder.build();
    }

    /**
     * Get a {@link List} of {@link InheritanceNode}s for the user and group name provided.
     * @param user The {@link User}
     * @param group The group name.
     * @param contexts The permission contexts.
     * @return A {@link List} of {@link InheritanceNode}s. Will be empty if the user has no permissions that matched.
     */
    public @NonNull List<InheritanceNode> getInheritanceNodes(
            @NonNull User user,
            @NonNull String group,
            @NonNull List<Context> contexts) {
        List<InheritanceNode> inheritanceNodeList = new ArrayList<>();

        for(Node node : user.getDistinctNodes()) {
            if(!(node instanceof InheritanceNode inheritanceNode)) continue;
            if(!inheritanceNode.getGroupName().equals(group)) continue;

            if(contexts.isEmpty()) {
                inheritanceNodeList.add(inheritanceNode);
            } else {
                ImmutableContextSet contextSet = inheritanceNode.getContexts();
                boolean contextsMatch = contexts.stream()
                        .filter(e -> e.type() != null && e.value() != null)
                        .allMatch(e -> contextSet.contains(e.type(), e.value()));
                if(contextsMatch) {
                    inheritanceNodeList.add(inheritanceNode);
                }
            }
        }

        return inheritanceNodeList;
    }

    /**
     * Create an inheritance node for the group name provided.
     * @param group The group name.
     * @param contexts The permission contexts.
     * @return An {@link InheritanceNode}.
     */
    public @NonNull InheritanceNode createInheritanceNode(@NonNull String group, @NonNull List<Context> contexts) {
        InheritanceNode.Builder nodeBuilder = InheritanceNode.builder(group);

        if(!contexts.isEmpty()) {
            ImmutableContextSet.Builder contextBuilder = ImmutableContextSet.builder();
            contexts.stream()
                    .filter(e -> e.type() != null && e.value() != null)
                    .forEach(e -> contextBuilder.add(e.type(), e.value()));
            ContextSet contextSet = contextBuilder.build();

            nodeBuilder.context(contextSet);
        }

        return nodeBuilder.build();
    }
}