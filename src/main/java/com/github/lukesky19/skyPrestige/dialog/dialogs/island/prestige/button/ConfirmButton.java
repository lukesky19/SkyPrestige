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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.prestige.button;

import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.util.enums.Operation;
import com.github.lukesky19.skyPrestige.util.number.NumberUtils;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
     * @param player The {@link Player} viewing the dialog.
     * @param islandDialog The {@link IslandDialog} to store changes to for review.
     * @return The {@link ActionButton}.
     */
    public static @NonNull ActionButton createButton(
            @NonNull DialogManager dialogManager,
            @NonNull Player player,
            @NonNull IslandDialog islandDialog) {
        return ActionButton.create(
                Component.text("Confirm", TextColor.color(0xAEFFC1)),
                Component.text("Click to confirm your changes."),
                100,
                DialogAction.customClick(
                        (view, _) -> {
                            UUID playerId = player.getUniqueId();

                            int prestigeLevel = Objects.requireNonNullElse(view.getFloat("prestige_level"), islandDialog.getPrestigeLevel()).intValue();

                            double prestigePoints = -1;
                            String prestigePointsText = view.getText("prestige_points");
                            if(prestigePointsText != null) {
                                Optional<Double> points = NumberUtils.getDouble(prestigePointsText);
                                if(points.isPresent()) {
                                    prestigePoints = points.get();
                                }
                            }

                            String prestigePointsOperationText = view.getText("prestige_points_operation");
                            Optional<Operation> optionalPrestigePointsOperation = Operation.getOperation(prestigePointsOperationText);
                            optionalPrestigePointsOperation.ifPresent(islandDialog::setPrestigePointsOperation);

                            islandDialog.setPrestigeLevel(prestigeLevel);
                            if(prestigePoints > -1) {
                                islandDialog.setPrestigePoints(prestigePoints);
                            }

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