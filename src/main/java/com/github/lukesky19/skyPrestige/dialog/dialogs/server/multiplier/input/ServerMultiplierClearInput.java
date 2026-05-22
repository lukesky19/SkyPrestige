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
package com.github.lukesky19.skyPrestige.dialog.dialogs.server.multiplier.input;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import org.jspecify.annotations.NonNull;

/**
 * This class manages the {@link DialogInput} to choose whether to clear any server multiplier data.
 */
public class ServerMultiplierClearInput {
    /**
     * Default Constructor
     */
    public ServerMultiplierClearInput () {}

    /**
     * Create the input to choose whether to clear any server multiplier data.
     * @return The {@link DialogInput}
     */
    public static @NonNull DialogInput createInput() {
        return DialogInput.bool("server_multiplier_clear", AdventureUtility.plain("Clear any server multiplier data."))
                .initial(false)
                .onTrue("Clear Server Multiplier Data")
                .onFalse("Do Not Clear Server Multiplier Data")
                .build();
    }
}