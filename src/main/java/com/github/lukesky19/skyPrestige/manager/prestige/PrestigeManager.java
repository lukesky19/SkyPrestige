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
package com.github.lukesky19.skyPrestige.manager.prestige;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.Locale;
import com.github.lukesky19.skyPrestige.config.PrestigeConfig;
import com.github.lukesky19.skyPrestige.config.Settings;
import com.github.lukesky19.skyPrestige.data.IslandData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.database.table.PlayerTeleportTable;
import com.github.lukesky19.skyPrestige.gui.BlueprintGUI;
import com.github.lukesky19.skyPrestige.hook.impl.*;
import com.github.lukesky19.skyPrestige.manager.config.GUIConfigManager;
import com.github.lukesky19.skyPrestige.manager.config.LocaleManager;
import com.github.lukesky19.skyPrestige.manager.config.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.manager.config.SettingsManager;
import com.github.lukesky19.skyPrestige.manager.gui.GUIManager;
import com.github.lukesky19.skyPrestige.manager.hook.HookManager;
import com.github.lukesky19.skyPrestige.manager.island.IslandDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.island.IslandCache;
import world.bentobox.bentobox.managers.island.NewIsland;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class manages the prestiging of islands.
 */
public class PrestigeManager {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull GUIManager guiManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull IslandsManager islandsManager;
    private final @NotNull List<String> prestigedIslandIds = new ArrayList<>();

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PrestigeManager(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull GUIManager guiManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull HookManager hookManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.guiManager = guiManager;
        this.islandDataManager = islandDataManager;
        this.databaseManager = databaseManager;
        this.hookManager = hookManager;
        this.islandsManager = BentoBox.getInstance().getIslandsManager();
    }

    /**
     * Checks if an island is in the process of being prestige.
     * @param islandId The island id to check.
     * @return true if the island is being prestiged, otherwise false.
     */
    public boolean isIslandIdPrestiged(@NotNull String islandId) {
        return prestigedIslandIds.contains(islandId);
    }

    /**
     * Adds an island id to the list of islands in the process of being prestiged.
     * @param islandId The island id to add.
     */
    public void addPrestigedIslandId(@NotNull String islandId) {
        prestigedIslandIds.add(islandId);
    }

    /**
     * Removes an island id from the list of islands in the process of being prestiged.
     * @param islandId The island id to remove.
     */
    public void removePrestigedIslandId(@NotNull String islandId) {
        prestigedIslandIds.removeIf(listIslandId -> listIslandId.equals(islandId));
    }

    /**
     * Checks if the player's island can be prestiged and opens the blueprint selection GUI or displays an error.
     * @param player The {@link Player}.
     * @param uuid The {@link UUID} of the player.
     * @param location The {@link Location} of the player's island, usually just the player's location.
     */
    public void prestigeIsland(@NotNull Player player, @NotNull UUID uuid, @NotNull Location location) {
        @NotNull Locale locale = localeManager.getLocale();

        // Check if the player is in a world managed by a GameModeAddon
        Optional<GameModeAddon> optionalGameModeAddon = getGameModeAddon(player);
        if(optionalGameModeAddon.isEmpty()) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigePlayerInWrongWorld()));
            return;
        }

        // Get the Island the player is on.
        @NotNull Optional<Island> optionalIsland = getIslandAtLocation(location);
        if(optionalIsland.isEmpty()) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigePlayerNotOnIsland()));
            return;
        }
        Island island = optionalIsland.get();
        String islandId = island.getUniqueId();

        // Check if the player attempting to prestige owns the island or is a member
        if(!isPlayerOwnerOrMember(island, uuid)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigePlayerNotMemberOrOwner()));
            return;
        }

        // Get the island data for the island.
        IslandData islandData = islandDataManager.getIslandData(island.getUniqueId());
        if(islandData == null) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.islandDataNotFound()));
            logger.error(AdventureUtil.serialize("No Island data found for player " + player.getName() + "'s island. Island Id: " + islandId));
            return;
        }

        // Get the next prestige level and the config for that level
        int nextPrestigeLevel = islandData.getPrestigeLevel() + 1;
        PrestigeConfig prestigeConfig = prestigeConfigManager.getPrestigeConfig(nextPrestigeLevel);

        // If the prestige config is null, the player is at the max prestige level
        if(prestigeConfig == null) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.islandPrestigeLevelMax()));
            return;
        }

        // Check if the player's island has enough prestige points to prestige
        if(lacksRequiredPrestigePoints(player, island, prestigeConfig, nextPrestigeLevel)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeNotEnoughPrestigePoints()));
            return;
        }

        // Let the player select a blueprint on prestige
        BlueprintGUI gui = new BlueprintGUI(skyPrestige, localeManager, guiConfigManager, this, guiManager, optionalGameModeAddon.get(), island, player, prestigeConfig, nextPrestigeLevel);

        // Create the GUI
        boolean creationResult = gui.create();
        if(!creationResult) {
            logger.error(AdventureUtil.serialize("Unable to create the InventoryView for the blueprint GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        // Update the GUI
        boolean updateResult = gui.update();
        if(!updateResult) {
            logger.error(AdventureUtil.serialize("Unable to decorate the blueprint GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
            return;
        }

        // Open the GUI
        boolean openResult = gui.open();
        if(!openResult) {
            logger.error(AdventureUtil.serialize("Unable to open the blueprint GUI for player " + player.getName() + " due to a configuration error."));
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.guiOpenError()));
        }
    }

    /**
     * Prestige the player's island.
     * The method {@link #prestigeIsland(Player, UUID, Location)} should be run before this method.
     * @param player The {@link Player}.
     * @param user The {@link User} for the {@link Player}.
     * @param oldIsland The old {@link Island}.
     * @param gameModeAddon The {@link GameModeAddon}.
     * @param blueprintName The blueprint name to use.
     * @param prestigeConfig The {@link PrestigeConfig} for the next prestige level.
     * @param prestigeLevel The prestige level the island is moving to.
     */
    public void prestigeIsland(
            @NotNull Player player,
            @NotNull User user,
            @NotNull Island oldIsland,
            @NotNull GameModeAddon gameModeAddon,
            String blueprintName,
            PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;
        if(settings.scaleFormula() == null) return;
        Locale locale = localeManager.getLocale();

        String oldIslandId = oldIsland.getUniqueId();

        // Re-check if the player meets the prestige requirements
        if(lacksRequiredPrestigePoints(player, oldIsland, prestigeConfig, prestigeLevel)) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeNotEnoughPrestigePoints()));
            return;
        }

        IslandData islandData = islandDataManager.getIslandData(oldIslandId);
        if(islandData == null) {
            logger.error(AdventureUtil.serialize("No island data found for island id " + oldIslandId + "."));
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.islandDataNotFound()));
            return;
        }

        try {
            addPrestigedIslandId(oldIslandId);

            // Create the new Island
            NewIsland.Builder islandBuilder = NewIsland.builder();
            islandBuilder.player(user);
            islandBuilder.addon(gameModeAddon);
            islandBuilder.reason(IslandEvent.Reason.RESET);
            islandBuilder.oldIsland(oldIsland);
            islandBuilder.name(blueprintName);

            Island newIsland = islandBuilder.build();
            String newIslandId = newIsland.getUniqueId();

            // Set the island owner and members for the new island
            newIsland.setOwner(oldIsland.getOwner());
            newIsland.setMembers(new HashMap<>(oldIsland.getMembers()));

            IslandsManager islandsManager = BentoBox.getInstance().getIslandsManager();
            IslandCache islandCache = islandsManager.getIslandCache();

            // Make the new island associated with each island member and set the primary island as necessary.
            newIsland.getMemberSet()
                    .forEach(uuid -> {
                        islandCache.addPlayer(uuid, newIsland);

                        if(oldIsland.isPrimary(uuid)) islandCache.setPrimaryIsland(uuid, newIsland);
                    });

            IslandsManager.updateIsland(newIsland);

            // Process PrestigeSettings
            processPrestigeSettings(player, oldIsland, prestigeConfig.prestigeSettings());

            // Set prestige level and reset prestige points
            islandData.setPrestigeLevel(prestigeLevel);
            islandData.setPrestigePoints(0);

            // Remove the old island data and store the island data for the new island.
            islandDataManager.removeIslandData(oldIslandId);
            islandDataManager.setIslandData(newIslandId, islandData);

            // Update data in the database.
            databaseManager.getIslandIdsTable().updateIslandId(oldIslandId, newIslandId)
                .thenAccept(v1 -> {
                    // For any offline island members, store the UUID to process prestige settings for them on login
                    oldIsland.getMemberSet().stream()
                        .map(skyPrestige.getServer()::getOfflinePlayer)
                        .filter(memberPlayer -> !memberPlayer.isOnline() && !memberPlayer.isConnected())
                        .forEach(memberPlayer -> databaseManager.getOfflinePrestigeTable().insertOfflinePrestige(memberPlayer.getUniqueId(), newIsland.getUniqueId(), prestigeLevel));

                    // Get any player UUIDs that are offline and their logout location is within the old island's bounds.
                    CompletableFuture<List<UUID>> offlinePlayerIdsOnIslandFuture = databaseManager.getPlayerLogoutLocationsTables().getPlayerIdsWithinByBounds(oldIsland.getWorld().getName(), oldIsland.getMinX(), oldIsland.getMaxX(), oldIsland.getMinZ(), oldIsland.getMaxZ());
                    offlinePlayerIdsOnIslandFuture.thenAccept(offlinePlayerIdsOnIsland -> {
                        PlayerTeleportTable playerTeleportTable = databaseManager.getPlayerTeleportTable();

                        // Mark any players for teleportation on login.
                        offlinePlayerIdsOnIsland.forEach(uuid -> playerTeleportTable.insertPlayerIdAndIslandId(uuid, newIslandId));
                    });

                    // Reset prestige points
                    databaseManager.getPrestigePointsTable().resetPrestigePoints(newIslandId)
                        .thenAccept(v2 -> {})
                        .exceptionally(ex -> {
                            logger.error(AdventureUtil.serialize("Failed to reset prestige points for new island id: " + newIslandId + ". Error: " + ex.getMessage()));
                            return null;
                        });

                    // Set prestige level
                    databaseManager.getPrestigeLevelsTable().setLevel(newIslandId, islandData.getPrestigeLevel())
                            .thenAccept(v2 -> {})
                            .exceptionally(ex -> {
                                logger.error(AdventureUtil.serialize("Failed to set prestige level for new island id: " + newIslandId + ". Error: " + ex.getMessage()));
                                return null;
                            });
                })
                .exceptionally(ex -> {
                    logger.error(AdventureUtil.serialize("Failed to update old island id " + oldIslandId + " to new island id " + newIslandId + ". Error: " + ex.getMessage()));
                    return null;
                });

            // Set the owner of the old island to null
            oldIsland.setOwner(null);
            // Remove the members from the old island
            oldIsland.getMemberSet()
                    .forEach(uuid -> {
                        // Remove the member from the old island
                        islandCache.removePlayer(oldIsland, uuid);
                    });
            // Update the island
            IslandsManager.updateIsland(oldIsland);

            // Process prestige rewards
            processPrestigeRewards(player, newIsland, prestigeConfig, prestigeLevel);

            // Send the plugin's teleport notice if BSkyBlock teleports the player to the island upon creation
            BSkyBlockHook bSkyBlockHook = hookManager.getHook(BSkyBlockHook.class);
            if(bSkyBlockHook.isHooked()) {
                if(bSkyBlockHook.isTeleportPlayerToIslandUponIslandCreation()) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.islandMemberIslandTeleportNotice()));
                }
            }

            // Delete the old island
            islandsManager.deleteIsland(oldIsland, true, player.getUniqueId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Processes the {@link PrestigeConfig.PrestigeSettings} for the prestige level.
     * @param player The {@link Player} who prestiged the island.
     * @param oldIsland The old {@link Island}.
     * @param prestigeSettings The {@link PrestigeConfig.PrestigeSettings} to process.
     */
    private void processPrestigeSettings(
            @NotNull Player player,
            @NotNull Island oldIsland,
            @NotNull PrestigeConfig.PrestigeSettings prestigeSettings) {
        Locale locale = localeManager.getLocale();

        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        SkySellWandsHook skySellWandsHook = hookManager.getHook(SkySellWandsHook.class);
        PlayerAuctionsHook playerAuctionsHook = hookManager.getHook(PlayerAuctionsHook.class);

        // Reset auction house items if configured to do so for offline island members
        if(prestigeSettings.resetAuctionItems() && playerAuctionsHook.isHooked()) {
            oldIsland.getMemberSet().stream().filter(uuid -> {
                @Nullable Player memberPlayer = skyPrestige.getServer().getPlayer(uuid);

                return memberPlayer == null || !memberPlayer.isOnline() || !memberPlayer.isConnected();
            }).forEach(playerAuctionsHook::clearPlayerAuctions);
        }

        // Loop through all online island members
        oldIsland.getMemberSet().stream()
                .map(skyPrestige.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .filter(memberPlayer -> memberPlayer.isOnline() && memberPlayer.isConnected())
                .forEach(memberPlayer -> {
                    UUID memberPlayerUniqueId = memberPlayer.getUniqueId();
                    // Reset the island member's inventory if configured to do so
                    if(prestigeSettings.inventorySettings().resetInventory()) {
                        ItemStack emptyStack = ItemType.AIR.createItemStack();

                        for(int i = 0; i < memberPlayer.getInventory().getSize(); i++) {
                            ItemStack itemStack = memberPlayer.getInventory().getItem(i);
                            if(itemStack == null || itemStack.isEmpty()) continue;
                            if(skySellWandsHook.isInfiniteSellWand(itemStack)) continue;

                            memberPlayer.getInventory().setItem(i, emptyStack);
                        }

                        memberPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeInventoryReset()));
                    }

                    // Reset the island member's ender chest if configured to do so
                    if(prestigeSettings.resetEnderChest()) {
                        memberPlayer.getEnderChest().clear();

                        memberPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeEnderChestReset()));
                    }

                    // Reset the island member's experience if configured to do so
                    if(prestigeSettings.resetExp()) {
                        memberPlayer.setLevel(0);
                        memberPlayer.setExp(0);

                        memberPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeExperienceReset()));
                    }

                    // Check if an economy is hooked into
                    if(economyHook.isHooked()) {
                        // Reset the island member's balance if configured to do so
                        if(prestigeSettings.resetMoney()) {
                            economyHook.removeFromBalance(memberPlayer, economyHook.getBalance(memberPlayer));

                            memberPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeBalanceReset()));
                        }

                        // Give starting money if configured and the starting money should be given to all island members
                        if(prestigeSettings.startingMoney() > 0 && prestigeSettings.giveStartingMoneyToAllIslandMembers()) {
                            economyHook.addToBalance(memberPlayer, prestigeSettings.startingMoney());

                            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("money", String.valueOf(prestigeSettings.startingMoney())));

                            memberPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeStartingMoneyGiven(), placeholders));
                        }
                    } else {
                        // Display appropriate errors, if any, if no economy was hooked into.
                        if(prestigeSettings.resetMoney() && prestigeSettings.startingMoney() > 0) {
                            // Display an error if an economy isn't hooked into and starting money is configured.
                            player.sendMessage(AdventureUtil.serialize("<red>Failed to reset a player's balance or give starting money due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                            logger.warn(AdventureUtil.serialize("<red>Failed to reset a player's balance or give starting money due to an Economy not being hooked into.</red>"));
                        } else if(prestigeSettings.resetMoney()) {
                            // Display an error if the economy isn't hooked into and starting money is configured.
                            player.sendMessage(AdventureUtil.serialize("<red>Failed to reset a player's balance due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                            logger.warn(AdventureUtil.serialize("<red>Failed to reset a player's balance due to an Economy not being hooked into.</red>"));
                        } else if(prestigeSettings.startingMoney() > 0) {
                            // Display an error if an economy isn't hooked into and starting money is configured.
                            player.sendMessage(AdventureUtil.serialize("<red>Failed to give a player starting money due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                            logger.warn(AdventureUtil.serialize("<red>Failed to give a player starting money due to an Economy not being hooked into.</red>"));
                        }
                    }

                    if(prestigeSettings.resetAuctionItems() && playerAuctionsHook.isHooked()) {
                        playerAuctionsHook.clearPlayerAuctions(memberPlayerUniqueId);

                        memberPlayer.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeAuctionHouseItemsReset()));
                    }

                    // Check if SkyPlayTime is hooked into.
                    if(skyPlayTimeHook.isHooked()) {
                        PrestigeConfig.PlayTimeSettings playTimeSettings = prestigeSettings.playTimeSettings();

                        // Reset any play time configured to do so.
                        skyPlayTimeHook.resetPlayTime(
                                memberPlayerUniqueId,
                                playTimeSettings.resetSession(),
                                playTimeSettings.resetDaily(),
                                playTimeSettings.resetWeekly(),
                                playTimeSettings.resetMonthly(),
                                playTimeSettings.resetYearly(),
                                playTimeSettings.resetTotal());
                    } else {
                        PrestigeConfig.PlayTimeSettings playTimeSettings = prestigeSettings.playTimeSettings();
                        if(playTimeSettings.resetSession()
                                || playTimeSettings.resetDaily()
                                || playTimeSettings.resetWeekly()
                                || playTimeSettings.resetMonthly()
                                || playTimeSettings.resetYearly()
                                || playTimeSettings.resetTotal()) {
                            player.sendMessage(AdventureUtil.serialize("<red>Failed to apply reset a player's play time due to SkyPlayTime not being hooked into. Contact your server's system administrator.</red>"));
                            logger.warn(AdventureUtil.serialize("<red>Failed to apply reset a player's play time due to SkyPlayTime not being hooked into.</red>"));
                        }
                    }
                });

        // Give starting money if configured and the starting money should only be given to the player prestiging the island.
        if(prestigeSettings.startingMoney() > 0 && !prestigeSettings.giveStartingMoneyToAllIslandMembers()) {
            if(economyHook.isHooked()) {
                economyHook.addToBalance(player, prestigeSettings.startingMoney());
            } else {
                // Display an error if an economy isn't hooked into and starting money is configured.
                player.sendMessage(AdventureUtil.serialize("<red>Failed to give a player starting money due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                logger.warn(AdventureUtil.serialize("<red>Failed to give a player starting money due to an Economy not being hooked into.</red>"));
            }
        }
    }

    /**
     * Processes the rewards for the prestige level.
     * @param player The {@link Player} to give rewards to.
     * @param island The new {@link Island} of the player.
     * @param prestigeConfig The {@link PrestigeConfig} to process rewards for.
     * @param prestigeLevel The prestige level.
     */
    private void processPrestigeRewards(
            @NotNull Player player,
            @NotNull Island island,
            @NotNull PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        // Get a list of players for the online island members
        List<Player> onlineIslandMembers = island.getMemberSet().stream()
                .map(skyPrestige.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .filter(memberPlayer -> memberPlayer.isOnline() && memberPlayer.isConnected())
                .toList();

        Server server = skyPrestige.getServer();
        ConsoleCommandSender commandSender = server.getConsoleSender();

        prestigeConfig.rewards().forEach(rewardConfig -> {
            // Give the item reward if configured to do so.
            if(rewardConfig.rewardItem().itemType() != null) {
                ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                itemStackBuilder.fromItemStackConfig(rewardConfig.rewardItem(), null, null, List.of());
                Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

                if(optionalItemStack.isPresent()) {
                    ItemStack itemStack = optionalItemStack.get();

                    // Give the item reward to all island members or only the player who triggered the prestige
                    if(rewardConfig.giveToAllIslandMembers()) {
                        onlineIslandMembers.forEach(memberPlayer ->
                                PlayerUtil.giveItem(memberPlayer.getInventory(), itemStack, itemStack.getAmount(), memberPlayer.getLocation()));
                    } else {
                        PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
                    }
                } else {
                    player.sendMessage(AdventureUtil.serialize("<red>Failed to give an ItemStack reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
                    logger.warn(AdventureUtil.serialize("Unable to process an ItemStack reward due to an invalid ItemStack. Prestige level: " + prestigeLevel));
                }
            }

            // Run command rewards for all island members or only the player prestiging the island.
            if(rewardConfig.giveToAllIslandMembers()) {
                onlineIslandMembers.forEach(islandMember ->
                        rewardConfig.commands().stream()
                                .map(command -> PlaceholderAPIUtil.parsePlaceholders(islandMember, command))
                                .forEach(parsedCommand -> server.dispatchCommand(commandSender, parsedCommand)));
            } else {
                rewardConfig.commands().stream()
                        .map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command))
                        .forEach(parsedCommand -> server.dispatchCommand(commandSender, parsedCommand));
            }
        });
    }

    /**
     * Handles when a player logs in after their island was prestiged while they were offline.
     * @param player The player who was offline during a prestige.
     * @param uuid The UUID of the player.
     * @param prestigeLevels The prestige levels that occurred while they are offline.
     */
    public void handleOfflinePrestige(@NotNull Player player, @NotNull UUID uuid, @NotNull List<Integer> prestigeLevels) {
        Locale locale = localeManager.getLocale();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);
        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        SkySellWandsHook skySellWandsHook = hookManager.getHook(SkySellWandsHook.class);

        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null) {
            logger.error(AdventureUtil.serialize("Unable to process offline prestige for player " + player.getName() + " due to invalid plugin settings."));
            return;
        }

        @Nullable Map<Integer, PrestigeConfig> prestigeConfigMap = getPrestigeConfigMap(prestigeLevels);
        if(prestigeConfigMap == null) {
            logger.error(AdventureUtil.serialize("Unable to process offline prestige for player " + player.getName() + " due to a missing prestige config for a level their island was prestiged for."));
            return;
        }

        boolean invReset = false;
        boolean enderChestReset = false;
        boolean expReset = false;
        boolean moneyReset = false;
        boolean auctionHouseMessageSent = false;

        // Process prestige settings for all prestige levels that occured while the player was offline
        for(Map.Entry<Integer, PrestigeConfig> prestigeConfigEntry  : prestigeConfigMap.entrySet()) {
            int prestigeLevel = prestigeConfigEntry.getKey();
            PrestigeConfig prestigeConfig = prestigeConfigEntry.getValue();
            PrestigeConfig.PrestigeSettings prestigeSettings = prestigeConfig.prestigeSettings();

            if(prestigeSettings.resetAuctionItems() && !auctionHouseMessageSent) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeAuctionHouseItemsReset()));
                auctionHouseMessageSent = true;
            }

            // Reset the player's inventory if configured to do so, and it hasn't been done so already
            if(prestigeSettings.inventorySettings().resetInventory() && !invReset) {
                ItemStack emptyStack = ItemType.AIR.createItemStack();

                for(int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack itemStack = player.getInventory().getItem(i);
                    if(itemStack == null || itemStack.isEmpty()) continue;
                    if(skySellWandsHook.isInfiniteSellWand(itemStack)) continue;

                    player.getInventory().setItem(i, emptyStack);
                }

                invReset = true;

                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeInventoryReset()));
            }

            // Reset the player's ender chest if configured to do so, and it hasn't been done so already
            if(prestigeSettings.resetEnderChest() && !enderChestReset) {
                player.getEnderChest().clear();
                enderChestReset = true;

                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeEnderChestReset()));
            }

            // Reset the player's experience if configured to do so, and it hasn't been done so already
            if(prestigeSettings.resetExp() && !expReset) {
                player.setTotalExperience(0);
                expReset = true;

                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeExperienceReset()));
            }

            // Check if an economy is hooked into
            if(economyHook.isHooked()) {
                // Reset the player's balance if configured to do so, and it hasn't been done so already
                if(prestigeSettings.resetMoney() && !moneyReset) {
                    double balance = economyHook.getBalance(player);

                    economyHook.removeFromBalance(player, balance);

                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeBalanceReset()));
                }

                // Give starting money if configured and the starting money should be given to all island members
                if(prestigeSettings.startingMoney() > 0 && prestigeSettings.giveStartingMoneyToAllIslandMembers()) {
                    economyHook.addToBalance(player, prestigeSettings.startingMoney());

                    List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("money", String.valueOf(prestigeSettings.startingMoney())));

                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeStartingMoneyGiven(), placeholders));
                }
            } else {
                // Display appropriate errors, if any, if no economy was hooked into.
                if(prestigeSettings.resetMoney() && prestigeSettings.startingMoney() > 0) {
                    // Display an error if an economy isn't hooked into and starting money is configured.
                    player.sendMessage(AdventureUtil.serialize("<red>Failed to reset a player's balance or give starting money due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                    logger.warn(AdventureUtil.serialize("<red>Failed to reset a player's balance or give starting money due to an Economy not being hooked into.</red>"));
                } else if(prestigeSettings.resetMoney()) {
                    // Display an error if the economy isn't hooked into and starting money is configured.
                    player.sendMessage(AdventureUtil.serialize("<red>Failed to reset a player's balance due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                    logger.warn(AdventureUtil.serialize("<red>Failed to reset a player's balance due to an Economy not being hooked into.</red>"));
                } else if(prestigeSettings.startingMoney() > 0) {
                    // Display an error if an economy isn't hooked into and starting money is configured.
                    player.sendMessage(AdventureUtil.serialize("<red>Failed to give a player starting money due to an Economy not being hooked into. Contact your server's system administrator.</red>"));
                    logger.warn(AdventureUtil.serialize("<red>Failed to give a player starting money due to an Economy not being hooked into.</red>"));
                }
            }

            // Check if SkyPlayTime is hooked into.
            if(skyPlayTimeHook.isHooked()) {
                PrestigeConfig.PlayTimeSettings playTimeSettings = prestigeSettings.playTimeSettings();

                // Reset any play time configured to do so.
                skyPlayTimeHook.resetPlayTime(
                        uuid,
                        playTimeSettings.resetSession(),
                        playTimeSettings.resetDaily(),
                        playTimeSettings.resetWeekly(),
                        playTimeSettings.resetMonthly(),
                        playTimeSettings.resetYearly(),
                        playTimeSettings.resetTotal());
            } else {
                PrestigeConfig.PlayTimeSettings playTimeSettings = prestigeSettings.playTimeSettings();
                if(playTimeSettings.resetSession()
                        || playTimeSettings.resetDaily()
                        || playTimeSettings.resetWeekly()
                        || playTimeSettings.resetMonthly()
                        || playTimeSettings.resetYearly()
                        || playTimeSettings.resetTotal()) {
                    player.sendMessage(AdventureUtil.serialize("<red>Failed to apply reset a player's play time due to SkyPlayTime not being hooked into. Contact your server's system administrator.</red>"));
                    logger.warn(AdventureUtil.serialize("<red>Failed to apply reset a player's play time due to SkyPlayTime not being hooked into.</red>"));
                }
            }

            Server server = skyPrestige.getServer();
            ConsoleCommandSender commandSender = server.getConsoleSender();

            prestigeConfig.rewards().stream()
                    .filter(rewardConfig ->
                            !rewardConfig.giveToAllIslandMembers()).forEach(rewardConfig -> {
                                // Give the item reward if configured to do so.
                                if(rewardConfig.rewardItem().itemType() != null) {
                                    ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
                                    itemStackBuilder.fromItemStackConfig(rewardConfig.rewardItem(), null, null, List.of());
                                    Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

                                    if(optionalItemStack.isPresent()) {
                                        ItemStack itemStack = optionalItemStack.get();

                                        PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
                                    } else {
                                        player.sendMessage(AdventureUtil.serialize("<red>Failed to give an ItemStack reward for prestige level " + prestigeLevel + " due to a configuration error. Contact your server's system administrator.</red>"));
                                        logger.warn(AdventureUtil.serialize("Unable to process an ItemStack reward due to an invalid ItemStack. Prestige level: " + prestigeLevel));
                                    }
                                }

                                // Run command rewards for the player
                                rewardConfig.commands().stream()
                                        .map(command -> PlaceholderAPIUtil.parsePlaceholders(player, command))
                                        .forEach(parsedCommand -> server.dispatchCommand(commandSender, parsedCommand));
                            });
        }

        // Remove any offline prestiges from the database for the player
        databaseManager.getOfflinePrestigeTable().removeOfflinePrestige(uuid);
    }

    /**
     * If the player's UUID is stored for teleportation, teleport them to either their new island or the fallback location.
     * @param player The {@link Player} to teleport.
     * @param uuid The {@link UUID} of the player.
     */
    public void handleQueuedTeleports(@NotNull Player player, @NotNull UUID uuid) {
        Locale locale = localeManager.getLocale();
        @Nullable Settings settings = settingsManager.getSettings();
        if(settings == null) {
            logger.error(AdventureUtil.serialize("Unable to teleport player " + player.getName() + " due to invalid plugin settings."));
            return;
        }

        @NotNull CompletableFuture<@Nullable String> islandIdFuture = databaseManager.getPlayerTeleportTable().getIslandId(uuid);
        islandIdFuture.thenAccept(islandId -> {
            if(islandId == null) return;

            IslandsManager islandsManager = BentoBox.getInstance().getIslandsManager();
            Optional<Island> optionalIsland = islandsManager.getIslandById(islandId);
            if(optionalIsland.isEmpty()) {
                logger.error(AdventureUtil.serialize("Unable to teleport player " + player.getName() + " due to no island found for island id " + islandId + "."));
                return;
            }

            Island island = optionalIsland.get();
            @Nullable Location spawnPoint = island.getSpawnPoint(World.Environment.NORMAL);
            if(spawnPoint != null) {
                if(island.getMemberSet().contains(uuid)) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.islandMemberIslandTeleportNotice()));
                } else {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.otherIslandTeleportNotice()));
                }

                player.teleportAsync(spawnPoint);
            } else {
                Settings.Location fallbackLocationConfig = settings.fallbackLocation();
                if(fallbackLocationConfig.world() == null) {
                    logger.error(AdventureUtil.serialize("Unable to teleport player " + player.getName() + " due to an invalid fallback location world name."));
                    return;
                }
                World world = skyPrestige.getServer().getWorld(fallbackLocationConfig.world());
                if(world == null) {
                    logger.error(AdventureUtil.serialize("Unable to teleport player " + player.getName() + " due to no world found for world name " + fallbackLocationConfig.world() + " for the fallback location config."));
                    return;
                }
                if(fallbackLocationConfig.x() == null) {
                    logger.error(AdventureUtil.serialize("Unable to teleport player " + player.getName() + " due to an invalid X coordinate for the fallback location config."));
                    return;
                }
                if(fallbackLocationConfig.y() == null) {
                    logger.error(AdventureUtil.serialize("Unable to teleport player " + player.getName() + " due to an invalid Y coordinate for the fallback location config."));
                    return;
                }
                if(fallbackLocationConfig.z() == null) {
                    logger.error(AdventureUtil.serialize("Unable to teleport player " + player.getName() + " due to an invalid Z coordinate for the fallback location config."));
                    return;
                }

                Location fallbackLocation = new Location(world, fallbackLocationConfig.x(), fallbackLocationConfig.y(), fallbackLocationConfig.z());

                if(island.getMemberSet().contains(uuid)) {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.islandMemberFallbackTeleportNotice()));
                } else {
                    player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.otherFallbackTeleportNotice()));
                }

                player.teleportAsync(fallbackLocation);
            }

            databaseManager.getPlayerTeleportTable().deletePlayerIdAndIslandId(uuid);
        });
    }

    /**
     * Checks if the island lacks the required prestige points to prestige their island.
     * Also sends any error messages to the player and console as needed.
     * @param player The {@link Player} attempting to prestige their island.
     * @param island The {@link Island} attempting to be prestiged.
     * @param prestigeConfig The {@link PrestigeConfig} for the next prestige level.
     * @param prestigeLevel The next prestige level.
     * @return true if the island lacks the required prestige points or any error occurs, otherwise false.
     */
    public boolean lacksRequiredPrestigePoints(
            @NotNull Player player,
            @NotNull Island island,
            @NotNull PrestigeConfig prestigeConfig,
            int prestigeLevel) {
        Locale locale = localeManager.getLocale();
        @Nullable Long requiredPrestigePoints = prestigeConfig.requiredPrestigePoints();
        @Nullable Double scaleFactor = prestigeConfig.scaleFactor();

        if(requiredPrestigePoints == null) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeConfigRequirementError()));
            logger.error(AdventureUtil.serialize("The required prestige points for prestige level " + prestigeLevel + " is invalid."));
            return true;
        }

        if(scaleFactor == null) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.prestigeConfigRequirementError()));
            logger.error(AdventureUtil.serialize("The scale factor for prestige level " + prestigeLevel + " is invalid."));
            return true;
        }

        IslandData islandData = islandDataManager.getIslandData(island.getUniqueId());
        if(islandData == null) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.islandDataNotFound()));
            logger.error(AdventureUtil.serialize("No island data was found for island " + island.getUniqueId() + " for player " + player.getName() + "."));
            return true;
        }

        return islandData.getPrestigePoints() < requiredPrestigePoints;
    }

    /**
     * Get the {@link GameModeAddon} that the player is in. If none, an empty {@link Optional} is returned.
     * @param player The {@link Player} to check.
     * @return An {@link Optional} containing the {@link GameModeAddon}. Will be empty if none was found.
     */
    private @NotNull Optional<GameModeAddon> getGameModeAddon(@NotNull Player player) {
        World world = player.getWorld();

        return BentoBox.getInstance().getAddonsManager().getGameModeAddons().stream()
                .filter(gameModeAddon -> gameModeAddon.inWorld(world))
                .findFirst();
    }

    /**
     * Get the {@link Island} at the provided {@link Location}.
     * @param location The {@link Location} to get the island for.
     * @return An {@link Optional} containing an {@link Island}.
     */
    private @NotNull Optional<Island> getIslandAtLocation(@NotNull Location location) {
        return islandsManager.getIslandAt(location);
    }

    /**
     * Checks if for the island provided, that the UUID is the owner or a member of the island.
     * @param island The {@link Island} to check.
     * @param uuid The {@link UUID} to check if they are the owner or a member for the island.
     * @return true if the owner or an island member, otherwise false.
     */
    private boolean isPlayerOwnerOrMember(@NotNull Island island, @NotNull UUID uuid) {
        return (island.getOwner() != null && island.getOwner().equals(uuid)) || island.getMembers().containsKey(uuid);
    }

    /**
     * Get a {@link Map} mapping prestige levels to {@link PrestigeConfig}.
     * The {@link Map} will be null if no {@link PrestigeConfig} was found for a prestige level.
     * @param prestigeLevels The {@link List} of prestige levels.
     * @return A {@link Map} mapping prestige levels to {@link PrestigeConfig} or null.
     */
    private @Nullable Map<Integer, PrestigeConfig> getPrestigeConfigMap(@NotNull List<Integer> prestigeLevels) {
        Map<Integer, PrestigeConfig> prestigeConfigMap = new HashMap<>();

        for(Integer prestigeLevel : prestigeLevels) {
            PrestigeConfig prestigeConfig = prestigeConfigManager.getPrestigeConfig(prestigeLevel);
            if(prestigeConfig == null) {
                logger.warn(AdventureUtil.serialize("No prestige config found for prestige level: " + prestigeLevel));
                return null;
            }

            prestigeConfigMap.put(prestigeLevel, prestigeConfig);
        }

        return prestigeConfigMap;
    }
}
