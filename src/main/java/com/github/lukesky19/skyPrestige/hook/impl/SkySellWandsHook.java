package com.github.lukesky19.skyPrestige.hook.impl;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.hook.Hook;
import com.github.lukesky19.skySellWands.SkySellWandsAPI;
import com.github.lukesky19.skyplaytime.SkyPlayTimeAPI;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages interfacing with the SkySellWands plugin.
 */
public class SkySellWandsHook implements Hook {
    private final @NotNull SkyPrestige skyPrestige;
    private @Nullable SkySellWandsAPI skySellWandsAPI;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     */
    public SkySellWandsHook(@NotNull SkyPrestige skyPrestige) {
        this.skyPrestige = skyPrestige;
    }

    /**
     * Attempt to get the {@link SkyPlayTimeAPI} from SkyPlayTime.
     */
    @Override
    public void initialize() {
        @Nullable Plugin plugin = skyPrestige.getServer().getPluginManager().getPlugin("SkySellWands");
        if(plugin != null && plugin.isEnabled()) {
            @Nullable RegisteredServiceProvider<SkySellWandsAPI> rsp = skyPrestige.getServer().getServicesManager().getRegistration(SkySellWandsAPI.class);
            if (rsp != null) {
                skySellWandsAPI = rsp.getProvider();
            }
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return skySellWandsAPI != null;
    }

    /**
     * Checks if the {@link ItemStack} provided is an infinite sell wand.
     * @param itemStack The {@link ItemStack} to check.
     * @return true if an infinite sell wand, otherwise false.
     */
    public boolean isInfiniteSellWand(@NotNull ItemStack itemStack) {
        if(skySellWandsAPI == null) return false;

        return skySellWandsAPI.isItemStackInfiniteSellWand(itemStack);
    }
}