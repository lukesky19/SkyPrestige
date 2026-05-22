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
package com.github.lukesky19.skyPrestige.data.data.island;

import com.github.lukesky19.skyPrestige.multiplier.Multiplier;
import com.github.lukesky19.skyPrestige.util.enums.Operation;
import com.github.lukesky19.skyPrestige.util.key.PageSlotKey;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class contains the data for an island.
 */
public class IslandData {
    private @NonNull String islandId;
    private int prestigeLevel = 0;
    private double prestigePoints = 0;
    private @NonNull Multiplier multiplier = new Multiplier();
    private boolean leaderboardExempt = false;
    private boolean prestigeExempt = false;
    private @NonNull Map<PageSlotKey, ItemStack> vaultItems = new HashMap<>();

    /**
     * Use {@link #IslandData(String)} or {@link IslandData#IslandData(String, int, double, Multiplier, boolean, boolean, Map)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated(since = "1.1.0.0")
    public IslandData() {
        throw new RuntimeException("The default constructor cannot be used.");
    }

    /**
     * Constructor
     * @param islandId The unique id of the island.
     */
    public IslandData(@NonNull String islandId) {
        this.islandId = islandId;
    }

    /**
     * Constructor
     * @param islandId The unique id of the island.
     * @param prestigeLevel The island's prestige level.
     * @param prestigePoints The island's prestige points
     * @param multiplier The {@link Multiplier}.
     * @param leaderboardExempt Whether the island is exempt from leaderboard reporting or not.
     * @param prestigeExempt Whether the island is exempt from prestige or not.
     * @param vaultItems The vault items.
     */
    public IslandData(
            @NonNull String islandId,
            int prestigeLevel,
            double prestigePoints,
            @NonNull Multiplier multiplier,
            boolean leaderboardExempt,
            boolean prestigeExempt,
            @NonNull Map<PageSlotKey, ItemStack> vaultItems) {
        this.islandId = islandId;
        this.prestigeLevel = prestigeLevel;
        this.prestigePoints = prestigePoints;
        this.multiplier = multiplier.clone();
        this.leaderboardExempt = leaderboardExempt;
        this.prestigeExempt = prestigeExempt;
        this.vaultItems = new HashMap<>(vaultItems);
    }

    /**
     * Creates a new {@link IslandData}.
     * @return The cloned {@link IslandData}.
     */
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod") // A constructor is used to clone data instead.
    public @NonNull IslandData clone() {
        return new IslandData(islandId, prestigeLevel, prestigePoints, multiplier, leaderboardExempt, prestigeExempt, vaultItems);
    }

    /**
     * Checks if the contents of the two IslandData objects are equal.
     * Will return false for any non-IslandData object.
     * @param compareObject The {@link Object} to compare.
     * @return true if equal, otherwise false.
     */
    @Override
    public boolean equals(@NonNull Object compareObject) {
        if(!(compareObject instanceof IslandData compareIslandData)) return false;

        return this.getIslandId().equals(compareIslandData.getIslandId())
                && this.getPrestigeLevel() == compareIslandData.getPrestigeLevel()
                && this.getPrestigePoints() == compareIslandData.getPrestigePoints()
                && this.multiplier.getMultiplier() == compareIslandData.getMultiplier()
                && this.multiplier.getTime() == compareIslandData.getMultiplierTime()
                && this.isLeaderboardExempt() == compareIslandData.isLeaderboardExempt()
                && this.isPrestigeExempt() == compareIslandData.isPrestigeExempt()
                && this.getVaultItems().equals(compareIslandData.getVaultItems());
    }

    /**
     * Get the island id that this IslandData belongs to.
     * @return The island id.
     */
    public @NonNull String getIslandId() {
        return islandId;
    }

    /**
     * Set the island id that this IslandData belongs to.
     * @param islandId The island id.
     */
    public void setIslandId(@NonNull String islandId) {
        this.islandId = islandId;
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
     * Modify the multiplier for the island based on the inputs.
     * @param multiplier The multiplier or null.
     * @param multiplierOperation The multiplier operation or null.
     * @param time The multiplier time or null.
     * @param multiplierTimeOperation The multiplier time operation or null.
     * @return true if successful, false if not.
     */
    public boolean modifyMultiplier(
            @Nullable Double multiplier,
            @Nullable Operation multiplierOperation,
            @Nullable Long time,
            @Nullable Operation multiplierTimeOperation) {
        return this.multiplier.modifyMultiplier(multiplier, multiplierOperation, time, multiplierTimeOperation);
    }

    /**
     * Set the multiplier for the island.
     * @param multiplier The multiplier.
     */
    public void setMultiplier(double multiplier) {
        this.multiplier.setMultiplier(multiplier);
    }

    /**
     * Add to the multiplier for the island.
     * @param multiplier The multiplier to add.
     */
    public void addMultiplier(double multiplier) {
        this.multiplier.addMultiplier(multiplier);
    }

    /**
     * Remove from the multiplier for the island.
     * @param multiplier The multiplier to remove.
     */
    public void removeMultiplier(double multiplier) {
        this.multiplier.removeMultiplier(multiplier);
    }

    /**
     * Get the current multiplier for the island.
     * @return The multiplier. 0.0 means the island has no multiplier.
     */
    public double getMultiplier() {
        return multiplier.getMultiplier();
    }

    /**
     * Set how long the island's multiplier should last for.
     * @param time The time in seconds.
     */
    public void setMultiplierTime(long time) {
        multiplier.setTime(time);
    }

    /**
     * Add time to the multiplier time.
     * @param time The time in seconds to add.
     */
    public void addMultiplierTime(long time) {
        multiplier.addTime(time);
    }

    /**
     * Remove time from the multiplier time.
     * @param time The time in seconds to remove.
     */
    public void removeMultiplierTime(long time) {
        multiplier.removeTime(time);
    }

    /**
     * Get the time in seconds the island multiplier lasts for.
     * @return The time in seconds.
     */
    public long getMultiplierTime() {
        return multiplier.getTime();
    }

    /**
     * Is the island exempt from leaderboard reporting?
     * @return true if exempt, or false if not.
     */
    public boolean isLeaderboardExempt() {
        return leaderboardExempt;
    }

    /**
     * Set the island's leaderboard exemption status
     * @param leaderboardExempt true if exempt, or false if not.
     */
    public void setLeaderboardExempt(boolean leaderboardExempt) {
        this.leaderboardExempt = leaderboardExempt;
    }

    /**
     * Is the island exempt from prestige?
     * @return true if exempt, or false if not.
     */
    public boolean isPrestigeExempt() {
        return prestigeExempt;
    }

    /**
     * Set whether the island is exempt from prestige or not.
     * @param prestigeExempt true if exempt from prestige, or false if not.
     */
    public void setPrestigeExempt(boolean prestigeExempt) {
        this.prestigeExempt = prestigeExempt;
    }

    /**
     * Add an {@link ItemStack} to the island's vault.
     * @param pageNum The page number to store the item on.
     * @param slot The slot number to store the item at.
     * @param itemStack The {@link ItemStack} to add.
     */
    public void addVaultItem(int pageNum, int slot, @NonNull ItemStack itemStack) {
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
    public @NonNull Map<PageSlotKey, ItemStack> getVaultItems() {
        return vaultItems;
    }

    /**
     * Get the {@link Map} mapping {@link PageSlotKey}s to {@link ItemStack}s for the page number provided.
     * @param pageNum The page number.
     * @return A {@link Map} mapping {@link PageSlotKey}s to {@link ItemStack}s.
     */
    public @NonNull Map<Integer, ItemStack> getVaultItemsByPageNumber(int pageNum) {
        return vaultItems.entrySet().stream()
                .filter(entry -> entry.getKey().page() == pageNum)
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
    public void setVaultItems(@NonNull Map<PageSlotKey, ItemStack> vaultItems) {
        this.vaultItems = new HashMap<>(vaultItems);
    }
}