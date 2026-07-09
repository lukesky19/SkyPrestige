package com.github.lukesky19.skyPrestige.integration.hooks;

import com.github.lukesky19.skySellWands.SkySellWandsAPI;
import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This class manages interfacing with the SkySellWands plugin.
 */
public class SkySellWandsHook implements Hook {
    private final @NonNull SkyPlugin plugin;
    private @Nullable SkySellWandsAPI skySellWandsAPI;

    /**
     * Constructor
     * @param plugin A {@link SkyPlugin} instance.
     */
    public SkySellWandsHook(@NonNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link SkySellWandsAPI} from SkyPlayTime.
     */
    @Override
    public void initialize() {
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("SkySellWands");
        if(plugin != null && plugin.isEnabled()) {
            RegisteredServiceProvider<SkySellWandsAPI> rsp = this.plugin.getServer().getServicesManager().getRegistration(SkySellWandsAPI.class);
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
     * Check if the {@link ItemStack} provided is a sell wand.
     * @apiNote Will always return false if SkySellWands was not hooked into.
     * @param itemStack The {@link ItemStack} to check.
     * @return true if a sell wand, or false if not or not hooked into SkySellWands
     */
    public boolean isItemStackSellWand(@NonNull ItemStack itemStack) {
        if(skySellWandsAPI == null) return false;

        return skySellWandsAPI.isItemStackSellWand(itemStack);
    }

    /**
     * Checks if the {@link ItemStack} provided is an infinite sell wand.
     * @param itemStack The {@link ItemStack} to check.
     * @return true if an infinite sell wand, otherwise false.
     */
    public boolean isInfiniteSellWand(@NonNull ItemStack itemStack) {
        if(skySellWandsAPI == null) return false;

        return skySellWandsAPI.isItemStackInfiniteSellWand(itemStack);
    }
}