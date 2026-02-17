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
package com.github.lukesky19.skyPrestige.prestige;

import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsMapping;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigePointsConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.util.cache.BlockPointsCache;
import com.github.lukesky19.skyPrestige.util.cache.EntityPointsCache;
import com.github.lukesky19.skyPrestige.util.cache.ItemPointsCache;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.math.EquationUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains a method related to calculating required prestige points.
 */
public class PrestigePointsManager {
    private final @NotNull ComponentLogger logger;
    private final @NotNull PrestigeConfigManager prestigeConfigManager;
    private final @NotNull PrestigePointsConfigManager prestigePointsConfigManager;
    private final @NotNull IslandDataManager islandDataManager;

    private final @NotNull BlockPointsCache blockPointsCache = new BlockPointsCache();
    private final @NotNull ItemPointsCache itemPointsCache = new ItemPointsCache();
    private final @NotNull EntityPointsCache entityPointsCache = new EntityPointsCache();

    /**
     * Constructor
     * @param logger The plugin's {@link ComponentLogger}.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     */
    public PrestigePointsManager(
            @NotNull ComponentLogger logger,
            @NotNull PrestigeConfigManager prestigeConfigManager,
            @NotNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NotNull IslandDataManager islandDataManager) {
        this.logger = logger;
        this.prestigeConfigManager = prestigeConfigManager;
        this.prestigePointsConfigManager = prestigePointsConfigManager;
        this.islandDataManager = islandDataManager;
    }

    /**
     * Clear the caches.
     */
    public void clearCaches() {
        blockPointsCache.clear();
        itemPointsCache.clear();
        entityPointsCache.clear();
    }

    /**
     * Get the prestige points to award for 1 second of play time.
     * @return The prestige points.
     */
    public double getPlayTimePoints() {
        @Nullable PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
        if(prestigePointsConfig == null) return 0;

        return prestigePointsConfig.prestigePointsMapping().playTime().points();
    }

    /**
     * Get the prestige points to award for the action and block data.
     * @param actionType The {@link ActionType}.
     * @param config The {@link PrestigePointsMapping.Block} config.
     * @param blockType The {@link BlockType} or null.
     * @param entityType The {@link EntityType} or null.
     * @param age The age or null.
     * @param waterLogged The water logged status or null.
     * @return The prestige points.
     */
    public double getBlockPoints(
            @NotNull ActionType actionType,
            @NotNull PrestigePointsMapping.Block config,
            @NotNull BlockType blockType,
            @Nullable EntityType entityType,
            @Nullable Integer age,
            @Nullable Boolean waterLogged) {
        return blockPointsCache.getPoints(actionType, config, blockType, entityType, age, waterLogged);
    }

    /**
     * Get the prestige points to award for the action and item data.
     * @param actionType The {@link ActionType}.
     * @param config The {@link PrestigePointsMapping.Item} config.
     * @param itemType The {@link ItemType}.
     * @param entityType The {@link EntityType} or null.
     * @param potionType The {@link PotionType} or null.
     * @param enchantments The {@link Map} mapping {@link Enchantment}s to levels as {@link Integer}s.
     * @return The prestige points.
     */
    public double getItemPoints(
            @NotNull ActionType actionType,
            @NotNull PrestigePointsMapping.Item config,
            @NotNull ItemType itemType,
            @Nullable EntityType entityType,
            @Nullable PotionType potionType,
            @Nullable Map<Enchantment, Integer> enchantments) {
        return itemPointsCache.getPoints(actionType, config, itemType, entityType, potionType, enchantments);
    }

    /**
     * Get the prestige points to award for the action and entity data.
     * @param actionType The {@link ActionType}.
     * @param config The {@link PrestigePointsMapping.Item} config.
     * @param entityType The {@link EntityType}.
     * @return The prestige points.
     */
    public double getEntityPoints(
            @NotNull ActionType actionType,
            @NotNull PrestigePointsMapping.Entity config,
            @NotNull EntityType entityType) {
        return entityPointsCache.getPoints(actionType, config, entityType);
    }

    /**
     * Recalculate the prestige points required for the island to prestige.
     * @param island The {@link Island}.
     */
    public void recalculateRequiredPrestigePoints(@NotNull Island island) {
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("Unable to recalculate island required prestige points due to island " + island.getUniqueId() + " not having any island data."));
            return;
        }

        // Don't recalculate for islands opted out of prestige.
        if(islandData.isPrestigeExempt()) {
            islandData.setRequiredPrestigePoints(null);
            return;
        }

        // Get the prestige points config.
        @Nullable PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
        // If no valid prestige points config was found, return
        if(prestigePointsConfig == null || prestigePointsConfig.scaleFormula() == null) {
            islandData.setRequiredPrestigePoints(null);
            return;
        }

        // Get the prestige config for the island's next prestige level.
        @Nullable PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(islandData.getPrestigeLevel() + 1);
        // If no valid prestige config was found, return
        if(prestigeConfig == null || prestigeConfig.requiredPrestigePoints() == null) {
            islandData.setRequiredPrestigePoints(null);
            return;
        }

        // Calculate the required prestige points for the island to prestige
        double requiredPoints;
        if(prestigeConfig.scaleFactor() != null && prestigeConfig.scaleFactor() != 0) {
            HashMap<String, String> variables = new HashMap<>();
            variables.put("r", String.valueOf(prestigeConfig.requiredPrestigePoints()));
            variables.put("p", String.valueOf(island.getMemberSet().size()));
            variables.put("k", String.valueOf(prestigeConfig.scaleFactor()));

            requiredPoints = EquationUtil.evaluateEquation(prestigePointsConfig.scaleFormula(), variables).intValue();
        } else {
            requiredPoints = prestigeConfig.requiredPrestigePoints();
        }

        // Cache the required prestige points
        islandData.setRequiredPrestigePoints(requiredPoints);
    }

    /**
     * Calculate the prestige points that would be required for an island to prestige with the provided level.
     * @param island The {@link Island}.
     * @param level The prestige level.
     * @return The required prestige points or null.
     */
    public @Nullable Double calculateRequiredPrestigePoints(@NotNull Island island, int level) {
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("Unable to calculate prestige points required for prestige level " + level + " for island " + island.getUniqueId() + " due to not having any island data."));
            return null;
        }

        // Don't recalculate for islands opted out of prestige.
        if(islandData.isPrestigeExempt()) return null;

        // Get the prestige points config.
        @Nullable PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
        // If no valid prestige points config was found, return
        if(prestigePointsConfig == null || prestigePointsConfig.scaleFormula() == null) return null;

        // Get the prestige config for the provided prestige level.
        @Nullable PrestigeConfig prestigeConfig = prestigeConfigManager.getConfiguration(level);
        // If no valid prestige config was found, return
        if(prestigeConfig == null || prestigeConfig.requiredPrestigePoints() == null) return null;

        // Calculate the required prestige points for the island to prestige
        double requiredPoints;
        if(prestigeConfig.scaleFactor() != null && prestigeConfig.scaleFactor() != 0) {
            HashMap<String, String> variables = new HashMap<>();
            variables.put("r", String.valueOf(prestigeConfig.requiredPrestigePoints()));
            variables.put("p", String.valueOf(island.getMemberSet().size()));
            variables.put("k", String.valueOf(prestigeConfig.scaleFactor()));

            requiredPoints = EquationUtil.evaluateEquation(prestigePointsConfig.scaleFormula(), variables).intValue();
        } else {
            requiredPoints = prestigeConfig.requiredPrestigePoints();
        }

        // Return the required prestige points
        return requiredPoints;
    }
}