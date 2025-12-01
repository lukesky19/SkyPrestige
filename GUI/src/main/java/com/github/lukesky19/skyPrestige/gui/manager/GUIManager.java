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

import com.github.lukesky19.skyPrestige.core.abstracts.SkyPlugin;
import com.github.lukesky19.skyPrestige.core.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skyPrestige.gui.gui.ExchangeGUI;
import com.github.lukesky19.skyPrestige.gui.gui.VaultGUI;
import com.github.lukesky19.skylib.api.gui.AbstractGUIManager;
import com.github.lukesky19.skylib.api.gui.interfaces.BaseGUI;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class manages open GUIs.
 */
public class GUIManager extends AbstractGUIManager {
    private final @NotNull Map<IslandIdUUIDKey, BaseGUI> openGUIsByIslandIdAndUUID = new HashMap<>();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public GUIManager(@NotNull SkyPlugin plugin) {
        super(plugin);
    }

    /**
     * Store the {@link BaseGUI} by the island id and the {@link UUID} provided.
     * @param islandId The island id.
     * @param uuid The {@link UUID} of the player.
     * @param baseGUI The {@link BaseGUI} that they opened.
     */
    public void addOpenGUI(@NotNull String islandId, @NotNull UUID uuid, @NotNull BaseGUI baseGUI) {
        openGUIsByIslandIdAndUUID.put(new IslandIdUUIDKey(islandId, uuid), baseGUI);
    }

    /**
     * Remove any {@link BaseGUI} mapped to the island id and the {@link UUID}.
     * @param islandId The island id.
     * @param uuid The {@link UUID} of the player.
     */
    public void removeOpenGUI(@NotNull String islandId, @NotNull UUID uuid) {
        IslandIdUUIDKey islandIdUUIDKey = new IslandIdUUIDKey(islandId, uuid);
        openGUIsByIslandIdAndUUID.keySet().removeIf(key -> key.equals(islandIdUUIDKey));
    }

    /**
     * Closes any open {@link BaseGUI}s for all players.
     * @param onDisable Whether the GUIs are being closed during plugin disable or not.
     */
    @Override
    public void closeOpenGUIs(boolean onDisable) {
        super.closeOpenGUIs(onDisable);

        openGUIsByIslandIdAndUUID.clear();
    }

    /**
     * Refresh any {@link ExchangeGUI}s that are open for the island id provided.
     * @param islandId The island id.
     */
    public void refreshExchangeGUIs(@NotNull String islandId) {
        openGUIsByIslandIdAndUUID.entrySet().stream()
                .filter(entry -> entry.getKey().islandId().equals(islandId))
                .forEach(entry -> {
                    BaseGUI gui = entry.getValue();
                    if(!(gui instanceof ExchangeGUI)) return;

                    gui.refresh();
                });
    }

    /**
     * Refresh any {@link VaultGUI}s that are open for the island id provided.
     * @param islandId The island id.
     */
    public void refreshVaultGUIs(@NotNull String islandId) {
        openGUIsByIslandIdAndUUID.entrySet().stream()
                .filter(entry -> entry.getKey().islandId().equals(islandId))
                .forEach(entry -> {
                    BaseGUI gui = entry.getValue();
                    if(!(gui instanceof VaultGUI)) return;

                    gui.refresh();
                });
    }
}
