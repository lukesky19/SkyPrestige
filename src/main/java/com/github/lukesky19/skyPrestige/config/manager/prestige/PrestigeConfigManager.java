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
package com.github.lukesky19.skyPrestige.config.manager.prestige;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.prestige.PrestigeConfig;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the configuration for prestige levels.
 */
public class PrestigeConfigManager {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull Map<Integer, PrestigeConfig> prestigeConfig = new HashMap<>();

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     */
    public PrestigeConfigManager(@NotNull SkyPrestige skyPrestige) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
    }

    /**
     * Get the {@link PrestigeConfig} for the provided level.
     * @param level The prestige level to get config for.
     * @return The {@link PrestigeConfig} or null.
     */
    public @Nullable PrestigeConfig getPrestigeConfig(int level) {
        return prestigeConfig.get(level);
    }

    /**
     * Get a {@link List} of {@link Integer}s for the currently configured prestige levels.
     * @return A {@link List} of {@link Integer}s for the currently configured prestige levels.
     */
    public @NotNull List<@NotNull Integer> getPrestigeLevels() {
        return prestigeConfig.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get a {@link Map} mapping prestige levels to {@link PrestigeConfig}.
     * The {@link Map} will be empty if no {@link PrestigeConfig} was found for any prestige levels.
     * @param prestigeLevels The {@link List} of prestige levels.
     * @return A {@link Map} mapping prestige levels to {@link PrestigeConfig}.
     */
    public @NotNull Map<Integer, PrestigeConfig> getPrestigeConfigMap(@NotNull List<Integer> prestigeLevels) {
        Map<Integer, PrestigeConfig> prestigeConfigMap = new HashMap<>();

        for(Integer prestigeLevel : prestigeLevels) {
            PrestigeConfig prestigeConfig = getPrestigeConfig(prestigeLevel);
            if(prestigeConfig == null) {
                logger.warn(AdventureUtil.deserialize("No prestige config found for prestige level: " + prestigeLevel));
                continue;
            }

            prestigeConfigMap.put(prestigeLevel, prestigeConfig);
        }

        return prestigeConfigMap;
    }

    /**
     * Reloads the plugin's prestige configurations.
     */
    public void reload() {
        ComponentLogger logger = skyPrestige.getComponentLogger();
        prestigeConfig.clear();

        saveDefaultConfig();

        try(Stream<Path> paths = Files.walk(Paths.get(skyPrestige.getDataFolder() + File.separator + "prestige"))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);
                        try {
                            PrestigeConfig config = yamlConfigurationLoader.load().get(PrestigeConfig.class);
                            if(config != null) {
                                PrestigeConfig updatedConfig = updateConfig(config);

                                if(!config.equals(updatedConfig)) {
                                    System.out.println("Config was migrated for level: " + updatedConfig.prestigeLevel());

                                    saveConfig(path, updatedConfig);
                                }

                                prestigeConfig.put(updatedConfig.prestigeLevel(), updatedConfig);
                            } else {
                                logger.error(AdventureUtil.deserialize("Failed to load prestige config file: " + path.getFileName()));
                            }
                        } catch (ConfigurateException e) {
                            logger.error(AdventureUtil.deserialize("Failed to load prestige config file: " + path.getFileName() + ". Error: " + e.getMessage()));
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save the prestige config.
     * @param path The path to save to.
     * @param prestigeConfig The {@link PrestigeConfig} to save.
     */
    public void saveConfig(@NotNull Path path, @NotNull PrestigeConfig prestigeConfig) {
        try {
            @NotNull YamlConfigurationLoader yamlConfigurationLoader = ConfigurationUtility.getYamlConfigurationLoader(path);

            ConfigurationNode node = yamlConfigurationLoader.createNode();

            node.set(PrestigeConfig.class, prestigeConfig);

            yamlConfigurationLoader.save(node);
        } catch (ConfigurateException e) {
            skyPrestige.getComponentLogger().error(AdventureUtil.deserialize("Failed to save prestige config file: " + path.getFileName() + ". Error: " + e.getMessage()));
        }
    }

    /**
     * Update a {@link PrestigeConfig} to the latest version.
     * @param prestigeConfig The {@link PrestigeConfig} to update.
     * @return The updated {@link PrestigeConfig}.
     */
    @SuppressWarnings("deprecation") // Suppressed because this deprecated usage is for migration purposes.
    private @NotNull PrestigeConfig updateConfig(@NotNull PrestigeConfig prestigeConfig) {
        switch(prestigeConfig.configVersion()) {
            case "1.1.0.0" -> {
                // Current version, do nothing
                return prestigeConfig;
            }

            case "1.0.0.0" -> {
                PrestigeConfig.PrestigeSettings prestigeSettings = prestigeConfig.prestigeSettings();
                PrestigeConfig.PrestigeSettings newSettings = new PrestigeConfig.PrestigeSettings(
                        new PrestigeConfig.PlayerSettings(
                                new PrestigeConfig.InventorySettings(
                                        prestigeSettings.resetInventory(),
                                        true
                                ),
                                new PrestigeConfig.EnderChestSettings(
                                        prestigeSettings.resetEnderChest(),
                                        true
                                ),
                                prestigeSettings.resetExp(),
                                prestigeSettings.resetMoney(),
                                true,
                                prestigeSettings.playTimeSettings()
                        ),
                        new PrestigeConfig.IslandSettings(
                                false,
                                false,
                                prestigeSettings.resetPrestigePoints(),
                                true),
                        prestigeSettings.giveStartingMoneyToAllIslandMembers(),
                        prestigeSettings.startingMoney(),
                        prestigeSettings.resetInventory(),
                        prestigeSettings.resetEnderChest(),
                        prestigeSettings.resetExp(),
                        prestigeSettings.resetMoney(),
                        prestigeSettings.playTimeSettings(),
                        prestigeSettings.resetPrestigePoints()
                );

                List<PrestigeConfig.ItemReward> itemRewardList = new ArrayList<>();
                List<PrestigeConfig.CommandReward> commandRewardList = new ArrayList<>();

                prestigeConfig.rewards().forEach(reward -> {
                    if(reward.rewardItem().itemType() != null && reward.displayItem().itemType() != null) {
                        itemRewardList.add(new PrestigeConfig.ItemReward(
                                reward.displayItem(),
                                reward.giveToAllIslandMembers(),
                                reward.rewardItem())
                        );
                    }

                    if(!reward.commands().isEmpty()) {
                        commandRewardList.add(new PrestigeConfig.CommandReward(
                                reward.displayItem(),
                                reward.giveToAllIslandMembers(),
                                reward.commands())
                        );
                    }
                });

                PrestigeConfig.RewardConfig rewardConfig = new PrestigeConfig.RewardConfig(
                        itemRewardList,
                        commandRewardList,
                        new ArrayList<>(),
                        new PrestigeConfig.IslandRangeReward(new ItemStackConfig(
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
                                new ItemStackConfig.OptionsConfig(null, null, null, null, null)),
                                0
                        )
                );

                return new PrestigeConfig(
                        "1.1.0.0",
                        prestigeConfig.prestigeLevel(),
                        prestigeConfig.scaleFactor(),
                        prestigeConfig.requiredPrestigePoints(),
                        newSettings,
                        rewardConfig,
                        prestigeConfig.rewards());
            }

            case null -> {
                return prestigeConfig;
            }

            default -> {
                skyPrestige.getComponentLogger().warn(AdventureUtil.deserialize("Unknown config version for prestige config. Unable to update config."));
                return prestigeConfig;
            }
        }
    }

    /**
     * Save the default prestige config for level 1 if it doesn't exist.
     */
    private void saveDefaultConfig() {
        Path path = Path.of(skyPrestige.getDataFolder() + File.separator + "prestige" + File.separator + "1.yml");
        if(!path.toFile().exists()) {
            skyPrestige.saveResource("prestige" + File.separator + "1.yml", false);
        }
    }
}
