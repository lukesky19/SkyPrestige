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
package com.github.lukesky19.skyPrestige.task.tasks;

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Objects;

/**
 * This task decrements multiplier time and removes any multipliers if necessary.
 */
public class MultiplierTask extends BukkitRunnable {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull MultiplierManager multiplierManager;
    private final @NonNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public MultiplierTask(
            @NonNull SkyPlugin plugin,
            @NonNull LocaleManager localeManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull HookManager hookManager) {
        this.plugin = plugin;
        this.localeManager = localeManager;
        this.islandDataManager = islandDataManager;
        this.multiplierManager = multiplierManager;
        this.hookManager = hookManager;
    }

    /**
     * Removes 1 second from all multiplier cooldowns and removes any cooldowns if the time has expired.
     */
    @Override
    public void run() {
        if(multiplierManager.getServerMultiplierTime() > 0) {
            multiplierManager.removeServerMultiplier(null, null, 1L, false);

            if(multiplierManager.getServerMultiplierTime() == 0) {
                sendServerMultiplierExpiredNotice();
            }
        }

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        islandDataManager.getAllData()
                .values()
                .stream()
                .filter(islandData -> islandData.getMultiplier() > 0.0 && islandData.getMultiplierTime() > 0)
                .forEach(islandData -> {
                    islandData.removeMultiplierTime(1);

                    if(islandData.getMultiplierTime() == 0) {
                        islandData.setMultiplier(0);

                        bentoBoxHook.getIslandById(islandData.getIslandId()).ifPresent(this::sendIslandMultiplierExpiredNotice);
                    }
                });
    }

    /**
     * Send a message to all online players that the server multiplier expired.
     */
    private void sendServerMultiplierExpiredNotice() {
        Locale locale = localeManager.getConfiguration();
        Locale.MultiplierMessages multiplierMessages = locale.multiplierMessages();

        // Send notice to online players
        Component message = AdventureUtility.deserialize(locale.prefix() + multiplierMessages.serverMultiplierExpiredNotice());
        plugin.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(message));
    }

    /**
     * Send a message to all island members that the island multiplier expired.
     * @param island The {@link Island} whose multiplier has expired.
     */
    private void sendIslandMultiplierExpiredNotice(@NonNull Island island) {
        Locale locale = localeManager.getConfiguration();
        Locale.MultiplierMessages multiplierMessages = locale.multiplierMessages();

        // Send notice to island members
        Component message = AdventureUtility.deserialize(locale.prefix() + multiplierMessages.islandMultiplierExpiredNotice());
        island.getMemberSet().stream()
                .map(memberId -> plugin.getServer().getPlayer(memberId))
                .filter(Objects::nonNull)
                .forEach(onlinePlayer -> onlinePlayer.sendMessage(message));
    }
}
