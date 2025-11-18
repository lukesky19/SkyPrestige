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
package com.github.lukesky19.skyPrestige.manager.config;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.PrestigeConfig;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the configuration for prestige levels.
 */
public class PrestigeConfigManager {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull Map<Integer, PrestigeConfig> prestigeConfig = new HashMap<>();

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     */
    public PrestigeConfigManager(@NotNull SkyPrestige skyPrestige) {
        this.skyPrestige = skyPrestige;
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
     * Reloads the plugin's prestige configurations.
     */
    public void reload() {
        ComponentLogger logger = skyPrestige.getComponentLogger();
        prestigeConfig.clear();

        saveDefaultConfig();

        try(Stream<Path> paths = Files.walk(Paths.get(skyPrestige.getDataFolder() + File.separator + "prestige"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);
                        try {
                            PrestigeConfig config = yamlConfigurationLoader.load().get(PrestigeConfig.class);
                            if(config != null) {
                                PrestigeConfig updatedConfig = updateConfig(config);

                                if(!config.equals(updatedConfig)) {
                                    System.out.println("Config was migrated for level: " + updatedConfig.prestigeLevel());

                                    saveConfig(path, updatedConfig);
                                }

                                prestigeConfig.put(updatedConfig.prestigeLevel(), updatedConfig);
                            } else {
                                logger.error(AdventureUtil.serialize("Failed to load prestige config file: " + path.getFileName()));
                            }
                        } catch (ConfigurateException e) {
                            logger.error(AdventureUtil.serialize("Failed to load prestige config file: " + path.getFileName() + ". Error: " + e.getMessage()));
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
            skyPrestige.getComponentLogger().error(AdventureUtil.serialize("Failed to save prestige config file: " + path.getFileName() + ". Error: " + e.getMessage()));
        }
    }

    private @NotNull PrestigeConfig updateConfig(@NotNull PrestigeConfig prestigeConfig) {
        switch(prestigeConfig.configVersion()) {
            case "1.1.0.0" -> {
                // Current version, do nothing
                return prestigeConfig;
            }

            case "1.0.0.0" -> {
                PrestigeConfig.PrestigeSettings prestigeSettings = prestigeConfig.prestigeSettings();
                PrestigeConfig.PrestigeSettings newSettings = new PrestigeConfig.PrestigeSettings(
                        null,
                        new PrestigeConfig.InventorySettings(Objects.requireNonNullElse(prestigeSettings.resetInventory(), true), false),
                        prestigeSettings.resetEnderChest(),
                        prestigeSettings.resetExp(),
                        prestigeSettings.resetMoney(),
                        true,
                        prestigeSettings.giveStartingMoneyToAllIslandMembers(),
                        prestigeSettings.startingMoney(),
                        prestigeSettings.playTimeSettings(),
                        prestigeSettings.resetPrestigePoints());

                return new PrestigeConfig(
                        "1.1.0.0",
                        prestigeConfig.prestigeLevel(),
                        prestigeConfig.scaleFactor(),
                        newSettings,
                        prestigeConfig.requiredPrestigePoints(),
                        prestigeConfig.rewards());
            }

            case null -> {
                return prestigeConfig;
            }

            default -> {
                skyPrestige.getComponentLogger().warn(AdventureUtil.serialize("Unknown config version for prestige config. Unable to update config."));
                return prestigeConfig;
            }
        }
    }

    /**
     * Save the default prestige config for level 1 if it doesn't exist.
     */
    private void saveDefaultConfig() {
        Path path = Path.of(skyPrestige.getDataFolder() + File.separator + "prestige" + File.separator + "1.yml");
        if(!path.toFile().exists()) {
            skyPrestige.saveResource("prestige" + File.separator + "1.yml", false);
        }
    }
}
