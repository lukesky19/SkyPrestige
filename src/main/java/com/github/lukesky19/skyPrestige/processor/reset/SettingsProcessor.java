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
package com.github.lukesky19.skyPrestige.processor.reset;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.configuration.data.opt_in_out.OptInOutConfig;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.common.GroupConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.common.PermissionConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.island.IslandSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.player.PlayerSettings;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.interfaces.IResetSettings;
import com.github.lukesky19.skyPrestige.configuration.manager.OptInConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.OptOutConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.QueuedSettingsTable;
import com.github.lukesky19.skyPrestige.integration.hooks.LuckPermsHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.player.PlayerSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.queued.QueuedSettingsProcessor;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skyPrestige.util.enums.SettingsType;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.placeholderapi.PlaceholderAPIUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class handles processing settings on island reset (non-prestige, non-opt-in/opt-out), island team join, island team leave, and island team kick.
 * Offline player handling on login is processed in {@link QueuedSettingsProcessor}.
 */
public class SettingsProcessor {
    private final @NonNull Server server;
    private final @NonNull ComponentLogger logger;
    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull HookManager hookManager;

    private final @NonNull SettingsManager settingsManager;
    private final @NonNull PrestigeConfigManager prestigeConfigManager;
    private final @NonNull OptInConfigManager optInConfigManager;
    private final @NonNull OptOutConfigManager optOutConfigManager;

    private final @NonNull IslandSettingsProcessor islandSettingsProcessor;
    private final @NonNull PlayerSettingsProcessor playerSettingsProcessor;
    private final @NonNull RewardsProcessor rewardsProcessor;

    /**
     * Constructor
     * @param plugin A {@link SkyPrestige} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param optInConfigManager A {@link OptInConfigManager} instance.
     * @param optOutConfigManager A {@link OptOutConfigManager} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param playerSettingsProcessor A {@link PlayerSettingsProcessor} instance.
     * @param rewardsProcessor A {@link RewardsProcessor} instance.
     */
    public SettingsProcessor(
            @NonNull SkyPrestige plugin,
            @NonNull DatabaseManager databaseManager,
            @NonNull HookManager hookManager,
            @NonNull SettingsManager settingsManager,
            @NonNull PrestigeConfigManager prestigeConfigManager,
            @NonNull OptInConfigManager optInConfigManager, 
            @NonNull OptOutConfigManager optOutConfigManager,
            @NonNull IslandSettingsProcessor islandSettingsProcessor,
            @NonNull PlayerSettingsProcessor playerSettingsProcessor,
            @NonNull RewardsProcessor rewardsProcessor) {
        this.server = plugin.getServer();
        this.logger = plugin.getComponentLogger();
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;
        this.settingsManager = settingsManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.optInConfigManager = optInConfigManager;
        this.optOutConfigManager = optOutConfigManager;
        this.islandSettingsProcessor = islandSettingsProcessor;
        this.playerSettingsProcessor = playerSettingsProcessor;
        this.rewardsProcessor = rewardsProcessor;
    }

    /**
     * Apply settings related to when an island is reset.
     * @param initiator The initiating {@link Player}.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}.
     */
    public void onIslandReset(
            @NonNull Player initiator,
            @NonNull Island oldIsland,
            @NonNull Island newIsland) {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.warn(AdventureUtility.plain("Unable to apply settings on island reset due to invalid settings.yml."));
            return;
        }

        IResetSettings resetSettings = settings.islandResetSettings();
        IslandSettings islandSettings = resetSettings.getIslandSettings();
        PlayerSettings playerSettings = resetSettings.getPlayerSettings();

        // Get online island members
        List<Player> onlineIslandMembers = oldIsland.getMemberSet().stream()
                .map(server::getPlayer)
                .filter(member -> member != null && member.isOnline() && member.isConnected())
                .toList();

        // Get offline island members
        List<UUID> offlineIslandMembers = oldIsland.getMemberSet().stream()
                .map(server::getOfflinePlayer)
                .filter(member -> !member.isOnline() || !member.isConnected())
                .map(OfflinePlayer::getUniqueId)
                .toList();

        // Queue offline island members for further processing when they login.
        QueuedSettingsTable queuedSettingsTable = databaseManager.getQueuedSettingsTable();
        offlineIslandMembers.forEach(memberId -> queuedSettingsTable.queueSettings(
                memberId, newIsland.getUniqueId(), SettingsType.ISLAND_RESET, -1));

        // Process island settings
        islandSettingsProcessor.processIslandSettings(
                initiator,
                islandSettings,
                oldIsland,
                newIsland,
                new IslandData(newIsland.getUniqueId()));

        // Process player settings
        playerSettingsProcessor.processPlayerSettings(
                playerSettings,
                initiator,
                onlineIslandMembers,
                offlineIslandMembers,
                resetSettings.getStartingMoney(),
                resetSettings.giveStartingMoneyToAllIslandMembers());

        // Execute commands for online players
        ConsoleCommandSender sender = server.getConsoleSender();
        onlineIslandMembers.forEach(member ->
                resetSettings.getCommands().forEach(command ->
                        server.dispatchCommand(sender, PlaceholderAPIUtil.parsePlaceholders(member, command))));

        // Process permissions and groups
        LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
        if(luckPermsHook.isHooked()) {
            onlineIslandMembers.forEach(player -> {
                CompletableFuture<@Nullable User> future = luckPermsHook.getOrLoadUser(player, player.getUniqueId());
                future.thenAccept(user -> {
                    if(user != null) {
                        modifyUser(luckPermsHook, user, resetSettings.getPermissions(), resetSettings.getGroups());
                    }
                });
            });

            offlineIslandMembers.forEach(playerId ->
                    luckPermsHook.getOrLoadUser(playerId).thenAcceptAsync(user -> {
                        if(user != null) {
                            modifyUser(luckPermsHook, user, resetSettings.getPermissions(), resetSettings.getGroups());
                        }
                    }));
        }
    }

    /**
     * Apply settings related to when a player joins an island.
     * @param island The {@link Island} the player joined.
     * @param islandData The {@link IslandData} for the island.
     * @param playerId The player's {@link UUID}.
     * @param player The {@link Player}. May be null, not online or not connected.
     */
    public void onTeamJoin(
            @NonNull Island island,
            @NonNull IslandData islandData,
            @NonNull UUID playerId,
            @Nullable Player player) {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.warn(AdventureUtility.plain("Unable to apply settings on island join due to invalid settings.yml."));
            return;
        }

        IResetSettings resetSettings = settings.teamJoinSettings();
        PlayerSettings playerSettings = resetSettings.getPlayerSettings();

        if(player != null && player.isOnline() && player.isConnected()) {
            // Process player settings
            playerSettingsProcessor.processPlayerSettings(
                    playerSettings,
                    player,
                    false,
                    resetSettings.getStartingMoney(),
                    resetSettings.giveStartingMoneyToAllIslandMembers());

            // Execute commands for player
            ConsoleCommandSender sender = server.getConsoleSender();
            resetSettings.getCommands().forEach(command ->
                    server.dispatchCommand(sender, PlaceholderAPIUtil.parsePlaceholders(player, command)));

            // Process permissions and groups
            LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
            CompletableFuture<@Nullable User> future = luckPermsHook.getOrLoadUser(player, playerId);
            future.thenAccept(user -> {
                if(user != null) {
                    modifyUser(luckPermsHook, user, resetSettings.getPermissions(), resetSettings.getGroups());
                }
            });
            
            if(islandData.isPrestigeExempt()) {
                if(settings.applyOptOutRewardsForInitialIslands()) {
                    OptInOutConfig optOutConfig = optOutConfigManager.getConfiguration();
                    if (optOutConfig != null) {
                        rewardsProcessor.processRetroactive(player, playerId, optOutConfig.rewardConfig());
                    }
                }
            } else {
                if(settings.applyOptInRewardsForInitialIslands()) {
                    OptInOutConfig optInConfig = optInConfigManager.getConfiguration();
                    if (optInConfig != null) {
                        rewardsProcessor.processRetroactive(player, playerId, optInConfig.rewardConfig());
                    }
                }

                if(islandData.getPrestigeLevel() > 0) {
                    for (int i = 1; i <= islandData.getPrestigeLevel(); i++) {
                        PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(i);
                        if (prestigeConfig == null) continue;

                        rewardsProcessor.processRetroactive(player, playerId, prestigeConfig.rewardConfig());
                    }
                }
            }
        } else {
            // Queue the remaining settings for later execution when the player logs in.
            QueuedSettingsTable queuedSettingsTable = databaseManager.getQueuedSettingsTable();
            queuedSettingsTable.queueSettings(playerId, island.getUniqueId(), SettingsType.TEAM_JOIN, islandData.getPrestigeLevel());

            // Process permissions and groups
            LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
            CompletableFuture<@Nullable User> future = luckPermsHook.getOrLoadUser(player, playerId);
            future.thenAccept(user -> {
                if(user != null) {
                    modifyUser(luckPermsHook, user, resetSettings.getPermissions(), resetSettings.getGroups());
                }
            });

            if(islandData.isPrestigeExempt()) {
                OptInOutConfig optOutConfig = optOutConfigManager.getConfiguration();
                if(optOutConfig != null) {
                    rewardsProcessor.processRetroactive(player, playerId, optOutConfig.rewardConfig());
                }
            } else {
                OptInOutConfig optInConfig = optInConfigManager.getConfiguration();
                if(optInConfig != null) {
                    rewardsProcessor.processRetroactive(player, playerId, optInConfig.rewardConfig());
                }

                for(int i = 1; i <= islandData.getPrestigeLevel(); i++) {
                    PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(i);
                    if(prestigeConfig == null) continue;

                    rewardsProcessor.processRetroactive(player, playerId, prestigeConfig.rewardConfig());
                }
            }
        }
    }

    /**
     * Apply settings related to when a player leaves an island.
     * @param island The {@link Island} the player left.
     * @param playerId The player's {@link UUID}.
     * @param player The {@link Player}. May be null, not online or not connected.
     */
    public void onTeamLeave(@NonNull Island island, @NonNull UUID playerId, @Nullable Player player) {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.warn(AdventureUtility.plain("Unable to apply settings on island leave due to invalid settings.yml."));
            return;
        }

        IResetSettings resetSettings = settings.teamLeaveSettings();
        PlayerSettings playerSettings = resetSettings.getPlayerSettings();

        if(player != null && player.isOnline() && player.isConnected()) {
            // Process player settings
            playerSettingsProcessor.processPlayerSettings(
                    playerSettings,
                    player,
                    false,
                    resetSettings.getStartingMoney(),
                    resetSettings.giveStartingMoneyToAllIslandMembers());

            // Execute commands for player
            ConsoleCommandSender sender = server.getConsoleSender();
            resetSettings.getCommands().forEach(command ->
                    server.dispatchCommand(sender, PlaceholderAPIUtil.parsePlaceholders(player, command)));

            // Process permissions and groups
            LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
            CompletableFuture<@Nullable User> future = luckPermsHook.getOrLoadUser(player, playerId);
            future.thenAccept(user -> {
                if(user != null) {
                    modifyUser(luckPermsHook, user, resetSettings.getPermissions(), resetSettings.getGroups());
                }
            });
        } else {
            // Queue the remaining settings for later execution when the player logs in.
            QueuedSettingsTable queuedSettingsTable = databaseManager.getQueuedSettingsTable();
            queuedSettingsTable.queueSettings(playerId, island.getUniqueId(), SettingsType.TEAM_LEAVE, -1);

            // Process permissions and groups
            LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
            CompletableFuture<@Nullable User> future = luckPermsHook.getOrLoadUser(player, playerId);
            future.thenAccept(user -> {
                if(user != null) {
                    modifyUser(luckPermsHook, user, resetSettings.getPermissions(), resetSettings.getGroups());
                }
            });
        }
    }

    /**
     * Apply settings related to when a player is kicked from an island.
     * @param island The {@link Island} the player left.
     * @param playerId The player's {@link UUID}.
     * @param player The {@link Player}. May be null, not online or not connected.
     */
    public void onTeamKick(@NonNull Island island, @NonNull UUID playerId, @Nullable Player player) {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.warn(AdventureUtility.plain("Unable to apply settings on island kick due to invalid settings.yml."));
            return;
        }

        IResetSettings resetSettings = settings.teamKickSettings();
        PlayerSettings playerSettings = resetSettings.getPlayerSettings();

        if(player != null && player.isOnline() && player.isConnected()) {
            // Process player settings
            playerSettingsProcessor.processPlayerSettings(
                    playerSettings,
                    player,
                    false,
                    resetSettings.getStartingMoney(),
                    resetSettings.giveStartingMoneyToAllIslandMembers());

            // Execute commands for player
            ConsoleCommandSender sender = server.getConsoleSender();
            resetSettings.getCommands().forEach(command ->
                    server.dispatchCommand(sender, PlaceholderAPIUtil.parsePlaceholders(player, command)));

            // Process permissions and groups
            LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
            CompletableFuture<@Nullable User> future = luckPermsHook.getOrLoadUser(player, playerId);
            future.thenAccept(user -> {
                if(user != null) {
                    modifyUser(luckPermsHook, user, resetSettings.getPermissions(), resetSettings.getGroups());
                }
            });
        } else {
            // Queue the remaining settings for later execution when the player logs in.
            QueuedSettingsTable queuedSettingsTable = databaseManager.getQueuedSettingsTable();
            queuedSettingsTable.queueSettings(playerId, island.getUniqueId(), SettingsType.TEAM_KICK, -1);

            // Process permissions and groups
            LuckPermsHook luckPermsHook = hookManager.getHook(LuckPermsHook.class);
            CompletableFuture<@Nullable User> future = luckPermsHook.getOrLoadUser(player, playerId);
            future.thenAccept(user -> {
                if(user != null) {
                    modifyUser(luckPermsHook, user, resetSettings.getPermissions(), resetSettings.getGroups());
                }
            });
        }
    }

    /**
     * Apply the permissions and groups changes to the user.
     * @param luckPermsHook A {@link LuckPermsHook} instance.
     * @param user The {@link User} to apply permission and group changes to.
     * @param permissionConfigList The {@link List} of {@link PermissionConfig}s.
     * @param groupConfigList The {@link List} of {@link GroupConfig}s.
     */
    private void modifyUser(
            @NonNull LuckPermsHook luckPermsHook,
            @NonNull User user,
            @NonNull List<PermissionConfig> permissionConfigList,
            @NonNull List<GroupConfig> groupConfigList) {
        for(PermissionConfig permissionConfig : permissionConfigList) {
            if(permissionConfig.permission() == null) continue;

            if(permissionConfig.removePermission()) {
                List<PermissionNode> permissionNodeList = luckPermsHook.getPermissionNodes(user, permissionConfig.permission(), permissionConfig.contexts());
                if(permissionNodeList.isEmpty()) continue;

                for(PermissionNode permissionNode : permissionNodeList) {
                    DataMutateResult result = luckPermsHook.removeNode(user, permissionNode);

                    switch(result) {
                        case SUCCESS -> logger.info(AdventureUtility.plain("Removed permission " + permissionConfig.permission() + " from user " + user.getUsername() + "."));

                        case FAIL, FAIL_ALREADY_HAS -> logger.info(AdventureUtility.plain("Failed to remove permission " + permissionConfig.permission() + " from user " + user.getUsername() + "."));

                        case FAIL_LACKS -> logger.info(AdventureUtility.plain("User " + user.getUsername() + " does not have permission " + permissionConfig.permission() + " to remove."));

                        case null -> logger.info(AdventureUtility.plain("LuckPerms not hooked into. Unable to remove permission."));
                    }
                }
            } else {
                PermissionNode permissionNode = luckPermsHook.createPermissionNode(permissionConfig.permission(), !permissionConfig.negatePermission(), permissionConfig.contexts());

                DataMutateResult result = luckPermsHook.addNode(user, permissionNode);

                switch(result) {
                    case SUCCESS -> logger.info(AdventureUtility.plain("Added permission " + permissionConfig.permission() + " to user " + user.getUsername() + "."));

                    case FAIL, FAIL_LACKS -> logger.info(AdventureUtility.plain("Failed to add permission " + permissionConfig.permission() + " to user " + user.getUsername() + "."));

                    case FAIL_ALREADY_HAS -> logger.info(AdventureUtility.plain("Unable to add permission " + permissionConfig.permission() + " because user " + user.getUsername() + " already has the permission."));

                    case null -> logger.info(AdventureUtility.plain("LuckPerms not hooked into. Unable to add permission."));
                }
            }
        }

        for(GroupConfig groupConfig : groupConfigList) {
            if(groupConfig.groupName() == null) continue;

            if(groupConfig.removeGroup()) {
                List<InheritanceNode> inheritanceNodeList = luckPermsHook.getInheritanceNodes(user, groupConfig.groupName(), groupConfig.contexts());
                if(inheritanceNodeList.isEmpty()) continue;

                for(InheritanceNode inheritanceNode : inheritanceNodeList) {
                    DataMutateResult result = luckPermsHook.removeNode(user, inheritanceNode);

                    switch(result) {
                        case SUCCESS -> logger.info(AdventureUtility.plain("Removed group " + inheritanceNode.getGroupName() + " from user " + user.getUsername() + "."));

                        case FAIL, FAIL_ALREADY_HAS -> logger.info(AdventureUtility.plain("Failed to remove group " + inheritanceNode.getGroupName() + " from user " + user.getUsername() + "."));

                        case FAIL_LACKS -> logger.info(AdventureUtility.plain("User " + user.getUsername() + " does not have the group " + inheritanceNode.getGroupName() + " to remove."));

                        case null -> logger.info(AdventureUtility.plain("LuckPerms not hooked into. Unable to remove group."));
                    }
                }
            } else {
                InheritanceNode inheritanceNode = luckPermsHook.createInheritanceNode(groupConfig.groupName(), groupConfig.contexts());

                DataMutateResult result = luckPermsHook.addNode(user, inheritanceNode);

                switch(result) {
                    case SUCCESS -> logger.info(AdventureUtility.plain("Added group " + inheritanceNode.getGroupName() + " to user " + user.getUsername() + "."));

                    case FAIL, FAIL_LACKS -> logger.info(AdventureUtility.plain("Failed to add group " + inheritanceNode.getGroupName() + " to user " + user.getUsername() + "."));

                    case FAIL_ALREADY_HAS -> logger.info(AdventureUtility.plain("Unable to add group " + inheritanceNode.getGroupName() + " because user " + user.getUsername() + " already has the group."));

                    case null -> logger.info(AdventureUtility.plain("LuckPerms not hooked into. Unable to add group."));
                }
            }
        }

        // Save the user
        luckPermsHook.saveUser(user);
    }
}