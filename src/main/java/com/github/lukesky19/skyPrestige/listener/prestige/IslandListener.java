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
package com.github.lukesky19.skyPrestige.listener.prestige;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.locale.Locale;
import com.github.lukesky19.skyPrestige.config.data.settings.Settings;
import com.github.lukesky19.skyPrestige.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.MagicCobblestoneGeneratorHook;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigeManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.api.events.island.IslandCreatedEvent;
import world.bentobox.bentobox.api.events.island.IslandResetEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;
import java.util.function.Predicate;

/**
 * Listens for when an island is created or reset to store or update the island id stored in the database.
 */
public class IslandListener implements Listener {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull PrestigeManager prestigeManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param prestigeManager A {@link PrestigeManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public IslandListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull PrestigeManager prestigeManager,
            @NotNull HookManager hookManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.databaseManager = databaseManager;
        this.islandDataManager = islandDataManager;
        this.prestigeManager = prestigeManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens to when an island is created and if the island was not a part of a prestige, adds the island id to the database.
     * @param islandCreatedEvent An {@link IslandCreatedEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandCreation(IslandCreatedEvent islandCreatedEvent) {
        Island island = islandCreatedEvent.getIsland();
        String islandId = island.getUniqueId();

        databaseManager.getIslandIdsTable().insertIslandId(island.getUniqueId());

        IslandData islandData = new IslandData();
        islandDataManager.setIslandData(islandId, islandData);
    }

    /**
     * Listens to when an island is reset and if the island was not a part of a prestige, and resets the island's prestige points and prestige level if configured.
     * @param islandResetEvent An {@link IslandResetEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandReset(IslandResetEvent islandResetEvent) {
        Locale locale = localeManager.getLocale();
        Island oldIsland = islandResetEvent.getOldIsland();
        String oldIslandId = oldIsland.getUniqueId();
        Island newIsland = islandResetEvent.getIsland();
        String newIslandId = newIsland.getUniqueId();

        // If the island was reset because of a prestige, return
        if(prestigeManager.isIslandIdPrestiged(oldIslandId)) {
            prestigeManager.removePrestigedIslandId(oldIslandId);
            return;
        }

        // Retrieve the IslandData for the old island.
        IslandData islandData = islandDataManager.getIslandData(oldIslandId);
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for the island id " + oldIslandId + "."));
            logger.error(AdventureUtil.deserialize(locale.islandDataNotFound()));
            return;
        }

        @Nullable Settings settings = settingsManager.getSettings();
        if(settings != null) {
            Settings.IslandResetSettings islandResetSettings = settings.islandResetSettings();
            MagicCobblestoneGeneratorHook magicCobblestoneGeneratorHook = hookManager.getHook(MagicCobblestoneGeneratorHook.class);

             if(islandResetSettings.resetPrestigeLevel()) {
                 islandData.setPrestigeLevel(0);
             }

             if(islandResetSettings.resetPrestigePoints()) {
                 islandData.setPrestigePoints(0);
             }

             if(islandResetSettings.keepGeneratorUpgrades() && magicCobblestoneGeneratorHook.isHooked()) {
                 magicCobblestoneGeneratorHook.copyGeneratorData(oldIsland, newIsland);
             }

             if(islandResetSettings.keepIslandRange()) {
                 newIsland.setProtectionRange(oldIsland.getProtectionRange());
             }
        }

        islandDataManager.removeIslandData(oldIslandId);
        islandDataManager.setIslandData(newIslandId, islandData);

        databaseManager.getIslandIdsTable().updateIslandId(oldIslandId, newIslandId)
            .thenAccept(v1 ->
                databaseManager.getIslandDataTable().saveIslandData(newIslandId, islandData).exceptionally(ex -> {
                    logger.error(AdventureUtil.deserialize("Failed to save island data for new island id: " + newIslandId + ". Error: " + ex.getMessage()));
                    return null;
                })
            .exceptionally(ex -> {
                logger.error(AdventureUtil.deserialize("Failed to update old island id " + oldIslandId + " to new island id " + newIslandId + ". Error: " + ex.getMessage()));
                return null;
            }));
    }

    /**
     * Listens for when an island is reset because of island prestige, teleports players to the new island, and sends appropriate teleport messages.
     * @param islandResettedEvent An {@link IslandResettedEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandResetted(IslandResettedEvent islandResettedEvent) {
        @NotNull Locale locale = localeManager.getLocale();
        Island oldIsland = islandResettedEvent.getOldIsland();
        String oldIslandId = oldIsland.getUniqueId();
        Island newIsland = islandResettedEvent.getIsland();

        // Remove island id from list of prestiged island ids if the event is cancelled
        if(islandResettedEvent.isCancelled()) {
            prestigeManager.removePrestigedIslandId(oldIslandId);
            return;
        }

        if(prestigeManager.isIslandIdPrestiged(oldIslandId)) {
            Settings settings = settingsManager.getSettings();
            if(settings == null) return;
            @Nullable Location newIslandSpawnPoint = newIsland.getSpawnPoint(World.Environment.NORMAL);

            List<Player> islandMemberPlayersOnOldIsland = getPlayerList(oldIsland.getMemberSet(), memberPlayer -> oldIsland.inIslandSpace(memberPlayer.getLocation()));
            List<Player> islandMemberPlayersNotOnOldIsland = getPlayerList(oldIsland.getMemberSet(), memberPlayer -> !oldIsland.inIslandSpace(memberPlayer.getLocation()));
            List<Player> islandMemberPlayersNotOnOldOrNewIsland = islandMemberPlayersNotOnOldIsland.stream().filter(memberPlayer -> !newIsland.inIslandSpace(memberPlayer.getLocation())).toList();
            List<Player> trustedPlayersOnOldIsland = getPlayerListByRank(oldIsland.getMembers(), 400, memberPlayer -> oldIsland.inIslandSpace(memberPlayer.getLocation()));
            List<Player> trustedPlayersNotOnOldIsland = getPlayerListByRank(oldIsland.getMembers(), 400, memberPlayer -> !oldIsland.inIslandSpace(memberPlayer.getLocation()));
            List<Player> coopPlayersOnOldIsland = getPlayerListByRank(oldIsland.getMembers(), 200, memberPlayer -> oldIsland.inIslandSpace(memberPlayer.getLocation()));
            List<Player> coopPlayersNotOnOldIsland = getPlayerListByRank(oldIsland.getMembers(), 200, memberPlayer -> !oldIsland.inIslandSpace(memberPlayer.getLocation()));
            List<Player> otherPlayersOnOldIsland = oldIsland.getVisitors();

            if(newIslandSpawnPoint != null) {
                islandMemberPlayersOnOldIsland.forEach(memberPlayer -> {
                    memberPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandMemberIslandTeleportNotice()));

                    memberPlayer.teleportAsync(newIslandSpawnPoint);
                });

                trustedPlayersOnOldIsland.forEach(trustedPlayer -> {
                    trustedPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.otherIslandTeleportNotice()));

                    trustedPlayer.teleportAsync(newIslandSpawnPoint);
                });

                coopPlayersOnOldIsland.forEach(coopPlayer -> {
                    coopPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.otherIslandTeleportNotice()));

                    coopPlayer.teleportAsync(newIslandSpawnPoint);
                });

                otherPlayersOnOldIsland.forEach(visitorPlayer -> {
                    visitorPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.otherIslandTeleportNotice()));

                    visitorPlayer.teleportAsync(newIslandSpawnPoint);
                });
            } else {
                Settings.Location fallbackLocationConfig = settings.fallbackLocation();
                if(fallbackLocationConfig.world() == null) {
                    logger.error(AdventureUtil.deserialize("Fallback location world is invalid. Unable to teleport players."));
                    return;
                }
                World world = skyPrestige.getServer().getWorld(fallbackLocationConfig.world());
                if(world == null) {
                    logger.error(AdventureUtil.deserialize("Fallback location world is invalid for world name " + fallbackLocationConfig.world() + ". Unable to teleport players."));
                    return;
                }
                if(fallbackLocationConfig.x() == null) {
                    logger.error(AdventureUtil.deserialize("Fallback location X coordinate is invalid. Unable to teleport players."));
                    return;
                }
                if(fallbackLocationConfig.y() == null) {
                    logger.error(AdventureUtil.deserialize("Fallback location Y coordinate is invalid. Unable to teleport players."));
                    return;
                }
                if(fallbackLocationConfig.z() == null) {
                    logger.error(AdventureUtil.deserialize("Fallback location Z coordinate is invalid. Unable to teleport players."));
                    return;
                }

                @NotNull Location fallbackLocation = new Location(world, fallbackLocationConfig.x(), fallbackLocationConfig.y(), fallbackLocationConfig.z());

                islandMemberPlayersOnOldIsland.forEach(memberPlayer -> {
                    memberPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandMemberFallbackTeleportNotice()));

                    memberPlayer.teleportAsync(fallbackLocation);
                });

                trustedPlayersOnOldIsland.forEach(trustedPlayer -> {
                    trustedPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.otherFallbackTeleportNotice()));

                    trustedPlayer.teleportAsync(fallbackLocation);
                });

                coopPlayersOnOldIsland.forEach(coopPlayer -> {
                    coopPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.otherFallbackTeleportNotice()));

                    coopPlayer.teleportAsync(fallbackLocation);
                });

                otherPlayersOnOldIsland.forEach(visitorPlayer -> {
                    visitorPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.otherFallbackTeleportNotice()));

                    visitorPlayer.teleportAsync(fallbackLocation);
                });
            }

            islandMemberPlayersNotOnOldOrNewIsland.forEach(memberPlayer -> memberPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandMemberPrestigeNotice())));

            trustedPlayersNotOnOldIsland.forEach(trustedPlayer -> trustedPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.otherPrestigeNotice())));

            coopPlayersNotOnOldIsland.forEach(coopPlayer -> coopPlayer.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.otherPrestigeNotice())));
            
            prestigeManager.removePrestigedIslandId(oldIslandId);
        }
    }

    /**
     * Get a {@link List} of {@link Player}s that are online, connected, and meet the provided predicate.
     * @param uuidSet The {@link Set} of {@link UUID}s to process.
     * @param predicate The extra criteria to filter the list by.
     * @return A {@link List} of {@link Player}s.
     */
    private @NotNull List<Player> getPlayerList(@NotNull Set<UUID> uuidSet, @NotNull Predicate<Player> predicate) {
        return uuidSet.stream()
                .map(uuid -> skyPrestige.getServer().getPlayer(uuid))
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .filter(Player::isConnected)
                .filter(predicate)
                .toList();
    }

    /**
     * Get a {@link List} of {@link Player}s that αre online, connected, have the island rank provided, and meet the provided predicate.
     * @param uuidRankMap The {@link Map} mapping {@link UUID}s to island ranks.
     * @param rank The island rank to filter.
     * @param predicate The extra criteria to filter the list by.
     * @return A {@link List} of {@link Player}s.
     */
    private @NotNull List<Player> getPlayerListByRank(@NotNull Map<UUID, Integer> uuidRankMap, int rank, @NotNull Predicate<Player> predicate) {
        return uuidRankMap.entrySet().stream()
                .filter(entry -> entry.getValue() == rank)
                .map(entry -> skyPrestige.getServer().getPlayer(entry.getKey()))
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .filter(Player::isConnected)
                .filter(predicate)
                .toList();
    }
}
