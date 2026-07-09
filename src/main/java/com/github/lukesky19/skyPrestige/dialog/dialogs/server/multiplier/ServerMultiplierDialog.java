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
package com.github.lukesky19.skyPrestige.dialog.dialogs.server.multiplier;

import com.github.lukesky19.skyPrestige.dialog.dialogs.server.common.button.DiscardButton;
import com.github.lukesky19.skyPrestige.dialog.dialogs.server.multiplier.button.ServerMultiplierConfirmButton;
import com.github.lukesky19.skyPrestige.dialog.dialogs.server.multiplier.input.*;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This class manages the dialog to manage multiplier options for the server.
 */
public class ServerMultiplierDialog {
    /**
     * Default Constructor
     */
    public ServerMultiplierDialog () {}

    /**
     * Create the dialog to manage server multiplier options.
     * @param dialogManager A {@link DialogManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @return The {@link Dialog}.
     */
    public static @NonNull Dialog createDialog(
            @NonNull DialogManager dialogManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull Player player) {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(AdventureUtility.plain("Server Prestige Point Multiplier"))
                        .inputs(List.of(
                                ServerMultiplierInput.createInput(multiplierManager),
                                ServerMultiplierOperationInput.createInput(),
                                ServerMultiplierTimeInput.createInput(multiplierManager),
                                ServerMultiplierTimeOperationInput.createInput(),
                                ServerNoticeInput.createInput(),
                                ServerMultiplierClearInput.createInput()
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ServerMultiplierConfirmButton.createButton(dialogManager, multiplierManager, player),
                        DiscardButton.createButton(dialogManager, player)
                ))
        );
    }
}