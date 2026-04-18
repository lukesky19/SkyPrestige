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

import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentcrates.CratesAPI;
import su.nightexpress.excellentcrates.key.KeyManager;

/**
 * This class manages interfacing with ExcellentCrates.
 */
public class ExcellentCratesHook implements Hook {
    private final @NonNull SkyPlugin plugin;
    private @Nullable KeyManager keyManager;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     */
    public ExcellentCratesHook(@NonNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link KeyManager} from ExcellentCrates.
     */
    @Override
    public void initialize() {
        if(plugin.getServer().getPluginManager().getPlugin("ExcellentCrates") != null) {
            keyManager = CratesAPI.getKeyManager();
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return keyManager != null;
    }

    /**
     * Is the {@link ItemStack} provided a key from ExcellentCrates?
     * @apiNote Will always return false if the hook was not initialized. Can be checked with {@link #isHooked()}.
     * @param itemStack The {@link ItemStack} to check.
     * @return true if a key, or false if not a key or if the hook was not initialized.
     */
    public boolean isItemStackKey(@NonNull ItemStack itemStack) {
        if(keyManager == null) return false;

        return keyManager.isKey(itemStack);
    }
}