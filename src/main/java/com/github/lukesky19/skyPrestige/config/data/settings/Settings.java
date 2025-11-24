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
package com.github.lukesky19.skyPrestige.config.data.settings;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * This record contains the plugin's configuration settings.
 * @param configVersion The file's config version.
 * @param locale The locale to use.
 * @param saveFrequencySeconds How frequently island data is periodically saved.
 * @param resetPrestigeLevelOnIslandReset Legacy option for migration purposes only.
 * @param islandResetSettings The {@link IslandResetSettings}.
 * @param awardPointsWhileAfk Whether to increment prestige points if the player is AFK. Requires SkyPlayTime.
 * @param scaleFormula The formula to scale requirements with.
 * @param exchangePrestigeLevel The required prestige level to be able to exchange prestige points.
 * @param fallbackLocation The location to teleport a player if the plugin is unable to teleport them to their island.
 * @param vaultDisallowedItems A {@link List} of {@link String}s for the {@link ItemType} names/keys not allowed inside prestige vaults.
 * @param prestigePointsMapping This configuration for the prestige points to give for a variety of actions.
 */
@ConfigSerializable
public record Settings(
        @Nullable String configVersion,
        @Nullable String locale,
        @Nullable Integer saveFrequencySeconds,
        @Deprecated(since = "1.1.0.0") @Nullable Boolean resetPrestigeLevelOnIslandReset,
        @NotNull IslandResetSettings islandResetSettings,
        boolean awardPointsWhileAfk,
        @Nullable String scaleFormula,
        int exchangePrestigeLevel,
        @NotNull Location fallbackLocation,
        @NotNull List<String> vaultDisallowedItems,
        @NotNull PrestigePointsMapping prestigePointsMapping) {
    /**
     * Island reset settings that apply to all non-prestiged islands (normal resets only).
     * @param keepIslandRange Should the island's protection range carry over?
     * @param keepGeneratorUpgrades Should generator upgrades carry over?
     * @param resetPrestigePoints Should prestige points be reset?
     * @param resetPrestigeLevel Should prestige level be reset?
     */
    @ConfigSerializable
    public record IslandResetSettings(
            boolean keepIslandRange,
            boolean keepGeneratorUpgrades,
            boolean resetPrestigePoints,
            boolean resetPrestigeLevel) {}

    /**
     * The config to create a {@link org.bukkit.Location}.
     * @param world The world name.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     */
    @ConfigSerializable
    public record Location(@Nullable String world, @Nullable Double x, @Nullable Double y, @Nullable Double z) {}

    /**
     * This record contains the configuration for prestige points obtained for different actions.
     * @param playTime The number of prestige points gained for 1 second of play time.
     * @param blockBreak The prestige points for breaking blocks.
     * @param blockPlace The prestige points for placing blocks.
     * @param boneMeal The prestige points for bone mealing blocks.
     * @param bottle The prestige points for bottling items or potions.
     * @param breed The prestige points for breeding.
     * @param brew The prestige points for brewing.
     * @param brush The prestige points for brushing blocks.
     * @param close The prestige points for closing containers.
     * @param compost The prestige points for composting items.
     * @param consume The prestige points for consuming items or potions.
     * @param craft The prestige points for crafting items.
     * @param empty The prestige points for emptying items.
     * @param enchant The prestige points for enchanting items.
     * @param fill The prestige points for filling items.
     * @param fish The prestige points for fishing items or potions.
     * @param harvest The prestige points for harvesting blocks.
     * @param itemPickup The prestige points for picking up items.
     * @param itemDrop The prestige points for dropping items.
     * @param kill The prestige points for killing entities.
     * @param milk The prestige points for milking entities.
     * @param name The prestige points for naming items, potions, enchanted items, or entities.
     * @param open The prestige points for opening containers.
     * @param shear The prestige points for shearing blocks or entities.
     * @param sleep The prestige points for sleeping in beds.
     * @param smelt The prestige points for smelting items.
     * @param strip The prestige points for stripping blocks.
     * @param tame The prestige points for taming entities.
     * @param thrown The prestige points for throwing items or potions.
     * @param waterLog The prestige points for water logging blocks.
     * @param wax The prestige points for waxing blocks.
     */
    @ConfigSerializable
    public record PrestigePointsMapping(
            @Nullable Double playTime,
            @NotNull BlockSpawnerMapping blockBreak,
            @NotNull BlockSpawnerMapping blockPlace,
            @NotNull Map<String, Double> boneMeal,
            @NotNull ItemPotionMapping bottle,
            @NotNull Map<String, Double> breed,
            @NotNull ItemPotionMapping brew,
            @NotNull Map<String, Double> brush,
            @NotNull Map<String, Double> close,
            @NotNull Map<String, Double> compost,
            @NotNull ItemPotionMapping consume,
            @NotNull Map<String, Double> craft,
            @NotNull Map<String, Double> empty,
            @NotNull Map<String, Map<String, Map<String, Double>>> enchant,
            @NotNull Map<String, Double> fill,
            @NotNull ItemPotionEnchantmentMapping fish,
            @NotNull Map<String, Double> harvest,
            @NotNull ItemPotionEnchantmentMapping itemPickup,
            @NotNull ItemPotionEnchantmentMapping itemDrop,
            @NotNull Map<String, Double> kill,
            @NotNull Map<String, Double> milk,
            @NotNull ItemPotionEnchantmentEntityMapping name,
            @NotNull Map<String, Double> open,
            @NotNull BlockEntityMapping shear,
            @NotNull Map<String, Double> sleep,
            @NotNull Map<String, Double> smelt,
            @NotNull BlockEntityMapping strip,
            @NotNull Map<String, Double> tame,
            @NotNull ItemPotionMapping thrown,
            @NotNull Map<String, Double> waterLog,
            @NotNull BlockEntityMapping wax) {
        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getBlockBreakPrestigePoints(@NotNull BlockType blockType) {
            return blockBreak.getBlockPrestigePoints(blockType);
        }

        /**
         * Get the prestige points for the {@link BlockType} and {@link EntityType}.
         * @param blockType A {@link BlockType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getBlockBreakPrestigePoints(@NotNull BlockType blockType, @NotNull EntityType entityType) {
            return blockBreak.getSpawnerPrestigePoints(blockType, entityType);
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getBlockPlacePrestigePoints(@NotNull BlockType blockType) {
            return blockPlace.getBlockPrestigePoints(blockType);
        }

        /**
         * Get the prestige points for the {@link BlockType} and {@link EntityType}.
         * @param blockType A {@link BlockType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getBlockPlacePrestigePoints(@NotNull BlockType blockType, @NotNull EntityType entityType) {
            return blockPlace.getSpawnerPrestigePoints(blockType, entityType);
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getBoneMealPrestigePoints(@NotNull BlockType blockType) {
            return boneMeal.getOrDefault(blockType.getKey().toString(), boneMeal.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getBottlePrestigePoints(@NotNull ItemType itemType) {
            return bottle.getItemPrestigePoints(itemType);
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getBottlePrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            return bottle.getPotionPrestigePoints(itemType, potionType);
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getBreedPrestigePoints(@NotNull EntityType entityType) {
            return breed.getOrDefault(entityType.getKey().toString(), breed.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getBrewPrestigePoints(@NotNull ItemType itemType) {
            return brew.getItemPrestigePoints(itemType);
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getBrewPrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            return brew.getPotionPrestigePoints(itemType, potionType);
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getBrushPrestigePoints(@NotNull BlockType blockType) {
            return brush.getOrDefault(blockType.getKey().toString(), brush.get("default"));
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getClosePrestigePoints(@NotNull BlockType blockType) {
            return close.getOrDefault(blockType.getKey().toString(), close.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getCompostPrestigePoints(@NotNull ItemType itemType) {
            return compost.getOrDefault(itemType.getKey().toString(), compost.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getConsumePrestigePoints(@NotNull ItemType itemType) {
            return consume.getItemPrestigePoints(itemType);
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getConsumePrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            return consume.getPotionPrestigePoints(itemType, potionType);
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getCraftPrestigePoints(@NotNull ItemType itemType) {
            return craft.getOrDefault(itemType.getKey().toString(), craft.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getEmptyPrestigePoints(@NotNull ItemType itemType) {
            return empty.getOrDefault(itemType.getKey().toString(), empty.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}, {@link Enchantment}, and enchantment level.
         * @param itemType A {@link ItemType}.
         * @param enchantment An {@link Enchantment}.
         * @param level The enchantment's level.
         * @return The prestige points or null if no mapping exists for the {@link ItemType}, {@link Enchantment}, and enchantment level, and there is no default value configured.
         */
        public @Nullable Double getEnchantmentPrestigePoints(@NotNull ItemType itemType, @NotNull Enchantment enchantment, int level) {
            @Nullable Map<String, @Nullable Map<String, Double>> enchantmentMap = this.enchant.getOrDefault(itemType.getKey().toString(), this.enchant.get("default"));
            if(enchantmentMap == null) return null;

            @Nullable Map<String, Double> levelMap = enchantmentMap.getOrDefault(enchantment.getKey().toString(), enchantmentMap.get("default"));
            if(levelMap == null) return null;

            return levelMap.getOrDefault(String.valueOf(level), levelMap.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getFillPrestigePoints(@NotNull ItemType itemType) {
            return fill.getOrDefault(itemType.getKey().toString(), fill.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getFishPrestigePoints(@NotNull ItemType itemType) {
            return fish.getItemPrestigePoints(itemType);
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getFishPrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            return fish.getPotionPrestigePoints(itemType, potionType);
        }

        /**
         * Get the prestige points for the {@link ItemType}, {@link Enchantment}, and enchantment level.
         * @param itemType A {@link ItemType}.
         * @param enchantment An {@link Enchantment}.
         * @param level The enchantment's level.
         * @return The prestige points or null if no mapping exists for the {@link ItemType}, {@link Enchantment}, and enchantment level, and there is no default value configured.
         */
        public @Nullable Double getFishPrestigePoints(@NotNull ItemType itemType, @NotNull Enchantment enchantment, int level) {
            return fish.getEnchantmentPrestigePoints(itemType, enchantment, level);
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getHarvestPrestigePoints(@NotNull BlockType blockType) {
            return harvest.getOrDefault(blockType.getKey().toString(), harvest.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getItemPickupPrestigePoints(@NotNull ItemType itemType) {
            return itemPickup.getItemPrestigePoints(itemType);
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getItemPickupPrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            return itemPickup.getPotionPrestigePoints(itemType, potionType);
        }

        /**
         * Get the prestige points for the {@link ItemType}, {@link Enchantment}, and enchantment level.
         * @param itemType A {@link ItemType}.
         * @param enchantment An {@link Enchantment}.
         * @param level The enchantment's level.
         * @return The prestige points or null if no mapping exists for the {@link ItemType}, {@link Enchantment}, and enchantment level, and there is no default value configured.
         */
        public @Nullable Double getItemPickupPrestigePoints(@NotNull ItemType itemType, @NotNull Enchantment enchantment, int level) {
            return itemPickup.getEnchantmentPrestigePoints(itemType, enchantment, level);
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getItemDropPrestigePoints(@NotNull ItemType itemType) {
            return itemDrop.getItemPrestigePoints(itemType);
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getItemDropPrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            return itemDrop.getPotionPrestigePoints(itemType, potionType);
        }

        /**
         * Get the prestige points for the {@link ItemType}, {@link Enchantment}, and enchantment level.
         * @param itemType A {@link ItemType}.
         * @param enchantment An {@link Enchantment}.
         * @param level The enchantment's level.
         * @return The prestige points or null if no mapping exists for the {@link ItemType}, {@link Enchantment}, and enchantment level, and there is no default value configured.
         */
        public @Nullable Double getItemDropPrestigePoints(@NotNull ItemType itemType, @NotNull Enchantment enchantment, int level) {
            return itemDrop.getEnchantmentPrestigePoints(itemType, enchantment, level);
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getKillPrestigePoints(@NotNull EntityType entityType) {
            return kill.getOrDefault(entityType.getKey().toString(), kill.get("default"));
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getMilkPrestigePoints(@NotNull EntityType entityType) {
            return milk.getOrDefault(entityType.getKey().toString(), milk.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getNamePrestigePoints(@NotNull ItemType itemType) {
            return name.getItemPrestigePoints(itemType);
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getNamePrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            return name.getPotionPrestigePoints(itemType, potionType);
        }

        /**
         * Get the prestige points for the {@link ItemType}, {@link Enchantment}, and enchantment level.
         * @param itemType A {@link ItemType}.
         * @param enchantment An {@link Enchantment}.
         * @param level The enchantment's level.
         * @return The prestige points or null if no mapping exists for the {@link ItemType}, {@link Enchantment}, and enchantment level, and there is no default value configured.
         */
        public @Nullable Double getNamePrestigePoints(@NotNull ItemType itemType, @NotNull Enchantment enchantment, int level) {
            return name.getEnchantmentPrestigePoints(itemType, enchantment, level);
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getNamePrestigePoints(@NotNull EntityType entityType) {
            return name.getEntityPrestigePoints(entityType);
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getOpenPrestigePoints(@NotNull BlockType blockType) {
            return open.getOrDefault(blockType.getKey().toString(), open.get("default"));
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getShearPrestigePoints(@NotNull BlockType blockType) {
            return shear.getBlockPrestigePoints(blockType);
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getShearPrestigePoints(@NotNull EntityType entityType) {
            return shear.getEntityPrestigePoints(entityType);
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getSleepPrestigePoints(@NotNull BlockType blockType) {
            return sleep.getOrDefault(blockType.getKey().toString(), sleep.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getSmeltPrestigePoints(@NotNull ItemType itemType) {
            return smelt.getOrDefault(itemType.getKey().toString(), smelt.get("default"));
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getStripPrestigePoints(@NotNull BlockType blockType) {
            return strip.getBlockPrestigePoints(blockType);
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getStripPrestigePoints(@NotNull EntityType entityType) {
            return strip.getEntityPrestigePoints(entityType);
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getTamePrestigePoints(@NotNull EntityType entityType) {
            return tame.getOrDefault(entityType.getKey().toString(), tame.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getThrownPrestigePoints(@NotNull ItemType itemType) {
            return thrown.getItemPrestigePoints(itemType);
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getThrownPrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            return thrown.getPotionPrestigePoints(itemType, potionType);
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getWaterLogPrestigePoints(@NotNull BlockType blockType) {
            return waterLog.getOrDefault(blockType.getKey().toString(), waterLog.get("default"));
        }

        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getWaxPrestigePoints(@NotNull BlockType blockType) {
            return wax.getBlockPrestigePoints(blockType);
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getWaxPrestigePoints(@NotNull EntityType entityType) {
            return wax.getEntityPrestigePoints(entityType);
        }
    }

    /**
     * This record contains the mapping of blocks and spawners to prestige points.
     * @param block The {@link Map} mapping {@link BlockType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     * @param spawner The {@link Map} mapping {@link BlockType}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping {@link EntityType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     */
    @ConfigSerializable
    public record BlockSpawnerMapping(
            @NotNull Map<String, Double> block,
            @NotNull Map<String, Map<String, Double>> spawner) {
        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getBlockPrestigePoints(@NotNull BlockType blockType) {
            return block.getOrDefault(blockType.getKey().toString(), block.get("default"));
        }

        /**
         * Get the prestige points for the {@link BlockType} and {@link EntityType}.
         * @param blockType A {@link BlockType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getSpawnerPrestigePoints(@NotNull BlockType blockType, @NotNull EntityType entityType) {
            @Nullable Map<String, Double> entityMap = spawner.getOrDefault(blockType.getKey().toString(), spawner.get("default"));
            if(entityMap == null) return null;

            return entityMap.getOrDefault(entityType.getKey().toString(), entityMap.get("default"));
        }
    }

    /**
     * This record contains the mapping of blocks and entities to prestige points.
     * @param block The {@link Map} mapping {@link BlockType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     * @param entity The {@link Map} mapping {@link EntityType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     */
    @ConfigSerializable
    public record BlockEntityMapping(
            @NotNull Map<String, Double> block,
            @NotNull Map<String, Double> entity) {
        /**
         * Get the prestige points for the {@link BlockType}.
         * @param blockType A {@link BlockType}.
         * @return The prestige points or null if no mapping exists for the {@link BlockType} and there is no default value configured.
         */
        public @Nullable Double getBlockPrestigePoints(@NotNull BlockType blockType) {
            return block.getOrDefault(blockType.getKey().toString(), block.get("default"));
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType An {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getEntityPrestigePoints(@NotNull EntityType entityType) {
            return entity.getOrDefault(entityType.getKey().toString(), entity.get("default"));
        }
    }

    /**
     * This record contains the mapping of items and potions to prestige points.
     * @param item The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     * @param potion The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping {@link PotionType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     */
    @ConfigSerializable
    public record ItemPotionMapping(
            @NotNull Map<String, Double> item,
            @NotNull Map<String, Map<String, Double>> potion) {
        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getItemPrestigePoints(@NotNull ItemType itemType) {
            return item.getOrDefault(itemType.getKey().toString(), item.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getPotionPrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            @Nullable Map<String, Double> potionMap = potion.getOrDefault(itemType.getKey().toString(), potion.get("default"));
            if(potionMap == null) return null;

            return potionMap.getOrDefault(potionType.getKey().toString(), potionMap.get("default"));
        }
    }

    /**
     * This record contains the mapping of items, potions, and enchantments to prestige points.
     * @param item The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     * @param potion The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping {@link PotionType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     * @param enchantment The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping {@link Enchantment}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping enchantment levels as a {@link String} to prestige points.
     */
    @ConfigSerializable
    public record ItemPotionEnchantmentMapping(
            @NotNull Map<String, Double> item,
            @NotNull Map<String, @Nullable Map<String, Double>> potion,
            @NotNull Map<String, @Nullable Map<String, @Nullable Map<String, Double>>> enchantment) {
        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getItemPrestigePoints(@NotNull ItemType itemType) {
            return item.getOrDefault(itemType.getKey().toString(), item.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getPotionPrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            @Nullable Map<String, Double> potionMap = potion.getOrDefault(itemType.getKey().toString(), potion.get("default"));
            if(potionMap == null) return null;

            return potionMap.getOrDefault(potionType.getKey().toString(), potionMap.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}, {@link Enchantment}, and enchantment level.
         * @param itemType A {@link ItemType}.
         * @param enchantment An {@link Enchantment}.
         * @param level The enchantment's level.
         * @return The prestige points or null if no mapping exists for the {@link ItemType}, {@link Enchantment}, and enchantment level, and there is no default value configured.
         */
        public @Nullable Double getEnchantmentPrestigePoints(@NotNull ItemType itemType, @NotNull Enchantment enchantment, int level) {
            @Nullable Map<String, @Nullable Map<String, Double>> enchantmentMap = this.enchantment.getOrDefault(itemType.getKey().toString(), this.enchantment.get("default"));
            if(enchantmentMap == null) return null;

            @Nullable Map<String, Double> levelMap = enchantmentMap.getOrDefault(enchantment.getKey().toString(), enchantmentMap.get("default"));
            if(levelMap == null) return null;

            return levelMap.getOrDefault(String.valueOf(level), levelMap.get("default"));
        }
    }

    /**
     * This record contains the mapping of items, potions, enchantments, and entities to prestige points.
     * @param item The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     * @param potion The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping {@link PotionType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     * @param enchantment The {@link Map} mapping {@link ItemType}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping {@link Enchantment}'s {@link NamespacedKey}s as a {@link String} to a {@link Map} mapping enchantment levels as a {@link String} to prestige points.
     * @param entity @param item The {@link Map} mapping {@link EntityType}'s {@link NamespacedKey}s as a {@link String} to prestige points.
     */
    @ConfigSerializable
    public record ItemPotionEnchantmentEntityMapping(
            @NotNull Map<String, Double> item,
            @NotNull Map<String, @Nullable Map<String, Double>> potion,
            @NotNull Map<String, @Nullable Map<String, @Nullable Map<String, Double>>> enchantment,
            @NotNull Map<String, Double> entity) {
        /**
         * Get the prestige points for the {@link ItemType}.
         * @param itemType A {@link ItemType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and there is no default value configured.
         */
        public @Nullable Double getItemPrestigePoints(@NotNull ItemType itemType) {
            return item.getOrDefault(itemType.getKey().toString(), item.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType} and {@link PotionType}.
         * @param itemType A {@link ItemType}.
         * @param potionType A {@link PotionType}.
         * @return The prestige points or null if no mapping exists for the {@link ItemType} and {@link PotionType}, and there is no default value configured.
         */
        public @Nullable Double getPotionPrestigePoints(@NotNull ItemType itemType, @NotNull PotionType potionType) {
            @Nullable Map<String, Double> potionMap = potion.getOrDefault(itemType.getKey().toString(), potion.get("default"));
            if(potionMap == null) return null;

            return potionMap.getOrDefault(potionType.getKey().toString(), potionMap.get("default"));
        }

        /**
         * Get the prestige points for the {@link ItemType}, {@link Enchantment}, and enchantment level.
         * @param itemType A {@link ItemType}.
         * @param enchantment An {@link Enchantment}.
         * @param level The enchantment's level.
         * @return The prestige points or null if no mapping exists for the {@link ItemType}, {@link Enchantment}, and enchantment level, and there is no default value configured.
         */
        public @Nullable Double getEnchantmentPrestigePoints(@NotNull ItemType itemType, @NotNull Enchantment enchantment, int level) {
            @Nullable Map<String, @Nullable Map<String, Double>> enchantmentMap = this.enchantment.getOrDefault(itemType.getKey().toString(), this.enchantment.get("default"));
            if(enchantmentMap == null) return null;

            @Nullable Map<String, Double> levelMap = enchantmentMap.getOrDefault(enchantment.getKey().toString(), enchantmentMap.get("default"));
            if(levelMap == null) return null;

            return levelMap.getOrDefault(String.valueOf(level), levelMap.get("default"));
        }

        /**
         * Get the prestige points for the {@link EntityType}.
         * @param entityType A {@link EntityType}.
         * @return The prestige points or null if no mapping exists for the {@link EntityType} and there is no default value configured.
         */
        public @Nullable Double getEntityPrestigePoints(@NotNull EntityType entityType) {
            return entity.getOrDefault(entityType.getKey().toString(), entity.get("default"));
        }
    }
}
