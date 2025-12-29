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
package com.github.lukesky19.skyPrestige.integration.manager;

import com.github.lukesky19.skyPrestige.integration.hooks.*;
import com.github.lukesky19.skyPrestige.integration.interfaces.Hook;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages hooks into different plugins.
 */
public class HookManager {
    private final @NotNull Map<Class<?>, Hook> hooks = new HashMap<>();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public HookManager(@NotNull SkyPlugin plugin) {
        BentoBoxHook bentoBoxHook = new BentoBoxHook();
        registerHook(BentoBoxHook.class, bentoBoxHook);

        EconomyHook economyHook = new EconomyHook(plugin);
        registerHook(EconomyHook.class, economyHook);

        LuckPermsHook luckPermsHook = new LuckPermsHook(plugin);
        registerHook(LuckPermsHook.class, luckPermsHook);

        MagicCobblestoneGeneratorHook magicCobblestoneGeneratorHook = new MagicCobblestoneGeneratorHook(plugin.getComponentLogger());
        registerHook(MagicCobblestoneGeneratorHook.class, magicCobblestoneGeneratorHook);

        RoseStackerHook roseStackerHook = new RoseStackerHook(plugin);
        registerHook(RoseStackerHook.class, roseStackerHook);

        SkyPlayTimeHook skyPlayTimeHook = new SkyPlayTimeHook(plugin);
        registerHook(SkyPlayTimeHook.class, skyPlayTimeHook);

        SkySellWandsHook skySellWandsHook = new SkySellWandsHook(plugin);
        registerHook(SkySellWandsHook.class, skySellWandsHook);

        PlayerAuctionsHook playerAuctionsHook = new PlayerAuctionsHook(plugin);
        registerHook(PlayerAuctionsHook.class, playerAuctionsHook);
    }

    /**
     * Register a hook.
     * @param hookClass The class.
     * @param hook The class instance.
     * @param <T> Parameter for any class that extends {@link Hook}.
     */
    public <T extends Hook> void registerHook(@NotNull Class<T> hookClass, @NotNull Hook hook) {
        hooks.put(hookClass, hook);
        hook.initialize();
    }

    /**
     * Get a hook.
     * @param hookClass The class.
     * @return The class instance.
     * @param <T> Parameter for any class that extends {@link Hook}.
     */
    public @NotNull <T extends Hook> T getHook(@NotNull Class<T> hookClass) {
        return hookClass.cast(hooks.get(hookClass));
    }
}
