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
package com.github.lukesky19.skyPrestige.protection;

import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.util.enums.SkyPrestigeNamespacedKeys;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class contains methods related to the protection orb.
 */
public class ProtectionOrbManager {
    private final @NotNull ComponentLogger logger;
    private final @NotNull SettingsManager settingsManager;

    private @Nullable ItemStack protectionOrbStack;
    private @Nullable Component loreComponent;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public ProtectionOrbManager(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager) {
        this.logger = plugin.getComponentLogger();
        this.settingsManager = settingsManager;
    }

    /**
     * Re-creates the protection orb item and lore.
     */
    public void reload() {
        this.protectionOrbStack = null;
        this.loreComponent = null;

        createProtectionOrb();
        createProtectedLore();
    }

    /**
     * Get an {@link ItemStack} for a protection orb.
     * @return The {@link ItemStack}. May be null.
     */
    public @Nullable ItemStack getProtectionOrb() {
        if(protectionOrbStack == null) return null;

        return protectionOrbStack.clone();
    }

    /**
     * Get the {@link Component} to add to an item's lore that is protected.
     * @return A {@link Component} or null.
     */
    public @Nullable Component getLoreComponent() {
        return loreComponent;
    }

    /**
     * Check if an {@link ItemType} is disallowed for use with protection orbs.
     * @param itemType The {@link ItemType} to check.
     * @return true if disallowed or if settings are null, otherwise false.
     */
    public boolean isProtectionOrbItemTypeDisallowed(@NotNull ItemType itemType) {
        @Nullable Settings settings = settingsManager.getConfiguration();
        if(settings == null) return true;

        return settings.protectionOrbSettings().disallowedItems().contains(itemType.getKey().toString());
    }

    /**
     * Is the {@link ItemStack} provided a protection orb?
     * @param itemStack The {@link ItemStack} to check.
     * @return true if a protection orb, or false.
     */
    public boolean isItemStackProtectionOrb(@NotNull ItemStack itemStack) {
        @Nullable ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return false;

        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        @NotNull NamespacedKey namespacedKey = SkyPrestigeNamespacedKeys.PROTECTION_ORB.getKey();

        return persistentDataContainer.has(namespacedKey);
    }

    /**
     * Is the {@link ItemStack} provided protected by a protection orb?
     * @param itemStack The {@link ItemStack} to check.
     * @return true if protected, or false.
     */
    public boolean isItemStackProtected(@NotNull ItemStack itemStack) {
        @Nullable ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return false;

        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        @NotNull NamespacedKey namespacedKey = SkyPrestigeNamespacedKeys.PROTECTED.getKey();

        return persistentDataContainer.has(namespacedKey);
    }

    /**
     * Mark the {@link ItemStack} as protected.
     * @param itemStack The {@link ItemStack} to protect.
     */
    public void protectItemStack(@NotNull ItemStack itemStack) {
        @Nullable ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return;

        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        @NotNull NamespacedKey namespacedKey = SkyPrestigeNamespacedKeys.PROTECTED.getKey();

        persistentDataContainer.set(namespacedKey, PersistentDataType.INTEGER, 1);

        // Get the protected lore component
        @Nullable Component protectedLoreComponent = getLoreComponent();
        if(protectedLoreComponent != null) {
            // Get the current lore if any or a new list
            List<Component> lore = Objects.requireNonNullElse(itemMeta.lore(), new ArrayList<>());

            // Add the component to the lore
            lore.add(protectedLoreComponent);

            // Set the lore
            itemMeta.lore(lore);
        }

        // Set the ItemMeta
        itemStack.setItemMeta(itemMeta);
    }

    /**
     * Create the {@link ItemStack} for the protection orb.
     */
    private void createProtectionOrb() {
        @Nullable Settings settings = settingsManager.getConfiguration();
        if(settings == null) return;

        Optional<ItemStack> optionalItemStack = new ItemStackBuilder(logger)
                .fromItemStackConfig(
                        settings.protectionOrbSettings().itemStackConfig(),
                        null,
                        null,
                        List.of()).buildItemStack();

        if(optionalItemStack.isPresent()) {
            ItemStack itemStack = optionalItemStack.get();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(itemMeta == null) return;

            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            NamespacedKey namespacedKey = SkyPrestigeNamespacedKeys.PROTECTION_ORB.getKey();
            persistentDataContainer.set(namespacedKey, PersistentDataType.INTEGER, 1);

            itemStack.setItemMeta(itemMeta);

            this.protectionOrbStack = itemStack;
        }
    }

    /**
     * Create the lore added to items that are protected.
     */
    private void createProtectedLore() {
        @Nullable Settings settings = settingsManager.getConfiguration();
        if(settings == null) return;

        @Nullable String protectedLoreString = settings.protectionOrbSettings().protectedLore();
        if(protectedLoreString != null && !protectedLoreString.isEmpty()) {
            loreComponent = AdventureUtil.deserialize(protectedLoreString);
        }
    }
}
