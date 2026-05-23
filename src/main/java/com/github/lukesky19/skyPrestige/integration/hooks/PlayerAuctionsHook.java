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
package com.github.lukesky19.skyPrestige.integration.hooks;

import com.github.lukesky19.skylib.common.api.integration.Hook;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.olziedev.playerauctions.api.PlayerAuctionsAPI;
import com.olziedev.playerauctions.api.events.auction.PlayerAuctionRemoveEvent;
import com.olziedev.playerauctions.api.player.APlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * This class manages interfacing with the Player Auctions plugin.
 */
public class PlayerAuctionsHook implements Hook {
    private final @NonNull SkyPlugin plugin;
    private @Nullable PlayerAuctionsAPI playerAuctionsAPI;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     */
    public PlayerAuctionsHook(@NonNull SkyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to get the {@link PlayerAuctionsAPI} from PlayerAuctions.
     */
    @Override
    public void initialize() {
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("PlayerAuctions");
        if(plugin != null && plugin.isEnabled()) {
            playerAuctionsAPI = PlayerAuctionsAPI.getInstance();
        }
    }

    /**
     * Is the hook initialized?
     * @return true if hooked, otherwise false.
     */
    @Override
    public boolean isHooked() {
        return playerAuctionsAPI != null;
    }

    /**
     * Clear the auctions for the {@link UUID} provided.
     * @param uuid The {@link UUID} of the player.
     */
    public void clearPlayerAuctions(@NonNull UUID uuid) {
        if(playerAuctionsAPI == null) return;

        APlayer auctionPlayer = playerAuctionsAPI.getAuctionPlayer(uuid);
        auctionPlayer.getPlayerAuctions().forEach(auction -> auction.removeAuction(PlayerAuctionRemoveEvent.Cause.PURGED, _ -> {}, null));
        auctionPlayer.setBackpack(List.of());
    }
}
