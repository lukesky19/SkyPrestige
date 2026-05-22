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
package com.github.lukesky19.skyPrestige.dialog.manager;

import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import io.papermc.paper.dialog.Dialog;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Server;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class manages whether the player has an open dialog from the plugin.
 */
public class DialogManager {
    private final @NonNull SkyPlugin skyPlugin;
    private final @NonNull Map<UUID, Dialog> dialogMap = new HashMap<>();

    /**
     * Constructor
     * @param skyPlugin A {@link SkyPlugin} instance.
     */
    public DialogManager(@NonNull SkyPlugin skyPlugin) {
        this.skyPlugin = skyPlugin;
    }

    /**
     * Store the open dialog for the player.
     * @param playerId The player's {@link UUID}.
     * @param dialog The player's {@link Dialog}.
     */
    public void addOpenDialog(@NonNull UUID playerId, @NonNull Dialog dialog) {
        dialogMap.put(playerId, dialog);
    }

    /**
     * Remove the open dialog for the player.
     * @param playerId The player's {@link UUID}.
     */
    public void removeOpenDialog(@NonNull UUID playerId) {
        dialogMap.remove(playerId);
    }

    /**
     * Get the open dialog for the player.
     * @param playerId The player's {@link UUID}.
     * @return The open {@link Dialog} or null.
     */
    public @Nullable Dialog getOpenDialog(@NonNull UUID playerId) {
        return dialogMap.get(playerId);
    }

    /**
     * Close open dialogs.
     */
    public void closeOpenDialogs() {
        Server server = skyPlugin.getServer();
        dialogMap.keySet().stream()
                .map(server::getPlayer)
                .filter(player -> player != null && player.isOnline() && player.isConnected())
                .forEach(Audience::closeDialog);
    }
}