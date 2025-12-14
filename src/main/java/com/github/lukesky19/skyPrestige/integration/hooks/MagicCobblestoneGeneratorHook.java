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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorDataObject;
import world.bentobox.magiccobblestonegenerator.managers.StoneGeneratorManager;

import java.util.HashSet;
import java.util.Optional;

/**
 * This class manages interfacing with the MagicCobblestoneGenerator addon.
 */
public class MagicCobblestoneGeneratorHook implements Hook {
    private final @NotNull ComponentLogger logger;
    private @Nullable StoneGeneratorAddon stoneGeneratorAddon;
    private @Nullable StoneGeneratorManager stoneGeneratorManager;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public MagicCobblestoneGeneratorHook(@NotNull SkyPlugin plugin) {
        this.logger = plugin.getComponentLogger();
    }

    /**
     * Attempt to get the magic cobblestone generator addon from BentoBox.
     */
    @Override
    public void initialize() {
        Optional<Addon> optionalAddon = BentoBox.getInstance().getAddonsManager().getAddonByName("MagicCobblestoneGenerator");
        if(optionalAddon.isEmpty()) return;

        stoneGeneratorAddon = (StoneGeneratorAddon) optionalAddon.get();
        stoneGeneratorManager = stoneGeneratorAddon.getAddonManager();
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return stoneGeneratorAddon != null && stoneGeneratorManager != null;
    }

    /**
     * Copy the generator data from the old island to the new island.
     * @apiNote If the addon isn't hooked into or any island data is null, no copying will occur.
     * @param oldIsland The old {@link Island}.
     * @param newIsland The new {@link Island}.
     */
    public void copyGeneratorData(@NotNull Island oldIsland, @NotNull Island newIsland) {
        if(stoneGeneratorAddon == null || stoneGeneratorManager == null) return;

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
}
