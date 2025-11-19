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
package com.github.lukesky19.skyPrestige.config.data.gui;

import com.github.lukesky19.skyPrestige.config.data.gui.button.ButtonConfig;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration for the progress gui.
 * @param configVersion The config version.
 * @param guiName The name to use in the GUI.
 * @param guiType The {@link GUIType}.
 * @param filler The {@link ItemStackConfig} to fill the GUI with.
 * @param exit The {@link ButtonConfig} for the exit button.
 * @param progressButtons The {@link ProgressButtons} config to use.
 * @param progressSlots The slots to place the appropriate progress buttons at.
 * @param dummyButtons A {@link List} of {@link ButtonConfig}s to display in the GUI.
 */
@ConfigSerializable
public record ProgressGUIConfig(
        @Nullable String configVersion,
        @Nullable String guiName,
        @Nullable GUIType guiType,
        @NotNull ItemStackConfig filler,
        @NotNull ButtonConfig exit,
        @NotNull ProgressButtons progressButtons,
        @NotNull List<Integer> progressSlots,
        @NotNull List<ButtonConfig> dummyButtons) {
    /**
     * This record contains the configuration for the progress buttons.
     * @param incomplete The {@link ItemStackConfig} for the incomplete progress button.
     * @param complete The {@link ItemStackConfig} for the complete progress button.
     */
    @ConfigSerializable
    public record ProgressButtons(
            ItemStackConfig incomplete,
            ItemStackConfig complete) {}
}
