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
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the plugin's locale.
 */
public class LocaleManager extends SimpleConfigManager<Locale> {
    private final @NonNull SimpleConfigManager<Settings> settingsManager;
    private @NonNull Locale DEFAULT_LOCALE;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public LocaleManager(@NonNull SkyPlugin plugin, @NonNull SimpleConfigManager<Settings> settingsManager) {
        super(plugin, Locale.class);
        this.settingsManager = settingsManager;

        createDefaultLocale();
    }

    /**
     * Gets the plugin's locale if not null or the default locale otherwise.
     * @return The plugin's locale if not null or the default locale otherwise.
     */
    @Override
    public @NonNull Locale getConfiguration() {
        if(configuration == null) return DEFAULT_LOCALE;
        return configuration;
    }

    @Override
    public void loadConfiguration() {
        configuration = null;

        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.error(AdventureUtility.plain("Failed to load plugin's locale due to plugin settings being null."));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtility.plain("Failed to load plugin's locale to use in settings.yml is null."));
            return;
        }

        saveDefaultConfiguration();

        this.configurationPath = Path.of(plugin.getDirectoryFile() + File.separator + "locale" + File.separator + (settings.locale() + ".yml"));

        YamlConfigurationLoader loader = createLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();
            migrateVersion(root);
            loader.save(root);

            Locale locale = root.get(Locale.class);
            if(locale == null) {
                logger.warn(AdventureUtility.plain("Failed to load " + settings.locale() + ".yml."));
                return;
            }

            Locale migratedConfiguration = migrateConfiguration(locale);
            if(migratedConfiguration == null) {
                logger.warn(AdventureUtility.plain("Failed to migrate " + settings.locale() + ".yml."));
                return;
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(migratedConfiguration)) {
                logger.warn(AdventureUtility.plain("Locale " + settings.locale() + ".yml configuration validation failed."));
                return;
            }

            this.configuration = migratedConfiguration;
        } catch (ConfigurateException configurateException) {
            logger.error(AdventureUtility.plain("Failed to load " + settings.locale() + ".yml configuration. Error: " + configurateException.getMessage()));
        }
    }

    @Override
    public void saveDefaultConfiguration() {
        Path path = Path.of(plugin.getDirectoryFile() + File.separator + "locale" + File.separator + "en_US.yml");
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
    public @Nullable Locale migrateConfiguration(@NonNull Locale locale) {
        switch(locale.version()) {
            case 2 -> {
                // latest version, do nothing
                return locale;
            }

            case 1 -> {
                logger.warn(AdventureUtility.plain("Unable to migrate version 1 config versions for locale config. Please regenerate or manually migrate your configuration."));
                return null;
            }

            default -> {
                logger.warn(AdventureUtility.plain("Unknown config version for locale config. Unable to update config."));
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
        Locale.PrestigeMessages prestigeMessages = configuration.prestigeMessages();
        Locale.OptInMessages optInMessages = configuration.optInMessages();
        Locale.OptOutMessages optOutMessages = configuration.optOutMessages();
        Locale.RewardMessages rewardMessages = configuration.rewardMessages();
        Locale.ExchangeMessages exchangeMessages = configuration.exchangeMessages();
        Locale.VaultMessages vaultMessages =  configuration.vaultMessages();
        Locale.RequirementMessages requirementMessages = configuration.requirementMessages();
        Locale.LeaderboardMessages leaderboardMessages = configuration.leaderboardMessages();
        Locale.ProtectionOrbMessages protectionOrbMessages = configuration.protectionOrbMessages();
        Locale.MultiplierMessages multiplierMessages = configuration.multiplierMessages();

        if(configuration.prefix() == null
                || configuration.reload() == null
                || configuration.guiOpenError() == null
                || configuration.islandDataNotFound() == null
                || configuration.prestigeLevelUpdated() == null

                || prestigeMessages.prestigePlayerOnly() == null
                || prestigeMessages.prestigeLevelMax() == null
                || prestigeMessages.playerInWrongWorld() == null
                || prestigeMessages.playerNotOnIsland() == null
                || prestigeMessages.islandNotOwned() == null
                || prestigeMessages.playerNotMemberOrOwner() == null
                || prestigeMessages.prestigeInProgress() == null
                || prestigeMessages.notEnoughItems() == null
                || prestigeMessages.notEnoughMoney() == null
                || prestigeMessages.notEnoughPrestigePoints() == null
                || prestigeMessages.questIncomplete() == null
                || prestigeMessages.islandOptedOut() == null
                || prestigeMessages.prestigeConfigError() == null
                || prestigeMessages.requirementError() == null
                || prestigeMessages.prestigeAnnouncement() == null
                || prestigeMessages.prestigeIslandMemberMessage() == null

                || optInMessages.playerInWrongWorld() == null
                || optInMessages.playerNotOnIsland() == null
                || optInMessages.islandNotOwned() == null
                || optInMessages.playerNotMemberOrOwner() == null
                || optInMessages.islandAlreadyOptedIn() == null
                || optInMessages.islandMemberMessage() == null
                || optInMessages.optInInProgress() == null

                || optOutMessages.playerInWrongWorld() == null
                || optOutMessages.playerNotOnIsland() == null
                || optOutMessages.islandNotOwned() == null
                || optOutMessages.playerNotMemberOrOwner() == null
                || optOutMessages.islandAlreadyOptedOut() == null
                || optOutMessages.islandMemberMessage() == null
                || optOutMessages.optOutInProgress() == null

                || rewardMessages.playerNotOnIsland() == null
                || rewardMessages.prestigeExempt() == null
                || rewardMessages.maxPrestigeLevel() == null
                || rewardMessages.prestigeConfigError() == null

                || exchangeMessages.playerNotOnIsland() == null
                || exchangeMessages.prestigeLevelNotMet() == null
                || exchangeMessages.prestigeExempt() == null
                || exchangeMessages.prestigeInProgress() == null
                || exchangeMessages.optOutInProgress() == null
                || exchangeMessages.notEnoughPrestigePoints() == null

                || vaultMessages.playerNotOnIsland() == null
                || vaultMessages.itemNotAllowed() == null
                || vaultMessages.prestigeExempt() == null
                || vaultMessages.prestigeInProgress() == null
                || vaultMessages.optOutInProgress() == null

                || requirementMessages.playerNotOnIsland() == null
                || requirementMessages.islandNotOwned() == null
                || requirementMessages.playerNotMemberOrOwner() == null
                || requirementMessages.prestigeExempt() == null
                || requirementMessages.maxPrestigeLevel() == null
                || requirementMessages.prestigeConfigError() == null
                || requirementMessages.requirementsConfigError() == null

                || leaderboardMessages.islandExempt() == null
                || leaderboardMessages.islandUnexempt() == null
                || leaderboardMessages.leaderboardTitle() == null
                || leaderboardMessages.leaderboardPosition() == null
                || leaderboardMessages.leaderboardPositionEmpty() == null

                || protectionOrbMessages.protectionOrbNotAllowed() == null
                || protectionOrbMessages.protectionOrbAlreadyProtected() == null
                || protectionOrbMessages.protectionOrbProtected() == null

                || multiplierMessages.serverMultiplierChangedTimeLimit() == null
                || multiplierMessages.serverMultiplierChangedNoTimeLimit() == null
                || multiplierMessages.islandMultiplierChangedTimeLimit() == null
                || multiplierMessages.islandMultiplierChangedNoTimeLimit() == null
                || multiplierMessages.serverMultiplierExpiredNotice() == null
                || multiplierMessages.islandMultiplierExpiredNotice() == null
                || multiplierMessages.serverMultiplierClearedNotice() == null
                || multiplierMessages.islandMultiplierClearedNotice() == null
                || multiplierMessages.serverMultiplierTimeLimit() == null
                || multiplierMessages.serverMultiplierNoTimeLimit() == null
                || multiplierMessages.islandMultiplierTimeLimit() == null
                || multiplierMessages.islandMultiplierNoTimeLimit() == null
                || multiplierMessages.serverMultiplierCleared() == null
                || multiplierMessages.islandMultiplierCleared() == null
                || multiplierMessages.effectiveMultiplier() == null
                || multiplierMessages.multiplierNotOnIsland() == null
                || isTimeFormatInvalid(multiplierMessages.multiplierTimePlaceholder())

                || multiplierMessages.shopMultiplierMessages().notOnIsland() == null
                || multiplierMessages.shopMultiplierMessages().multiplierActive() == null
                || multiplierMessages.shopMultiplierMessages().higherMultiplierActive() == null
                || multiplierMessages.shopMultiplierMessages().multiplierTimeMax() == null

                || configuration.delimiter() == null
                || configuration.finalDelimiter() == null) {
            this.configuration = null;

            logger.error(AdventureUtility.plain("Your locale is missing one of the plugin's messages. The default locale will be used."));
            logger.info(AdventureUtility.plain("You can regenerate your locale file by deleting it or adding the missing messages to resolve the issue."));

            return false;
        }

        return true;
    }

    /**
     * Checks if all {@link String}s in a {@link TimeFormat} are null.
     * @param timeFormat The {@link TimeFormat} to check.
     * @return true if invalid, false if not.
     */
    private boolean isTimeFormatInvalid(@NonNull TimeFormat timeFormat) {
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
                2,
                "<green><bold>SkyPrestige</bold></green><gray> ▪ </gray>",
                List.of(
                        "<green>SkyPrestige is developed by <white><bold>lukeskywlker19</bold></white>.</green>",
                        "<green>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click></green>",
                        " ",
                        "<green><bold>List of Commands:</bold></green>",
                        "<white>/</white><aqua>skyprestige</aqua>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>help</yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>reload</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>rewards [prestige] [level]</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>rewards [opt-in | opt-out]</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>vault</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>values</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>info</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>requirements</yellow>",
                        "<white>/</white><aqua>skyprestige</aqua> <yellow>level set <island_id> <level></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>manage <island_id></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>multiplier</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>multiplier server <add | remove | set> <multiplier> <time> <notice></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>multiplier island <add | remove | set> <island_id> <multiplier> <time> <notice></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>multiplier server get</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>multiplier island get [island_id]</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>multiplier server dialog</yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>points <add | remove | set> <island_id> <amount></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>points get <island_id></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>exempt <island_id></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>unexempt <island_id></yellow>",
                        "<white>/</white><green>skyprestige</green> <yellow>leaderboard</yellow>"),
                "<green>The plugin has reloaded successfully.</green>",
                "<red>Unable to open this GUI because of a configuration error.</red>",
                "<red>No island data was found for the island.</red>",
                "<green>Island <island_id> had their prestige level set to <prestige_level>.</green>",
                new Locale.PrestigeMessages(
                        "<red>Only players are able to prestige islands.</red>",
                        "<red>Your island is currently at the max prestige level.</red>",
                        "<red>You must be in an island world to prestige.</red>",
                        "<red>You must be on your island to prestige it.</red>",
                        "<red>You cannot prestige an island that is not owned.</red>",
                        "<red>You must be the island owner or an island member to prestige.</red>",
                        "<red>Another player has already initiated the process of prestiging your island.</red>",
                        "<red>Online island members collectively do not have enough items to prestige.</red>",
                        "<red>Island members collectively do not have enough money to prestige.</red>",
                        "<red>You do not have enough prestige points to prestige.</red>",
                        "<red>Quest <white><quest_id></white>, which is required to prestige. At least one island member must complete the quest.</red>",
                        "<red>You cannot prestige an island that is opted out of prestige.</red>",
                        "<red>Unable to prestige your island due to a configuration error.</red>",
                        "<red>Unable to check the required prestige points due to a configuration error.</red>",
                        "<green>Player <#0055ff><player></#0055ff> has reached prestige level <#0055ff><prestige_level></#0055ff>.</green>",
                        "<green>Player <#0055ff><player></#0055ff> has prestiged the island you are a member of.</green>"),
                new Locale.OptInMessages(
                        "<red>You must be in an island world to opt into prestige.</red>",
                        "<red>You must be on your island to opt into prestige.</red>",
                        "<red>You cannot opt into prestige for an island that is not owned.</red>",
                        "<red>You must be the island owner or an island member to opt into prestige.</red>",
                        "<red>Your island is already opted into prestige.</red>",
                        "<green>Player <#0055ff><player></#0055ff> has opted into prestige for the island you are a member.</green>",
                        "<red>Another player has already initiated the process of opting in your island to prestige.</red>"),
                new Locale.OptOutMessages(
                        "<red>You must be in an island world to opt of prestige.</red>",
                        "<red>You must be on your island to opt out of prestige.</red>",
                        "<red>You cannot opt out of prestige for an island that is not owned.</red>",
                        "<red>You must be the island owner or an island member to opt out of prestige.</red>",
                        "<red>Your island is already opted out of prestige.</red>",
                        "<green>Player <#0055ff><player></#0055ff> has opted out of prestige for the island you are a member.</green>",
                        "<red>Another player has already initiated the process of opting your island out of prestige.</red>"),
                new Locale.RewardMessages(
                        "<red>You must be on your island to view rewards.</red>",
                        "<red>Your island is opted out of prestige. Prestige rewards can only be viewed for islands that can prestige.</red>",
                        "<green>No rewards to view because your island is at the max prestige level.</green>",
                        "<red>There is no configuration found for the prestige level <white><level></white>.</red>"),
                new Locale.ExchangeMessages(
                        "<red>You must be on your island to exchange prestige points.</red>",
                        "<red>Your island doesn't meet the required prestige level to exchange prestige points.</red>",
                        "<red>Your island is opted out of prestige. Only islands that can prestige can exchange prestige points.</red>",
                        "<red>You cannot exchange prestige points while your island is in the process of prestiging.</red>",
                        "<red>You cannot exchange prestige points while your island is in the process of opting out of prestige.</red>",
                        "<red>You do not have enough prestige points to exchange.</red>"),
                new Locale.VaultMessages(
                        "<red>You must be on your island to view the island vault.</red>",
                        "<red>The item you clicked is not allowed to be placed inside the vault.</red>",
                        "<red>Your island is opted out of prestige. The vault can only be used by islands opted into prestige.</red>",
                        "<red>You cannot modify the vault while your island is in the process of prestiging.</red>",
                        "<red>You cannot modify the vault while your island is in the process of opting out of prestige.</red>"),
                new Locale.RequirementMessages(
                        "<red>You must be on your island to view prestige requirements.</red>",
                        "<red>You cannot view prestige requirements for an island that is not owned.</red>",
                        "<red>You must be the island owner or an island member to view prestige requirements.</red>",
                        "<red>Your island is opted out of prestige. Prestige requirements can only be viewed for islands that can prestige.</red>",
                        "<green>No requirements to view because your island is at the max prestige level.</green>",
                        "<red>There is no configuration found for the prestige level <white><level></white>.</red>",
                        "<red>Unable to view prestige level requirements due to a configuration error.</red>"),
                new Locale.LeaderboardMessages(
                        "<green>Island <yellow><island_id></yellow> is now exempt from leaderboard.</green>",
                        "<green>Island <yellow><island_id></yellow> is now unexempt from leaderboard.</green>",
                        "<green><bold>Top 10 Islands By Prestige Level and Points</bold></green>",
                        "<gray>[</gray><aqua><position></aqua><gray>]</gray> <yellow><player_name></yellow> <white>Level:</white> <aqua><prestige_level></aqua> <white>Points:</white> <aqua><prestige_points></aqua>",
                        "<gray>[</gray><aqua><position></aqua><gray>] ----------</gray>"),
                new Locale.ProtectionOrbMessages(
                        "<red>This item can not be protected by a protection orb.</red>",
                        "<red>This item is already protected by a protection orb.</red>",
                        "<green>This item is now protected and will not be removed on prestige.</green>"),
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
                        new TimeFormat(
                                "",
                                "<aqua><years></aqua> year(s)",
                                "<aqua><months></aqua> month(s)",
                                "<aqua><weeks></aqua> week(s)",
                                "<aqua><days></aqua> day(s)",
                                "<aqua><hours></aqua> hour(s)",
                                "<aqua><minutes></aqua> minute(s)",
                                "<aqua><seconds></aqua> second(s)",
                                ""),
                        new Locale.MultiplierMessages.ShopMultiplierMessages(
                                "<red>You must be on your island to purchase an island multiplier.</red>",
                                "<red>You cannot purchase this multiplier as one is already active.</red>",
                                "<red>You cannot purchase this multiplier as a higher one is already active.</red>",
                                "<red>The multiplier time is already at or would exceed the maximum time allowed.</red>")),
                ", ",
                ", and ");
    }

    /**
     * Migrate the string-based version to a numeric version number.
     * @param root The root {@link ConfigurationNode}.
     */
    private void migrateVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();
        if(version > 0) return;

        ConfigurationNode legacyVersionNode = root.node("config-version");
        String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
        try {
            switch(legacyVersion) {
                case "2.0.0.0" -> versionNode.set(3);

                case "1.1.0.0" -> versionNode.set(2);

                case "1.0.0.0" -> versionNode.set(1);

                case null, default -> logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version due to an unrecognized version."));
            }
        } catch (SerializationException e) {
            logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version. Error: " + e.getMessage()));
        }
    }
}