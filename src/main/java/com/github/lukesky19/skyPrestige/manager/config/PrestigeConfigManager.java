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
import java.util.Map;
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
     * Reloads the plugin's prestige configurations.
     */
    public void reload() {
        ComponentLogger logger = skyPrestige.getComponentLogger();
        prestigeConfig.clear();

        saveDefaultConfig();

        try(Stream<Path> paths = Files.walk(Paths.get(skyPrestige.getDataFolder() + File.separator + "prestige"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        @NotNull YamlConfigurationLoader Manager = ConfigurationUtility.getYamlConfigurationLoader(path);
                        try {
                            PrestigeConfig config = Manager.load().get(PrestigeConfig.class);
                            if (config != null) {
                                prestigeConfig.put(config.prestigeLevel(), config);
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
     * Save the default prestige config for level 1 if it doesn't exist.
     */
    private void saveDefaultConfig() {
        Path path = Path.of(skyPrestige.getDataFolder() + File.separator + "prestige" + File.separator + "1.yml");
        if(!path.toFile().exists()) {
            skyPrestige.saveResource("prestige" + File.separator + "1.yml", false);
        }
    }
}
