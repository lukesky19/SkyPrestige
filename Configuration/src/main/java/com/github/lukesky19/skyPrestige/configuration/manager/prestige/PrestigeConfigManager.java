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
import com.github.lukesky19.skyPrestige.core.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
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
public class PrestigeConfigManager {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;
    private final @NotNull Map<Integer, PrestigeConfig> prestigeConfig = new HashMap<>();

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     */
    public PrestigeConfigManager(@NotNull SkyPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
    }

    /**
     * Get the {@link PrestigeConfig} for the provided level.
     * @param level The prestige level to get config for.
     * @return The {@link PrestigeConfig} or null.
     */
    public @Nullable PrestigeConfig getPrestigeConfig(int level) {
        return prestigeConfig.get(level);
    }

    /**
     * Get a {@link List} of {@link Integer}s for the currently configured prestige levels.
     * @return A {@link List} of {@link Integer}s for the currently configured prestige levels.
     */
    public @NotNull List<@NotNull Integer> getPrestigeLevels() {
        return prestigeConfig.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get a {@link Map} mapping prestige levels to {@link PrestigeConfig}.
     * The {@link Map} will be empty if no {@link PrestigeConfig} was found for any prestige levels.
     * @param prestigeLevels The {@link List} of prestige levels.
     * @return A {@link Map} mapping prestige levels to {@link PrestigeConfig}.
     */
    public @NotNull Map<Integer, PrestigeConfig> getPrestigeConfigMap(@NotNull List<Integer> prestigeLevels) {
        Map<Integer, PrestigeConfig> prestigeConfigMap = new HashMap<>();

        for(Integer prestigeLevel : prestigeLevels) {
            PrestigeConfig prestigeConfig = getPrestigeConfig(prestigeLevel);
            if(prestigeConfig == null) {
                logger.warn(AdventureUtil.deserialize("No prestige config found for prestige level: " + prestigeLevel));
                continue;
            }

            prestigeConfigMap.put(prestigeLevel, prestigeConfig);
        }

        return prestigeConfigMap;
    }

    /**
     * Reloads the plugin's prestige configurations.
     */
    public void reload() {
        prestigeConfig.clear();

        saveDefaultConfig();

        try(Stream<Path> paths = Files.walk(Paths.get(plugin.getDataFolder() + File.separator + "prestige"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);
                        try {
                            PrestigeConfig config = yamlConfigurationLoader.load().get(PrestigeConfig.class);
                            if(config != null) {
                                @Nullable PrestigeConfig updatedConfig = updateConfig(path.getFileName().toString(), config);
                                if(updatedConfig != null) {
                                    if(!config.equals(updatedConfig)) {
                                        saveConfig(path, updatedConfig);
                                    }

                                    prestigeConfig.put(updatedConfig.prestigeLevel(), updatedConfig);
                                }
                            } else {
                                logger.error(AdventureUtil.deserialize("Failed to load prestige config file: " + path.getFileName()));
                            }
                        } catch (ConfigurateException e) {
                            logger.error(AdventureUtil.deserialize("Failed to load prestige config file: " + path.getFileName() + ". Error: " + e.getMessage()));
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save the prestige config.
     * @param path The path to save to.
     * @param prestigeConfig The {@link PrestigeConfig} to save.
     */
    public void saveConfig(@NotNull Path path, @NotNull PrestigeConfig prestigeConfig) {
        try {
            @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(PrestigeConfig.class, prestigeConfig);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.deserialize("Failed to save prestige config file: " + path.getFileName() + ". Error: " + e.getMessage()));
        }
    }

    /**
     * Update a {@link PrestigeConfig} to the latest version.
     * @param fileName The name of the file the prestige config was loaded from.
     * @param prestigeConfig The {@link PrestigeConfig} to update.
     * @return The updated {@link PrestigeConfig}.
     */
    private @Nullable PrestigeConfig updateConfig(@NotNull String fileName, @NotNull PrestigeConfig prestigeConfig) {
        switch(prestigeConfig.configVersion()) {
            case "2.0.0.0" -> {
                // Current version, do nothing
                return prestigeConfig;
            }

            case "1.0.0.0" -> {
                logger.error(AdventureUtil.deserialize("Prestige config file " + fileName + " is from version 1.0.0.0 and cannot be migrated."));
                logger.error(AdventureUtil.deserialize("Please regenerate or update your files."));

                return null;
            }

            case null, default -> {
                logger.error(AdventureUtil.deserialize("Prestige config file " + fileName + " version is unrecognized and cannot be migrated."));
                logger.error(AdventureUtil.deserialize("Please regenerate or update your files to version 2.0.0.0."));

                return null;
            }
        }
    }

    /**
     * Save the default prestige config for level 1 if it doesn't exist.
     */
    private void saveDefaultConfig() {
        Path path = Path.of(plugin.getDataFolder() + File.separator + "prestige" + File.separator + "1.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("prestige" + File.separator + "1.yml", false);
        }
    }
}
