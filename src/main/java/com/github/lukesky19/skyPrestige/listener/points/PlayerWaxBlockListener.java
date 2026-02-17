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
package com.github.lukesky19.skyPrestige.listener.points;

import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigePointsConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PointsListener;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.prestige.PrestigePointsManager;
import com.github.lukesky19.skyPrestige.util.block.BlockUtils;
import com.github.lukesky19.skyPrestige.util.enums.ActionType;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * Listens for when a player waxes a block on an island and increments prestige points.
 */
public class PlayerWaxBlockListener extends PointsListener {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param prestigePointsManager A {@link PrestigePointsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerWaxBlockListener(
            @NotNull SkyPlugin plugin,
            @NotNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NotNull PrestigePointsManager prestigePointsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, prestigePointsConfigManager, prestigePointsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player waxes a block on an island and increments prestige points.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWaxBlock(PlayerInteractEvent playerInteractEvent) {
        // Process 1 tick later to let the block be waxed first (if waxed at all)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Config
            @Nullable PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
            if(prestigePointsConfig == null) {
                logger.warn(AdventureUtil.deserialize("Unable to process prestige points due to invalid prestige points config."));
                return;
            }

            // Action
            Action action = playerInteractEvent.getAction();
            if(action != Action.RIGHT_CLICK_BLOCK) return;

            // Player
            @NotNull Player player = playerInteractEvent.getPlayer();
            if(!player.isOnline() || !player.isConnected()) return;
            @NotNull UUID playerId = player.getUniqueId();
            if(isPlayerInvalid(player, playerId, prestigePointsConfig)) return;

            // Island Check
            @Nullable Island island = checkIsland(player, playerId);
            if(island == null) return;

            // IslandData check.
            @Nullable IslandData islandData = checkIslandData(island);
            if(islandData == null) return;

            // Block
            Block block = playerInteractEvent.getClickedBlock();
            if(block == null) return;
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) return;
            if(!BlockUtils.isBlockWaxed(block, blockType)) return;

            // Item (Checks for honeycomb)
            ItemStack itemStack = playerInteractEvent.getItem();
            if(itemStack == null) return;
            ItemType itemType = itemStack.getType().asItemType();
            if(itemType == null) return;
            if(!itemType.equals(ItemType.HONEYCOMB)) return;

            // Points
            double points = prestigePointsManager.getBlockPoints(ActionType.WAX_BLOCK, prestigePointsConfig.prestigePointsMapping().waxBlock(), blockType, null, null, null);
            if(points <= 0) return;

            // Add points
            islandData.addPrestigePoints((points * 1) * multiplierManager.getMultiplier(islandData));
        }, 1L);
    }
}