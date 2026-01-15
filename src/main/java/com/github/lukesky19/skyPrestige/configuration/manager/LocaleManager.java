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

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the plugin's locale.
 */
public class LocaleManager extends SimpleConfigManager<Locale> {
    private final @NotNull SimpleConfigManager<Settings> settingsManager;
    private @NotNull Locale DEFAULT_LOCALE;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public LocaleManager(@NotNull SkyPlugin plugin, @NotNull SimpleConfigManager<Settings> settingsManager) {
        super(plugin, Locale.class);
        this.settingsManager = settingsManager;

        createDefaultLocale();
    }

    /**
     * Gets the plugin's locale if not null or the default locale otherwise.
     * @return The plugin's locale if not null or the default locale otherwise.
     */
    @Override
    public @NotNull Locale getConfiguration() {
        if(configuration == null) return DEFAULT_LOCALE;
        return configuration;
    }

    @Override
    public void loadConfiguration() {
        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.error(AdventureUtil.deserialize("<red>Failed to load plugin's locale due to plugin settings being null.</red>"));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtil.deserialize("<red>Failed to load plugin's locale to use in settings.yml is null.</red>"));
            return;
        }

        String localeString = settings.locale();
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + (localeString + ".yml"));
        setConfigurationPath(path);

        super.loadConfiguration();
    }

    @Override
    public void saveBundledConfig() {
        Path path = Path.of(plugin.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("locale" + File.separator + "en_US.yml", false);
        }
    }

    /**
     * Update the locale configuration to the latest version if possible, or display an error.
     * @param locale The {@link Locale} to migrate.
     * @return The migreated {@link Locale} or null if migration failed.
     */
    @Override
    public @Nullable Locale migrateConfiguration(@NotNull Locale locale) {
        switch(locale.configVersion()) {
            case "2.0.0.0" -> {
                // latest version, do nothing
                return locale;
            }

            case "1.1.0.0", "1.0.0.0" -> {
                logger.warn(AdventureUtil.deserialize("Unable to migrate version 1.0.0.0 or 1.1.0.0 config versions for locale config. Please regenerate or manually migrate your configuration."));
                return null;
            }

            case null, default -> {
                logger.warn(AdventureUtil.deserialize("Unknown config version for locale config. Unable to update config."));
                return null;
            }
        }
    }

    /**
     * Validates if the locale is missing any strings.
     */
    @Override
    public boolean validateConfiguration(@Nullable Locale configuration) {
        if(configuration == null) return false;

        if(configuration.configVersion()  == null
                || configuration.prefix()  == null
                || configuration.reload()  == null
                || configuration.guiOpenError()  == null
                || configuration.islandDataNotFound()  == null
                || configuration.islandPrestigeLevelUpdated()  == null
                || configuration.prestigePlayerOnly()  == null
                || configuration.islandPrestigeLevelMax()  == null
                || configuration.prestigePlayerInWrongWorld()  == null
                || configuration.prestigePlayerNotOnIsland()  == null
                || configuration.prestigeIslandNotOwned()  == null
                || configuration.prestigePlayerNotMemberOrOwner()  == null
                || configuration.prestigeNotEnoughPrestigePoints()  == null
                || configuration.prestigeIslandOptedOut() == null
                || configuration.prestigeConfigError()  == null
                || configuration.prestigeConfigRequirementError()  == null
                || configuration.progressPlayerNotOnIsland()  == null
                || configuration.progressMaxPrestigeLevel()  == null
                || configuration.progressPrestigeExempt() == null
                || configuration.rewardsPlayerNotOnIsland()  == null
                || configuration.rewardsMaxPrestigeLevel()  == null
                || configuration.rewardsPrestigeExempt() == null
                || configuration.exchangePlayerNotOnIsland() == null
                || configuration.exchangePrestigeLevelNotMet() == null
                || configuration.exchangeNotEnoughPrestigePoints() == null
                || configuration.exchangePrestigeExempt() == null
                || configuration.vaultPlayerNotOnIsland() == null
                || configuration.vaultItemNotAllowed() == null
                || configuration.vaultPrestigeExempt() == null
                || configuration.requirementsLevelNotFound() == null
                || configuration.requirementsConfigError() == null
                || configuration.requirementsPointsForLevel() == null
                || configuration.islandExempt() == null
                || configuration.islandUnexempt() == null
                || configuration.islandAlreadyOptedIn() == null
                || configuration.islandAlreadyOptedOut() == null
                || configuration.leaderboardTitle() == null
                || configuration.leaderboardPosition() == null
                || configuration.leaderboardPositionEmpty() == null
                || configuration.protectionOrbNotAllowed() == null
                || configuration.protectionOrbAlreadyProtected() == null
                || configuration.protectionOrbProtected() == null
                || configuration.multiplier().serverMultiplierChangedTimeLimit() == null
                || configuration.multiplier().serverMultiplierChangedNoTimeLimit() == null
                || configuration.multiplier().islandMultiplierChangedTimeLimit() == null
                || configuration.multiplier().islandMultiplierChangedNoTimeLimit() == null
                || configuration.multiplier().serverMultiplierExpiredNotice() == null
                || configuration.multiplier().islandMultiplierExpiredNotice() == null
                || configuration.multiplier().serverMultiplierClearedNotice() == null
                || configuration.multiplier().islandMultiplierClearedNotice() == null
                || configuration.multiplier().serverMultiplierTimeLimit() == null
                || configuration.multiplier().serverMultiplierNoTimeLimit() == null
                || configuration.multiplier().islandMultiplierTimeLimit() == null
                || configuration.multiplier().islandMultiplierNoTimeLimit() == null
                || configuration.multiplier().serverMultiplierCleared() == null
                || configuration.multiplier().islandMultiplierCleared() == null
                || configuration.multiplier().effectiveMultiplier() == null
                || configuration.multiplier().multiplierNotOnIsland() == null
                || isTimeFormatInvalid(configuration.multiplier().multiplierTimePlaceholder())
                || configuration.prestigeStatusPlayerInWrongWorld() == null
                || configuration.prestigeStatusPlayerNotOnIsland() == null
                || configuration.prestigeStatusIslandNotOwned() == null
                || configuration.prestigeStatusPlayerNotMemberOrOwner() == null
                || configuration.delimiter() == null
                || configuration.finalDelimiter() == null) {
            this.configuration = null;

            logger.error(AdventureUtil.deserialize("Your locale is missing one of the plugin's messages. The default locale will be used."));
            logger.info(AdventureUtil.deserialize("You can regenerate your locale file by deleting it or adding the missing messages to resolve the issue."));

            return false;
        }

        return true;
    }

    /**
     * Checks if all {@link String}s in a {@link Locale.TimeFormat} are null.
     * @param timeFormat The {@link Locale.TimeFormat} to check.
     * @return true if invalid, false if not.
     */
    private boolean isTimeFormatInvalid(@NotNull Locale.TimeFormat timeFormat) {
        return timeFormat.prefix() == null
                || timeFormat.years() == null
                || timeFormat.months() == null
                || timeFormat.weeks() == null
                || timeFormat.days() == null
                || timeFormat.hours() == null
                || timeFormat.minutes() == null
                || timeFormat.seconds() == null
                || timeFormat.suffix() == null;
    }

    /**
     * Creates the default locale.
     * It is created in a separate method so that the method can be minimized.
     */
    private void createDefaultLocale() {
        DEFAULT_LOCALE = new Locale(
                "2.0.0.0",
                "<green><bold>SkyPrestige</bold></green><gray> ▪ </gray>",
                List.of(
                        "<green>SkyPrestige is developed by <white><bold>lukeskywlker19</bold></white>.</green>",
                        "<green>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></green>",
                        " ",
                        "<green><bold>List of Commands:</bold></green>",
                        "<white>/</white><aqua>skyprestige</aqua>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>help</yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>reload</yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>progress</yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>rewards</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>values</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>info</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>requirements <level></yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>level set <island_id> <level></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>multiplier event [current | next]</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>multiplier <add | remove | set> <amount></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>multiplier get [additional | event | total]</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>points <add | remove | set> <island_id> <amount></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>points get <island_id></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>exempt <island_id></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>unexempt <island_id></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>leaderboard</yellow>"),
                "<green>The plugin has reloaded successfully.</green>",
                "<red>Unable to open this GUI because of a configuration error.</red>",
                "<red>No island data was found for the island.</red>",
                "<green>Island <island_id> had their prestige level set to <prestige_level>.</green>",
                "<red>Only players are able to prestige islands.</red>",
                "<red>Your island is currently at the max prestige level.</red>",
                "<red>You must be in an island world to prestige.</red>",
                "<red>You must be on your island to prestige it.</red>",
                "<red>You cannot prestige an island that is not owned.</red>",
                "<red>You must be the island owner or an island member to prestige.</red>",
                "<red>You do not have enough prestige points to prestige.</red>",
                "<red>You cannot prestige an island that is opted out of prestige.</red>",
                "<red>Unable to prestige your island due to a configuration error.</red>",
                "<red>Unable to check the required prestige points due to a configuration error.</red>",
                "<red>You must be on your island to view prestige progress.</red>",
                "<green>No progress to view because your island is at the max prestige level.</green>",
                "<red>Your island is opted out of prestige. Prestige progress can only be viewed for islands that can prestige.</red>",
                "<red>You must be on your island to view rewards.</red>",
                "<green>No rewards to view because your island is at the max prestige level.</green>",
                "<red>Your island is opted out of prestige. Prestige progress can only be viewed for islands that can prestige.</red>",
                "<red>You must be on your island to exchange prestige points.</red>",
                "<red>Your island doesn't meet the required prestige level to exchange prestige points.</red>",
                "<red>You do not have enough prestige points to exchange.</red>",
                "<red>Your island is opted out of prestige. Only islands that can prestige can exchange prestige points.</red>",
                "<red>You must be on your island to view the island vault.</red>",
                "<red>The item you clicked is not allowed to be placed inside the vault.</red>",
                "<red>Your island is opted out of prestige. The vault can only be used by islands opted into prestige.</red>",
                "<red>The level provided is not a prestige level.</red>",
                "<red>Unable to view prestige level requirements due to a configuration error.</red>",
                "<green>Prestige level <prestige_level> requires <prestige_points> prestige points.</green>",
                "<green>Island <yellow><island_id></yellow> is now exempt from top placeholders.</green>",
                "<green>Island <yellow><island_id></yellow> is now unexempt from top placeholders.</green>",
                "<red>Your island is already opted into prestige.</red>",
                "<red>Your island is already opted out of prestige.</red>",
                "<green><bold>Top 10 Islands By Prestige Level and Points</bold></green>",
                "<gray>[</gray><aqua><position></aqua><gray>]</gray> <yellow><player_name></yellow> <white>Level:</white> <aqua><prestige_level></aqua> <white>Points:</white> <aqua><prestige_points></aqua>",
                "<gray>[</gray><aqua><position></aqua><gray>] ----------</gray>",
                "<red>This item can not be protected by a protection orb.</red>",
                "<red>This item is already protected by a protection orb.</red>",
                "<green>This item is now protected and will not be removed on prestige.</green>",
                new Locale.MultiplierMessages(
                        "<green>The prestige points server multiplier is now <aqua><multiplier></aqua> with <time> remaining.</green>",
                        "<green>The prestige points server multiplier is now <aqua><multiplier></aqua> with no time limit.</green>",
                        "<green>Your island's prestige points multiplier is now <aqua><multiplier></aqua> with <time> remaining.</green>",
                        "<green>Your island's prestige points multiplier is now <aqua><multiplier></aqua> with no time limit.</green>",
                        "<green>The server's prestige points multiplier has expired.</green>",
                        "<green>Your island's prestige points multiplier has expired.</green>",
                        "<green>The server's prestige points multiplier has been cleared.</green>",
                        "<green>Your island's prestige points multiplier has been cleared.</green>",
                        "<green>The prestige points server multiplier is <aqua><multiplier></aqua> with <time> remaining.</green>",
                        "<green>The prestige points server multiplier is <aqua><multiplier></aqua> with no time limit.</green>",
                        "<green>The prestige points island multiplier is <aqua><multiplier></aqua> with <time> remaining.</green>",
                        "<green>The prestige points island multiplier is <aqua><multiplier></aqua> with no time limit.</green>",
                        "<green>The server's prestige points multiplier has been cleared.</green>",
                        "<green>The island's prestige points multiplier has been cleared.</green>",
                        "<green>Your prestige points effective multiplier is <aqua><multiplier></aqua>.</green>",
                        "<red>You must be on an island to view the multiplier.</red>",
                        new Locale.TimeFormat(
                                "",
                                "<aqua><years></aqua> year(s)",
                                "<aqua><months></aqua> month(s)",
                                "<aqua><weeks></aqua> week(s)",
                                "<aqua><days></aqua> day(s)",
                                "<aqua><hours></aqua> hour(s)",
                                "<aqua><minutes></aqua> minute(s)",
                                "<aqua><seconds></aqua> second(s)",
                                "")
                ),
                "<red>You must be in an island world to opt in or out of prestige.</red>",
                "<red>You must be on your island to opt in or out of prestige.</red>",
                "<red>You cannot opt in our out of prestige for an island that is not owned.</red>",
                "<red>You must be the island owner or an island member to opt in or out of prestige.</red>",
                ", ",
                ", and ");
    }
}