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
package com.github.lukesky19.skyPrestige.gui.abstracts;

import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.paper.api.gui.interfaces.IGUIManager;
import com.github.lukesky19.skylib.paper.api.gui.templates.ChestGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

/**
 * This class can be used to create a confirmation GUI.
 */
public abstract class ConfirmGUI extends ChestGUI<IslandIdUUIDKey> {
    /**
     * Constructor.
     * @param plugin The {@link JavaPlugin} creating the GUI.
     * @param guiManager An {@link IGUIManager} that is used to track open GUIs.
     * @param identifier The identifier that the GUI is tied to. Used in conjunction with {@link IGUIManager}.
     * @param player The {@link Player} associated with the created GUI.
     */
    public ConfirmGUI(
            @NonNull JavaPlugin plugin,
            @NonNull IGUIManager<IslandIdUUIDKey> guiManager,
            @NonNull IslandIdUUIDKey identifier,
            @NonNull Player player) {
        super(plugin, guiManager, identifier, player);
    }

    /**
     * Create the {@link InventoryView} for the GUI.
     * @return true if successful, otherwise false.
     */
    public abstract boolean create();

    /**
     * Cancel the confirmation process.
     */
    public abstract void cancel();
}
