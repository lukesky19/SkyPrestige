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

import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import org.jspecify.annotations.NonNull;

/**
 * This class manages the {@link DialogInput} to set the server's multiplier time.
 */
public class ServerMultiplierTimeInput {
    /**
     * Default Constructor
     */
    public ServerMultiplierTimeInput () {}

    /**
     * Create the input to set the server's multiplier time.
     * @param multiplierManager A {@link MultiplierManager} instance.
     * @return The {@link DialogInput}
     */
    public static @NonNull DialogInput createInput(@NonNull MultiplierManager multiplierManager) {
        return DialogInput.text("server_multiplier_time", AdventureUtility.plain("Server Multiplier Time In Seconds"))
                .labelVisible(true)
                .initial(String.valueOf(multiplierManager.getServerMultiplierTime()))
                .maxLength(50)
                .width(300)
                .build();
    }
}