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
import com.github.lukesky19.skyplaytime.SkyPlayTimeAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This class manages interfacing with SkyPlayTime.
 */
public class SkyPlayTimeHook implements Hook {
    private final @NonNull SkyPlugin plugin;
    private @Nullable SkyPlayTimeAPI skyPlayTimeAPI;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public SkyPlayTimeHook(@NonNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link SkyPlayTimeAPI} from SkyPlayTime.
     */
    @Override
    public void initialize() {
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("SkyPlayTime");
        if(plugin != null && plugin.isEnabled()) {
            RegisteredServiceProvider<SkyPlayTimeAPI> rsp = this.plugin.getServer().getServicesManager().getRegistration(SkyPlayTimeAPI.class);
            if (rsp != null) {
                skyPlayTimeAPI = rsp.getProvider();
            }
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return skyPlayTimeAPI != null;
    }

    /**
     * Checks if the player is marked as afk according to SkyPlayTime.
     * @apiNote Will always return false if SkyPlayTime was not hooked into.
     * @param player The {@link Player} to check.
     * @return true if afk, or false if not afk or SkyPlayTime was not hooked into.
     */
    public boolean isPlayerAFK(@NonNull Player player) {
        if(skyPlayTimeAPI == null) return false;

        return skyPlayTimeAPI.isPlayerAfk(player);
    }

    /**
     * Resets the play time for the player provided.
     * @apiNote If SkyPlayTime was not hooked into, this method will do nothing. Can be checked with {@link #isHooked()}.
     * @apiNote If all booleans are false, this method will do nothing.
     * @param player The {@link Player} to reset play time for.
     * @param session Should session play time be reset?
     * @param daily Should daily play time be reset?
     * @param weekly Should weekly play time be reset?
     * @param monthly Should monthly play time be reset?
     * @param yearly Should yearly play time be reset?
     * @param total Should total play time be reset?
     */
    public void resetPlayTime(@NonNull Player player, boolean session, boolean daily, boolean weekly, boolean monthly, boolean yearly, boolean total) {
        if(skyPlayTimeAPI == null) return;
        if(!session && !daily && !weekly && !monthly && !yearly && !total) return;

        skyPlayTimeAPI.resetPlayTime(player, session, daily, weekly, monthly, yearly, total);
    }
}
