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
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
    public SettingsManager(@NonNull SkyPlugin plugin) {
        super(plugin, Path.of(plugin.getDataFolder() + File.separator + "settings.yml"), Settings.class);
    }

    @Override
    public void loadConfiguration() {
        configuration = null;

        if(configurationPath == null) return;

        saveDefaultConfiguration();

        YamlConfigurationLoader loader = createLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();
            migrateVersion(root);
            loader.save(root);

            Settings settings = root.get(Settings.class);
            if(settings == null) {
                logger.warn(AdventureUtility.plain("Failed to load settings.yml."));
                return;
            }

            Settings migratedConfiguration = migrateConfiguration(settings);
            if(migratedConfiguration == null) {
                logger.warn(AdventureUtility.plain("Failed to migrate settings.yml."));
                return;
            }

            if(!settings.equals(migratedConfiguration)) {
                saveConfiguration(migratedConfiguration);
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(migratedConfiguration)) {
                logger.warn(AdventureUtility.plain("Settings configuration validation failed."));
                return;
            }

            this.configuration = migratedConfiguration;
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to load plugin settings configuration. Error: " + configurateException.getMessage()));
        }
    }

    @Override
    public @Nullable Settings migrateConfiguration(@NonNull Settings settings) {
        switch(settings.version()) {
            case 3 -> {
                // latest version, do nothing
                return settings;
            }

            case 2, 1 -> {
                logger.warn(AdventureUtility.plain("Unable to migrate version 1 or 2 config versions for settings config. Please regenerate or manually migrate your configuration."));
                return null;
            }

            default -> {
                logger.warn(AdventureUtility.plain("Unknown config version for settings config. Unable to update config."));
                return null;
            }
        }
    }

    @Override
    public boolean validateConfiguration(@Nullable Settings configuration) {
        return configuration != null;
    }

    @Override
    public void saveDefaultConfiguration() {
        if(configurationPath == null) return;

        if(!configurationPath.toFile().exists()) {
            plugin.saveResource("settings.yml", false);
        }
    }

    /**
     * Migrate the string-based version to a numeric version number.
     * @param root The root {@link ConfigurationNode}.
     */
    private void migrateVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();
        if(version > 0) return;

        ConfigurationNode legacyVersionNode = root.node("config-version");
        String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
        if(legacyVersion == null) return;
        try {
            switch(legacyVersion) {
                case "2.0.0.0" -> versionNode.set(3);

                case "1.1.0.0" -> versionNode.set(2);

                case "1.0.0.0" -> versionNode.set(1);

                default -> logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version due to an unrecognized version."));
            }
        } catch (SerializationException e) {
            logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version. Error: " + e.getMessage()));
        }
    }
}