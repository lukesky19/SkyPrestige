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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.button;

import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.info.InfoDialog;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.UUID;

/**
 * This class manages the {@link ActionButton} to open the info dialog.
 */
public class InfoButton {
    /**
     * Default Constructor
     */
    public InfoButton () {}

    /**
     * Create the button to open the info dialog.
     * @param plugin A {@link SkyPlugin} instance.
     * @param dialogManager A {@link DialogManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @param island The player's {@link Island}.
     * @param islandData The island's {@link IslandData}.
     * @param islandIdentifier The island's identifier.
     * @param islandDialog The {@link IslandDialog}.
     * @return The {@link ActionButton}.
     */
    public static @NonNull ActionButton createButton(
            @NonNull SkyPlugin plugin,
            @NonNull DialogManager dialogManager,
            @NonNull Player player,
            @NonNull Island island,
            @NonNull IslandData islandData,
            @NonNull String islandIdentifier,
            @NonNull IslandDialog islandDialog) {
        return ActionButton.create(
                AdventureUtility.plain("Island Info"),
                null,
                150,
                DialogAction.customClick(
                        (_, _) -> {
                            UUID playerId = player.getUniqueId();

                            player.closeDialog();
                            dialogManager.removeOpenDialog(playerId);

                            Dialog dialog = InfoDialog.createDialog(plugin, dialogManager, player, island, islandData, islandIdentifier, islandDialog);

                            player.showDialog(dialog);
                            dialogManager.addOpenDialog(playerId, dialog);
                        },
                        ClickCallback.Options.builder()
                                .uses(ClickCallback.UNLIMITED_USES)
                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                .build()));
    }
}