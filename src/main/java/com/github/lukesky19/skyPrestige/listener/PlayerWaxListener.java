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
package com.github.lukesky19.skyPrestige.listener;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.settings.Settings;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.hook.HookManager;
import com.github.lukesky19.skyPrestige.hook.impl.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skyPrestige.island.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.util.BlockTypeUtils;
import com.github.lukesky19.skyPrestige.util.ItemTypeUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.version.VersionUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.Sign;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a player waxes a block or removes wax from a block on an island and increments prestige points.
 */
public class PlayerWaxListener implements Listener {
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     */
    public PlayerWaxListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        this.skyPrestige = skyPrestige;
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens for when a player waxes a block on an island and increments prestige points.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWaxBlock(PlayerInteractEvent playerInteractEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = playerInteractEvent.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        Action action = playerInteractEvent.getAction();
        if(action != Action.RIGHT_CLICK_BLOCK) return;
        Block block = playerInteractEvent.getClickedBlock();
        if(block == null) return;
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return;
        ItemStack itemStack = playerInteractEvent.getItem();
        if(itemStack == null) return;
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;
        if(!itemType.equals(ItemType.HONEYCOMB)) return;

        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        if(skyPlayTimeHook.isHooked()) {
            if(!settings.awardPointsWhileAfk()) {
                if(skyPlayTimeHook.isPlayerAFK(uuid)) return;
            }
        }

        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(player.getLocation());
        if(optionalIsland.isEmpty()) return;
        Island island = optionalIsland.get();
        if(!island.getMemberSet().contains(uuid)) return;

        String islandId = island.getUniqueId();
        IslandData islandData = islandDataManager.getIslandData(islandId);
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        skyPrestige.getServer().getScheduler().runTaskLater(skyPrestige, () -> {
            if(!isBlockWaxed(block)) return;

            @Nullable Double prestigePoints = settings.prestigePointsMapping().getWaxPrestigePoints(blockType);
            if(prestigePoints == null) return;

            islandData.addPrestigePoints(prestigePoints);
        }, 1L);
    }

    /**
     * Listens for when a player waxes an entity on an island and increments prestige points.
     * @param playerInteractAtEntityEvent A {@link PlayerInteractAtEntityEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWaxEntity(PlayerInteractAtEntityEvent playerInteractAtEntityEvent) {
        // Don't listen to waxing entities if on a version without Copper Golems (< 1.21.9)
        if(VersionUtil.getMajorVersion() < 21 || (VersionUtil.getMajorVersion() == 21 && VersionUtil.getMinorVersion() < 9)) return;

        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = playerInteractAtEntityEvent.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        Entity entity = playerInteractAtEntityEvent.getRightClicked();
        if(!(entity instanceof CopperGolem copperGolem)) return;
        if(copperGolem.getOxidizing().equals(CopperGolem.Oxidizing.waxed())) return;
        EntityType entityType = entity.getType();
        ItemType itemTypeUsed = player.getInventory().getItemInMainHand().getType().asItemType();
        if(itemTypeUsed == null) return;
        if(!itemTypeUsed.equals(ItemType.HONEYCOMB)) return;

        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        if(skyPlayTimeHook.isHooked()) {
            if(!settings.awardPointsWhileAfk()) {
                if(skyPlayTimeHook.isPlayerAFK(uuid)) return;
            }
        }

        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(player.getLocation());
        if(optionalIsland.isEmpty()) return;
        Island island = optionalIsland.get();
        if(!island.getMemberSet().contains(uuid)) return;

        String islandId = island.getUniqueId();
        IslandData islandData = islandDataManager.getIslandData(islandId);
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getWaxPrestigePoints(entityType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints);
    }

    /**
     * Listens for when a player removes wax from a block on an island and increments prestige points.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRemoveWax(PlayerInteractEvent playerInteractEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = playerInteractEvent.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        Action action = playerInteractEvent.getAction();
        if(action != Action.RIGHT_CLICK_BLOCK) return;
        Block block = playerInteractEvent.getClickedBlock();
        if(block == null) return;
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return;
        ItemStack itemStack = playerInteractEvent.getItem();
        if(itemStack == null) return;
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;
        if(!ItemTypeUtils.isItemTypeAxe(itemType)) return;

        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        if(skyPlayTimeHook.isHooked()) {
            if(!settings.awardPointsWhileAfk()) {
                if(skyPlayTimeHook.isPlayerAFK(uuid)) return;
            }
        }

        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(player.getLocation());
        if(optionalIsland.isEmpty()) return;
        Island island = optionalIsland.get();
        if(!island.getMemberSet().contains(uuid)) return;

        String islandId = island.getUniqueId();
        IslandData islandData = islandDataManager.getIslandData(islandId);
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        skyPrestige.getServer().getScheduler().runTaskLater(skyPrestige, () -> {
            if(isBlockWaxed(block)) return;

            @Nullable Double prestigePoints = settings.prestigePointsMapping().getStripPrestigePoints(blockType);
            if(prestigePoints == null) return;

            islandData.addPrestigePoints(prestigePoints);
        }, 1L);
    }

    /**
     * Listens for when a player removes wax from an entity on an island and increments prestige points.
     * @param playerInteractAtEntityEvent A {@link PlayerInteractAtEntityEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRemoveWax(PlayerInteractAtEntityEvent playerInteractAtEntityEvent) {
        // Don't listen to waxing entities if on a version without Copper Golems (< 1.21.9)
        if(VersionUtil.getMajorVersion() < 21 || (VersionUtil.getMajorVersion() == 21 && VersionUtil.getMinorVersion() < 9)) return;

        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = playerInteractAtEntityEvent.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        Entity entity = playerInteractAtEntityEvent.getRightClicked();
        if(!(entity instanceof CopperGolem copperGolem)) return;
        if(!copperGolem.getOxidizing().equals(CopperGolem.Oxidizing.waxed())) return;
        EntityType entityType = entity.getType();

        ItemStack itemStack = player.getInventory().getItem(playerInteractAtEntityEvent.getHand());
        if(itemStack.isEmpty()) return;
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;
        if(!ItemTypeUtils.isItemTypeAxe(itemType)) return;

        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        if(skyPlayTimeHook.isHooked()) {
            if(!settings.awardPointsWhileAfk()) {
                if(skyPlayTimeHook.isPlayerAFK(uuid)) return;
            }
        }

        Optional<Island> optionalIsland = BentoBox.getInstance().getIslandsManager().getIslandAt(player.getLocation());
        if(optionalIsland.isEmpty()) return;
        Island island = optionalIsland.get();
        if(!island.getMemberSet().contains(uuid)) return;

        String islandId = island.getUniqueId();
        IslandData islandData = islandDataManager.getIslandData(islandId);
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getStripPrestigePoints(entityType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints);
    }

    /**
     * Checks if a {@link Block} is waxed.
     * @param block The {@link Block} to check.
     * @return true if waxed, otherwise false.
     */
    private boolean isBlockWaxed(@NotNull Block block) {
        BlockType blockType = block.getType().asBlockType();
        if(blockType == null) return false;

        if(BlockTypeUtils.isBlockTypeWaxed(blockType)) {
            return true;
        } else if(block.getState(false) instanceof Sign sign) {
            return sign.isWaxed();
        } else {
            return false;
        }
    }
}
