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
import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages hooks into different plugins.
 */
public class HookManager {
    private final @NonNull Map<Class<?>, Hook> hooks = new HashMap<>();

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     */
    public HookManager(@NonNull SkyPlugin plugin) {
        registerHook(BentoBoxHook.class, new BentoBoxHook());

        registerHook(EconomyHook.class, new EconomyHook(plugin));

        registerHook(ExcellentCratesHook.class, new ExcellentCratesHook(plugin));

        registerHook(LMBQuestHook.class, new LMBQuestHook(plugin));

        registerHook(LuckPermsHook.class, new LuckPermsHook(plugin));

        registerHook(MagicCobblestoneGeneratorHook.class, new MagicCobblestoneGeneratorHook(plugin));

        registerHook(PlayerAuctionsHook.class, new PlayerAuctionsHook(plugin));

        registerHook(RoseStackerHook.class, new RoseStackerHook(plugin));

        registerHook(SkyHoppersHook.class, new SkyHoppersHook(plugin));

        registerHook(SkyPlayTimeHook.class, new SkyPlayTimeHook(plugin));

        registerHook(SkySellWandsHook.class, new SkySellWandsHook(plugin));
    }

    /**
     * Register a hook.
     * @param hookClass The class.
     * @param hook The class instance.
     * @param <T> Parameter for any class that extends {@link Hook}.
     */
    public <T extends Hook> void registerHook(@NonNull Class<T> hookClass, @NonNull Hook hook) {
        hooks.put(hookClass, hook);
        hook.initialize();
    }

    /**
     * Get a hook.
     * @param hookClass The class.
     * @return The class instance.
     * @param <T> Parameter for any class that extends {@link Hook}.
     */
    public @NonNull <T extends Hook> T getHook(@NonNull Class<T> hookClass) {
        return hookClass.cast(hooks.get(hookClass));
    }
}
