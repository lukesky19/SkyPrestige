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
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration for the exchange gui.
 * @param version The config version.
 * @param guiName The name to use in the GUI.
 * @param guiType The {@link GUIType}.
 * @param pages The {@link List} of {@link PageConfig}s for the GUI.
 */
@ConfigSerializable
public record ExchangeGUIConfig(
        int version,
        @Nullable String guiName,
        @Nullable GUIType guiType,
        @NonNull List<PageConfig> pages) {
    /**
     * The configuration for a specific page.
     * @param filler The {@link ItemStackConfig} to fill the GUI with.
     * @param prevPage The {@link ButtonConfig} for the previous page.
     * @param nextPage The {@link ButtonConfig} for the next page.
     * @param exit The {@link ButtonConfig} for the exit button.
     * @param prestigePoints {@link ButtonConfig} for the button that displays a player's prestige points.
     * @param exchangeButtons The {@link List} of {@link ButtonConfig} for the exchange buttons.
     * @param dummyButtons A {@link List} of {@link ButtonConfig}s to display in the GUI.
     */
    @ConfigSerializable
    public record PageConfig(
            @NonNull ItemStackConfig filler,
            @NonNull ButtonConfig prevPage,
            @NonNull ButtonConfig nextPage,
            @NonNull ButtonConfig exit,
            @NonNull ButtonConfig prestigePoints,
            @NonNull List<ExchangeButtonConfig> exchangeButtons,
            @NonNull List<ButtonConfig> dummyButtons) {}

    /**
     * This record contains the configuration for an exchange button to display in a GUI.
     * @param displayItem The {@link ItemStackConfig} for the {@link ItemStack} to display in the gui.
     * @param exchangeItem The {@link ItemStackConfig} for the {@link ItemStack} to give the player.
     * @param exchangeCommands The {@link List} of commands as a {@link String} to run on exchange.
     * @param slot The slot to place the {@link ItemStack} at.
     * @param exchangePoints The prestige points to exchange for this button. Only used for the exchange GUI.
     */
    @ConfigSerializable
    public record ExchangeButtonConfig(
            @NonNull ItemStackConfig displayItem,
            @NonNull ItemStackConfig exchangeItem,
            @NonNull List<String> exchangeCommands,
            @Nullable Integer slot,
            @Nullable Double exchangePoints) {}
}
