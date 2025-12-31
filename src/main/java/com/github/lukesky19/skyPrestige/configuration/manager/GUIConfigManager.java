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

import com.github.lukesky19.skyPrestige.configuration.data.gui.*;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * Manages the plugin's GUI configurations.
 */
public class GUIConfigManager {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;

    private @Nullable ProgressGUIConfig progressGUIConfig;
    private @Nullable BlueprintGUIConfig blueprintGUIConfig;
    private @Nullable ExchangeGUIConfig exchangeGUIConfig;
    private @Nullable VaultGUIConfig vaultGUIConfig;
    private @Nullable ValuesGUIConfig valuesGUIConfig;
    private @Nullable InfoGUIConfig infoGUIConfig;

    private @Nullable RewardsGUIConfig prestigeRewardsGUIConfig;
    private @Nullable RewardsGUIConfig optInRewardsGUIConfig;
    private @Nullable RewardsGUIConfig optOutRewardsGUIConfig;

    private @Nullable ConfirmGUIConfig confirmPrestigeGUIConfig;
    private @Nullable ConfirmGUIConfig confirmOptInGUIConfig;
    private @Nullable ConfirmGUIConfig confirmOptOutGUIConfig;

    private final @NotNull Path progressPath;
    private final @NotNull Path blueprintsPath;
    private final @NotNull Path exchangePath;
    private final @NotNull Path vaultPath;
    private final @NotNull Path valuesPath;
    private final @NotNull Path infoPath;

    private final @NotNull Path prestigeRewardsPath;
    private final @NotNull Path optInRewardsPath;
    private final @NotNull Path optOutRewardsPath;

    private final @NotNull Path confirmPrestigePath;
    private final @NotNull Path confirmOptInPath;
    private final @NotNull Path confirmOptOutPath;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     */
    public GUIConfigManager(@NotNull SkyPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();

        progressPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "progress.yml");
        blueprintsPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "blueprints.yml");
        exchangePath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "exchange.yml");
        vaultPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "vault.yml");
        valuesPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "values.yml");
        infoPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "info.yml");

        prestigeRewardsPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "prestige_rewards.yml");
        optInRewardsPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "opt_in_rewards.yml");
        optOutRewardsPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "opt_out_rewards.yml");

        confirmPrestigePath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "confirm_prestige.yml");
        confirmOptInPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "confirm_opt_in.yml");
        confirmOptOutPath = Path.of(plugin.getDataFolder() + File.separator + "gui" + File.separator + "confirm_opt_out.yml");
    }

    /**
     * Get the {@link ProgressGUIConfig}. May be null.
     * @return The {@link ProgressGUIConfig} or null.
     */
    public @Nullable ProgressGUIConfig getProgressGUIConfig() {
        return progressGUIConfig;
    }

    /**
     * Get the {@link BlueprintGUIConfig}. May be null.
     * @return The {@link BlueprintGUIConfig} or null.
     */
    public @Nullable BlueprintGUIConfig getBlueprintGUIConfig() {
        return blueprintGUIConfig;
    }

    /**
     * Get the {@link ExchangeGUIConfig}. May be null.
     * @return The {@link ExchangeGUIConfig} or null.
     */
    public @Nullable ExchangeGUIConfig getExchangeGUIConfig() {
        return exchangeGUIConfig;
    }

    /**
     * Get the {@link VaultGUIConfig}. May be null.
     * @return The {@link VaultGUIConfig} or null.
     */
    public @Nullable VaultGUIConfig getVaultGUIConfig() {
        return vaultGUIConfig;
    }

    /**
     * Get the {@link ValuesGUIConfig}. May be null.
     * @return The {@link ValuesGUIConfig} or null.
     */
    public @Nullable ValuesGUIConfig getValuesGUIConfig() {
        return valuesGUIConfig;
    }

    /**
     * Get the {@link InfoGUIConfig}. May be null.
     * @return The {@link InfoGUIConfig} or null.
     */
    public @Nullable InfoGUIConfig getInfoGUIConfig() {
        return infoGUIConfig;
    }

    /**
     * Get the {@link RewardsGUIConfig} for prestige. May be null.
     * @return The {@link RewardsGUIConfig} or null.
     */
    public @Nullable RewardsGUIConfig getPrestigeRewardsGUIConfig() {
        return prestigeRewardsGUIConfig;
    }

    /**
     * Get the {@link RewardsGUIConfig} for opt in. May be null.
     * @return The {@link RewardsGUIConfig} or null.
     */
    public @Nullable RewardsGUIConfig getOptInRewardsGUIConfig() {
        return optInRewardsGUIConfig;
    }

    /**
     * Get the {@link RewardsGUIConfig} for opt out. May be null.
     * @return The {@link RewardsGUIConfig} or null.
     */
    public @Nullable RewardsGUIConfig getOptOutRewardsGUIConfig() {
        return optOutRewardsGUIConfig;
    }

    /**
     * Get the {@link ConfirmGUIConfig} for prestige confirmation. May be null.
     * @return The {@link ConfirmGUIConfig} or null.
     */
    public @Nullable ConfirmGUIConfig getConfirmPrestigeGUIConfig() {
        return confirmPrestigeGUIConfig;
    }

    /**
     * Get the {@link ConfirmGUIConfig} for opt-in. May be null.
     * @return The {@link ConfirmGUIConfig} or null.
     */
    public @Nullable ConfirmGUIConfig getConfirmOptInGUIConfig() {
        return confirmOptInGUIConfig;
    }

    /**
     * Get the {@link ConfirmGUIConfig} for opt-out. May be null.
     * @return The {@link ConfirmGUIConfig} or null.
     */
    public @Nullable ConfirmGUIConfig getConfirmOptOutGUIConfig() {
        return confirmOptOutGUIConfig;
    }

    /**
     * (Re-)load the GUI configurations.
     */
    public void reload() {
        progressGUIConfig = null;
        blueprintGUIConfig = null;
        exchangeGUIConfig = null;
        vaultGUIConfig = null;
        valuesGUIConfig = null;
        infoGUIConfig = null;

        prestigeRewardsGUIConfig = null;
        optInRewardsGUIConfig = null;
        optOutRewardsGUIConfig = null;

        confirmPrestigeGUIConfig = null;
        confirmOptInGUIConfig = null;
        confirmOptOutGUIConfig = null;

        saveDefaultConfig();

        progressGUIConfig = loadConfiguration(progressPath, ProgressGUIConfig.class);
        blueprintGUIConfig = loadConfiguration(blueprintsPath, BlueprintGUIConfig.class);
        exchangeGUIConfig = loadConfiguration(exchangePath, ExchangeGUIConfig.class);
        vaultGUIConfig = loadConfiguration(vaultPath, VaultGUIConfig.class);
        valuesGUIConfig = loadConfiguration(valuesPath, ValuesGUIConfig.class);
        infoGUIConfig = loadConfiguration(infoPath, InfoGUIConfig.class);

        prestigeRewardsGUIConfig = loadConfiguration(prestigeRewardsPath, RewardsGUIConfig.class);
        optInRewardsGUIConfig = loadConfiguration(confirmOptInPath, RewardsGUIConfig.class);
        optOutRewardsGUIConfig = loadConfiguration(optOutRewardsPath, RewardsGUIConfig.class);

        confirmPrestigeGUIConfig = loadConfiguration(confirmPrestigePath, ConfirmGUIConfig.class);
        confirmOptInGUIConfig = loadConfiguration(confirmOptInPath, ConfirmGUIConfig.class);
        confirmOptOutGUIConfig = loadConfiguration(confirmOptOutPath, ConfirmGUIConfig.class);
    }

    /**
     * Load the configuration.
     * @param path The path to load the config for.
     * @param clazz The class to load configuration to.
     * @return The configuration or null.
     * @param <T> The class created for the configuration.
     */
    private <T> @Nullable T loadConfiguration(@NotNull Path path, @NotNull Class<T> clazz) {
        YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);

        try {
            return loader.load().get(clazz);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Unable to load GUI config for record " + clazz.getName() + ". Error: " + configurateException.getMessage()));
            return null;
        }
    }

    /**
     * Save the default config files if they don't exist.
     */
    private void saveDefaultConfig() {
        if(!progressPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "progress.yml", false);
        }
        if(!blueprintsPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "blueprints.yml", false);
        }
        if(!exchangePath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "exchange.yml", false);
        }
        if(!vaultPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "vault.yml", false);
        }
        if(!valuesPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "values.yml", false);
        }
        if(!infoPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "info.yml", false);
        }

        if(!prestigeRewardsPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "prestige_rewards.yml", false);
        }
        if(!optInRewardsPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "opt_in_rewards.yml", false);
        }
        if(!optOutRewardsPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "opt_out_rewards.yml", false);
        }

        if(!confirmPrestigePath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "confirm_prestige.yml", false);
        }
        if(!confirmOptInPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "confirm_opt_in.yml", false);
        }
        if(!confirmOptOutPath.toFile().exists()) {
            plugin.saveResource("gui" + File.separator + "confirm_opt_out.yml", false);
        }
    }
}
