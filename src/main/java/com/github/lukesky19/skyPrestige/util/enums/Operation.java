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
package com.github.lukesky19.skyPrestige.util.enums;

import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This enum is used to identify how to apply a numeric modification.
 */
public enum Operation {
    /**
     * No change.
     */
    NONE,
    /**
     * Add the numeric value.
     */
    ADD,
    /**
     * Remove the numeric value.
     */
    REMOVE,
    /**
     * Set the numeric value.
     */
    SET;

    /**
     * Create the {@link List} of {@link SingleOptionDialogInput.OptionEntry} for use in a dialog for the Operations.
     * @return A {@link List} of {@link SingleOptionDialogInput.OptionEntry}.
     */
    public static @NonNull List<SingleOptionDialogInput.OptionEntry> createDialogEntries() {
        return List.of(
                SingleOptionDialogInput.OptionEntry.create("none", Component.text("No Operation"), true),
                SingleOptionDialogInput.OptionEntry.create("add", Component.text("Add Operation"), false),
                SingleOptionDialogInput.OptionEntry.create("remove", Component.text("Remove Operation"), false),
                SingleOptionDialogInput.OptionEntry.create("set", Component.text("Set Operation"), false)
        );
    }

    /**
     * Get the operation from the name.
     * @param name The name or null.
     * @return An {@link Optional} {@link Operation}.
     */
    public static @NonNull Optional<Operation> getOperation(@Nullable String name) {
        if(name == null) return Optional.empty();

        try {
            return Optional.of(Operation.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}