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
package com.github.lukesky19.skyPrestige.points.listener;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.points.context.EventContext;
import com.github.lukesky19.skyPrestige.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.settings.Settings;
import com.github.lukesky19.skyPrestige.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.util.type.BlockTypeUtils;
import com.github.lukesky19.skyPrestige.util.type.ItemTypeUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player removes wax from a block on an island and increments prestige points.
 */
public class PlayerUnwaxBlockListener extends PrestigePointsListener<PlayerInteractEvent> {
    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PlayerUnwaxBlockListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        super(skyPrestige, settingsManager, islandDataManager, hookManager);
    }

    /**
     * Listens for when a player waxes a block on an island and increments prestige points.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWaxBlock(PlayerInteractEvent playerInteractEvent) {
        skyPrestige.getServer().getScheduler().runTaskLater(skyPrestige, () -> process(playerInteractEvent), 1L);
    }

    @Override
    protected @NotNull EventContextExtractor<PlayerInteractEvent> extractor() {
        return playerInteractEvent -> {
            Player player = playerInteractEvent.getPlayer();
            if(!player.isOnline() || !player.isConnected()) return null;
            Action action = playerInteractEvent.getAction();
            if(action != Action.RIGHT_CLICK_BLOCK) return null;
            Block block = playerInteractEvent.getClickedBlock();
            if(block == null) return null;
            BlockType blockType = block.getType().asBlockType();
            if(blockType == null) return null;
            ItemStack itemStack = playerInteractEvent.getItem();
            if(itemStack == null) return null;
            ItemType itemType = itemStack.getType().asItemType();
            if(itemType == null) return null;
            if(!ItemTypeUtils.isItemTypeAxe(itemType)) return null;
            if(isBlockWaxed(block, blockType)) return null;

            EventContext eventContext = new EventContext();
            eventContext.setPlayer(player);
            eventContext.setItemType(itemType);
            eventContext.setBlockType(blockType);
            eventContext.setAmount(1);

            return eventContext;
        };
    }

    @Override
    protected void handle(@NotNull Settings settings, @NotNull IslandData islandData, @NotNull PlayerInteractEvent playerInteractEvent, @NotNull EventContext eventContext) {
        @Nullable BlockType blockType = eventContext.getBlockType();
        if(blockType == null) return;

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getStripPrestigePoints(blockType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * eventContext.getAmount());
    }

    /**
     * Checks if a {@link Block} is waxed.
     * @param block The {@link Block} to check.
     * @param blockType The {@link BlockType} of the block to check.
     * @return true if waxed, otherwise false.
     */
    private boolean isBlockWaxed(@NotNull Block block, @NotNull BlockType blockType) {
        if(BlockTypeUtils.isBlockTypeWaxed(blockType)) {
            return true;
        } else if(block.getState(false) instanceof Sign sign) {
            return sign.isWaxed();
        } else {
            return false;
        }
    }
}