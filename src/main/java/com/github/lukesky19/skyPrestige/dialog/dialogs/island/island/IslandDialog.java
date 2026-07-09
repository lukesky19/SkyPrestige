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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.island;

import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.button.*;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.util.enums.Operation;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * This class manages the dialog to manage island data.
 */
public class IslandDialog {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull IslandDataManager islandDataManager;
    private final @NonNull MultiplierManager multiplierManager;
    private final @NonNull DialogManager dialogManager;
    private final @NonNull PrestigeConfigManager prestigeConfigManager;

    private final @NonNull Island island;
    private final @NonNull IslandData islandData;
    private final @NonNull Player player;
    private final @NonNull String islandIdentifier;

    // Settings
    private int prestigeLevel;
    private double prestigePoints;
    private @NonNull Operation prestigePointsOperation = Operation.ADD;
    private boolean leaderboardExempt;
    private boolean prestigeExempt;

    private double multiplier;
    private @NonNull Operation multiplierOperation = Operation.ADD;
    private long multiplierSeconds;
    private @NonNull Operation multiplierTimeOperation = Operation.ADD;
    private boolean notice;
    private boolean clear;

    private boolean save;
    private boolean unload;

    /**
     * Constructor
     * @param skyPlugin A {@link SkyPlugin} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param dialogManager A {@link DialogManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param island The {@link Island} being managed.
     * @param islandOwnerId The {@link UUID} that owns the island.
     * @param islandData The island's {@link IslandData}.
     * @param player The {@link Player}.
     */
    public IslandDialog(
            @NonNull SkyPlugin skyPlugin,
            @NonNull IslandDataManager islandDataManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull DialogManager dialogManager,
            @NonNull PrestigeConfigManager prestigeConfigManager,
            @NonNull Island island,
            @NonNull UUID islandOwnerId,
            @NonNull IslandData islandData,
            @NonNull Player player) {
        this.plugin = skyPlugin;
        this.islandDataManager = islandDataManager;
        this.multiplierManager = multiplierManager;
        this.dialogManager = dialogManager;
        this.prestigeConfigManager = prestigeConfigManager;

        this.island = island;
        this.islandData = islandData;
        this.player = player;

        OfflinePlayer islandOwner = skyPlugin.getServer().getOfflinePlayer(islandOwnerId);
        islandIdentifier = Objects.requireNonNullElse(islandOwner.getName(), islandOwnerId.toString());

        prestigeLevel = islandData.getPrestigeLevel();
        prestigePoints = islandData.getPrestigePoints();
        leaderboardExempt = islandData.isLeaderboardExempt();
        prestigeExempt = islandData.isPrestigeExempt();
        multiplier = islandData.getMultiplier();
        multiplierSeconds = islandData.getMultiplierTime();
    }

    /**
     * Create the main menu dialog to open other dialogs.
     * @return A {@link Dialog} for the main menu.
     */
    public @NonNull Dialog createDialog() {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(AdventureUtility.plain(player.getName() + "'s Island")).build())
                .type(DialogType.multiAction(createButtons(), null, 2)));
    }

    /**
     * Create the buttons to display inside the dialog.
     * @return A {@link List} of {@link ActionButton}s.
     */
    private @NonNull List<ActionButton> createButtons() {
        return List.of(
                PrestigeLevelPointsButton.createButton(dialogManager, prestigeConfigManager, player, islandIdentifier, this),
                MultiplierButton.createButton(dialogManager, multiplierManager, player, islandIdentifier, this),
                OptionsButton.createButton(dialogManager, player, islandIdentifier, this),
                InfoButton.createButton(plugin, dialogManager, player, island, islandData, islandIdentifier, this),
                ReviewButton.createButton(dialogManager, islandDataManager, multiplierManager, player, island, islandData, islandIdentifier, this),
                DiscardButton.createButton(dialogManager, player));
    }

    /**
     * Set the prestige level to set the island to once confirmed.
     * @param prestigeLevel The prestige level.
     */
    public void setPrestigeLevel(int prestigeLevel) {
        this.prestigeLevel = prestigeLevel;
    }

    /**
     * Get the prestige level that the island will be set to once confirmed.
     * @return The prestige level.
     */
    public int getPrestigeLevel() {
        return prestigeLevel;
    }

    /**
     * Set the prestige points that will modify the current island's prestige points depending on the operation.
     * @param prestigePoints The prestige points.
     */
    public void setPrestigePoints(double prestigePoints) {
        this.prestigePoints = prestigePoints;
    }

    /**
     * Get the prestige points that will modify the current island's prestige points depending on the operation.
     * @return The prestige points.
     */
    public double getPrestigePoints() {
        return prestigePoints;
    }

    /**
     * Set the operation that will be used to apply the prestige points to the island's data.
     * @param prestigePointsOperation The {@link Operation}.
     */
    public void setPrestigePointsOperation(@NonNull Operation prestigePointsOperation) {
        this.prestigePointsOperation = prestigePointsOperation;
    }

    /**
     * Get the operation that will be used to apply the prestige points to the island's data.
     * @return The {@link Operation}.
     */
    public @NonNull Operation getPrestigePointsOperation() {
        return prestigePointsOperation;
    }

    /**
     * Set the prestige exempt status that the island will be set to once confirmed.
     * @param prestigeExempt true to exempt the island from prestige, or false.
     */
    public void setPrestigeExempt(boolean prestigeExempt) {
        this.prestigeExempt = prestigeExempt;
    }

    /**
     * Get the prestige exempt status that the island will be set to once confirmed.
     * @return The prestige exemption status.
     */
    public boolean isPrestigeExempt() {
        return prestigeExempt;
    }

    /**
     * Set the leaderboard exempt status that the island will be set to once confirmed.
     * @param leaderboardExempt true to exempt the island from leaderboards, or false.
     */
    public void setLeaderboardExempt(boolean leaderboardExempt) {
        this.leaderboardExempt = leaderboardExempt;
    }

    /**
     * Get the leaderboard exempt status that the island will be set to once confirmed.
     * @return The leaderboard exemption status.
     */
    public boolean isLeaderboardExempt() {
        return leaderboardExempt;
    }

    /**
     * Set the multiplier that the island will modify the current island's multiplier depending on the operation.
     * @param multiplier The multiplier.
     */
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Get the multiplier that the island will modify the current island's multiplier depending on the operation.
     * @return The multiplier.
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Set the operation that will be used to apply the multiplier to the island's data.
     * @param multiplierOperation The {@link Operation}.
     */
    public void setMultiplierOperation(@NonNull Operation multiplierOperation) {
        this.multiplierOperation = multiplierOperation;
    }

    /**
     * Get the operation that will be used to apply the multiplier to the island's data.
     * @return The {@link Operation}.
     */
    public @NonNull Operation getMultiplierOperation() {
        return multiplierOperation;
    }

    /**
     * Set the multiplier in seconds that the island will modify the current island's multiplier in seconds depending on the operation.
     * @param multiplierSeconds The multiplier time in seconds.
     */
    public void setMultiplierSeconds(long multiplierSeconds) {
        this.multiplierSeconds = multiplierSeconds;
    }

    /**
     * Get the multiplier time in seconds that the island will modify the current island's multiplier time in seconds depending on the operation.
     * @return The multiplier time in seconds.
     */
    public long getMultiplierSeconds() {
        return multiplierSeconds;
    }

    /**
     * Set the operation that will be used to apply the multiplier time to the island's data.
     * @param multiplierTimeOperation The {@link Operation}.
     */
    public void setMultiplierTimeOperation(@NonNull Operation multiplierTimeOperation) {
        this.multiplierTimeOperation = multiplierTimeOperation;
    }

    /**
     * Get the operation that will be used to apply the multiplier time to the island's data.
     * @return The {@link Operation}.
     */
    public @NonNull Operation getMultiplierTimeOperation() {
        return multiplierTimeOperation;
    }

    /**
     * Set whether to notice the island members of the multiplier changes.
     * @param notice true to notify, false if not.
     */
    public void setNotice(boolean notice) {
        this.notice = notice;
    }

    /**
     * Are the multiplier changes going to be broadcasted to island members?
     * @return true to notify, or false.
     */
    public boolean isNotice() {
        return notice;
    }

    /**
     * Set whether to clear any island multiplier data.
     * @param clear true to clear, false if not.
     */
    public void setClear(boolean clear) {
        this.clear = clear;
    }

    /**
     * Should any island multiplier data be cleared?
     * @return true to clear, or false.
     */
    public boolean isClear() {
        return clear;
    }

    /**
     * Set whether any modifications should be immediately saved to the database.
     * @param save true to save otherwise false
     */
    public void setSave(boolean save) {
        this.save = save;
    }

    /**
     * Are the modifications set to be immediately saved to the database or not?
     * @return true to save, otherwise false.
     */
    public boolean isSave() {
        return save;
    }

    /**
     * Set whether to unload the island data after modifications.
     * @param unload true to unload otherwise false
     */
    public void setUnload(boolean unload) {
        this.unload = unload;
    }

    /**
     * Is the island data set to be unloaded after modifications?
     * @return true to unload, otherwise false.
     */
    public boolean isUnload() {
        return unload;
    }
}