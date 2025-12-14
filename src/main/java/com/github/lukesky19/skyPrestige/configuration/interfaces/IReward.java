package com.github.lukesky19.skyPrestige.configuration.interfaces;

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to create reward configurations.
 */
public interface IReward {
    /**
     * Get the {@link ItemStackConfig} to create an {@link ItemStack} that is displayed inside the rewards GUI.
     * @return An {@link ItemStackConfig}.
     */
    @NotNull ItemStackConfig displayItem();
}
