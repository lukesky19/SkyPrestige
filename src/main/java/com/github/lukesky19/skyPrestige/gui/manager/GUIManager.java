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
package com.github.lukesky19.skyPrestige.gui.manager;

import com.github.lukesky19.skyPrestige.gui.gui.ExchangeGUI;
import com.github.lukesky19.skyPrestige.gui.gui.VaultGUI;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.api.gui.abstracts.AbstractGUIManager;
import com.github.lukesky19.skylib.api.gui.interfaces.BaseGUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class manages open GUIs.
 */
public class GUIManager extends AbstractGUIManager<IslandIdUUIDKey> {
    /**
     * Constructor
     */
    public GUIManager() {}

    @Override
    public @Nullable BaseGUI<IslandIdUUIDKey> getOpenGUI(@NotNull IslandIdUUIDKey identifier) {
        BaseGUI<IslandIdUUIDKey> gui = dataMap.get(identifier);
        if(gui != null) return gui;

        IslandIdUUIDKey playerIdentifier = new IslandIdUUIDKey(null, identifier.uuid());
        return dataMap.get(playerIdentifier);
    }

    /**
     * Refresh any {@link ExchangeGUI}s that are open for the island id provided.
     * @param islandId The island id.
     */
    public void refreshExchangeGUIs(@NotNull String islandId) {
        dataMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().islandId() != null
                        && entry.getKey().islandId().equals(islandId))
                .forEach(entry -> {
                    BaseGUI<IslandIdUUIDKey> gui = entry.getValue();
                    if(!(gui instanceof ExchangeGUI)) return;

                    gui.refresh();
                });
    }

    /**
     * Refresh any {@link VaultGUI}s that are open for the island id provided.
     * @param islandId The island id.
     */
    public void refreshVaultGUIs(@NotNull String islandId) {
        dataMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().islandId() != null
                        && entry.getKey().islandId().equals(islandId))
                .forEach(entry -> {
                    BaseGUI<IslandIdUUIDKey> gui = entry.getValue();
                    if(!(gui instanceof VaultGUI)) return;

                    gui.refresh();
                });
    }
}
