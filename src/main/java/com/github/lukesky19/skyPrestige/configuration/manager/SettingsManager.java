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

import com.github.lukesky19.skyPrestige.configuration.data.inventory.EnderChestInventorySettings;
import com.github.lukesky19.skyPrestige.configuration.data.inventory.PlayerInventorySettings;
import com.github.lukesky19.skyPrestige.configuration.data.island.NonPrestigeIslandSettings;
import com.github.lukesky19.skyPrestige.configuration.data.player.PlayerSettings;
import com.github.lukesky19.skyPrestige.configuration.data.playtime.PlayTimeSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reward.IslandRangeReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.common.abstracts.config.SimpleConfigManager;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class manages the plugin's settings.
 */
public class SettingsManager extends SimpleConfigManager<Settings> {
    /**
     * Constructor
     * @param plugin A {@link SkyPlugin}.
     */
    public SettingsManager(@NotNull SkyPlugin plugin) {
        super(plugin, Path.of(plugin.getDataFolder() + File.separator + "settings.yml"), Settings.class);
    }

    @Override
    public @Nullable Settings migrateConfiguration(@NotNull Settings settings) {
        switch(settings.configVersion()) {
            case "1.1.0.0" -> {
                // latest version, do nothing
                return settings;
            }

            case "1.0.0.0" -> {
                PlayerSettings playerSettings = new PlayerSettings(
                        new PlayerInventorySettings(true, false, true),
                        new EnderChestInventorySettings(true, false, true),
                        true,
                        true,
                        true,
                        new PlayTimeSettings(false, false, false, false, false, false));
                return new Settings(
                        "1.1.0.0",
                        settings.locale(),
                        settings.saveFrequencySeconds(),
                        settings.awardPointsWhileAfk(),
                        settings.scaleFormula(),
                        settings.exchangePrestigeLevel(),
                        settings.fallbackLocation(),
                        true,
                        new NonPrestigeIslandSettings(
                                true,
                                true,
                                true,
                                Objects.requireNonNullElse(settings.resetPrestigeLevelOnIslandReset(), false),
                                false),
                        new Settings.OptInOutSettings(
                                new NonPrestigeIslandSettings(
                                        false,
                                        false,
                                        true,
                                        true,
                                        true),
                                playerSettings,
                                new RewardConfig(
                                        new ArrayList<>(),
                                        new ArrayList<>(),
                                        new ArrayList<>(),
                                        new IslandRangeReward(
                                                new ItemStackConfig(
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        List.of(),
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
                                                        new ItemStackConfig.OptionsConfig(true, null, null, null, null)),
                                                10
                                        )),
                                true,
                                2000),
                        new Settings.OptInOutSettings(
                                new NonPrestigeIslandSettings(
                                        false,
                                        false,
                                        true,
                                        true,
                                        true),
                                playerSettings,
                                new RewardConfig(
                                        new ArrayList<>(),
                                        new ArrayList<>(),
                                        new ArrayList<>(),
                                        new IslandRangeReward(
                                                new ItemStackConfig(
                                                        null,
                                                        null,
                                                        null,
                                                        null,
                                                        List.of(),
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
                                                        new ItemStackConfig.OptionsConfig(true, null, null, null, null)),
                                                10
                                        )),
                                true,
                                2000),
                        new Settings.ProtectionOrbSettings(
                                new ItemStackConfig(
                                        "heart_of_the_sea",
                                        1,
                                        null,
                                        "<gold><bold>Protection Orb</bold></gold>",
                                        List.of("<gray>Click an item with this and it will not be removed on prestige!</gray>"),
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
                                        new ItemStackConfig.OptionsConfig(true, null, null, null, null)),
                                List.of(
                                        "minecraft:spawner",
                                        "minecraft:iron_ore",
                                        "minecraft:deepslate_iron_ore",
                                        "minecraft:raw_iron",
                                        "minecraft:raw_iron_block",
                                        "minecraft:iron_ingot",
                                        "minecraft:iron_nugget",
                                        "minecraft:iron_block",
                                        "minecraft:gold_ore",
                                        "minecraft:deepslate_gold_ore",
                                        "minecraft:nether_gold_ore",
                                        "minecraft:raw_gold",
                                        "minecraft:raw_gold_block",
                                        "minecraft:gold_ingot",
                                        "minecraft:gold_nugget",
                                        "minecraft:gold_block",
                                        "minecraft:emerald_ore",
                                        "minecraft:deepslate_emerald_ore",
                                        "minecraft:emerald",
                                        "minecraft:emerald_block",
                                        "minecraft:diamond_ore",
                                        "minecraft:deepslate_diamond_ore",
                                        "minecraft:diamond",
                                        "minecraft:diamond_block",
                                        "minecraft:ancient_debris",
                                        "minecraft:netherite_scrap",
                                        "minecraft:netherite_ingot",
                                        "minecraft:netherite_block",
                                        "minecraft:shulker_box",
                                        "minecraft:white_shulker_box",
                                        "minecraft:light_gray_shulker_box",
                                        "minecraft:gray_shulker_box",
                                        "minecraft:black_shulker_box",
                                        "minecraft:brown_shulker_box",
                                        "minecraft:red_shulker_box",
                                        "minecraft:orange_shulker_box",
                                        "minecraft:yellow_shulker_box",
                                        "minecraft:lime_shulker_box",
                                        "minecraft:green_shulker_box",
                                        "minecraft:cyan_shulker_box",
                                        "minecraft:light_blue_shulker_box",
                                        "minecraft:blue_shulker_box",
                                        "minecraft:purple_shulker_box",
                                        "minecraft:magenta_shulker_box",
                                        "minecraft:pink_shulker_box",
                                        "minecraft:bundle",
                                        "minecraft:white_bundle",
                                        "minecraft:light_gray_bundle",
                                        "minecraft:gray_bundle",
                                        "minecraft:black_bundle",
                                        "minecraft:brown_bundle",
                                        "minecraft:red_bundle",
                                        "minecraft:orange_bundle",
                                        "minecraft:yellow_bundle",
                                        "minecraft:lime_bundle",
                                        "minecraft:green_bundle",
                                        "minecraft:cyan_bundle",
                                        "minecraft:light_blue_bundle",
                                        "minecraft:blue_bundle",
                                        "minecraft:purple_bundle",
                                        "minecraft:magenta_bundle",
                                        "minecraft:pink_bundle"),
                                "<gray>This item is now protected and will not be removed on prestige.</gray>"),
                        new Settings.MultiplierEventSettings(
                                true,
                                DayOfWeek.SUNDAY.toString(),
                                "America/New_York",
                                10,
                                86400,
                                2),
                        settings.vaultDisallowedItems(),
                        settings.prestigePointsMapping(),
                        null);
            }

            case null, default -> {
                logger.warn(AdventureUtil.deserialize("Unknown config version for settings config. Unable to update config."));
                return null;
            }
        }
    }

    @Override
    public boolean validateConfiguration() {
        @Nullable Settings settings = getConfiguration();
        return settings != null;
    }

    @Override
    public void saveBundledConfig() {
        plugin.saveResource("settings.yml", false);
    }
}