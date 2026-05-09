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

import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.google.common.collect.ImmutableSet;
import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.QPlayerManager;
import com.leonardobishop.quests.common.player.questprogressfile.QuestProgress;
import com.leonardobishop.quests.common.player.questprogressfile.QuestProgressFile;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.QuestManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * The hook for the LMBishop's Quests plugin.
 */
public class LMBQuestHook implements Hook {
    private final @NonNull SkyPlugin plugin;
    private @Nullable BukkitQuestsPlugin questsPlugin;
    private @Nullable QuestManager questManager;
    private @Nullable QPlayerManager playerManager;
    private final @NonNull Map<UUID, QPlayer> qPlayerMap = new HashMap<>();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public LMBQuestHook(@NonNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link Economy} from Vault.
     */
    @Override
    public void initialize() {
        if(plugin.getServer().getPluginManager().getPlugin("Quests") != null) {
            Plugin questsPlugin = plugin.getServer().getPluginManager().getPlugin("Quests");
            if(questsPlugin != null) {
                this.questsPlugin = (BukkitQuestsPlugin) questsPlugin;

                questManager = this.questsPlugin.getQuestManager();
                playerManager = this.questsPlugin.getPlayerManager();
            }
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return questsPlugin != null && questManager != null && playerManager != null;
    }

    /**
     * Get a {@link Quest} for the quest id.
     * @apiNote Will always return null if the quests plugin isn't hooked into.
     * @param questId The quest id or null.
     * @return The {@link Quest} or null.
     */
    public @Nullable Quest getQuestById(@Nullable String questId) {
        if(questManager == null) return null;
        if(questId == null) return null;

        return questManager.getQuestById(questId);
    }

    /**
     * Has the quest been completed by any of the qPlayers provided?
     * @apiNote If the list is empty, the quest will be assumed as not complete.
     * If the quests plugin isn't hooked into or the quest can't be found, the quest will be assumed as not complete.
     * @param playerList The {@link List} containing {@link QPlayer}s.
     * @param questId The quest id or null.
     * @return true if complete, false if not or on any error.
     */
    public boolean isQuestComplete(@NonNull List<QPlayer> playerList, @Nullable String questId) {
        if(playerList.isEmpty()) return false;
        if(questId == null) return false;
        Quest quest = getQuestById(questId);
        if(quest == null) return false;

        return isQuestComplete(playerList, quest);
    }

    /**
     * Has the quest been completed by any of the qPlayers provided?
     * @apiNote If the player list is empty, the quest will be assumed as not complete.
     * @param playerList The {@link List} containing {@link QPlayer}s.
     * @param quest The {@link Quest}.
     * @return true if complete, false if not or on any error.
     */
    public boolean isQuestComplete(@NonNull List<QPlayer> playerList, @NonNull Quest quest) {
        if(playerList.isEmpty()) return false;

        for(QPlayer qPlayer : playerList) {
            QuestProgress questProgress = qPlayer.getQuestProgressFile().getQuestProgress(quest);
            if(questProgress == null) continue;

            if(questProgress.isCompleted() || questProgress.isCompletedBefore()) return true;
        }

        return false;
    }

    /**
     * Load all {@link QPlayer}s for the player id provided.
     * @param playerIds The {@link ImmutableSet} of {@link UUID}s.
     */
    public void loadPlayerData(@NonNull ImmutableSet<UUID> playerIds) {
        if(playerManager == null) return;

        playerIds.stream()
                .filter(playerId -> !qPlayerMap.containsKey(playerId))
                .forEach(playerId -> {
            QPlayer qPlayer = playerManager.getPlayer(playerId);
            if(qPlayer != null) {
                qPlayerMap.put(playerId, qPlayer);
            } else {
                CompletableFuture<QPlayer> future = playerManager.loadPlayer(playerId);
                future.thenAccept(loadedQPlayer -> qPlayerMap.put(playerId, loadedQPlayer));
            }
        });
    }

    /**
     * Get the {@link List} of {@link QPlayer}s for the player ids provided.
     * @param playerIds The {@link Set} of {@link UUID}s.
     * @return The {@link List} of {@link QPlayer}s.
     */
    public @NonNull List<QPlayer> getQPlayerList(@NonNull Set<UUID> playerIds) {
        return playerIds.stream()
                .map(qPlayerMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Unload all {@link QPlayer}s for the player id provided.
     * @param playerIds The {@link ImmutableSet} of {@link UUID}s.
     */
    public void unloadPlayerData(@NonNull ImmutableSet<UUID> playerIds) {
        if(playerManager == null) return;

        playerIds.forEach(qPlayerMap::remove);
    }

    /**
     * Reset the quest progress for the {@link UUID} provided.
     * @param playerId The {@link UUID}.
     */
    public void resetQuestProgress(@NonNull UUID playerId) {
        QPlayer qPlayer = qPlayerMap.get(playerId);
        if(qPlayer == null) return;

        resetQuestProgress(qPlayer);
    }

    /**
     * Reset the quest progress for the {@link QPlayer} provided.
     * @param qPlayer The {@link QPlayer}.
     */
    public void resetQuestProgress(@NonNull QPlayer qPlayer) {
        QuestProgressFile progress = qPlayer.getQuestProgressFile();
        progress.reset();
    }
}