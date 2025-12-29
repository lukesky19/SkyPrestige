package com.github.lukesky19.skyPrestige.integration.hooks;

import com.github.lukesky19.skyPrestige.integration.interfaces.Hook;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import net.luckperms.api.LuckPerms;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages interfacing with the luckperms api.
 */
public class LuckPermsHook implements Hook {
    private final @NotNull SkyPlugin plugin;
    private @Nullable LuckPerms luckPermsAPI;
    private @Nullable UserManager userManager;
    private @Nullable PlayerAdapter<Player> playerAdapter;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public LuckPermsHook(@NotNull SkyPlugin plugin) {
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
     * Get the {@link User} for the {@link Player} provided.
     * @param player The player.
     * @return The {@link User} or null.
     */
    public @Nullable User getUser(@NotNull Player player) {
        if(luckPermsAPI == null || userManager == null || playerAdapter == null) return null;

        return playerAdapter.getUser(player);
    }

    /**
     * Load the {@link User} for the {@link UUID} provided.
     * @param uuid The {@link UUID} of the player.
     * @return A {@link CompletableFuture} containing a {@link User} or null (user can be null, not the future).
     */
    public @NotNull CompletableFuture<@Nullable User> loadUser(@NotNull UUID uuid) {
        if(userManager == null) return CompletableFuture.completedFuture(null);

        return userManager.loadUser(uuid);
    }

    /**
     * Save the {@link User}.
     * @param user The {@link User} to save.
     */
    public void saveUser(@NotNull User user) {
        if(userManager == null) return;

        userManager.saveUser(user);
    }

    /**
     * Add a node to the user. Make sure to call {@link #saveUser(User)} afterward.
     * @param user The user.
     * @param node The node to add.
     * @return The {@link DataMutateResult} or null if LuckPerms was not hooked into.
     */
    public @Nullable DataMutateResult addNode(@NotNull User user, @NotNull Node node) {
        if(userManager == null) return null;

        return user.data().add(node);
    }

    /**
     * Remove a node from the user. Make sure to call {@link #saveUser(User)} afterward.
     * @param user The user.
     * @param node The node to remove.
     * @return The {@link DataMutateResult} or null if LuckPerms was not hooked into.
     */
    public @Nullable DataMutateResult removeNode(@NotNull User user, @NotNull Node node) {
        if(userManager == null) return null;

        return user.data().remove(node);
    }

    /**
     * Get a {@link List} of {@link PermissionNode}s for the user and permission provided.
     * @param user The {@link User}
     * @param permission The permission.
     * @return A {@link List} of {@link PermissionNode}s. Will be empty if the user has no permissions that matched.
     */
    public @NotNull List<PermissionNode> getPermissionNodes(@NotNull User user, @NotNull String permission) {
        List<PermissionNode> permissionNodeList = new ArrayList<>();

        for(Node node : user.getDistinctNodes()) {
            if(!(node instanceof PermissionNode permissionNode)) continue;

            if(permissionNode.getPermission().equals(permission)) {
                permissionNodeList.add(permissionNode);
            }
        }

        return permissionNodeList;
    }

    /**
     * Create a permission node for the permission and value provided.
     * @param permission The permission.
     * @param value The value of the permission. true allows the player to access the permission, false doesn't (negated).
     * @return A {@link PermissionNode}.
     */
    public @NotNull PermissionNode createPermissionNode(@NotNull String permission, boolean value) {
        return PermissionNode.builder(permission).value(value).build();
    }

    /**
     * Get a {@link List} of {@link InheritanceNode}s for the user and group name provided.
     * @param user The {@link User}
     * @param group The group name.
     * @return A {@link List} of {@link InheritanceNode}s. Will be empty if the user has no permissions that matched.
     */
    public @NotNull List<InheritanceNode> getInheritanceNodes(@NotNull User user, @NotNull String group) {
        List<InheritanceNode> inheritanceNodeList = new ArrayList<>();

        for(Node node : user.getDistinctNodes()) {
            if(!(node instanceof InheritanceNode inheritanceNode)) continue;

            if(inheritanceNode.getGroupName().equals(group)) {
                inheritanceNodeList.add(inheritanceNode);
            }
        }

        return inheritanceNodeList;
    }

    /**
     * Create an inheritance node for the group name provided.
     * @param group The group name.
     * @return An {@link InheritanceNode}.
     */
    public @NotNull InheritanceNode createInheritanceNode(@NotNull String group) {
        return InheritanceNode.builder(group).build();
    }
}