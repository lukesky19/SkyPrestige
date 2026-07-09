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
package com.github.lukesky19.skyPrestige.dialog.dialogs.server.multiplier.button;

import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.util.enums.Operation;
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
 * This class manages the {@link ActionButton} to apply the changes.
 */
public class ServerMultiplierConfirmButton {
    /**
     * Default Constructor
     */
    public ServerMultiplierConfirmButton () {}

    /**
     * Create the button to apply the changes.
     * @param dialogManager A {@link DialogManager} instance.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @return The {@link DialogInput}
     */
    public static @NonNull ActionButton createButton(
            @NonNull DialogManager dialogManager,
            @NonNull MultiplierManager multiplierManager,
            @NonNull Player player) {
        return ActionButton.create(
                Component.text("Confirm", TextColor.color(0xAEFFC1)),
                Component.text("Click to confirm your changes."),
                100,
                DialogAction.customClick(
                        (view, _) -> {
                            UUID playerId = player.getUniqueId();

                            // Check if clear is true
                            boolean notice = Objects.requireNonNullElse(view.getBoolean("server_multiplier_notice"), false);
                            boolean clear = Objects.requireNonNullElse(view.getBoolean("server_multiplier_clear"), false);
                            if(clear) {
                                multiplierManager.clearServerMultiplier(player, notice);

                                player.closeDialog();
                                dialogManager.removeOpenDialog(playerId);

                                return;
                            }

                            // Modify Server Multiplier
                            String multiplierText = view.getText("server_multiplier");
                            String multiplierTimeText = view.getText("server_multiplier_time");
                            String multiplierOperationText = view.getText("server_multiplier_operation");
                            String multiplierTimeOperationText = view.getText("server_multiplier_operation");

                            Optional<Double> optionalMultiplier = getDouble(multiplierText);
                            Optional<Long> optionalMultiplierTime = getLong(multiplierTimeText);
                            Optional<Operation> optionalMultiplierOperation = Operation.getOperation(multiplierOperationText);
                            Optional<Operation> optionalMultiplierTimeOperation = Operation.getOperation(multiplierTimeOperationText);

                            multiplierManager.modifyServerMultiplier(
                                    player,
                                    optionalMultiplier.orElse(null),
                                    optionalMultiplierOperation.orElse(null),
                                    optionalMultiplierTime.orElse(null),
                                    optionalMultiplierTimeOperation.orElse(null),
                                    notice);

                            player.closeDialog();
                            dialogManager.removeOpenDialog(playerId);
                        },
                        ClickCallback.Options.builder()
                                .uses(1)
                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                .build()
                )
        );
    }
}