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
package com.github.lukesky19.skyPrestige.configuration.data.gui;

import com.github.lukesky19.skyPrestige.configuration.data.gui.common.ButtonConfig;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration for the vault gui.
 * @param configVersion The config version.
 * @param guiName The name to use in the GUI.
 * @param guiType The {@link GUIType}.
 * @param pages The {@link List} of {@link PageConfig} for the GUI.
 */
@ConfigSerializable
public record VaultGUIConfig(
        @Nullable String configVersion,
        @Nullable String guiName,
        @Nullable GUIType guiType,
        @NonNull List<PageConfig> pages) {
    /**
     * The button configuration for a page.
     * @param filler The {@link ItemStackConfig} to fill the GUI with.
     * @param nextPage The {@link ButtonConfig} for the next page button.
     * @param prevPage The {@link ButtonConfig} for the previous page button.
     * @param exit The {@link ButtonConfig} for the exit button.
     * @param slots The list of {@link SlotConfig}s that players can input items inf or storage.
     * @param dummyButtons A {@link List} of {@link ButtonConfig}s to display in the GUI.
     */
    @ConfigSerializable
    public record PageConfig(
            @NonNull ItemStackConfig filler,
            @NonNull ButtonConfig nextPage,
            @NonNull ButtonConfig prevPage,
            @NonNull ButtonConfig exit,
            @NonNull List<SlotConfig> slots,
            @NonNull List<ButtonConfig> dummyButtons) {}
    /**
     * The configuration for slots that players can insert items into.
     * @param slot The slot number.
     * @param prestigeLevel The prestige level required for this slot.
     * @param unlockedItem The {@link ItemStackConfig} used when the player has access to a slot and the slot doesn't contain any items.
     * @param lockedItem The {@link ItemStackConfig} used when the island doesn't meet the required prestige level for the slot.
     */
    @ConfigSerializable
    public record SlotConfig(
            @Nullable Integer slot,
            @Nullable Integer prestigeLevel,
            @NonNull ItemStackConfig unlockedItem,
            @NonNull ItemStackConfig lockedItem) {}
}
