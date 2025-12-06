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
package com.github.lukesky19.skyPrestige.listener.points.context;

import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockType;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * This class holds the data extracted from an event.
 */
public class EventContext {
    private @Nullable Player player = null;
    private @Nullable UUID playerId = null;

    private @Nullable ItemType itemType = null;
    private @Nullable PotionType potionType = null;
    private @Nullable BlockType blockType = null;
    private @Nullable EntityType entityType = null;
    private @Nullable Map<Enchantment, Integer> enchantments = null;

    private @Nullable Container container = null;
    private @Nullable PersistentDataContainer persistentDataContainer = null;
    private @Nullable NamespacedKey namespacedKey = null;

    private int amount = 0;

    /**
     * Constructor
     */
    public EventContext() {}

    /**
     * Get the {@link Player}.
     * @return A {@link Player}. May be null.
     */
    public @Nullable Player getPlayer() {
        return player;
    }

    /**
     * Get the {@link UUID} of the player.
     * @return The {@link UUID} of the player. May be null.
     */
    public @Nullable UUID getPlayerId() {
        return playerId;
    }

    /**
     * Get the {@link ItemType}.
     * @return An {@link ItemType}. May be null.
     */
    public @Nullable ItemType getItemType() {
        return itemType;
    }

    /**
     * Get the {@link PotionType}.
     * @return A {@link PotionType}. May be null.
     */
    public @Nullable PotionType getPotionType() {
        return potionType;
    }

    /**
     * Get the {@link BlockType}.
     * @return A {@link BlockType}. May be null.
     */
    public @Nullable BlockType getBlockType() {
        return blockType;
    }

    /**
     * Get the {@link EntityType}.
     * @return An {@link EntityType}. May be null.
     */
    public @Nullable EntityType getEntityType() {
        return entityType;
    }

    /**
     * Get the {@link Map} mapping {@link Enchantment}s to enchantment levels.
     * @return A {@link Map} mapping {@link Enchantment}s to enchantment levels. May be null.
     */
    public @Nullable Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    /**
     * Get the {@link Container}.
     * @return A {@link Container}. May be null.
     */
    public @Nullable Container getContainer() {
        return container;
    }

    /**
     * Get the {@link PersistentDataContainer}.
     * @return A {@link PersistentDataContainer}. May be null.
     */
    public @Nullable PersistentDataContainer getPersistentDataContainer() {
        return persistentDataContainer;
    }

    /**
     * Get the {@link NamespacedKey}.
     * @return A {@link NamespacedKey}. May be null.
     */
    public @Nullable NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    /**
     * Get the amount.
     * @return The amount. 0 means the amount was not set.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Set the {@link Player}.
     * @param player A {@link Player}. May be null.
     */
    public void setPlayer(@Nullable Player player) {
        this.playerId = null;

        this.player = player;
        if(player != null) this.playerId = player.getUniqueId();
    }

    /**
     * Set the {@link ItemType}.
     * @param itemType A {@link ItemType}. May be null.
     */
    public void setItemType(@Nullable ItemType itemType) {
        this.itemType = itemType;
    }

    /**
     * Set the {@link PotionType}.
     * @param potionType A {@link PotionType}. May be null.
     */
    public void setPotionType(@Nullable PotionType potionType) {
        this.potionType = potionType;
    }

    /**
     * Set the {@link BlockType}.
     * @param blockType A {@link BlockType}. May be null.
     */
    public void setBlockType(@Nullable BlockType blockType) {
        this.blockType = blockType;
    }

    /**
     * Set the {@link EntityType}.
     * @param entityType A {@link EntityType}. May be null.
     */
    public void setEntityType(@Nullable EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * Set the {@link Map} mapping {@link Enchantment}s to enchantment levels.
     * @param enchantments A {@link Map} mapping {@link Enchantment}s to enchantment levels. May be null.
     */
    public void setEnchantments(@Nullable Map<Enchantment, Integer> enchantments) {
        this.enchantments = enchantments;
    }

    /**
     * Set the {@link Container}.
     * @param container A {@link Container}. May be null.
     */
    public void setContainer(@Nullable Container container) {
        this.container = container;

        if(container != null) {
            this.persistentDataContainer = container.getPersistentDataContainer();
        }
    }

    /**
     * Set the {@link NamespacedKey}.
     * @param namespacedKey A {@link NamespacedKey}. May be null.
     */
    public void setNamespacedKey(@Nullable NamespacedKey namespacedKey) {
        this.namespacedKey = namespacedKey;
    }

    /**
     * Set the amount.
     * @param amount The amount. Clamped to 0 if less than 0.
     */
    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
    }
}
