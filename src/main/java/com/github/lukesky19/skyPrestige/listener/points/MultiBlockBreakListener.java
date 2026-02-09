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

import com.github.lukesky19.skyEnchants.api.event.MultiBlockBreakEvent;
import com.github.lukesky19.skyPrestige.configuration.data.points.PrestigePointsConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.PrestigePointsConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.hooks.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

/**
 * This class listens for when a multiple blocks have been broken by a custom enchantment from SkyEnchants and increments prestige points.
 */
public class MultiBlockBreakListener implements Listener {
    private final @NotNull ComponentLogger logger;
    private final @NotNull PrestigePointsConfigManager prestigePointsConfigManager;
    private final @NotNull IslandDataManager islandDataManager;
    private final @NotNull HookManager hookManager;
    private final @NotNull MultiplierManager multiplierManager;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     * @param prestigePointsConfigManager A {@link PrestigePointsConfigManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     */
    public MultiBlockBreakListener(
            @NotNull SkyPlugin plugin,
            @NotNull PrestigePointsConfigManager prestigePointsConfigManager,
            @NotNull IslandDataManager islandDataManager,
            @NotNull HookManager hookManager,
            @NotNull MultiplierManager multiplierManager) {
        this.logger = plugin.getComponentLogger();
        this.prestigePointsConfigManager = prestigePointsConfigManager;
        this.islandDataManager = islandDataManager;
        this.hookManager = hookManager;
        this.multiplierManager = multiplierManager;
    }

    /**
     * Listens for when multiple blocks have been broken by a custom enchantment from SkyEnchants and increments prestige points.
     * @param multiBlockBreakEvent A {@link MultiBlockBreakEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMultiBlockBreak(MultiBlockBreakEvent multiBlockBreakEvent) {
        @Nullable PrestigePointsConfig prestigePointsConfig = prestigePointsConfigManager.getConfiguration();
        if(prestigePointsConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to process prestige points due to invalid prestige points config."));
            return;
        }

        Player player = multiBlockBreakEvent.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID playerId = player.getUniqueId();

        // AFK check
        SkyPlayTimeHook skyPlayTimeHook = hookManager.getHook(SkyPlayTimeHook.class);
        if(skyPlayTimeHook.isHooked()
                && !prestigePointsConfig.awardPointsWhileAfk()
                && skyPlayTimeHook.isPlayerAFK(playerId)) return;

        // Island Check
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        Optional<Island> optionalIsland = bentoBoxHook.getIslandAtLocation(player.getLocation());
        if(optionalIsland.isEmpty()) return;
        Island island = optionalIsland.get();

        // Island Member Check
        if(!island.getMemberSet().contains(playerId)) return;

        // Island Data check.
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) {
            logger.error(AdventureUtil.deserialize("No island data found for island id " + island.getUniqueId() + "."));
            return;
        }

        // Check for prestige exemption
        if(islandData.isPrestigeExempt()) return;

        // Calculate prestige points to add
        double prestigePoints = 0;
        for(BlockState blockState : multiBlockBreakEvent.getBlocks()) {
            @Nullable BlockType blockType = blockState.getType().asBlockType();
            if(blockType == null) continue;
            @Nullable Double points = prestigePointsConfig.prestigePointsMapping().getBlockBreakPrestigePoints(blockType);
            if(points == null) continue;

            prestigePoints += points;
        }

        islandData.addPrestigePoints(prestigePoints * multiplierManager.getMultiplier(islandData));
    }
}