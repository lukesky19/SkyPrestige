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
package com.github.lukesky19.skyPrestige.configuration.manager.prestige;

import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.common.abstracts.config.KeyValueConfigManager;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public PrestigeConfigManager(@NotNull SkyPlugin plugin) {
        super(plugin);
    }

    /**
     * Get a {@link List} of {@link Integer}s for the currently configured prestige levels.
     * @return A {@link List} of {@link Integer}s for the currently configured prestige levels.
     */
    public @NotNull List<@NotNull Integer> getPrestigeLevels() {
        return dataMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
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
    public void loadConfiguration(@NotNull Integer identifier, @NotNull Class<PrestigeConfig> configClass, @NotNull Path configurationPath) {
        @Nullable PrestigeConfig configuration;

        YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(configurationPath);
        try {
            configuration = yamlConfigurationLoader.load().get(configClass);
            if(configuration == null) return;

            if(validateConfiguration(configuration)) {
                @Nullable PrestigeConfig migratedConfiguration = migrateConfiguration(configuration);
                if(migratedConfiguration == null) return;

                // Store the configuration
                setData(configuration.prestigeLevel(), migratedConfiguration);

                // Save the migrated configuration if different
                if(configuration != migratedConfiguration) {
                    saveConfiguration(configClass, configurationPath, migratedConfiguration);
                }
            }
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load the configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Get a {@link Map} mapping prestige levels to {@link PrestigeConfig}.
     * The {@link Map} will be empty if no {@link PrestigeConfig} was found for any prestige levels.
     * @param prestigeLevels The {@link List} of prestige levels.
     * @return A {@link Map} mapping prestige levels to {@link PrestigeConfig}.
     */
    public @NotNull Map<Integer, PrestigeConfig> getPrestigeConfigMapForLevels(@NotNull List<Integer> prestigeLevels) {
        Map<Integer, PrestigeConfig> prestigeConfigMap = new HashMap<>();

        for(Integer prestigeLevel : prestigeLevels) {
            PrestigeConfig prestigeConfig = getData(prestigeLevel);
            if(prestigeConfig == null) {
                logger.warn(AdventureUtil.deserialize("No prestige config found for prestige level: " + prestigeLevel));
                continue;
            }

            prestigeConfigMap.put(prestigeLevel, prestigeConfig);
        }

        return prestigeConfigMap;
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
    protected @Nullable PrestigeConfig migrateConfiguration(@NotNull PrestigeConfig configuration) {
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
    protected boolean validateConfiguration(@NotNull PrestigeConfig configuration) {
        return true;
    }
}
