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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.prestige;

import com.github.lukesky19.skyPrestige.configuration.manager.PrestigeConfigManager;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.common.button.DiscardButton;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.prestige.button.ConfirmButton;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.prestige.input.PrestigeLevelInput;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.prestige.input.PrestigePointsInput;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.prestige.input.PrestigePointsOperationInput;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This class manages the dialog to manage prestige level and points.
 */
public class PrestigeDialog {
    /**
     * Default Constructor
     */
    public PrestigeDialog () {}

    /**
     * Create the dialog to manage prestige level and points.
     * @param dialogManager A {@link DialogManager} instance.
     * @param prestigeConfigManager A {@link PrestigeConfigManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @param islandDialog The {@link IslandDialog}.
     * @param islandIdentifier The island's identifier.
     * @return The {@link Dialog}.
     */
    public static @NonNull Dialog createDialog(
            @NonNull DialogManager dialogManager,
            @NonNull PrestigeConfigManager prestigeConfigManager,
            @NonNull Player player,
            @NonNull IslandDialog islandDialog,
            @NonNull String islandIdentifier) {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(AdventureUtility.plain(islandIdentifier + "'s Island Prestige Level & Points"))
                        .inputs(List.of(
                                PrestigeLevelInput.createInput(prestigeConfigManager, islandDialog),
                                PrestigePointsInput.createInput(),
                                PrestigePointsOperationInput.createInput()
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ConfirmButton.createButton(dialogManager, player, islandDialog),
                        DiscardButton.createButton(dialogManager, player, islandDialog)
                ))
        );
    }
}