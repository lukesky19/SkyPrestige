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

import com.github.lukesky19.skyPrestige.configuration.data.opt_in_out.OptInOutConfig;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the opt-in configuration.
 */
public class OptInConfigManager extends SimpleConfigManager<OptInOutConfig> {
    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     */
    public OptInConfigManager(@NonNull SkyPlugin plugin) {
        super(plugin, Path.of(plugin.getDataFolder() + File.separator + "opt-in.yml"), OptInOutConfig.class);
    }

    @Override
    public @Nullable OptInOutConfig migrateConfiguration(@NonNull OptInOutConfig optInOutConfig) {
        switch(optInOutConfig.version()) {
            case 1 -> {
                // latest version, do nothing
                return optInOutConfig;
            }

            default -> {
                logger.warn(AdventureUtility.plain("Unknown config version for the opt-in config. Unable to update config."));
                return null;
            }
        }
    }

    @Override
    public boolean validateConfiguration(@Nullable OptInOutConfig configuration) {
        return configuration != null;
    }

    @Override
    public void saveDefaultConfiguration() {
        plugin.saveResource("opt-in.yml", false);
    }
}