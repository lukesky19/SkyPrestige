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
package com.github.lukesky19.skyPrestige.data.data.island;

import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.ResetSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.inventory.InventorySettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.island.IslandSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.player.PlayerSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.playtime.PlayTimeSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reward.IslandRangeReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.blueprints.dataobjects.BlueprintBundle;
import world.bentobox.bentobox.database.objects.Island;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests {@link IslandResetData}.
 */
@ExtendWith(MockitoExtension.class)
public class IslandResetDataTest {
    @Mock
    private Player player;
    @Mock
    private User user;
    @Mock
    private Island island;
    @Mock
    private IslandData islandData;
    @Mock
    private GameModeAddon gameModeAddon;
    @Mock
    private BlueprintBundle blueprintBundle;

    private static PrestigeConfig prestigeConfig;

    /**
     * Setup any data required for the test.
     */
    @BeforeAll
    public static void beforeAll() {
        prestigeConfig = new PrestigeConfig(
                "1.0.0.0",
                1,
                1.5,
                100.0,
                new ResetSettings(
                        new IslandSettings(
                                true,
                                true,
                                true,
                                true,
                                false,
                                false,
                                false),
                        new PlayerSettings(
                                new InventorySettings(
                                        true,
                                        true,
                                        true),
                                new InventorySettings(
                                        true,
                                        true,
                                        true),
                                true,
                                true,
                                true,
                                new PlayTimeSettings(
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false)),
                        true,
                        2000.0),
                new RewardConfig(
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new IslandRangeReward(
                                new ItemStackConfig(
                                        null,
                                        null,
                                        null,
                                        null,
                                        new ArrayList<>(),
                                        null,
                                        null,
                                        new ArrayList<>(),
                                        new ItemStackConfig.PotionConfig(
                                                null,
                                                new ArrayList<>()),
                                        new ItemStackConfig.ColorConfig(
                                                false,
                                                null,
                                                null,
                                                null),
                                        null,
                                        new ArrayList<>(),
                                        new ItemStackConfig.DecoratedPotConfig(null, null, null, null),
                                        new ItemStackConfig.ArmorTrimConfig(null, null),
                                        new ArrayList<>(),
                                        new ItemStackConfig.OptionsConfig(false, false, false, false, false)),
                                false,
                                2)));
    }

    /**
     * Test that the default constructor throws an error.
     */
    @Test
    public void testDefaultIslandResetDataConstructor() {
        assertThrows(RuntimeException.class, IslandResetData::new);
    }

    /**
     * Test the constructor for prestige island reset data.
     */
    @Test
    public void testPrestigeIslandResetDataConstructor() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNotNull(islandResetData);
    }

    /**
     * Test the constructor for non-prestige island reset data.
     */
    @Test
    public void testNonPrestigeIslandResetDataConstructor() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        assertNotNull(islandResetData);
    }

    /**
     * Test that the island reset data is for prestige.
     */
    @Test
    public void testIsPrestigePrestige() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertTrue(islandResetData.isPrestige());
    }

    /**
     * Test that the island reset data is not for prestige by prestige config being null.
     */
    @Test
    public void testIsPrestigeNonPrestigeByPrestigeConfig() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        assertFalse(islandResetData.isPrestige());
    }

    /**
     * Test that the island reset data is not for prestige by prestige level being null.
     */
    @Test
    public void testIsPrestigeNonPrestigeByPrestigeLevel() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        islandResetData.setPrestigeConfig(prestigeConfig);

        assertFalse(islandResetData.isPrestige());
    }

    /**
     * Test that the island reset data is not for prestige by prestige points being null.
     */
    @Test
    public void testIsPrestigeNonPrestigeByPrestigePoints() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        islandResetData.setPrestigeConfig(prestigeConfig);
        islandResetData.setPrestigeLevel(0);

        assertFalse(islandResetData.isPrestige());
    }

    /**
     * Test the retrieval of the player.
     */
    @Test
    public void testGetPlayer() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNotNull(islandResetData.getPlayer());
        assertEquals(player, islandResetData.getPlayer());
    }

    /**
     * Test the retrieval of the user.
     */
    @Test
    public void testGetUser() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNotNull(islandResetData.getUser());
        assertEquals(user, islandResetData.getUser());
    }

    /**
     * Test the retrieval of the old island.
     */
    @Test
    public void testGetOldIsland() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNotNull(islandResetData.getOldIsland());
        assertEquals(island, islandResetData.getOldIsland());
    }

    /**
     * Test the retrieval of the old island data.
     */
    @Test
    public void testGetOldIslandData() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNotNull(islandResetData.getOldIslandData());
        assertEquals(islandData, islandResetData.getOldIslandData());
    }

    /**
     * Test the retrieval of the player.
     */
    @Test
    public void testGetGameModeAddon() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNotNull(islandResetData.getGameModeAddon());
        assertEquals(gameModeAddon, islandResetData.getGameModeAddon());
    }

    /**
     * Test the setting and getting of the blueprint.
     */
    @Test
    public void testSetGetBlueprint() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        islandResetData.setBlueprint(blueprintBundle);

        assertNotNull(islandResetData.getBlueprint());
        assertEquals(blueprintBundle, islandResetData.getBlueprint());
    }

    /**
     * Test the getting of the blueprint, but it's null
     */
    @Test
    public void testGetBlueprintNull() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNull(islandResetData.getBlueprint());
        assertNotEquals(blueprintBundle, islandResetData.getBlueprint());
    }

    /**
     * Test the getting of the prestige config.
     */
    @Test
    public void testGetPrestigeConfig() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNotNull(islandResetData.getPrestigeConfig());
        assertEquals(prestigeConfig, islandResetData.getPrestigeConfig());
    }

    /**
     * Test the getting of the prestige config, but it's null.
     */
    @Test
    public void testGetPrestigeConfigNull() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        assertNull(islandResetData.getPrestigeConfig());
        assertNotEquals(prestigeConfig, islandResetData.getPrestigeConfig());
    }

    /**
     * Test the setting of the prestige config.
     */
    @Test
    public void testSetPrestigeConfig() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        islandResetData.setPrestigeConfig(prestigeConfig);

        assertNotNull(islandResetData.getPrestigeConfig());
        assertEquals(prestigeConfig, islandResetData.getPrestigeConfig());
    }

    /**
     * Test the getting of the prestige level.
     */
    @Test
    public void testGetPrestigeLevel() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNotNull(islandResetData.getPrestigeLevel());
        assertEquals(0, islandResetData.getPrestigeLevel());
    }

    /**
     * Test the getting of the prestige level, but it's null.
     */
    @Test
    public void testGetPrestigeLevelNull() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        assertNull(islandResetData.getPrestigeLevel());
    }

    /**
     * Test the setting of the prestige level.
     */
    @Test
    public void testSetPrestigeLevel() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        islandResetData.setPrestigeLevel(0);

        assertNotNull(islandResetData.getPrestigeLevel());
        assertEquals(0, islandResetData.getPrestigeLevel());
    }

    /**
     * Test the getting of the required prestige points.
     */
    @Test
    public void testGetPrestigePoints() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon,
                prestigeConfig,
                0,
                100);

        assertNotNull(islandResetData.getPrestigePoints());
        assertEquals(100, islandResetData.getPrestigePoints());
    }

    /**
     * Test the getting of the required prestige points, but it's null.
     */
    @Test
    public void testGetPrestigePointsNull() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        assertNull(islandResetData.getPrestigePoints());
    }

    /**
     * Test the setting of the required prestige points.
     */
    @Test
    public void testSetPrestigePoints() {
        IslandResetData islandResetData = new IslandResetData(
                player,
                user,
                island,
                islandData,
                gameModeAddon);

        islandResetData.setPrestigePoints(100.0);

        assertNotNull(islandResetData.getPrestigePoints());
        assertEquals(100.0, islandResetData.getPrestigePoints());
    }
}
