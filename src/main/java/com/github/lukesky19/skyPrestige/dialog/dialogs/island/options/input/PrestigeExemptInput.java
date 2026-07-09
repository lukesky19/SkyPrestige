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
package com.github.lukesky19.skyPrestige.dialog.dialogs.island.options.input;

import com.github.lukesky19.skyPrestige.dialog.dialogs.island.island.IslandDialog;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import org.jspecify.annotations.NonNull;

/**
 * This class manages the {@link DialogInput} to set the prestige exemption status.
 */
public class PrestigeExemptInput {
    /**
     * Default Constructor
     */
    public PrestigeExemptInput () {}

    /**
     * Create the input to set the prestige exemption status.
     * @param islandDialog The {@link IslandDialog} storing player inputs.
     * @return The {@link DialogInput}
     */
    public static @NonNull DialogInput createInput(@NonNull IslandDialog islandDialog) {
        return DialogInput.bool("prestige_Exempt", AdventureUtility.plain("Island Prestige Exemption"))
                .initial(islandDialog.isPrestigeExempt())
                .onTrue("Island Prestige Exempt")
                .onFalse("Island Not Prestige Exempt")
                .build();
    }
}