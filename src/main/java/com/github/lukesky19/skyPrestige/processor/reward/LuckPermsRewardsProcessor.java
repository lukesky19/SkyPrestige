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
package com.github.lukesky19.skyPrestige.processor.reward;

import com.github.lukesky19.skyPrestige.configuration.data.reward.GroupReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.PermissionReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skyPrestige.integration.hooks.LuckPermsHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Process rewards related to LuckPerms.
 */
public class LuckPermsRewardsProcessor {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull HookManager hookManager;

    // These Maps store the nodes added or removed before the island is reset so they can be reverted later.
    private final @NotNull Map<UUID, List<Node>> removedNodes = new HashMap<>();
    private final @NotNull Map<UUID, List<Node>> addedNodes = new HashMap<>();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param logger A {@link ComponentLogger}.
     * @param hookManager A {@link HookManager} instance.
     */
    public LuckPermsRewardsProcessor(@NotNull SkyPlugin plugin, @NotNull ComponentLogger logger, @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.logger = logger;
        this.hookManager = hookManager;
    }

    /**
     * Process the early permission and group rewards.
     * @param initiatingPlayer The initiating player.
     * @param onlineIslandMembers The {@link List} of {@link Player}s for online island members.
     * @param offlineIslandMembers The {@link List} of {@link UUID}s for offline island members.
     * @param rewardConfig The {@link RewardConfig}.
     */
    public void processEarlyRewards(
            @NotNull Player initiatingPlayer,
            @NotNull List<Player> onlineIslandMembers,
            @NotNull List<UUID> offlineIslandMembers,
            @NotNull RewardConfig rewardConfig) {
        LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
        if(luckPermsHook.isHooked()) {
            List<PermissionReward> earlyPermissionRewards = rewardConfig.permissionRewards().stream()
                    .filter(PermissionReward::beforeIslandReset)
                    .toList();
            List<PermissionReward> allMemberPermissionRewards = earlyPermissionRewards.stream()
                    .filter(PermissionReward::giveToAllIslandMembers)
                    .toList();
            List<PermissionReward> initiatorPermissionRewards = earlyPermissionRewards.stream()
                    .filter(permissionReward -> !permissionReward.giveToAllIslandMembers())
                    .toList();

            List<GroupReward> earlyGroupRewards = rewardConfig.groupRewards().stream()
                    .filter(GroupReward::beforeIslandReset)
                    .toList();
            List<GroupReward> allMemberGroupRewards = earlyGroupRewards.stream()
                    .filter(GroupReward::giveToAllIslandMembers)
                    .toList();
            List<GroupReward> initiatorGroupRewards = earlyGroupRewards.stream()
                    .filter(groupReward -> !groupReward.giveToAllIslandMembers())
                    .toList();

            // Process rewards given to the initiating player only first
            @Nullable User initiatingUser = luckPermsHook.getUser(initiatingPlayer);
            if(initiatingUser != null) {
                processRewards(luckPermsHook, initiatingUser, initiatorPermissionRewards, initiatorGroupRewards, true);
            }

            // Process rewards given to all island members
            // Online members
            onlineIslandMembers.forEach(player -> {
                @Nullable User user = luckPermsHook.getUser(player);
                if(user != null) {
                    processRewards(luckPermsHook, user, allMemberPermissionRewards, allMemberGroupRewards, true);
                }
            });

            // Offline members
            offlineIslandMembers.forEach(playerId ->
                    luckPermsHook.loadUser(playerId).thenAcceptAsync(user -> {
                        if(user != null) {
                            processRewards(luckPermsHook, user, allMemberPermissionRewards, allMemberGroupRewards,true);
                        }    
                    }));
        }
    }

    /**
     * Process the post permission and group rewards.
     * @param initiatingPlayer The initiating player.
     * @param onlineIslandMembers The {@link List} of {@link Player}s for online island members.
     * @param offlineIslandMembers The {@link List} of {@link UUID}s for offline island members.
     * @param rewardConfig The {@link RewardConfig}.
     */
    public void processPostRewards(
            @NotNull Player initiatingPlayer,
            @NotNull List<Player> onlineIslandMembers,
            @NotNull List<UUID> offlineIslandMembers,
            @NotNull RewardConfig rewardConfig) {
        LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
        if(luckPermsHook.isHooked()) {
            List<PermissionReward> postPermissionRewards = rewardConfig.permissionRewards().stream()
                    .filter(permissionReward -> !permissionReward.beforeIslandReset())
                    .toList();
            List<PermissionReward> allMemberPermissionRewards = postPermissionRewards.stream()
                    .filter(PermissionReward::giveToAllIslandMembers)
                    .toList();
            List<PermissionReward> initiatorPermissionRewards = postPermissionRewards.stream()
                    .filter(permissionReward -> !permissionReward.giveToAllIslandMembers())
                    .toList();

            List<GroupReward> postGroupRewards = rewardConfig.groupRewards().stream()
                    .filter(groupReward -> !groupReward.beforeIslandReset())
                    .toList();
            List<GroupReward> allMemberGroupRewards = postGroupRewards.stream()
                    .filter(GroupReward::giveToAllIslandMembers)
                    .toList();
            List<GroupReward> initiatorGroupRewards = postGroupRewards.stream()
                    .filter(groupReward -> !groupReward.giveToAllIslandMembers())
                    .toList();

            // Process rewards given to the initiating player only first
            User initiatingUser = luckPermsHook.getUser(initiatingPlayer);
            if(initiatingUser != null) {
                processRewards(luckPermsHook, initiatingUser, initiatorPermissionRewards, initiatorGroupRewards, false);
            }

            // Process rewards given to all island members
            // Online members
            onlineIslandMembers.forEach(player -> {
                @Nullable User user = luckPermsHook.getUser(player);
                if(user != null) {
                    processRewards(luckPermsHook, user, allMemberPermissionRewards, allMemberGroupRewards, false);
                }
            });

            // Offline members
            offlineIslandMembers.forEach(playerId ->
                    luckPermsHook.loadUser(playerId).thenAcceptAsync(user -> {
                        if(user != null) {
                            processRewards(luckPermsHook, user, allMemberPermissionRewards, allMemberGroupRewards,false);
                        }
                    }));
        }
    }

    /**
     * Remove the early rewards given to the {@link UUID}s provided that were stored, if any.
     * @param uniqueIds The {@link ImmutableSet} of {@link UUID}s to remove early rewards from.
     */
    public void revertEarlyRewards(@NotNull ImmutableSet<UUID> uniqueIds) {
        LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
        if(!luckPermsHook.isHooked()) return;

        // Get online island member's players
        List<Player> onlineIslandMembers = uniqueIds.stream()
                .map(plugin.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .filter(memberPlayer -> memberPlayer.isOnline() && memberPlayer.isConnected())
                .toList();
        // Get offline island member's unique ids
        List<UUID> offlineIslandMembers = uniqueIds
                .stream()
                .map(memberId -> plugin.getServer().getOfflinePlayer(memberId))
                .filter(offlinePlayer -> !offlinePlayer.isOnline() && !offlinePlayer.isConnected())
                .map(OfflinePlayer::getUniqueId)
                .toList();

        // Process reversion of rewards given
        // Online members
        onlineIslandMembers.forEach(player -> {
            User user = luckPermsHook.getUser(player);
            if(user != null) {
                revertEarlyRewards(luckPermsHook, user);
            }
        });

        // Offline members
        offlineIslandMembers.forEach(playerId ->
                luckPermsHook.loadUser(playerId).thenAcceptAsync(user -> {
                    if(user != null) {
                        revertEarlyRewards(luckPermsHook, user);
                    }
                }));
    }

    /**
     * Give the permission and group rewards to the user.
     * @param luckPermsHook A {@link LuckPermsHook}.
     * @param user The {@link User}.
     * @param permissionRewards The {@link List} of {@link PermissionReward}s.
     * @param groupRewards The {@link List} of {@link GroupReward}s.
     * @param undo Should the changes be stored to undo later?
     */
    private void processRewards(
            @NotNull LuckPermsHook luckPermsHook,
            @NotNull User user,
            @NotNull List<PermissionReward> permissionRewards,
            @NotNull List<GroupReward> groupRewards,
            boolean undo) {
        @Nullable List<Node> removedNodes = null;
        @Nullable List<Node> addedNodes = null;
        UUID userId = user.getUniqueId();
        if(undo) {
            removedNodes = this.removedNodes.computeIfAbsent(userId, uuid -> new ArrayList<>());
            addedNodes = this.addedNodes.computeIfAbsent(userId, uuid -> new ArrayList<>());
        }

        for(PermissionReward permissionReward : permissionRewards) {
            if(permissionReward.permission() == null) continue;

            if(permissionReward.removePermission()) {
                List<PermissionNode> permissionNodeList = luckPermsHook.getPermissionNodes(user, permissionReward.permission());
                if(permissionNodeList.isEmpty()) continue;

                for(PermissionNode permissionNode : permissionNodeList) {
                    DataMutateResult result = luckPermsHook.removeNode(user, permissionNode);

                    switch(result) {
                        case SUCCESS -> {
                            if(removedNodes != null) {
                                removedNodes.add(permissionNode);
                            }

                            logger.info(AdventureUtil.deserialize("Removed permission " + permissionReward.permission() + " from user " + user.getUsername() + "."));
                        }

                        case FAIL, FAIL_ALREADY_HAS -> logger.info(AdventureUtil.deserialize("Failed to remove permission " + permissionReward.permission() + " from user " + user.getUsername() + "."));

                        case FAIL_LACKS -> logger.info(AdventureUtil.deserialize("User " + user.getUsername() + " does not have permission " + permissionReward.permission() + " to remove."));

                        case null -> logger.info(AdventureUtil.deserialize("LuckPerms not hooked into. Unable to remove permission."));
                    }
                }
            } else {
                PermissionNode permissionNode = luckPermsHook.createPermissionNode(permissionReward.permission(), !permissionReward.negatePermission());

                DataMutateResult result = luckPermsHook.addNode(user, permissionNode);

                switch(result) {
                    case SUCCESS -> {
                        if(addedNodes != null) {
                            addedNodes.add(permissionNode);
                        }

                        logger.info(AdventureUtil.deserialize("Added permission " + permissionReward.permission() + " to user " + user.getUsername() + "."));
                    }

                    case FAIL, FAIL_LACKS -> logger.info(AdventureUtil.deserialize("Failed to add permission " + permissionReward.permission() + " to user " + user.getUsername() + "."));

                    case FAIL_ALREADY_HAS -> logger.info(AdventureUtil.deserialize("Unable to add permission " + permissionReward.permission() + " because user " + user.getUsername() + " already the permission."));

                    case null -> logger.info(AdventureUtil.deserialize("LuckPerms not hooked into. Unable to add permission."));
                }
            }
        }

        for(GroupReward groupReward : groupRewards) {
            if(groupReward.groupName() == null) continue;

            if(groupReward.removeGroup()) {
                List<InheritanceNode> inheritanceNodeList = luckPermsHook.getInheritanceNodes(user, groupReward.groupName());
                if(inheritanceNodeList.isEmpty()) continue;

                for(InheritanceNode inheritanceNode : inheritanceNodeList) {
                    DataMutateResult result = luckPermsHook.removeNode(user, inheritanceNode);

                    switch(result) {
                        case SUCCESS -> {
                            if(removedNodes != null) {
                                removedNodes.add(inheritanceNode);
                            }

                            logger.info(AdventureUtil.deserialize("Removed group " + inheritanceNode.getGroupName() + " from user " + user.getUsername() + "."));
                        }

                        case FAIL, FAIL_ALREADY_HAS -> logger.info(AdventureUtil.deserialize("Failed to remove group " + inheritanceNode.getGroupName() + " from user " + user.getUsername() + "."));

                        case FAIL_LACKS -> logger.info(AdventureUtil.deserialize("User " + user.getUsername() + " does not have the group " + inheritanceNode.getGroupName() + " to remove."));

                        case null -> logger.info(AdventureUtil.deserialize("LuckPerms not hooked into. Unable to remove group."));
                    }
                }
            } else {
                InheritanceNode inheritanceNode = luckPermsHook.createInheritanceNode(groupReward.groupName());

                DataMutateResult result = luckPermsHook.addNode(user, inheritanceNode);

                switch(result) {
                    case SUCCESS -> {
                        if(addedNodes != null) {
                            addedNodes.add(inheritanceNode);
                        }

                        logger.info(AdventureUtil.deserialize("Added group " + inheritanceNode.getGroupName() + " to user " + user.getUsername() + "."));
                    }

                    case FAIL, FAIL_LACKS -> logger.info(AdventureUtil.deserialize("Failed to add group " + inheritanceNode.getGroupName() + " to user " + user.getUsername() + "."));

                    case FAIL_ALREADY_HAS -> logger.info(AdventureUtil.deserialize("Unable to add group " + inheritanceNode.getGroupName() + " because user " + user.getUsername() + " already the group."));

                    case null -> logger.info(AdventureUtil.deserialize("LuckPerms not hooked into. Unable to add group."));
                }
            }
        }

        // Save the user
        luckPermsHook.saveUser(user);
    }

    /**
     * Revert the early rewards given to the user.
     * @param luckPermsHook A {@link LuckPermsHook}.
     * @param user The {@link User}.
     */
    private void revertEarlyRewards(
            @NotNull LuckPermsHook luckPermsHook,
            @NotNull User user) {
        UUID userId = user.getUniqueId();
        @Nullable List<Node> removedNodes = this.removedNodes.get(userId);
        @Nullable List<Node> addedNodes = this.addedNodes.get(userId);

        logger.info(AdventureUtil.deserialize("Reverting early permission/group rewards."));

        if(removedNodes != null && !removedNodes.isEmpty()) {
            for(Node removedNode : removedNodes) {
                DataMutateResult result = luckPermsHook.addNode(user, removedNode);

                switch (result) {
                    case SUCCESS ->
                            logger.info(AdventureUtil.deserialize("Re-added node " + removedNode.getKey() + " to user " + user.getUsername() + "."));

                    case FAIL, FAIL_LACKS ->
                            logger.info(AdventureUtil.deserialize("Failed to re-add node " + removedNode.getKey() + " to user " + user.getUsername() + "."));

                    case FAIL_ALREADY_HAS ->
                            logger.info(AdventureUtil.deserialize("Unable to re-add node " + removedNode.getKey() + " because user " + user.getUsername() + " already the permission."));

                    case null ->
                            logger.info(AdventureUtil.deserialize("LuckPerms not hooked into. Unable to re-add nodes."));
                }
            }
        }

        if(addedNodes != null && !addedNodes.isEmpty()) {
            for(Node addedNode : addedNodes) {
                DataMutateResult result = luckPermsHook.removeNode(user, addedNode);

                switch (result) {
                    case SUCCESS ->
                            logger.info(AdventureUtil.deserialize("Removed node " + addedNode.getKey() + " from user " + user.getUsername() + "."));

                    case FAIL, FAIL_ALREADY_HAS ->
                            logger.info(AdventureUtil.deserialize("Failed to remove node " + addedNode.getKey() + " from user " + user.getUsername() + "."));

                    case FAIL_LACKS ->
                            logger.info(AdventureUtil.deserialize("User " + user.getUsername() + " does not have node " + addedNode.getKey() + " to remove."));

                    case null ->
                            logger.info(AdventureUtil.deserialize("LuckPerms not hooked into. Unable to remove node."));
                }
            }
        }

        // Clear stored permission nodes for the user id
        this.removedNodes.remove(userId);
        this.addedNodes.remove(userId);

        // Save the user
        luckPermsHook.saveUser(user);
    }
}
