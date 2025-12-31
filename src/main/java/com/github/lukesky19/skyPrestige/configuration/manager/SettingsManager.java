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
package com.github.lukesky19.skyPrestige.configuration.manager;

import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the plugin's settings.
 */
public class SettingsManager extends SimpleConfigManager<Settings> {
    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     */
    public SettingsManager(@NotNull SkyPlugin plugin) {
        super(plugin, Path.of(plugin.getDataFolder() + File.separator + "settings.yml"), Settings.class);
    }

    @Override
    public @Nullable Settings migrateConfiguration(@NotNull Settings settings) {
        switch(settings.configVersion()) {
            case "2.0.0.0" -> {
                // latest version, do nothing
                return settings;
            }

            case "1.1.0.0", "1.0.0.0" -> {
                logger.warn(AdventureUtil.deserialize("Unable to migrate version 1.0.0.0 or 1.1.0.0 config versions for settings config. Please regenerate or manually migrate your configuration."));
                return null;
            }

            case null, default -> {
                logger.warn(AdventureUtil.deserialize("Unknown config version for settings config. Unable to update config."));
                return null;
            }
        }
    }

    @Override
    public boolean validateConfiguration() {
        return getConfiguration() != null;
    }

    @Override
    public void saveBundledConfig() {
        plugin.saveResource("settings.yml", false);
    }
}