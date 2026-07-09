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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.review;

import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.common.button.ReturnButton;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.review.button.ConfirmButton;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the dialog to review changes to be made to the island data.
 */
public class ReviewDialog {
    /**
     * Default Constructor
     */
    public ReviewDialog () {}

    /**
     * Create the dialog to review the changes to be made to the island data.
     * @param dialogManager A {@link DialogManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @param island The {@link Island}.
     * @param islandData The {@link IslandData}.
     * @param islandIdentifier The island's identifier.
     * @param islandDialog The {@link IslandDialog}.
     * @return The {@link Dialog}.
     */
    public static @NonNull Dialog createDialog(
            @NonNull DialogManager dialogManager,
            @NonNull IslandDataManager islandDataManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull Player player,
            @NonNull Island island,
            @NonNull IslandData islandData,
            @NonNull String islandIdentifier,
            @NonNull IslandDialog islandDialog) {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(AdventureUtility.plain("Confirm changes for " + islandIdentifier + "'s Island"))
                        .body(createBody(islandData, islandDialog))
                        .build())
                .type(DialogType.multiAction(List.of(
                        ConfirmButton.createButton(dialogManager, islandDataManager, multiplierManager, player, island, islandData, islandDialog),
                        ReturnButton.createButton(dialogManager, player, islandDialog)))
                        .build()));
    }

    /**
     * Create the dialog's body displaying the information.
     * @param islandData The {@link IslandData}.
     * @param islandDialog The {@link IslandDialog} that stores changes for review.
     * @return The {@link List} of {@link DialogBody}.
     */
    private static @NonNull List<DialogBody> createBody(
            @NonNull IslandData islandData,
            @NonNull IslandDialog islandDialog) {
        List<DialogBody> bodyList = new ArrayList<>();

        // Prestige Level
        bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Set Prestige Level: " + islandDialog.getPrestigeLevel())));

        // Prestige Points
        if(islandDialog.getPrestigePoints() >= 0) {
            switch(islandDialog.getPrestigePointsOperation()) {
                case NONE -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("No change to Prestige Points")));
                case ADD -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Add Prestige Points: " + islandDialog.getPrestigePoints())));
                case REMOVE -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Remove Prestige Points: " + islandDialog.getPrestigePoints())));
                case SET -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Set Prestige Points: " + islandDialog.getPrestigePoints())));
            }
        }

        // Prestige Exempt
        if(islandDialog.isPrestigeExempt() != islandData.isPrestigeExempt()) {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Prestige Exemption: " + islandDialog.isPrestigeExempt())));
        } else {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("No change to prestige exemption")));
        }

        // Leaderboard Exempt
        if(islandDialog.isLeaderboardExempt() != islandData.isLeaderboardExempt()) {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Leaderboard Exemption: " + islandDialog.isLeaderboardExempt())));
        } else {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("No change to leaderboard exemption")));
        }

        // Multiplier
        if(islandDialog.getMultiplier() >= 0) {
            switch(islandDialog.getMultiplierOperation()) {
                case NONE -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("No change to Multiplier")));
                case ADD -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Add To Multiplier: " + islandDialog.getMultiplier())));
                case REMOVE -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Remove from Multiplier: " + islandDialog.getMultiplier())));
                case SET -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Set Multiplier: " + islandDialog.getMultiplier())));
            }
        } else {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("No change to Multiplier")));
        }

        if(islandDialog.getMultiplierSeconds() >= 0) {
            switch(islandDialog.getMultiplierTimeOperation()) {
                case NONE -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("No change to Multiplier Time")));
                case ADD -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Add To Multiplier Time: " + islandDialog.getMultiplierSeconds())));
                case REMOVE -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Remove from Multiplier Time: " + islandDialog.getMultiplierSeconds())));
                case SET -> bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Set Multiplier Time: " + islandDialog.getMultiplierSeconds())));
            }
        } else {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("No change to Multiplier Time")));
        }

        // Save
        if(islandDialog.isSave()) {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Will save island data to database.")));
        } else {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Will not save island data to database.")));
        }

        // Unload
        if(islandDialog.isUnload()) {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Will unload island data.")));
        } else {
            bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Will not unload island data.")));
        }

        return bodyList;
    }
}