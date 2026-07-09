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

import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.KeyValueConfigManager;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the configuration for prestige levels.
 */
public class PrestigeConfigManager extends KeyValueConfigManager<Integer, PrestigeConfig> {
    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     */
    public PrestigeConfigManager(@NonNull SkyPlugin plugin) {
        super(plugin);
    }

    /**
     * Get a {@link List} of {@link Integer}s for the currently configured prestige levels.
     * @return A {@link List} of {@link Integer}s for the currently configured prestige levels.
     */
    public @NonNull List<@NonNull Integer> getPrestigeLevels() {
        return dataMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get the max prestige level.
     * @return The max prestige level.
     */
    public int getMaxLevel() {
        return dataMap.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    @Override
    public void loadConfigurations() {
        clearData();

        saveDefaultConfiguration();

        try(Stream<Path> paths = Files.walk(Paths.get(plugin.getDirectoryFile() + File.separator + "prestige"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> loadConfiguration(-1, PrestigeConfig.class, path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadConfiguration(@NonNull Integer identifier, @NonNull Class<PrestigeConfig> configClass, @NonNull Path configurationPath) {
        PrestigeConfig configuration;

        YamlConfigurationLoader yamlConfigurationLoader = createLoader(configurationPath);
        try {
            ConfigurationNode root = yamlConfigurationLoader.load();
            migrateVersion(root);
            yamlConfigurationLoader.save(root);

            configuration = root.get(configClass);
            if(configuration == null) return;

            PrestigeConfig migratedConfiguration = migrateConfiguration(configuration);
            if(migratedConfiguration == null) {
                logger.info(AdventureUtility.plain("Prestige Configuration migration failed for file " + configurationPath));
                return;
            }

            if(!validateConfiguration(migratedConfiguration)) return;

            // Save the migrated configuration if different
            if(configuration != migratedConfiguration) {
                saveConfiguration(configClass, configurationPath, migratedConfiguration);
            }

            // Store the configuration
            setData(migratedConfiguration.prestigeLevel(), migratedConfiguration);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to load the configuration file at " + configurationPath + ". Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Save the default prestige config for level 1 if it doesn't exist.
     */
    @Override
    public void saveDefaultConfiguration() {
        Path path = Path.of(plugin.getDirectoryFile() + File.separator + "prestige" + File.separator + "1.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("prestige" + File.separator + "1.yml", false);
        }
    }

    /**
     * Update a {@link PrestigeConfig} to the latest version.
     * @param configuration The {@link PrestigeConfig} to update.
     * @return The updated {@link PrestigeConfig}.
     */
    @Override
    protected @Nullable PrestigeConfig migrateConfiguration(@NonNull PrestigeConfig configuration) {
        switch(configuration.version()) {
            case 2 -> {
                // Current version, do nothing
                return configuration;
            }

            case 1 -> {
                logger.error(AdventureUtility.plain("A prestige config file is from version 1 and cannot be migrated."));
                logger.error(AdventureUtility.plain("Please regenerate or update your files."));

                return null;
            }

            default -> {
                logger.error(AdventureUtility.plain("A prestige config file version is unrecognized and cannot be migrated. Version: " + configuration.version()));
                logger.error(AdventureUtility.plain("Please regenerate or update your files to version 2."));

                return null;
            }
        }
    }

    @Override
    protected boolean validateConfiguration(@NonNull PrestigeConfig configuration) {
        return true;
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
        try {
            switch(legacyVersion) {
                case "2.0.0.0" -> versionNode.set(2);

                case "1.0.0.0" -> versionNode.set(1);

                case null, default -> logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version due to an unrecognized version."));
            }
        } catch (SerializationException e) {
            logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version. Error: " + e.getMessage()));
        }
    }
}
