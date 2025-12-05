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
package com.github.lukesky19.skyPrestige.prestige.validator;

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.prestige.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.dataHandler.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.hook.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.common.abstracts.data.HashMapDataManager;
import com.github.lukesky19.skylib.api.math.EquationUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * This class contains methods to validate if an island can be prestiged.
 */
public class PrestigeValidator {
    private final @NotNull ComponentLogger logger;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HashMapDataManager} instance.
     */
    public PrestigeValidator(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        this.logger = plugin.getComponentLogger();
        this.localeManager = localeManager;
        this.prestigeConfigManager = prestigeConfigManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Is the player in a world managed by a {@link GameModeAddon}?
     * @param player The {@link Player} to check.
     * @return A {@link GameModeAddon} or null if not in a world managed by a {@link GameModeAddon}.
     */
    public @Nullable GameModeAddon validateGameModeAddon(@NotNull Player player) {
        Locale locale = localeManager.getConfiguration();
        World playerWorld = player.getWorld();

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return null;

        @NotNull Optional<GameModeAddon> optionalGameModeAddon = bentoBoxHook.getGameModeAddon(playerWorld);
        if(optionalGameModeAddon.isPresent()) {
            return optionalGameModeAddon.get();
        } else {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigePlayerInWrongWorld()));
            return null;
        }
    }

    /**
     * Is the player in on an {@link Island}?
     * @param player The {@link Player} to check.
     * @return The {@link Island} the player is on or null.
     */
    public @Nullable Island validateIsland(@NotNull Player player) {
        Locale locale = localeManager.getConfiguration();
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(!bentoBoxHook.isHooked()) return null;

        Optional<Island> optionalIsland = bentoBoxHook.getIslandAtLocation(player.getLocation());
        if(optionalIsland.isPresent()) {
            return optionalIsland.get();
        } else {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigePlayerNotOnIsland()));
            return null;
        }
    }

    /**
     * Is the player the owner or a member of the island?
     * @param player The {@link Player} to check.
     * @param island The {@link Island} to check.
     * @return true or false.
     */
    public boolean isPlayerIslandOwnerOrMember(@NotNull Player player, @NotNull Island island) {
        Locale locale = localeManager.getConfiguration();
        UUID playerId = player.getUniqueId();

        boolean isOwnerOrMember;
        if(island.getOwner() != null) {
            isOwnerOrMember = island.getOwner().equals(playerId) || island.getMemberSet().contains(playerId);
        } else {
            isOwnerOrMember = island.getMemberSet().contains(playerId);
        }

        if(!isOwnerOrMember) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigePlayerNotMemberOrOwner()));
        }

        return isOwnerOrMember;
    }

    /**
     * Is there {@link IslandData} for the {@link Island}?
     * @param player The {@link Player} prestiging the island.
     * @param island The {@link Island} to validate.
     * @return The {@link IslandData} or null.
     */
    public @Nullable IslandData validateIslandData(@NotNull Player player, @NotNull Island island) {
        Locale locale = localeManager.getConfiguration();

        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandDataNotFound()));
            logger.error(AdventureUtil.deserialize("No Island data found for player " + player.getName() + "'s island. Island Id: " + island.getUniqueId()));
        }

        return islandData;
    }

    /**
     * Is there {@link PrestigeConfig} for the prestige level provided?
     * @param player The {@link Player} prestiging.
     * @param prestigeLevel The prestige level to get config for.
     * @return The {@link PrestigeConfig} or null.
     */
    public @Nullable PrestigeConfig validatePrestigeConfig(@NotNull Player player, int prestigeLevel) {
        Locale locale = localeManager.getConfiguration();

        @Nullable PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(prestigeLevel);
        if(prestigeConfig == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.islandPrestigeLevelMax()));
        }

        return prestigeConfig;
    }

    /**
     * Are the base required prestige points valid?
     * @param player The {@link Player} prestiging.
     * @param prestigePoints The prestige points from {@link PrestigeConfig#requiredPrestigePoints()}.
     * @param prestigeLevel The prestige level the prestige points are being validated for.
     * @return The base required prestige points or null.
     */
    public @Nullable Double validateBasePrestigePoints(
            @NotNull Player player,
            @Nullable Double prestigePoints,
            int prestigeLevel) {
        Locale locale = localeManager.getConfiguration();

        if(prestigePoints == null || prestigePoints <= 0) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeConfigRequirementError()));
            logger.error(AdventureUtil.deserialize("The required prestige points for prestige level " + prestigeLevel + " is invalid."));
        }

        return prestigePoints;
    }

    /**
     * Calculate the prestige points required to prestige an island.
     * @param scaleFormula The scale formula from {@link Settings#scaleFormula()}.
     * @param scaleFactor The scale factor from {@link PrestigeConfig#scaleFactor()}.
     * @param islandMemberCount The number of island members on the island's team.
     * @param basePrestigePoints The base required prestige points.
     * @return The required prestige points.
     */
    public double calculateRequiredPrestigePoints(
            @NotNull String scaleFormula,
            @Nullable Double scaleFactor,
            int islandMemberCount,
            double basePrestigePoints) {
        double requiredPoints;
        if(scaleFactor != null && scaleFactor != 0) {
            HashMap<String, String> variables = new HashMap<>();
            variables.put("r", String.valueOf(basePrestigePoints));
            variables.put("p", String.valueOf(islandMemberCount));
            variables.put("k", String.valueOf(scaleFactor));

            requiredPoints = EquationUtil.evaluateEquation(scaleFormula, variables).intValue();
        } else {
            requiredPoints = basePrestigePoints;
        }

        return requiredPoints;
    }

    /**
     * Does the player's island meet the required prestige points to prestige?
     * @param player The {@link Player} to prestiging.
     * @param islandData The {@link IslandData} for the island prestiging.
     * @param scaleFormula The scale formula from {@link Settings#scaleFormula()}.
     * @param islandMemberCount The number of players on the island's team.
     * @param basePrestigePoints The base prestige points from {@link PrestigeConfig#requiredPrestigePoints()}.
     * @param scaleFactor The scale factor from {@link PrestigeConfig#scaleFactor()}.
     * @return true if the island meets the required prestige points, or false.
     */
    public boolean hasRequiredPrestigePoints(
            @NotNull Player player,
            @NotNull IslandData islandData,
            @NotNull String scaleFormula,
            @Nullable Double scaleFactor,
            int islandMemberCount,
            double basePrestigePoints) {
        double requiredPrestigePoints = calculateRequiredPrestigePoints(scaleFormula, scaleFactor, islandMemberCount, basePrestigePoints);

        return hasRequiredPrestigePoints(player, islandData, requiredPrestigePoints);
    }

    /**
     * Does the player's island meet the required prestige points to prestige?
     * @param player The {@link Player} to prestiging.
     * @param islandData The {@link IslandData} for the island prestiging.
     * @param requiredPrestigePoints The prestige points required.
     * @return true if the island meets the required prestige points, or false.
     */
    public boolean hasRequiredPrestigePoints(
            @NotNull Player player,
            @NotNull IslandData islandData,
            double requiredPrestigePoints) {
        Locale locale = localeManager.getConfiguration();

        if(islandData.getPrestigePoints() < requiredPrestigePoints) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeNotEnoughPrestigePoints()));
            return false;
        } else {
            return true;
        }
    }
}
