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
package com.github.lukesky19.skyPrestige.integration.hooks;

import com.github.lukesky19.skyPrestige.integration.interfaces.Hook;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class manages interfacing with the MagicCobblestoneGenerator addon.
 */
public class MagicCobblestoneGeneratorHook implements Hook {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull ComponentLogger logger;
    private StoneGeneratorAddon stoneGeneratorAddon;
    private StoneGeneratorManager stoneGeneratorManager;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     */
    public MagicCobblestoneGeneratorHook(@NotNull SkyPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getComponentLogger();
    }

    /**
     * Attempt to get the magic cobblestone generator addon from BentoBox.
     */
    @Override
    public void initialize() {
        Optional<Addon> optionalAddon = BentoBox.getInstance().getAddonsManager().getAddonByName("MagicCobblestoneGenerator");
        if(optionalAddon.isEmpty()) {
            logger.error(AdventureUtil.deserialize("No MagicCobblestoneGenerator Addon!"));
            return;
        }

        stoneGeneratorAddon = (StoneGeneratorAddon) optionalAddon.get();
        stoneGeneratorManager = stoneGeneratorAddon.getAddonManager();
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        if(stoneGeneratorAddon == null) return false;
        if(stoneGeneratorManager == null) stoneGeneratorManager = stoneGeneratorAddon.getAddonManager();

        return stoneGeneratorAddon != null && stoneGeneratorManager != null;
    }

    /**
     * Copy the generator data from the old island to the new island.
     * @apiNote If the addon isn't hooked into or any island data is null, no copying will occur.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}.
     */
    public void copyGeneratorData(@NotNull Island oldIsland, @NotNull Island newIsland) {
        if(!isHooked()) {
            logger.warn(AdventureUtil.deserialize("MagicCobblestoneGenerator not hooked into."));
            return;
        }
        if(oldIsland.getUniqueId().equals(newIsland.getUniqueId())) return;

        @Nullable GeneratorDataObject oldIslandGeneratorData = stoneGeneratorManager.validateIslandData(oldIsland);
        if(oldIslandGeneratorData == null) {
            logger.error(AdventureUtil.deserialize("Failed to copy generator data due to invalid island generator data for the old island."));
            return;
        }
        @Nullable GeneratorDataObject newIslandGeneratorData = stoneGeneratorManager.validateIslandData(newIsland);
        if(newIslandGeneratorData == null) {
            logger.error(AdventureUtil.deserialize("Failed to copy generator data due to invalid island generator data for the new island."));
            return;
        }

        // Set the island/unique id
        newIslandGeneratorData.setUniqueId(newIsland.getUniqueId());

        // Copy Unlocked Tiers
        newIslandGeneratorData.setUnlockedTiers(new HashSet<>(oldIslandGeneratorData.getUnlockedTiers()));

        // Copy Purchased Tiers
        newIslandGeneratorData.setPurchasedTiers(new HashSet<>(oldIslandGeneratorData.getPurchasedTiers()));

        // Copy Active Tiers
        newIslandGeneratorData.setActiveGeneratorList(new HashSet<>(oldIslandGeneratorData.getActiveGeneratorList()));

        // Copy active generator count
        newIslandGeneratorData.setIslandActiveGeneratorCount(oldIslandGeneratorData.getIslandActiveGeneratorCount());
        newIslandGeneratorData.setOwnerActiveGeneratorCount(oldIslandGeneratorData.getOwnerActiveGeneratorCount());

        // Copy Bundles
        newIslandGeneratorData.setIslandBundle(oldIslandGeneratorData.getIslandBundle());
        newIslandGeneratorData.setOwnerBundle(oldIslandGeneratorData.getOwnerBundle());

        // Copy Working Ranges
        newIslandGeneratorData.setIslandWorkingRange(oldIslandGeneratorData.getIslandWorkingRange());
        newIslandGeneratorData.setOwnerWorkingRange(oldIslandGeneratorData.getOwnerWorkingRange());

        // Save updated data
        stoneGeneratorManager.saveGeneratorData(newIslandGeneratorData);

        // Delete old data
        stoneGeneratorManager.wipeGeneratorData(oldIslandGeneratorData);
    }

    /**
     * Reset the generator data for the island.
     * @apiNote If the addon isn't hooked into or any island data is null, no copying will occur.
     * @param island The {@link Island}.
     */
    public void resetGeneratorData(@NotNull Island island) {
        if(!isHooked()) {
            logger.warn(AdventureUtil.deserialize("MagicCobblestoneGenerator not hooked into."));
            return;
        }

        @Nullable GeneratorDataObject generatorData = stoneGeneratorManager.validateIslandData(island);
        if(generatorData == null) {
            logger.error(AdventureUtil.deserialize("Failed to reset generator data due to invalid island generator data for the island."));
            return;
        }

        // Reset Unlocked Tiers
        Set<Player> onlineIslandMembers = island.getMemberSet()
                .stream()
                .map(memberId -> plugin.getServer().getPlayer(memberId))
                .filter(member -> member != null && member.isOnline() && member.isConnected())
                .collect(Collectors.toSet());
        Set<String> unlockedTiers = new HashSet<>();
        stoneGeneratorManager.getAllGeneratorTiers(island.getWorld())
                .stream()
                .filter(GeneratorTierObject::isDeployed)
                .forEach(tier -> {
                    Set<String> permissions = tier.getRequiredPermissions();

                    if(permissions.isEmpty()) {
                        unlockedTiers.add(tier.getUniqueId());
                    } else {
                        boolean tierUnlocked = true;
                        for(String permission : permissions) {
                            if(onlineIslandMembers.stream().noneMatch(member -> member.hasPermission(permission))) {
                                tierUnlocked = false;
                                break;
                            }
                        }

                        if(tierUnlocked) {
                            unlockedTiers.add(tier.getUniqueId());
                        }
                    }
                });

        generatorData.setUnlockedTiers(unlockedTiers);

        // Reset Purchased Tiers
        generatorData.setPurchasedTiers(new HashSet<>());

        // Reset Active Tiers
        generatorData.setActiveGeneratorList(new HashSet<>());

        // Reset active generator count
        generatorData.setIslandActiveGeneratorCount(0);
        generatorData.setOwnerActiveGeneratorCount(0);

        // Reset Bundles
        generatorData.setIslandBundle(null);
        generatorData.setOwnerBundle(null);

        // Save updated data
        stoneGeneratorManager.saveGeneratorData(generatorData);
    }
}