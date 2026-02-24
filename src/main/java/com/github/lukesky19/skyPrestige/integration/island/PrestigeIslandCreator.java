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
package com.github.lukesky19.skyPrestige.integration.island;

import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.processor.island.IslandSettingsProcessor;
import com.github.lukesky19.skyPrestige.requirements.RequirementsManager;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.Map;

/**
 * This class can be used to create a new {@link Island} from an old {@link Island}.
 */
public class PrestigeIslandCreator extends AbstractIslandCreator {
    private final @NonNull RequirementsManager requirementsManager;

    private final @NonNull List<Player> onlinePlayerList;
    private final @NonNull List<OfflinePlayer> offlinePlayerList;

    private final @NonNull PrestigeConfig prestigeConfig;
    private final int prestigeLevel;

    private final @NonNull IslandData islandData;

    private final double requiredPrestigePoints;
    private final double requiredMoney;
    private final @NonNull Map<ItemStack, Boolean> requiredItems;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param islandSettingsProcessor An {@link IslandSettingsProcessor} instance.
     * @param requirementsManager A {@link RequirementsManager} instance.
     * @param player The {@link Player} creating the island.
     * @param onlinePlayerList The {@link List} of {@link Player}s for the online island members.
     * @param offlinePlayerList The {@link List} of {@link OfflinePlayer}s for all island members, regardless if online or not.
     * @param world The {@link World} the island is being created in.
     * @param gameModeAddon the {@link GameModeAddon} the island is being created for.
     * @param blueprintName The blueprint name to use.
     * @param oldIsland The old {@link Island}.
     * @param islandData The old {@link Island}'s {@link IslandData}.
     * @param prestigeConfig The {@link PrestigeConfig} for the prestige level.
     * @param prestigeLevel The prestige level. This is the level being achieved for the island.
     * @param requiredPrestigePoints The prestige points required to reset the island.
     * @param requiredMoney The money required to reset the island.
     * @param requiredItemsMap The {@link Map} mapping {@link ItemStack}s to {@link Boolean}s that are required to reset the island. The booleans are for whether the item should be removed or not.
     */
    public PrestigeIslandCreator(
            @NonNull SkyPlugin plugin,
            @NonNull DatabaseManager databaseManager,
            @NonNull HookManager hookManager,
            @NonNull IslandSettingsProcessor islandSettingsProcessor,
            @NonNull RequirementsManager requirementsManager,
            @NonNull Player player,
            @NonNull List<Player> onlinePlayerList,
            @NonNull List<OfflinePlayer> offlinePlayerList,
            @NonNull World world,
            @NonNull GameModeAddon gameModeAddon,
            @NonNull String blueprintName,
            @NonNull Island oldIsland,
            @NonNull IslandData islandData,
            @NonNull PrestigeConfig prestigeConfig,
            int prestigeLevel,
            double requiredPrestigePoints,
            double requiredMoney,
            @NonNull Map<ItemStack, Boolean> requiredItemsMap) {
        super(plugin, databaseManager, hookManager, islandSettingsProcessor, player,  world, gameModeAddon,  blueprintName, oldIsland);

        this.requirementsManager = requirementsManager;

        this.onlinePlayerList = onlinePlayerList;
        this.offlinePlayerList = offlinePlayerList;

        this.prestigeConfig = prestigeConfig;
        this.prestigeLevel = prestigeLevel;

        this.islandData = islandData.clone();

        this.requiredPrestigePoints = requiredPrestigePoints;
        this.requiredMoney = requiredMoney;
        this.requiredItems = requiredItemsMap;
    }

    @Override
    protected void beforeBlueprintPaste() {
        if(newIsland == null) return;

        // Process Requirements
        requirementsManager.removeRequiredItems(onlinePlayerList, requiredItems);
        if(prestigeConfig.moneyRequirement().removeMoney()) {
            requirementsManager.removeRequiredMoney(offlinePlayerList, requiredMoney);
        }
        if(prestigeConfig.prestigePointsRequirement().removePrestigePoints()) {
            requirementsManager.removeRequiredPrestigePoints(islandData, requiredPrestigePoints);
        }

        islandData.setPrestigeLevel(prestigeLevel);

        // Process IslandSettings
        islandSettingsProcessor.processIslandSettings(player, prestigeConfig.prestigeSettings().islandSettings(), oldIsland, newIsland, islandData);
    }

    @Override
    protected void beforeDeletion() {}

    @Override
    protected void afterDeletion() {}
}