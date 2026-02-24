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
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.common.abstracts.config.KeyValueConfigManager;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
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

        saveBundledConfig();

        try(Stream<Path> paths = Files.walk(Paths.get(plugin.getDataFolder() + File.separator + "prestige"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> loadConfiguration(-1, PrestigeConfig.class, path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadConfiguration(@NonNull Integer identifier, @NonNull Class<PrestigeConfig> configClass, @NonNull Path configurationPath) {
        PrestigeConfig configuration;

        YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);
        try {
            configuration = yamlConfigurationLoader.load().get(configClass);
            if(configuration == null) return;

            PrestigeConfig migratedConfiguration = migrateConfiguration(configuration);
            if(migratedConfiguration == null) return;

            if(!validateConfiguration(migratedConfiguration)) return;

            // Save the migrated configuration if different
            if(configuration != migratedConfiguration) {
                saveConfiguration(configClass, configurationPath, migratedConfiguration);
            }

            // Store the configuration
            setData(migratedConfiguration.prestigeLevel(), migratedConfiguration);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load the configuration file at " + configurationPath + ". Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Save the default prestige config for level 1 if it doesn't exist.
     */
    @Override
    protected void saveBundledConfig() {
        Path path = Path.of(plugin.getDataFolder() + File.separator + "prestige" + File.separator + "1.yml");
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
        switch(configuration.configVersion()) {
            case "2.0.0.0" -> {
                // Current version, do nothing
                return configuration;
            }

            case "1.0.0.0" -> {
                logger.error(AdventureUtil.deserialize("A prestige config file is from version 1.0.0.0 and cannot be migrated."));
                logger.error(AdventureUtil.deserialize("Please regenerate or update your files."));

                return null;
            }

            case null, default -> {
                logger.error(AdventureUtil.deserialize("A prestige config file version is unrecognized and cannot be migrated."));
                logger.error(AdventureUtil.deserialize("Please regenerate or update your files to version 2.0.0.0."));

                return null;
            }
        }
    }

    @Override
    protected boolean validateConfiguration(@NonNull PrestigeConfig configuration) {
        return true;
    }
}
