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
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

/**
 * Listens for when a player uses a cauldron on an island and increments prestige points.
 */
public class CauldronListener implements Listener {
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
    public CauldronListener(
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
     * Listens for when a player changes a cauldron's level on an island and increments prestige points.
     * @param cauldronLevelChangeEvent A {@link CauldronLevelChangeEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCauldronLevelChange(CauldronLevelChangeEvent cauldronLevelChangeEvent) {
        Settings settings = settingsManager.getSettings();
        if(settings == null) return;

        Entity entity = cauldronLevelChangeEvent.getEntity();
        if(!(entity instanceof Player player)) return;
        if(player.getGameMode().equals(GameMode.CREATIVE)) return;
        UUID uuid = player.getUniqueId();
        CauldronLevelChangeEvent.ChangeReason reason = cauldronLevelChangeEvent.getReason();

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

        switch(reason) {
            case BOTTLE_FILL -> {
                ItemType itemType = Material.POTION.asItemType();
                if(itemType == null) return;

                @Nullable Double prestigePoints = settings.prestigePointsMapping().getBottlePrestigePoints(itemType, PotionType.WATER);
                if(prestigePoints == null) return;

                islandData.addPrestigePoints(prestigePoints);
            }

            case BUCKET_FILL -> {
                if(cauldronLevelChangeEvent.getBlock().getBlockData() instanceof Levelled levelled) {
                    ItemType itemType = levelled.getMaterial().asItemType();
                    if(itemType == null) return;

                    @Nullable Double prestigePoints = settings.prestigePointsMapping().getFillPrestigePoints(itemType);
                    if(prestigePoints == null) return;

                    islandData.addPrestigePoints(prestigePoints);
                }
            }

            case BUCKET_EMPTY -> {
                if(cauldronLevelChangeEvent.getBlock().getBlockData() instanceof Levelled levelled) {
                    ItemType itemType = levelled.getMaterial().asItemType();
                    if(itemType == null) return;

                    @Nullable Double prestigePoints = settings.prestigePointsMapping().getEmptyPrestigePoints(itemType);
                    if(prestigePoints == null) return;

                    islandData.addPrestigePoints(prestigePoints);
                }
            }
        }
    }
}
