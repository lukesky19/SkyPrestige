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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.multiplier.input;

import com.github.lukesky19.skyPrestige.util.enums.Operation;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import org.jspecify.annotations.NonNull;

/**
 * This class manages the {@link DialogInput} to for choosing a multiplier operation.
 */
public class IslandMultiplierOperationInput {
    /**
     * Default Constructor
     */
    public IslandMultiplierOperationInput () {}

    /**
     * Create the input to choose a multiplier operation.
     * @return The {@link DialogInput}
     */
    public static @NonNull DialogInput createInput() {
        return DialogInput.singleOption(
                "island_multiplier_operation",
                300,
                Operation.createDialogEntries(),
                AdventureUtility.plain("Select a Multiplier Operation"),
                true);
    }
}