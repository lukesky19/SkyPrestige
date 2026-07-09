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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.common.button;

import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * This class manages the {@link ActionButton} to close the dialog, discarding any changes, and return to the main menu.
 */
public class DiscardButton {
    /**
     * Default Constructor
     */
    public DiscardButton () {}

    /**
     * Create the button to close the dialog, discarding any inputs, and return to the main menu.
     * @param dialogManager A {@link DialogManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @param islandDialog The {@link IslandDialog} to return to.
     * @return The {@link ActionButton}.
     */
    public static @NonNull ActionButton createButton(
            @NonNull DialogManager dialogManager,
            @NonNull Player player,
            @NonNull IslandDialog islandDialog) {
        return ActionButton.create(
                Component.text("Discard", TextColor.color(0xFFA0B1)),
                Component.text("Click to discard your changes."),
                100,
                DialogAction.customClick(
                        (_, _) -> {
                            UUID playerId = player.getUniqueId();

                            player.closeDialog();
                            dialogManager.removeOpenDialog(playerId);

                            Dialog mainMenu = islandDialog.createDialog();

                            player.showDialog(mainMenu);
                            dialogManager.addOpenDialog(playerId, mainMenu);
                        },
                        ClickCallback.Options.builder()
                                .uses(1)
                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                .build()
                )
        );
    }
}