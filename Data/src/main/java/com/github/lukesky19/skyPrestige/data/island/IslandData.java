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
package com.github.lukesky19.skyPrestige.data.island;

import com.github.lukesky19.skyPrestige.core.util.key.PageSlotKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class contains the data for an island.
 */
public class IslandData implements Cloneable {
    private int prestigeLevel = 0;
    private double prestigePoints = 0;
    private final @NotNull Map<PageSlotKey, ItemStack> vaultItems = new HashMap<>();
    private boolean exempt = false;

    /**
     * Constructor
     */
    public IslandData() {}

    /**
     * Constructor
     * @param prestigeLevel The island's prestige level.
     * @param prestigePoints The island's prestige points
     * @param exempt Whether the island is exempt from leaderboard reporting or not.
     */
    public IslandData(
            int prestigeLevel,
            double prestigePoints,
            boolean exempt) {
        this.prestigeLevel = prestigeLevel;
        this.prestigePoints = prestigePoints;
        this.exempt = exempt;
    }

    /**
     * Creates a new {@link IslandData}.
     * @return The cloned {@link IslandData}.
     */
    public @NotNull IslandData clone() {
        try {
            return (IslandData) super.clone();
        } catch (CloneNotSupportedException cloneNotSupportedException) {
            throw new RuntimeException(cloneNotSupportedException);
        }
    }

    /**
     * Set the prestige level for the island.
     * @param prestigeLevel The prestige level to set the island's prestige level to.
     */
    public void setPrestigeLevel(int prestigeLevel) {
        this.prestigeLevel = prestigeLevel;

        if(this.prestigeLevel < 0) this.prestigeLevel = 0;
    }

    /**
     * Get the prestige points for the island.
     * @return The prestige level.
     */
    public int getPrestigeLevel() {
        return prestigeLevel;
    }

    /**
     * Set the prestige points the island has.
     * @param prestigePoints The prestige points to set the island's prestige points to.
     */
    public void setPrestigePoints(double prestigePoints) {
        this.prestigePoints = prestigePoints;

        if(this.prestigePoints < 0) this.prestigePoints = 0;
    }

    /**
     * Add the prestige points to the island's prestige points.
     * @param prestigePoints The prestige points to add to the island's prestige points.
     */
    public void addPrestigePoints(double prestigePoints) {
        this.prestigePoints += prestigePoints;
    }

    /**
     * Remove the prestige points from the island's prestige points.
     * @param prestigePoints The prestige points to remove from the island's prestige points.
     */
    public void removePrestigePoints(double prestigePoints) {
        this.prestigePoints -= prestigePoints;

        if(this.prestigePoints < 0) this.prestigePoints = 0;
    }

    /**
     * Get the prestige points the island has.
     * @return The prestige points the island has.
     */
    public double getPrestigePoints() {
        return prestigePoints;
    }

    /**
     * Add an {@link ItemStack} to the island's vault.
     * @param pageNum The page number to store the item on.
     * @param slot The slot number to store the item at.
     * @param itemStack The {@link ItemStack} to add.
     */
    public void addVaultItem(int pageNum, int slot, @NotNull ItemStack itemStack) {
        PageSlotKey pageSlotKey = new PageSlotKey(pageNum, slot);
        vaultItems.put(pageSlotKey, itemStack);
    }

    /**
     * Remove an {@link ItemStack} from the island's vault.
     * @param pageNum The page number to remove the item from.
     * @param slot The slot number to remove the item at.
     */
    public void removeVaultItem(int pageNum, int slot) {
        PageSlotKey pageSlotKey = new PageSlotKey(pageNum, slot);
        vaultItems.remove(pageSlotKey);
    }

    /**
     * Get the {@link Map} mapping {@link PageSlotKey}s to {@link ItemStack}s.
     * @return A {@link Map} mapping {@link PageSlotKey}s to {@link ItemStack}s.
     */
    public @NotNull Map<PageSlotKey, ItemStack> getVaultItems() {
        return vaultItems;
    }

    /**
     * Get the {@link Map} mapping {@link PageSlotKey}s to {@link ItemStack}s for the page number provided.
     * @param pageNum The page number.
     * @return A {@link Map} mapping {@link PageSlotKey}s to {@link ItemStack}s.
     */
    public @NotNull Map<Integer, ItemStack> getVaultItemsByPageNumber(int pageNum) {
        return vaultItems.entrySet().stream()
                .filter((entry) -> entry.getKey().page() == pageNum)
                .collect(Collectors.toMap(entry -> entry.getKey().slot(), Map.Entry::getValue));
    }

    /**
     * Clear the {@link ItemStack}s in the island's vault.
     */
    public void clearVaultItems() {
        vaultItems.clear();
    }

    /**
     * Replace the current vault items with the one provided.
     * @param vaultItems A {@link Map} mapping {@link PageSlotKey}s to {@link ItemStack}s.
     */
    public void setVaultItems(@NotNull Map<PageSlotKey, ItemStack> vaultItems) {
        this.vaultItems.clear();

        this.vaultItems.putAll(vaultItems);
    }

    /**
     * Is the island exempt from leaderboard reporting?
     * @return true if exempt, or false if not.
     */
    public boolean isExempt() {
        return exempt;
    }

    /**
     * Set the island's leaderboard exemption status
     * @param exempt true if exempt, or false if not.
     */
    public void setExempt(boolean exempt) {
        this.exempt = exempt;
    }
}
