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

import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.core.abstracts.SkyPlugin;
import com.github.lukesky19.skyPrestige.core.util.type.BlockTypeUtils;
import com.github.lukesky19.skyPrestige.data.island.IslandData;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.listener.points.abstracts.PrestigePointsListener;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContext;
import com.github.lukesky19.skyPrestige.listener.points.context.EventContextExtractor;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for when a player waxes a block on an island and increments prestige points.
 */
public class PlayerWaxBlockListener extends PrestigePointsListener<PlayerInteractEvent> {
    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public PlayerWaxBlockListener(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        super(plugin, settingsManager, islandDataManager, hookManager, multiplierManager);
    }

    /**
     * Listens for when a player waxes a block on an island and increments prestige points.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWaxBlock(PlayerInteractEvent playerInteractEvent) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> process(playerInteractEvent), 1L);
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
            if(!itemType.equals(ItemType.HONEYCOMB)) return null;
            if(isBlockNotWaxed(block, blockType)) return null;

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

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getWaxPrestigePoints(blockType);
        if(prestigePoints == null) return;

        addPrestigePoints(islandData, prestigePoints, eventContext.getAmount());
    }

    /**
     * Checks if a {@link Block} is not waxed.
     * @param block The {@link Block} to check.
     * @param blockType The {@link BlockType} of the block to check.
     * @return true if not waxed, otherwise false.
     */
    private boolean isBlockNotWaxed(@NotNull Block block, @NotNull BlockType blockType) {
        if(BlockTypeUtils.isBlockTypeWaxed(blockType)) {
            return false;
        } else if(block.getState(false) instanceof Sign sign) {
            return !sign.isWaxed();
        } else {
            return true;
        }
    }
}