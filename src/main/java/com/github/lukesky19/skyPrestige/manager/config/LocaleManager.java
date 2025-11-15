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
import com.github.lukesky19.skyPrestige.config.Locale;
import com.github.lukesky19.skyPrestige.config.Settings;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
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
 * This class manages the plugin's locale.
 */
public class LocaleManager {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull SettingsManager settingsManager;
    private @Nullable Locale locale;
    private @NotNull Locale DEFAULT_LOCALE;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public LocaleManager(@NotNull SkyPrestige skyPrestige, @NotNull SettingsManager settingsManager) {
        this.skyPrestige = skyPrestige;
        this.settingsManager = settingsManager;

        createDefaultLocale();
    }

    /**
     * Gets the plugin's locale if not null or the default locale otherwise.
     * @return The plugin's locale if not null or the default locale otherwise.
     */
    public @NotNull Locale getLocale() {
        if(locale == null) return DEFAULT_LOCALE;
        return locale;
    }

    /**
     * Reloads the plugin's locale.
     */
    public void reload() {
        ComponentLogger logger = skyPrestige.getComponentLogger();
        locale = null;

        copyDefaultLocales();

        Settings settings = settingsManager.getSettings();
        if(settings == null) {
            logger.error(AdventureUtil.serialize("<red>Failed to load plugin's locale due to plugin settings being null.</red>"));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtil.serialize("<red>Failed to load plugin's locale to use in settings.yml is null.</red>"));
            return;
        }

        String localeString = settings.locale();
        Path path = Path.of(skyPrestige.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));

        YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            locale = yamlConfigurationLoader.load().get(Locale.class);

            updateLocale(path);
        } catch (ConfigurateException exception) {
            throw new RuntimeException(exception);
        }

        validateLocale();
    }

    /**
     * Copies the default locale files that come bundled with the plugin, if they do not exist at least.
     */
    private void copyDefaultLocales() {
        Path path = Path.of(skyPrestige.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            skyPrestige.saveResource("locale" + File.separator + "en_US.yml", false);
    private void updateLocale(@NotNull Path path) {
        if(locale == null) return;

        switch(locale.configVersion()) {
            case "1.1.0.0" -> {
                // latest version, do nothing
            }

            case "1.0.0.0" -> {
                locale = new Locale(
                        "1.1.0.0",
                        locale.prefix(),
                        locale.help(),
                        locale.reload(),
                        locale.guiOpenError(),
                        locale.islandNotFound(),
                        locale.islandDataNotFound(),
                        locale.islandPrestigeLevelUpdated(),
                        locale.prestigePlayerOnly(),
                        locale.islandPrestigeLevelMax(),
                        locale.prestigePlayerInWrongWorld(),
                        locale.prestigePlayerNotOnIsland(),
                        locale.prestigeIslandNotOwned(),
                        locale.prestigePlayerNotMemberOrOwner(),
                        locale.prestigeNotEnoughPrestigePoints(),
                        locale.prestigeInventoryReset(),
                        locale.prestigeEnderChestReset(),
                        locale.prestigeExperienceReset(),
                        locale.prestigeBalanceReset(),
                        "<yellow>Your auction house items have been cleared as part of your island being prestiged.",
                        locale.prestigeStartingMoneyGiven(),
                        locale.islandMemberPrestigeNotice(),
                        locale.otherPrestigeNotice(),
                        locale.islandMemberIslandTeleportNotice(),
                        locale.islandMemberFallbackTeleportNotice(),
                        locale.otherIslandTeleportNotice(),
                        locale.otherFallbackTeleportNotice(),
                        locale.prestigeConfigError(),
                        locale.prestigeConfigRequirementError(),
                        locale.progressPlayerNotOnIsland(),
                        locale.progressMaxPrestigeLevel(),
                        locale.rewardsPlayerNotOnIsland(),
                        locale.rewardsMaxPrestigeLevel(),
                        locale.exchangePlayerNotOnIsland(),
                        locale.exchangePrestigeLevelNotMet(),
                        locale.exchangeNotEnoughPrestigePoints(),
                        locale.vaultPlayerNotOnIsland(),
                        locale.vaultItemNotAllowed(),
                        "<red>The level provided is not a prestige level.</red>",
                        "<red>Unable to view prestige level requirements due to a configuration error.</red>",
                        "<green>Prestige level <prestige_level> requires <prestige_points> prestige points.</green>",
                        locale.delimiter(),
                        locale.finalDelimiter());

                saveLocale(path);
            }

            case null, default -> skyPrestige.getComponentLogger().warn(AdventureUtil.serialize("Unknown config version for locale config. Unable to update config."));
        }
    }

    private void saveLocale(@NotNull Path path) {
        if(locale == null) return;

        try {
            @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(Locale.class, locale);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException e) {
            skyPrestige.getComponentLogger().error(AdventureUtil.serialize("Failed to save locale config file. Error: " + e.getMessage()));
        }
    }

    /**
     * Validates if the locale is missing any strings.
     */
    private void validateLocale() {
        if(locale == null) return;

        if(locale.configVersion()  == null
                || locale.prefix()  == null
                || locale.reload()  == null
                || locale.guiOpenError()  == null
                || locale.islandNotFound()  == null
                || locale.islandDataNotFound()  == null
                || locale.islandPrestigeLevelUpdated()  == null
                || locale.prestigePlayerOnly()  == null
                || locale.islandPrestigeLevelMax()  == null
                || locale.prestigePlayerInWrongWorld()  == null
                || locale.prestigePlayerNotOnIsland()  == null
                || locale.prestigeIslandNotOwned()  == null
                || locale.prestigePlayerNotMemberOrOwner()  == null
                || locale.prestigeNotEnoughPrestigePoints()  == null
                || locale.prestigeInventoryReset()  == null
                || locale.prestigeEnderChestReset()  == null
                || locale.prestigeExperienceReset()  == null
                || locale.prestigeBalanceReset()  == null
                || locale.prestigeAuctionHouseItemsReset() == null
                || locale.prestigeStartingMoneyGiven()  == null
                || locale.islandMemberPrestigeNotice()  == null
                || locale.otherPrestigeNotice()  == null
                || locale.islandMemberIslandTeleportNotice()  == null
                || locale.islandMemberFallbackTeleportNotice()  == null
                || locale.otherIslandTeleportNotice()  == null
                || locale.otherFallbackTeleportNotice()  == null
                || locale.prestigeConfigError()  == null
                || locale.prestigeConfigRequirementError()  == null
                || locale.progressPlayerNotOnIsland()  == null
                || locale.progressMaxPrestigeLevel()  == null
                || locale.rewardsPlayerNotOnIsland()  == null
                || locale.rewardsMaxPrestigeLevel()  == null
                || locale.requirementsLevelNotFound() == null
                || locale.requirementsConfigError() == null
                || locale.requirementsPointsForLevel() == null
                || locale.delimiter()  == null
                || locale.finalDelimiter() == null) {
            locale = null;

            ComponentLogger logger = skyPrestige.getComponentLogger();
            logger.error(AdventureUtil.serialize("Your locale is missing one of the plugin's messages. The default locale will be used."));
            logger.info(AdventureUtil.serialize("You can regenerate your locale file by deleting it or adding the missing messages to resolve the issue."));
        }
    }

    /**
     * Copies the default locale files that come bundled with the plugin, if they do not exist at least.
     */
    private void copyDefaultLocales() {
        Path path = Path.of(skyPrestige.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if (!path.toFile().exists()) {
            skyPrestige.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    /**
     * Creates the default locale.
     * It is created in a separate method so that the method can be minimized.
     */
    private void createDefaultLocale() {
        DEFAULT_LOCALE = new Locale(
                "1.1.0.0",
                "<green><bold>SkyPrestige</bold></green><gray> ▪ </gray>",
                List.of(
                        "<green>SkyPrestige is developed by <white><bold>lukeskywlker19</bold></white>.</green>",
                        "<green>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></green>",
                        " ",
                        "<green><bold>List of Commands:</bold></green>",
                        "<white>/</white><aqua>skyprestige</aqua>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>help</yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>reload</yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>requirements</yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>rewards</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>requirements <level></yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>level set <island_id> <level></yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>points add <island_id> <amount></yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>points remove <island_id> <amount></yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>points set <island_id> <amount></yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>points get <island_id> <amount></yellow>"),
                "<green>The plugin has reloaded successfully.</green>",
                "<red>Unable to open this GUI because of a configuration error.</red>",
                "<red>No island found for the island id <island_id>.</red>",
                "<red>No island data was found for the island.</red>",
                "<green>Island <island_id> had their prestige level set to <prestige_level>.</green>",
                "<red>Only players are able to prestige islands.</red>",
                "<red>Your island is currently at the max prestige level.</red>",
                "<red>You must be in an island world to prestige.</red>",
                "<red>You must be on your island to prestige it.</red>",
                "<red>You cannot prestige an island that is not owned.</red>",
                "<red>You must be the island owner or an island member to prestige.</red>",
                "<red>You do not have enough presitge points to prestige.</red>",
                "<yellow>Your inventory has been reset as part of your island being prestiged.</yellow>",
                "<yellow>Your ender chest has been reset as part of your island being prestiged.</yellow>",
                "<yellow>Your experience has been reset as part of your island being prestiged.</yellow>",
                "<yellow>Your balance has been reset as part of your island being prestiged.</yellow>",
                "<yellow>Your auction house items have been cleared as part of your island being prestiged.",
                "<yellow>You received <money> to start your new island with.</yellow>",
                "<green>Your island has been prestiged.</green>",
                "<green>An island your are cooped or trusted on was prestiged.</green>",
                "<green>Your island was prestiged. Teleporting you to your new island...</green>",
                "<yellow>Your island was prestiged, but your spawn point could not be found. Teleporting you to the fallback location.</yellow>",
                "<green>The island you were on was prestiged. Teleporting you to the new island...</green>",
                "<yellow>The island you were on was prestiged, but the spawn point could not be found. Teleporting you to the fallback location.</yellow>",
                "<red>Unable to prestige your island due to a configuration error.</red>",
                "<red>Unable to check the required prestige points due to a configuration error.</red>",
                "<red>You must be on your island to view prestige requirements.</red>",
                "<green>No requirements to view because your island is at the max prestige level.</green>",
                "<red>You must be on your island to view prestige rewards.</red>",
                "<green>No rewards to view because your island is at the max prestige level.</green>",
                "<red>You must be on your island to exchange prestige points.</red>",
                "<red>Your island doesn't meet the required prestige level to exchange prestige points.</red>",
                "<red>You do not have enough prestige points to exchange.</red>",
                "<red>You must be on your island to view the island vault.</red>",
                "<red>The item you clicked is not allowed to be placed inside the vault.</red>",
                "<red>The level provided is not a prestige level.</red>",
                "<red>Unable to view prestige level requirements due to a configuration error.</red>",
                "<green>Prestige level <prestige_level> requires <prestige_points> prestige points.</green>",
                ", ",
                ", and ");
    }
}
