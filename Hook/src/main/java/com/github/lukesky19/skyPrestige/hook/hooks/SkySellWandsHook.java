package com.github.lukesky19.skyPrestige.hook.hooks;

import com.github.lukesky19.skyPrestige.hook.interfaces.Hook;
import com.github.lukesky19.skySellWands.SkySellWandsAPI;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skyplaytime.SkyPlayTimeAPI;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages interfacing with the SkySellWands plugin.
 */
public class SkySellWandsHook implements Hook {
    private final @NotNull SkyPlugin plugin;
    private @Nullable SkySellWandsAPI skySellWandsAPI;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public SkySellWandsHook(@NotNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link SkyPlayTimeAPI} from SkyPlayTime.
     */
    @Override
    public void initialize() {
        @Nullable Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("SkySellWands");
        if(plugin != null && plugin.isEnabled()) {
            @Nullable RegisteredServiceProvider<SkySellWandsAPI> rsp = this.plugin.getServer().getServicesManager().getRegistration(SkySellWandsAPI.class);
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