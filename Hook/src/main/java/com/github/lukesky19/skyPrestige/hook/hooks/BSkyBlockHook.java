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
package com.github.lukesky19.skyPrestige.hook.hooks;

import com.github.lukesky19.skyPrestige.hook.interfaces.Hook;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bskyblock.BSkyBlock;

import java.util.Optional;

/**
 * This class manages interfacing with the BSkyBlock addon.
 */
public class BSkyBlockHook implements Hook {
    private @Nullable BSkyBlock bSkyBlock;

    /**
     * Constructor
     */
    public BSkyBlockHook() {}

    /**
     * Attempt to get the {@link BSkyBlock} addon from BentoBox.
     */
    @Override
    public void initialize() {
        Optional<Addon> optionalAddon = BentoBox.getInstance().getAddonsManager().getAddonByName("BSkyBlock");
        if(optionalAddon.isEmpty()) return;

        bSkyBlock = (BSkyBlock) optionalAddon.get();
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return bSkyBlock != null;
    }

    /**
     * Is the player to creates or resets an island teleported to the island upon creation?
     * Will return false if BSkyBlock was not hooked into.
     * @return true or false.
     */
    public boolean isTeleportPlayerToIslandUponIslandCreation() {
        if(bSkyBlock == null) return false;

        return bSkyBlock.getSettings().isTeleportPlayerToIslandUponIslandCreation();
    }
}
