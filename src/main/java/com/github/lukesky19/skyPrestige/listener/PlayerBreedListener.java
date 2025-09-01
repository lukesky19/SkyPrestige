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
import com.github.lukesky19.skyPrestige.hook.impl.RoseStackerHook;
import com.github.lukesky19.skyPrestige.hook.impl.SkyPlayTimeHook;
import com.github.lukesky19.skyPrestige.manager.config.SettingsManager;
import com.github.lukesky19.skyPrestige.manager.hook.HookManager;
import com.github.lukesky19.skyPrestige.manager.island.IslandDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a player breeds two entities on an island and increments prestige points.
 */
public class PlayerBreedListener implements Listener {
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
    public PlayerBreedListener(
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
     * Listens for when a player breeds two entities on an island and increments prestige points.
     * @param entityBreedEvent An {@link EntityBreedEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent entityBreedEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        LivingEntity breeder = entityBreedEvent.getBreeder();
        if(!(breeder instanceof Player player)) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        LivingEntity bredEntity = entityBreedEvent.getEntity();
        EntityType entityType = bredEntity.getType();

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

        int amount;
        RoseStackerHook roseStackerHook = hookManager.getHook(RoseStackerHook.class);
        if(roseStackerHook.isHooked()) {
            int stackSize = roseStackerHook.getStackSize(bredEntity);
            if(stackSize == 1) {
                amount = 1;
            } else {
                amount = getBredAmount(stackSize);
            }
        } else {
            amount = 1;
        }

        @Nullable Double prestigePoints = settings.prestigePointsMapping().getBreedPrestigePoints(entityType);
        if(prestigePoints == null) return;

        islandData.addPrestigePoints(prestigePoints * amount);
    }

    /**
     * Calculate the amount of entities bred from a stack size.
     * If the stack size is less than or equal to 0, 0 is returned.
     * If the stack size is not divisible by 2, 1 is subtracted from the stack size before dividing by 2.
     * Otherwise, the stack size divided by two is returned.
     * @param stackSize The amount of entities stacked.
     * @return The amount of entities bred.
     */
    private int getBredAmount(int stackSize) {
        if(stackSize <= 0) return 0;

        if (stackSize % 2 != 0) {
            stackSize = stackSize - 1;
            if(stackSize <= 0) return 0;
        }

        return stackSize / 2;
    }
}
