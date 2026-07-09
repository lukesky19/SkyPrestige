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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.info;

import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.common.button.ReturnButton;
import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skyPrestige.dialog.manager.DialogManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the dialog to view island info.
 */
public class InfoDialog {
    /**
     * Default Constructor
     */
    public InfoDialog () {}

    /**
     * Create the dialog to manage island multiplier options.
     * @param plugin A {@link SkyPlugin} instance.
     * @param dialogManager A {@link DialogManager} instance.
     * @param player The {@link Player} viewing the dialog.
     * @param island The {@link Island}.
     * @param islandData The {@link IslandData}.
     * @param islandIdentifier The island's identifier.
     * @param islandDialog The {@link IslandDialog}.
     * @return The {@link Dialog}.
     */
    public static @NonNull Dialog createDialog(
            @NonNull SkyPlugin plugin,
            @NonNull DialogManager dialogManager,
            @NonNull Player player,
            @NonNull Island island,
            @NonNull IslandData islandData,
            @NonNull String islandIdentifier,
            @NonNull IslandDialog islandDialog) {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(AdventureUtility.plain("Island Info for " + islandIdentifier + "'s Island"))
                        .body(createBody(plugin, island, islandData))
                        .build())
                .type(DialogType.notice(ReturnButton.createButton(dialogManager, player, islandDialog))));
    }

    /**
     * Create the dialog's body displaying the information.
     * @param plugin A {@link SkyPlugin} instance.
     * @param island The {@link Island}.
     * @param islandData The {@link IslandData}.
     * @return The {@link List} of {@link DialogBody}.
     */
    private static @NonNull List<DialogBody> createBody(
            @NonNull SkyPlugin plugin,
            @NonNull Island island,
            @NonNull IslandData islandData) {
        List<DialogBody> bodyList = new ArrayList<>();

        // Island Members
        bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Island Members:")));

        Server server = plugin.getServer();
        island.getMemberSet()
                .stream()
                .map(server::getOfflinePlayer)
                .filter(offlinePlayer -> offlinePlayer.getName() != null)
                .forEach(offlinePlayer ->
                        bodyList.add(DialogBody.plainMessage(AdventureUtility.plain(offlinePlayer.getName()))));

        // Prestige Level
        bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Prestige Level: " + islandData.getPrestigeLevel())));

        // Prestige Points
        bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Prestige Points: " + islandData.getPrestigePoints())));

        // Prestige Exempt
        bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Prestige Exempt: " + islandData.isPrestigeExempt())));

        // Leaderboard Exempt
        bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Leaderboard Exempt: " + islandData.isLeaderboardExempt())));

        // Multiplier
        bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Multiplier: " + islandData.getMultiplier())));
        bodyList.add(DialogBody.plainMessage(AdventureUtility.deserialize("Multiplier Time: " + islandData.getMultiplierTime())));

        return bodyList;
    }
}