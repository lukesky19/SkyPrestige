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
    private @Nullable Locale locale;
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
        if(locale == null) return DEFAULT_LOCALE;
        return locale;
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
    public @Nullable Locale migrateConfiguration(@NotNull Locale locale) {
        switch(locale.configVersion()) {
            case "1.1.0.0" -> {
                // latest version, do nothing
                return locale;
            }

            case "1.0.0.0" -> {
                List<String> help = locale.help();
                help.add("<white>/</white><green>skyprestige</green> <yellow>values</yellow>");
                help.add("<white>/</white><green>skyprestige</green> <yellow>info</yellow>");
                help.add("<white>/</white><green>skyprestige</green> <yellow>requirements <level></yellow>");
                help.add("<white>/</white><green>skyprestige</green> <yellow>exempt <island_id></yellow>");
                help.add("<white>/</white><green>skyprestige</green> <yellow>unexempt <island_id></yellow>");
                help.add("<white>/</white><green>skyprestige</green> <yellow>leaderboard</yellow>");
                help.add("<white>/</white><green>skyprestige</green> <yellow>multiplier event</yellow>");
                help.add("<white>/</white><green>skyprestige</green> <yellow>multiplier <add | remove | set> <amount></yellow>");
                help.add("<white>/</white><green>skyprestige</green> <yellow>multiplier get [additional | event | total]</yellow>");

                return new Locale(
                        "1.1.0.0",
                        locale.prefix(),
                        help,
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
                        "<green>Island <yellow><island_id></yellow> is now exempt from top placeholders.</green>",
                        "<green>Island <yellow><island_id></yellow> is now unexempt from top placeholders.</green>",
                        "<green><bold>Top 10 Islands By Prestige Level and Points</bold></green>",
                        "<gray>[</gray><aqua><position></aqua><gray>]</gray> <yellow><player_name></yellow> <white>Level:</white> <aqua><prestige_level></aqua> <white>Points:</white> <aqua><prestige_points></aqua>",
                        "<gray>[</gray><aqua><position></aqua><gray>] ----------</gray>",
                        "<red>This item can not be protected by a protection orb.</red>",
                        "<red>This item is already protected by a protection orb.</red>",
                        "<green>This item is now protected and will not be removed on prestige.</green>",
                        "<green>The current additional multiplier is <aqua><additional_multiplier></aqua>.</green>",
                        "<green>The current event multiplier is <aqua><event_multiplier></aqua>.</green>",
                        "<green>The current total multiplier is <aqua><total_multiplier></aqua>.</green>",
                        "<green>The prestige points multiplier is now <aqua><current_multiplier></aqua>.</green>",
                        "<green>A <aqua><event_multiplier>x</aqua> prestige points event has now started. The total multiplier is now <aqua><current_multiplier></aqua>.</green>",
                        "<green>The <aqua><event_multiplier>x</aqua> prestige points event has ended. The total multiplier is now <aqua><current_multiplier></aqua>.</green>",
                        "<green>There is <time> left until the <aqua><event_multiplier>x</aqua> prestige points event ends. The total multiplier is <aqua><current_multiplier></aqua>.</green>",
                        "<green>The next <aqua><event_multiplier>x</aqua> prestige points event starts in <time>.</green>",
                        "<green>There is no prestige points multiplier event active. There is no next event scheduled.</green>",
                        new Locale.TimeFormat(
                                "",
                                "<aqua><years></aqua> year(s)",
                                "<aqua><months></aqua> month(s)",
                                "<aqua><weeks></aqua> week(s)",
                                "<aqua><days></aqua> day(s)",
                                "<aqua><hours></aqua> hour(s)",
                                "<aqua><minutes></aqua> minute(s)",
                                "<aqua><seconds></aqua> second(s)",
                                ""),
                        locale.delimiter(),
                        locale.finalDelimiter());
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
    public boolean validateConfiguration() {
        if(locale == null) return false;

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
                || locale.vaultItemNotAllowed() == null
                || locale.vaultPlayerNotOnIsland() == null
                || locale.requirementsLevelNotFound() == null
                || locale.requirementsConfigError() == null
                || locale.requirementsPointsForLevel() == null
                || locale.islandExempt() == null
                || locale.islandUnexempt() == null
                || locale.leaderboardTitle() == null
                || locale.leaderboardPosition() == null
                || locale.leaderboardPositionEmpty() == null
                || locale.protectionOrbNotAllowed() == null
                || locale.protectionOrbAlreadyProtected() == null
                || locale.protectionOrbProtected() == null
                || locale.additionalMultiplierGet() == null
                || locale.eventMultiplierGet() == null
                || locale.totalMultiplierGet() == null
                || locale.multiplierChanged() == null
                || locale.multiplierEventStarted() == null
                || locale.multiplierEventEnded() == null
                || locale.multiplierEventRemainingTime() == null
                || locale.multiplierEventNextTime() == null
                || locale.multiplierEventDisabled() == null
                || isTimeFormatInvalid(locale.multiplierTimePlaceholder())
                || locale.delimiter()  == null
                || locale.finalDelimiter() == null) {
            locale = null;

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
                "<red>No island found for the island id <island_id>.</red>",
                "<red>No island data was found for the island.</red>",
                "<green>Island <island_id> had their prestige level set to <prestige_level>.</green>",
                "<red>Only players are able to prestige islands.</red>",
                "<red>Your island is currently at the max prestige level.</red>",
                "<red>You must be in an island world to prestige.</red>",
                "<red>You must be on your island to prestige it.</red>",
                "<red>You cannot prestige an island that is not owned.</red>",
                "<red>You must be the island owner or an island member to prestige.</red>",
                "<red>You do not have enough prestige points to prestige.</red>",
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
                "<green>Island <yellow><island_id></yellow> is now exempt from top placeholders.</green>",
                "<green>Island <yellow><island_id></yellow> is now unexempt from top placeholders.</green>",
                "<green><bold>Top 10 Islands By Prestige Level and Points</bold></green>",
                "<gray>[</gray><aqua><position></aqua><gray>]</gray> <yellow><player_name></yellow> <white>Level:</white> <aqua><prestige_level></aqua> <white>Points:</white> <aqua><prestige_points></aqua>",
                "<gray>[</gray><aqua><position></aqua><gray>] ----------</gray>",
                "<red>This item can not be protected by a protection orb.</red>",
                "<red>This item is already protected by a protection orb.</red>",
                "<green>This item is now protected and will not be removed on prestige.</green>",
                "<green>The current additional multiplier is <aqua><additional_multiplier></aqua>.</green>",
                "<green>The current event multiplier is <aqua><event_multiplier></aqua>.</green>",
                "<green>The current total multiplier is <aqua><total_multiplier></aqua>.</green>",
                "<green>The prestige points multiplier is now <aqua><current_multiplier></aqua>.</green>",
                "<green>A <aqua><event_multiplier>x</aqua> prestige points event has now started. The total multiplier is now <aqua><current_multiplier></aqua>.</green>",
                "<green>The <aqua><event_multiplier>x</aqua> prestige points event has ended. The total multiplier is now <aqua><current_multiplier></aqua>.</green>",
                "<green>There is <time> left until the <aqua><event_multiplier>x</aqua> prestige points event ends. The total multiplier is <aqua><current_multiplier></aqua>.</green>",
                "<green>The next <aqua><event_multiplier>x</aqua> prestige points event starts in <time>.</green>",
                "<green>There is no prestige points multiplier event active. There is no next event scheduled.</green>",
                new Locale.TimeFormat(
                        "",
                        "<aqua><years></aqua> year(s)",
                        "<aqua><months></aqua> month(s)",
                        "<aqua><weeks></aqua> week(s)",
                        "<aqua><days></aqua> day(s)",
                        "<aqua><hours></aqua> hour(s)",
                        "<aqua><minutes></aqua> minute(s)",
                        "<aqua><seconds></aqua> second(s)",
                        ""),
                ", ",
                ", and ");
    }
}