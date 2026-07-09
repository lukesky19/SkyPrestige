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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.multiplier;

import com.github.lukesky19.skyPrestige.dialog.dialogs.island.common.button.DiscardButton;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.multiplier.button.IslandMultiplierConfirmButton;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.multiplier.input.*;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This class manages the dialog to manage multiplier options for the island.
 */
public class IslandMultiplierDialog {
    /**
     * Default Constructor
     */
    public IslandMultiplierDialog () {}

    /**
     * Create the dialog to manage island multiplier options.
     * @param dialogManager A {@link DialogManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @param islandDialog The {@link IslandDialog}.
     * @param islandIdentifier The island's identifier.
     * @return The {@link Dialog}.
     */
    public static @NonNull Dialog createDialog(
            @NonNull DialogManager dialogManager,
            @NotNull MultiplierManager multiplierManager,
            @NonNull Player player,
            @NonNull IslandDialog islandDialog,
            @NonNull String islandIdentifier) {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(AdventureUtility.plain(islandIdentifier + "'s Island's Prestige Point Multiplier"))
                        .inputs(List.of(
                                IslandMultiplierInput.createInput(islandDialog),
                                IslandMultiplierOperationInput.createInput(),
                                IslandMultiplierTimeInput.createInput(islandDialog),
                                IslandMultiplierTimeOperationInput.createInput(),
                                IslandNoticeInput.createInput(islandDialog),
                                IslandMultiplierClearInput.createInput(islandDialog)
                        ))
                        .build())
                .type(DialogType.confirmation(
                        IslandMultiplierConfirmButton.createButton(dialogManager, multiplierManager, player, islandDialog),
                        DiscardButton.createButton(dialogManager, player, islandDialog)
                ))
        );
    }
}