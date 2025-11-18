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
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a player produces a bottle of something on an island and increments prestige points.
 */
public class PlayerBottleListener implements Listener {
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
    public PlayerBottleListener(
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
     * Listens for when a player produces a bottle of water from a cauldron on an island and increments prestige points.
     * @param playerInteractEvent A {@link PlayerInteractEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR) // Cancelled events are purposely not ignored here.
    public void onPlayerBottleWater(PlayerInteractEvent playerInteractEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = playerInteractEvent.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();

        Action action = playerInteractEvent.getAction();
        if(!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) return;

        // Get the ItemStack the player is holding
        @Nullable ItemStack itemStack = playerInteractEvent.getItem();
        // If the player is holding nothing, return
        if(itemStack == null || itemStack.isEmpty()) return;
        // Get the ItemType for the item the player is holding
        ItemType itemType = itemStack.getType().asItemType();
        // If the ItemType is null, return
        if(itemType == null) return;
        // If the item isn't a glass bottle, return
        if(!itemType.equals(ItemType.GLASS_BOTTLE)) return;

        // Ray trace to check if the player is looking at a water source block
        @Nullable RayTraceResult rayTraceResult = player.rayTraceBlocks(5, FluidCollisionMode.SOURCE_ONLY);
        // If no result was found, return
        if(rayTraceResult == null) return;

        // Get the block ray traced
        @Nullable Block block = rayTraceResult.getHitBlock();
        // If the block is null, return
        if(block == null) return;

        // Get the BlockType
        BlockType blockType = block.getType().asBlockType();
        // If the BlockType is null, return
        if(blockType == null) return;
        // If the BlockType is not water, return
        if(!blockType.equals(BlockType.WATER)) return;

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

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getBottlePrestigePoints(itemType, PotionType.WATER);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints);
    }

    /**
     * Listens for when a player produces a bottle of dragon's breath on an island and increments prestige points.
     * @param playerInteractEntityEvent A {@link PlayerInteractEntityEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBottleDragonBreath(PlayerInteractEntityEvent playerInteractEntityEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Player player = playerInteractEntityEvent.getPlayer();
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        Entity entity = playerInteractEntityEvent.getRightClicked();
        if(!(entity instanceof AreaEffectCloud areaEffectCloud)) return;
        @Nullable ProjectileSource projectileSource = areaEffectCloud.getSource();
        if(projectileSource == null) return;
        if(!(projectileSource instanceof EnderDragon)) return;

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

        @Nullable Double prestigePoints = settings.prestigePointsMapping().bottle().getItemPrestigePoints(ItemType.DRAGON_BREATH);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints);
    }
}
