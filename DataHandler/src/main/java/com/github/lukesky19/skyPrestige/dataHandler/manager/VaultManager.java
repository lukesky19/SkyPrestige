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
package com.github.lukesky19.skyPrestige.dataHandler.manager;

import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.settings.SettingsManager;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class contains methods related to island vaults.
 */
public class VaultManager {
    private final @NotNull SettingsManager settingsManager;

    /**
     * Constructor
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public VaultManager(@NotNull SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    /**
     * Check if an {@link ItemType} is disallowed inside vaults.
     * @param itemType The {@link ItemType} to check.
     * @return true if disallowed or if settings are null, otherwise false.
     */
    public boolean isVaultItemTypeDisallowed(@NotNull ItemType itemType) {
        @Nullable Settings settings = settingsManager.getConfiguration();
        if(settings == null) return true;

        return settings.vaultDisallowedItems().contains(itemType.getKey().toString());
    }
}
