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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.multiplier.button;

import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.util.enums.Operation;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.github.lukesky19.skyPrestige.util.number.NumberUtils.getDouble;
import static com.github.lukesky19.skyPrestige.util.number.NumberUtils.getLong;

/**
 * This class manages the {@link ActionButton} to save changes to apply after review.
 */
public class IslandMultiplierConfirmButton {
    /**
     * Default Constructor
     */
    public IslandMultiplierConfirmButton () {}

    /**
     * Create the button to confirm the save the changes to apply after review and reopen the main menu.
     * @param dialogManager A {@link DialogManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @param islandDialog The {@link IslandDialog} storing player inputs.
     * @return The {@link DialogInput}
     */
    public static @NonNull ActionButton createButton(
            @NonNull DialogManager dialogManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull Player player,
            @NonNull IslandDialog islandDialog) {
        return ActionButton.create(
                Component.text("Confirm", TextColor.color(0xAEFFC1)),
                Component.text("Click to confirm your changes."),
                100,
                DialogAction.customClick(
                        (view, _) -> {
                            UUID playerId = player.getUniqueId();

                            boolean notice = Objects.requireNonNullElse(view.getBoolean("island_multiplier_notice"), false);
                            boolean clear = Objects.requireNonNullElse(view.getBoolean("island_multiplier_clear"), false);
                            if(clear) {
                                multiplierManager.clearServerMultiplier(player, notice);

                                player.closeDialog();
                                dialogManager.removeOpenDialog(playerId);

                                return;
                            }

                            String multiplierText = view.getText("island_multiplier");
                            String multiplierTimeText = view.getText("island_multiplier_time");
                            String multiplierOperationText = view.getText("island_multiplier_operation");
                            String multiplierTimeOperationText = view.getText("island_multiplier_operation");
                            Optional<Double> optionalMultiplier = getDouble(multiplierText);
                            Optional<Long> optionalMultiplierTime = getLong(multiplierTimeText);
                            Optional<Operation> optionalMultiplierOperation = Operation.getOperation(multiplierOperationText);
                            Optional<Operation> optionalMultiplierTimeOperation = Operation.getOperation(multiplierTimeOperationText);

                            optionalMultiplier.ifPresent(islandDialog::setMultiplier);
                            optionalMultiplierTime.ifPresent(islandDialog::setMultiplierSeconds);
                            optionalMultiplierOperation.ifPresent(islandDialog::setMultiplierOperation);
                            optionalMultiplierTimeOperation.ifPresent(islandDialog::setMultiplierTimeOperation);
                            islandDialog.setNotice(notice);

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