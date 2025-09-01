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
import com.github.lukesky19.skyPrestige.config.Settings;
import com.github.lukesky19.skyPrestige.data.IslandData;
import com.github.lukesky19.skyPrestige.hook.impl.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.manager.config.SettingsManager;
import com.github.lukesky19.skyPrestige.manager.hook.HookManager;
import com.github.lukesky19.skyPrestige.manager.island.IslandDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import dev.rosewood.rosestacker.event.*;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedEntity;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.GameMode;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a block or spawner is stacked or destacked and when a stacked entity died on an island and increments prestige points.
 */
public class RoseStackerListener implements Listener {
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
    public RoseStackerListener(
            @NotNull SkyPrestige skyPrestige,
            @NotNull SettingsManager settingsManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager) {
        this.logger = skyPrestige.getComponentLogger();
        this.settingsManager = settingsManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
    }

    /**
     * Listens for when a block is stacked on an island and increments prestige points.
     * @param blockStackEvent A {@link BlockStackEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockStack(BlockStackEvent blockStackEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = blockStackEvent.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        StackedBlock stackedBlock = blockStackEvent.getStack();
        BlockType blockType = stackedBlock.getBlock().getType().asBlockType();
        if(blockType == null) return;
        int amount = blockStackEvent.getIncreaseAmount();

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
            logger.error(AdventureUtil.serialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getBlockPlacePrestigePoints(blockType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * amount);
    }

    /**
     * Listens for when a block is unstacked on an island and increments prestige points.
     * @param blockUnstackEvent A {@link BlockUnstackEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockUnstack(BlockUnstackEvent blockUnstackEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = blockUnstackEvent.getPlayer();
        if(player == null) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = blockUnstackEvent.getPlayer().getUniqueId();
        StackedBlock stackedBlock = blockUnstackEvent.getStack();
        BlockType blockType = stackedBlock.getBlock().getType().asBlockType();
        if(blockType == null) return;
        int amount = blockUnstackEvent.getDecreaseAmount();

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
            logger.error(AdventureUtil.serialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getBlockBreakPrestigePoints(blockType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * amount);
    }

    /**
     * Listens for when a spawner is stacked on an island and increments prestige points.
     * @param spawnerStackEvent A {@link SpawnerStackEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerStack(SpawnerStackEvent spawnerStackEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = spawnerStackEvent.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        StackedSpawner stackedSpawner = spawnerStackEvent.getStack();
        Spawner spawner = stackedSpawner.getSpawner();
        EntityType entityType = spawner.getSpawnedType();
        BlockType blockType = stackedSpawner.getBlock().getType().asBlockType();
        if(blockType == null) return;
        int amount = spawnerStackEvent.getIncreaseAmount();

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
            logger.error(AdventureUtil.serialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        if(entityType != null) {
            @Nullable Double prestigePoints = settings.prestigePointsMapping().getBlockPlacePrestigePoints(blockType, entityType);
            if(prestigePoints != null) {
                islandData.addPrestigePoints(prestigePoints * amount);
                return;
            }
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().blockPlace().getBlockPrestigePoints(blockType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * amount);
    }

    /**
     * Listens for when a spawner is unstacked on an island and increments prestige points.
     * @param spawnerUnstackEvent A {@link SpawnerUnstackEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerUnstack(SpawnerUnstackEvent spawnerUnstackEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = spawnerUnstackEvent.getPlayer();
        if(player == null) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        StackedSpawner stackedSpawner = spawnerUnstackEvent.getStack();
        Spawner spawner = stackedSpawner.getSpawner();
        EntityType entityType = spawner.getSpawnedType();
        BlockType blockType = stackedSpawner.getBlock().getType().asBlockType();
        if(blockType == null) return;
        int amount = spawnerUnstackEvent.getDecreaseAmount();

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
            logger.error(AdventureUtil.serialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        if(entityType != null) {
            @Nullable Double prestigePoints = settings.prestigePointsMapping().getBlockBreakPrestigePoints(blockType, entityType);
            if(prestigePoints != null) {
                islandData.addPrestigePoints(prestigePoints * amount);
                return;
            }
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().blockBreak().getBlockPrestigePoints(blockType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * amount);
    }

    /**
     * Listens for when a stacked entity died on an island and increments prestige points.
     * @param entityStackMultipleDeathEvent A {@link EntityStackMultipleDeathEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStackedEntityDeath(EntityStackMultipleDeathEvent entityStackMultipleDeathEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        StackedEntity stackedEntity = entityStackMultipleDeathEvent.getStack();
        EntityType entityType = stackedEntity.getEntity().getType();
        Player player = stackedEntity.getEntity().getKiller();
        if(player == null) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        int amountKilled = entityStackMultipleDeathEvent.getEntityKillCount();

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
            logger.error(AdventureUtil.serialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getKillPrestigePoints(entityType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * amountKilled);
    }
}
