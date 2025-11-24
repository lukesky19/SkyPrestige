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
package com.github.lukesky19.skyPrestige.config.manager.settings;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.settings.Settings;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class manages the plugin's settings.
 */
public class SettingsManager {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private @Nullable Settings settings;
    private final @NotNull List<ItemType> disallowedItems = new ArrayList<>();

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     */
    public SettingsManager(@NotNull SkyPrestige skyPrestige) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
    }

    /**
     * Get the plugin's {@link Settings}. May be null.
     * @return The plugin's {@link Settings} or null.
     */
    public @Nullable Settings getSettings() {
        return settings;
    }

    /**
     * Check if an {@link ItemType} is disallowed inside vaults.
     * @param itemType The {@link ItemType} to check.
     * @return true if disallowed, otherwise false.
     */
    public boolean isItemDisallowed(@NotNull ItemType itemType) {
        return disallowedItems.contains(itemType);
    }

    /**
     * A method to reload the plugin's settings.
     */
    public void reload() {
        ComponentLogger logger = skyPrestige.getComponentLogger();
        settings = null;
        disallowedItems.clear();

        Path path = Path.of(skyPrestige.getDataFolder() + File.separator + "settings.yml");
        if(!path.toFile().exists()) {
            skyPrestige.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            settings = yamlConfigurationLoader.load().get(Settings.class);

            updateSettings(path);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load plugin settings. Error: " + configurateException.getMessage()));
            return;
        }

        if(settings != null) {
            settings.vaultDisallowedItems().forEach(key -> {
                Optional<ItemType> optionalItemType = RegistryUtil.getItemType(logger, key);
                optionalItemType.ifPresent(disallowedItems::add);
            });
        }
    }

    /**
     * Update the settings configuration to the latest version if possible, or display an error.
     * @param path The {@link Path} to save the locale to.
     */
    private void updateSettings(@NotNull Path path) {
        if(settings == null) return;

        switch(settings.configVersion()) {
            case "1.1.0.0" -> {
                // latest version, do nothing
            }

            case "1.0.0.0" -> {
                settings = new Settings(
                        "1.1.0.0",
                        settings.locale(),
                        settings.saveFrequencySeconds(),
                        null,
                        new Settings.IslandResetSettings(
                                false,
                                false,
                                true,
                                Objects.requireNonNullElse(settings.resetPrestigeLevelOnIslandReset(), false)),
                        settings.awardPointsWhileAfk(),
                        settings.scaleFormula(),
                        settings.exchangePrestigeLevel(),
                        settings.fallbackLocation(),
                        settings.vaultDisallowedItems(),
                        settings.prestigePointsMapping());

                saveSettings(path);
            }

            case null, default -> logger.warn(AdventureUtil.deserialize("Unknown config version for settings config. Unable to update config."));
        }
    }

    /**
     * Save the plugin's settings to the path provided.
     * @param path The {@link Path} to save to.
     */
    private void saveSettings(@NotNull Path path) {
        if(settings == null) return;

        try {
            @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(Settings.class, settings);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException e) {
            logger.error(AdventureUtil.deserialize("Failed to save settings config file. Error: " + e.getMessage()));
        }
    }
}
