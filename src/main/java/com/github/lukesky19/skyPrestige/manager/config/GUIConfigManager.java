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
import com.github.lukesky19.skyPrestige.config.gui.*;
import com.github.lukesky19.skyPrestige.config.gui.button.ButtonConfig;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Manages the plugin's GUI configurations.
 */
public class GUIConfigManager {
    private final @NotNull SkyPrestige skyPrestige;
    private @Nullable ProgressGUIConfig progressGUIConfig;
    private @Nullable BlueprintGUIConfig blueprintGUIConfig;
    private @Nullable ConfirmPrestigeGUIConfig confirmPrestigeGUIConfig;
    private @Nullable RewardsGUIConfig rewardsGUIConfig;
    private @Nullable ExchangeGUIConfig exchangeGUIConfig;
    private @Nullable VaultGUIConfig vaultGUIConfig;

    private final @NotNull Path progressPath;
    private final @NotNull Path blueprintsPath;
    private final @NotNull Path confirmPrestigePath;
    private final @NotNull Path rewardsPath;
    private final @NotNull Path exchangePath;
    private final @NotNull Path vaultPath;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     */
    public GUIConfigManager(@NotNull SkyPrestige skyPrestige) {
        this.skyPrestige = skyPrestige;

        progressPath = Path.of(skyPrestige.getDataFolder() + File.separator + "gui" + File.separator + "progress.yml");
        blueprintsPath = Path.of(skyPrestige.getDataFolder() + File.separator + "gui" + File.separator + "blueprints.yml");
        confirmPrestigePath = Path.of(skyPrestige.getDataFolder() + File.separator + "gui" + File.separator + "confirm_prestige.yml");
        rewardsPath = Path.of(skyPrestige.getDataFolder() + File.separator + "gui" + File.separator + "rewards.yml");
        exchangePath = Path.of(skyPrestige.getDataFolder() + File.separator + "gui" + File.separator + "exchange.yml");
        vaultPath = Path.of(skyPrestige.getDataFolder() + File.separator + "gui" + File.separator + "vault.yml");
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
     * Get the {@link ConfirmPrestigeGUIConfig}. May be null.
     * @return The {@link ConfirmPrestigeGUIConfig} or null.
     */
    public @Nullable ConfirmPrestigeGUIConfig getConfirmPrestigeGUIConfig() {
        return confirmPrestigeGUIConfig;
    }

    /**
     * Get the {@link RewardsGUIConfig}. May be null.
     * @return The {@link RewardsGUIConfig} or null.
     */
    public @Nullable RewardsGUIConfig getRewardsGUIConfig() {
        return rewardsGUIConfig;
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
     * (Re-)load the GUI configurations.
     */
    public void reload() {
        ComponentLogger logger = skyPrestige.getComponentLogger();
        blueprintGUIConfig = null;

        saveDefaultConfig();

        YamlConfigurationLoader progressLoader = ConfigurationUtility.getYamlConfigurationLoader(progressPath);
        YamlConfigurationLoader blueprintsLoader = ConfigurationUtility.getYamlConfigurationLoader(blueprintsPath);
        YamlConfigurationLoader confirmPrestigeLoader = ConfigurationUtility.getYamlConfigurationLoader(confirmPrestigePath);
        YamlConfigurationLoader rewardsLoader = ConfigurationUtility.getYamlConfigurationLoader(rewardsPath);
        YamlConfigurationLoader exchangeLoader = ConfigurationUtility.getYamlConfigurationLoader(exchangePath);
        YamlConfigurationLoader vaultLoader = ConfigurationUtility.getYamlConfigurationLoader(vaultPath);

        try {
            progressGUIConfig = progressLoader.load().get(ProgressGUIConfig.class);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load progress GUI config. Error:" + configurateException.getMessage()));
        }

        try {
            blueprintGUIConfig = blueprintsLoader.load().get(BlueprintGUIConfig.class);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load blueprints GUI config. Error:" + configurateException.getMessage()));
        }

        try {
            confirmPrestigeGUIConfig = confirmPrestigeLoader.load().get(ConfirmPrestigeGUIConfig.class);

            updateConfirmGUIConfig();
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load confirm prestige GUI config. Error:" + configurateException.getMessage()));
        }

        try {
            rewardsGUIConfig = rewardsLoader.load().get(RewardsGUIConfig.class);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load rewards GUI config. Error:" + configurateException.getMessage()));
        }

        try {
            exchangeGUIConfig = exchangeLoader.load().get(ExchangeGUIConfig.class);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load the exchange GUI config. Error:" + configurateException.getMessage()));
        }

        try {
            vaultGUIConfig = vaultLoader.load().get(VaultGUIConfig.class);
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtil.deserialize("Failed to load the vault GUI config. Error:" + configurateException.getMessage()));
        }

    private void updateConfirmGUIConfig() {
        if(confirmPrestigeGUIConfig == null) return;

        switch(confirmPrestigeGUIConfig.configVersion()) {
            case "1.1.0.0" -> {
                // latest version, do nothing
            }

            case "1.0.0.0" -> {
                ConfirmPrestigeGUIConfig.ConditionalButtons conditionalButtons = confirmPrestigeGUIConfig.conditionalButtons();
                ConfirmPrestigeGUIConfig.ConditionalButtons newConditionalButtons = new ConfirmPrestigeGUIConfig.ConditionalButtons(
                        conditionalButtons.keepInventory(),
                        conditionalButtons.clearInventory(),
                        conditionalButtons.keepEnderChest(),
                        conditionalButtons.clearEnderChest(),
                        conditionalButtons.keepExp(),
                        conditionalButtons.resetExp(),
                        conditionalButtons.keepMoney(),
                        conditionalButtons.resetMoney(),
                        new ButtonConfig(
                                new ItemStackConfig(
                                        "orange_shulker_box",
                                        1,
                                        null,
                                        "<white>Auction House Items",
                                        List.of("<gray>Your auction house items will be carried over on prestige."),
                                        null,
                                        null,
                                        List.of(),
                                        new ItemStackConfig.PotionConfig(null, List.of()),
                                        new ItemStackConfig.ColorConfig(false, null, null, null),
                                        null,
                                        List.of(),
                                        new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                        new ItemStackConfig.ArmorTrimConfig(null, null),
                                        List.of(),
                                        new ItemStackConfig.OptionsConfig(null, null, null, null, null)),
                                40),
                        new ButtonConfig(
                                new ItemStackConfig(
                                        "orange_shulker_box",
                                        1,
                                        null,
                                        "<white>Auction House Items",
                                        List.of("<gray>Your auction house items will be reset on prestige."),
                                        null,
                                        null,
                                        List.of(),
                                        new ItemStackConfig.PotionConfig(null, List.of()),
                                        new ItemStackConfig.ColorConfig(false, null, null, null),
                                        null,
                                        List.of(),
                                        new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                        new ItemStackConfig.ArmorTrimConfig(null, null),
                                        List.of(),
                                        new ItemStackConfig.OptionsConfig(null, null, null, null, null)),
                                40),
                        conditionalButtons.startingMoney(),
                        conditionalButtons.noStartingMoney(),
                        conditionalButtons.keepSessionPlayTime(),
                        conditionalButtons.resetSessionPlayTime(),
                        conditionalButtons.keepDailyPlayTime(),
                        conditionalButtons.resetDailyPlayTime(),
                        conditionalButtons.keepWeeklyPlayTime(),
                        conditionalButtons.resetWeeklyPlayTime(),
                        conditionalButtons.keepMonthlyPlayTime(),
                        conditionalButtons.resetMonthlyPlayTime(),
                        conditionalButtons.keepYearlyPlayTime(),
                        conditionalButtons.resetYearlyPlayTime(),
                        conditionalButtons.keepTotalPlayTime(),
                        conditionalButtons.resetTotalPlayTime());

                this.confirmPrestigeGUIConfig = new ConfirmPrestigeGUIConfig(
                        "1.1.0.0",
                        confirmPrestigeGUIConfig.guiName(),
                        confirmPrestigeGUIConfig.guiType(),
                        confirmPrestigeGUIConfig.blueprintBundleSlot(),
                        confirmPrestigeGUIConfig.filler(),
                        confirmPrestigeGUIConfig.confirmButton(),
                        confirmPrestigeGUIConfig.cancelButton(),
                        confirmPrestigeGUIConfig.rewardsButton(),
                        confirmPrestigeGUIConfig.keepMembers(),
                        confirmPrestigeGUIConfig.keepFlags(),
                        confirmPrestigeGUIConfig.keepCommandRanks(),
                        newConditionalButtons,
                        confirmPrestigeGUIConfig.dummyButtons());

                try {
                    @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(confirmPrestigePath);

                    ConfigurationNode node = yamlConfigurationLoader.createNode();

                    node.set(ConfirmPrestigeGUIConfig.class, confirmPrestigeGUIConfig);

                    yamlConfigurationLoader.save(node);
                } catch (ConfigurateException e) {
                    skyPrestige.getComponentLogger().error(AdventureUtil.deserialize("Failed to save confirm prestige gui config file. Error: " + e.getMessage()));
                }
            }

            case null, default -> skyPrestige.getComponentLogger().warn(AdventureUtil.deserialize("Unknown config version for confirm prestige gui config. Unable to update config."));
        }
    }

    /**
     * Save the default config files if they don't exist.
     */
    private void saveDefaultConfig() {
        if(!progressPath.toFile().exists()) {
            skyPrestige.saveResource("gui" + File.separator + "progress.yml", false);
        }
        if(!blueprintsPath.toFile().exists()) {
            skyPrestige.saveResource("gui" + File.separator + "blueprints.yml", false);
        }
        if(!confirmPrestigePath.toFile().exists()) {
            skyPrestige.saveResource("gui" + File.separator + "confirm_prestige.yml", false);
        }
        if(!rewardsPath.toFile().exists()) {
            skyPrestige.saveResource("gui" + File.separator + "rewards.yml", false);
        }
        if(!exchangePath.toFile().exists()) {
            skyPrestige.saveResource("gui" + File.separator + "exchange.yml", false);
        }
        if(!vaultPath.toFile().exists()) {
            skyPrestige.saveResource("gui" + File.separator + "vault.yml", false);
        }
    }
}
