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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration for a confirm gui.
 * @param configVersion The config version.
 * @param guiName The name to use in the GUI.
 * @param guiType The {@link GUIType}.
 * @param blueprintBundleSlot The slot to place the blueprint bundle that was selected in.
 * @param filler The {@link ItemStackConfig} to fill the GUI with.
 * @param confirmButton The {@link ButtonConfig} for the confirm button.
 * @param cancelButton The {@link ButtonConfig} for the cancel button.
 * @param rewardsButton The {@link ButtonConfig} to open the rewards GUI.
 * @param keepMembers The {@link ButtonConfig} for the button that says island members are carried over on island reset.
 * @param keepCommandRanks The {@link ButtonConfig} for the button that says command ranks are carried over on island reset.
 * @param keepFlags The {@link ButtonConfig} for the button that says island flags are carried over on island reset.
 * @param conditionalButtons The {@link ConditionalButtons} config for the GUI.
 * @param dummyButtons A {@link List} of {@link ButtonConfig}s to display in the GUI.
 */
@ConfigSerializable
public record ConfirmGUIConfig(
        @Nullable String configVersion,
        @Nullable String guiName,
        @Nullable GUIType guiType,
        int blueprintBundleSlot,
        @NotNull ItemStackConfig filler,
        @NotNull ButtonConfig confirmButton,
        @NotNull ButtonConfig cancelButton,
        @NotNull ButtonConfig rewardsButton,
        @NotNull ButtonConfig keepMembers,
        @NotNull ButtonConfig keepFlags,
        @NotNull ButtonConfig keepCommandRanks,
        @NotNull ConditionalButtons conditionalButtons,
        @NotNull List<ButtonConfig> dummyButtons) {
    /**
     * This record contains the configuration for the buttons displayed depending on the prestige level config.
     * @param keepInventory The {@link ButtonConfig} for the button shown when island member's inventories are carried over on island reset.
     * @param clearInventory The {@link ButtonConfig} for the button shown when island member's inventories are reset on island reset.
     * @param keepEnderChest The {@link ButtonConfig} for the button shown when island member's ender chests are carried over prestige.
     * @param clearEnderChest The {@link ButtonConfig} for the button shown when island member's ender chests are reset on island reset.
     * @param keepExp The {@link ButtonConfig} for the button shown when island member's ender chests is carried over on island reset.
     * @param resetExp The {@link ButtonConfig} for the button shown when island member's experience is reset on island reset.
     * @param keepMoney The {@link ButtonConfig} for the button shown when island member's balance is carried over on island reset.
     * @param resetMoney The {@link ButtonConfig} for the button shown when island member's balance is reset on island reset.
     * @param keepAuctionItems The {@link ButtonConfig} for the button shown when an island member's auction house items are carried over on island reset.
     * @param resetAuctionItems The {@link ButtonConfig} for the button shown when an island member's auction house items are reset on island reset.
     * @param keepGeneratorUpgrades The {@link ButtonConfig} for the button shown when an island's generator upgrades carry over on island reset.
     * @param resetGeneratorUpgrades The {@link ButtonConfig} for the button shown when an island's generator upgrades are reset on island reset.
     * @param keepVaultItems The {@link ButtonConfig} for the button shown when the island's vault is carried over on island reset.
     * @param clearVaultItems The {@link ButtonConfig} for the button shown when the island's vault is reset on island reset.
     * @param startingMoney The {@link ButtonConfig} for the button shown when one or more island members receive starting money on island reset.
     * @param noStartingMoney The {@link ButtonConfig} for the button shown when no starting money is given on island reset.
     * @param keepSessionPlayTime The {@link ButtonConfig} for the button shown when session play time is carried over on island reset.
     * @param resetSessionPlayTime The {@link ButtonConfig} for the button shown when session play time is reset on island reset.
     * @param keepDailyPlayTime The {@link ButtonConfig} for the button shown when daily play time is carried over on island reset.
     * @param resetDailyPlayTime The {@link ButtonConfig} for the button shown when daily play time is reset on island reset.
     * @param keepWeeklyPlayTime The {@link ButtonConfig} for the button shown when weekly play time is carried over on island reset.
     * @param resetWeeklyPlayTime The {@link ButtonConfig} for the button shown when weekly play time is reset on island reset.
     * @param keepMonthlyPlayTime The {@link ButtonConfig} for the button shown when monthly play time is carried over on island reset.
     * @param resetMonthlyPlayTime The {@link ButtonConfig} for the button shown when monthly play time is reset on island reset.
     * @param keepYearlyPlayTime The {@link ButtonConfig} for the button shown when yearly play time is carried over on island reset.
     * @param resetYearlyPlayTime The {@link ButtonConfig} for the button shown when yearly play time is reset on island reset.
     * @param keepTotalPlayTime The {@link ButtonConfig} for the button shown when total play time is carried over on island reset.
     * @param resetTotalPlayTime The {@link ButtonConfig} for the button shown when total play time is reset on island reset.
     */
    @ConfigSerializable
    public record ConditionalButtons(
            @NotNull ButtonConfig keepInventory,
            @NotNull ButtonConfig clearInventory,
            @NotNull ButtonConfig keepEnderChest,
            @NotNull ButtonConfig clearEnderChest,
            @NotNull ButtonConfig keepExp,
            @NotNull ButtonConfig resetExp,
            @NotNull ButtonConfig keepMoney,
            @NotNull ButtonConfig resetMoney,
            @NotNull ButtonConfig keepAuctionItems,
            @NotNull ButtonConfig resetAuctionItems,
            @NotNull ButtonConfig keepGeneratorUpgrades,
            @NotNull ButtonConfig resetGeneratorUpgrades,
            @NotNull ButtonConfig keepVaultItems,
            @NotNull ButtonConfig clearVaultItems,
            @NotNull ButtonConfig startingMoney,
            @NotNull ButtonConfig noStartingMoney,
            @NotNull ButtonConfig keepSessionPlayTime,
            @NotNull ButtonConfig resetSessionPlayTime,
            @NotNull ButtonConfig keepDailyPlayTime,
            @NotNull ButtonConfig resetDailyPlayTime,
            @NotNull ButtonConfig keepWeeklyPlayTime,
            @NotNull ButtonConfig resetWeeklyPlayTime,
            @NotNull ButtonConfig keepMonthlyPlayTime,
            @NotNull ButtonConfig resetMonthlyPlayTime,
            @NotNull ButtonConfig keepYearlyPlayTime,
            @NotNull ButtonConfig resetYearlyPlayTime,
            @NotNull ButtonConfig keepTotalPlayTime,
            @NotNull ButtonConfig resetTotalPlayTime) {}
}