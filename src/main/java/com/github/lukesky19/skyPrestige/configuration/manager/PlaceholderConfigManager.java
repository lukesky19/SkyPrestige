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

import com.github.lukesky19.skyPrestige.configuration.data.common.TimeFormat;
import com.github.lukesky19.skyPrestige.configuration.data.placeholder.PlaceholderConfig;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the plugin's {@link PlaceholderConfig}.
 */
public class PlaceholderConfigManager extends SimpleConfigManager<PlaceholderConfig> {
    private PlaceholderConfig defaultPlaceholderConfig;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     */
    public PlaceholderConfigManager(@NotNull SkyPlugin plugin) {
        super(plugin, Path.of(plugin.getDataFolder() + File.separator + "placeholders.yml"), PlaceholderConfig.class);

        createDefaultPlaceholderConfig();
    }

    /**
     * Gets the plugin's placeholder config if not null or the default placeholder config otherwise.
     * @return The plugin's placeholder config if not null or the default placeholder config otherwise.
     */
    @Override
    public @NotNull PlaceholderConfig getConfiguration() {
        if(configuration == null) return defaultPlaceholderConfig;
        return configuration;
    }

    /**
     * There is no migration required so this just returns the passed config.
     * @param placeholderConfig The configuration to migrate.
     * @return The configuration passed.
     */
    @Override
    public @Nullable PlaceholderConfig migrateConfiguration(@NotNull PlaceholderConfig placeholderConfig) {
        return placeholderConfig;
    }

    @Override
    public boolean validateConfiguration(@Nullable PlaceholderConfig configuration) {
        return configuration != null;
    }

    @Override
    public void saveBundledConfig() {
        plugin.saveResource("placeholders.yml", false);
    }

    /**
     * Creates the default placeholder config.
     * It is created in a separate method so that the method can be minimized.
     */
    private void createDefaultPlaceholderConfig() {
        defaultPlaceholderConfig = new PlaceholderConfig(
                "1.0.0.0",
                new PlaceholderConfig.PrestigeNumberConfig(
                        "0",
                        "0"),
                new PlaceholderConfig.PrestigeNumberConfig(
                        "0",
                        "0"),
                new PlaceholderConfig.PrestigeNumberConfig(
                        "0.0",
                        "0.0"),
                new PlaceholderConfig.ProgressBarConfig(
                        "<gray>No Island Found",
                        "<gray>Island Opted Out",
                        "<green>|",
                        "<red>|"
                ),
                new PlaceholderConfig.ProgressBarConfig(
                        "&7No Island Found",
                        "&7Island Opted Out",
                        "&a|",
                        "&c|"
                ),
                new PlaceholderConfig.MultiplierConfig(
                        "0.0",
                        "0.0",
                        new TimeFormat(
                                "",
                                "<years> year(s)",
                                "<months> month(s)",
                                "<weeks> week(s)",
                                "<days> day(s)",
                                "<hours> hour(s)",
                                "<minutes> minute(s)",
                                "<seconds> second(s)",
                                "")
                )
        );
    }
}