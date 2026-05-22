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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.options.button;

import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

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
     * @param islandDialog The {@link IslandDialog} storing player inputs.
     * @return The {@link DialogInput}
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

                            Boolean prestigeExempt = view.getBoolean("prestige_exempt");
                            Boolean leaderboardExempt = view.getBoolean("leaderboard_exempt");
                            Boolean save = view.getBoolean("save");
                            Boolean unload = view.getBoolean("unload");

                            if(prestigeExempt != null) islandDialog.setPrestigeExempt(prestigeExempt);
                            if(leaderboardExempt != null) islandDialog.setLeaderboardExempt(leaderboardExempt);
                            if(save != null) islandDialog.setSave(save);
                            if(unload != null) islandDialog.setUnload(unload);

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