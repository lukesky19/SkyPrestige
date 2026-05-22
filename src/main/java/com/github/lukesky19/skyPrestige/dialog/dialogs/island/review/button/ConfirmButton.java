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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.review.button;

import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

/**
 * This class manages the {@link ActionButton} to save changes to apply after review.
 */
public class ConfirmButton {
    /**
     * Default Constructor
     */
    public ConfirmButton () {}

    /**
     * Create the button to confirm the save the changes to apply after review and reopen the main menu.
     * @param dialogManager A {@link DialogManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @param island The {@link Island} being changes will be applied to.
     * @param islandData The {@link IslandData} to make changes to.
     * @param islandDialog The {@link IslandDialog} to store changes to for review.
     * @return The {@link ActionButton}.
     */
    public static @NonNull ActionButton createButton(
            @NonNull DialogManager dialogManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull Player player,
            @NonNull Island island,
            @NonNull IslandData islandData,
            @NonNull IslandDialog islandDialog) {
        return ActionButton.create(
                AdventureUtility.deserialize("<#d0ff7f>Confirm Changes"),
                null,
                150,
                DialogAction.customClick(
                        (_, _) -> {
                            player.closeDialog();
                            dialogManager.removeOpenDialog(player.getUniqueId());

                            // Set prestige level
                            islandData.setPrestigeLevel(islandDialog.getPrestigeLevel());

                            // Modify prestige points
                            if(islandDialog.getPrestigePoints() >= 0) {
                                switch(islandDialog.getPrestigePointsOperation()) {
                                    case NONE -> {}
                                    case ADD -> islandData.addPrestigePoints(islandDialog.getPrestigePoints());
                                    case REMOVE -> islandData.removePrestigePoints(islandDialog.getPrestigePoints());
                                    case SET -> islandData.setPrestigePoints(islandDialog.getPrestigePoints());
                                }
                            }

                            // Prestige Exempt
                            islandData.setPrestigeExempt(islandDialog.isPrestigeExempt());

                            // Leaderboard Exempt
                            islandData.setLeaderboardExempt(islandDialog.isLeaderboardExempt());

                            // Multiplier
                            if(islandDialog.isClear()) {
                                multiplierManager.clearIslandMultiplier(
                                        player,
                                        island,
                                        islandDialog.isNotice());
                            } else {
                                multiplierManager.modifyIslandMultiplier(
                                        player,
                                        island,
                                        islandDialog.getMultiplier(),
                                        islandDialog.getMultiplierOperation(),
                                        islandDialog.getMultiplierSeconds(),
                                        islandDialog.getMultiplierTimeOperation(),
                                        islandDialog.isNotice());
                            }

                            // Save
                            if(islandDialog.isSave()) {
                                islandDataManager.saveData(island.getUniqueId(), islandData);
                            }

                            // Unload
                            if(islandDialog.isUnload()) {
                                islandDataManager.removeDataByIdentifier(island.getUniqueId());
                            }
                        },
                        ClickCallback.Options.builder()
                                .uses(1)
                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                .build()));
    }
}