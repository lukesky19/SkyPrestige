/*
    SkyPrestige allows players to reset their Island after meeting certain requirements to unlock rewards.
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

import com.github.lukesky19.skyHoppers.SkyHoppersAPI;
import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This class manages interfacing with SkyHoppers.
 */
public class SkyHoppersHook implements Hook {
    private final @NonNull SkyPlugin plugin;
    private @Nullable SkyHoppersAPI skyHoppersAPI;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     */
    public SkyHoppersHook(@NonNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link SkyHoppersAPI} from SkyHoppers.
     */
    @Override
    public void initialize() {
        if(plugin.getServer().getPluginManager().getPlugin("SkyHoppers") != null) {
            RegisteredServiceProvider<SkyHoppersAPI> rsp = plugin.getServer().getServicesManager().getRegistration(SkyHoppersAPI.class);
            if (rsp != null) {
                skyHoppersAPI = rsp.getProvider();
            }
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return skyHoppersAPI != null;
    }

    /**
     * Check if the {@link ItemStack} provided is a SkyHopper.
     * @param itemStack The {@link ItemStack} to check.
     * @return true if a SkyHopper, or false if not or not hooked into SkyHoppers
     * @apiNote Will always return false if SkyHoppers was not hooked into.
     */
    public boolean isItemStackSkyHopper(@NonNull ItemStack itemStack) {
        if (skyHoppersAPI == null) return false;

        return skyHoppersAPI.isItemStackSkyHopper(itemStack);
    }
}
