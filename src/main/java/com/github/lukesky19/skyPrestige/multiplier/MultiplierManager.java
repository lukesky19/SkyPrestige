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
package com.github.lukesky19.skyPrestige.multiplier;

import com.github.lukesky19.skyPrestige.configuration.data.common.TimeFormat;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.data.manager.IslandDataManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class manages the multiplier that is applied to prestige points earned.
 */
public class MultiplierManager {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull IslandDataManager islandDataManager;

    private final @NotNull Multiplier serverMultiplier = new Multiplier();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandDataManager An {@link IslandDataManager} instance.
     */
    public MultiplierManager(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull IslandDataManager islandDataManager) {
        this.plugin = plugin;
        this.islandDataManager = islandDataManager;
        this.localeManager = localeManager;
    }

    /**
     * Get the effective multiplier.
     * Adds the server multiplier to the island multiplier.
     * @apiNote 1.0 (Base) + (Server Multiplier) + (Island Multiplier) = value returned
     * @param island The {@link Island} to get the island multiplier for.
     * @return The total multiplier.
     */
    public double getMultiplier(@NotNull Island island) {
        return 1.0 + getServerMultiplier() + getIslandMultiplier(island);
    }

    /**
     * Get the effective multiplier.
     * Adds the server multiplier to the island multiplier.
     * @apiNote 1.0 (Base) + (Server Multiplier) + (Island Multiplier) = value returned
     * @param islandData The {@link IslandData} to get the island multiplier for.
     * @return The total multiplier.
     */
    public double getMultiplier(@NotNull IslandData islandData) {
        return 1.0 + getServerMultiplier() + getIslandMultiplier(islandData);
    }

    /**
     * Set the multiplier for the server.
     * @apiNote This multiplier is added to the base and island multiplier.<br>
     * Example: 1.0 (Base) + 0.0 (Server) + 1.0 (Island) is a 2.0 multiplier.<br>
     * Example: 1.0 (Base) + 1.0 (Server) + 1.0 (Island) is a 3.0 multiplier.
     * @param player The {@link Player} that initiated the change or null.
     * @param multiplier The multiplier or null.
     * @param time The multiplier time or null.
     * @param notice Should the online players be told about the change?
     * @return true if successful, false if not.
     */
    public boolean setServerMultiplier(@Nullable Player player, @Nullable Double multiplier, @Nullable Long time, boolean notice) {
        // If no change, return false
        if(multiplier == null && time == null) return false;

        // Update the multiplier
        if(multiplier != null) {
            serverMultiplier.setMultiplier(multiplier);
        }

        if(time != null) {
            serverMultiplier.setTime(time);
        }

        // Send relevant messages
        sendServerMultiplierChangedNotice(player, serverMultiplier.getMultiplier(), serverMultiplier.getTime(), notice);

        return true;
    }

    /**
     * Add to the multiplier for the server.
     * @apiNote This multiplier is added to the base and island multiplier.<br>
     * Example: 1.0 (Base) + 0.0 (Server) + 1.0 (Island) is a 2.0 multiplier.<br>
     * Example: 1.0 (Base) + 1.0 (Server) + 1.0 (Island) is a 3.0 multiplier.
     * @param player The {@link Player} that initiated the change or null.
     * @param multiplier The multiplier or null.
     * @param time The multiplier time or null.
     * @param notice Should the online players be told about the change?
     * @return true if successful, false if not.
     */
    public boolean addServerMultiplier(@Nullable Player player, @Nullable Double multiplier, @Nullable Long time, boolean notice) {
        // If no change, return false
        if(multiplier == null && time == null) return false;

        // Update the multiplier
        if(multiplier != null && multiplier > 0.0) {
            serverMultiplier.addMultiplier(multiplier);
        }

        if(time != null && time > 0) {
            serverMultiplier.addTime(time);
        }

        // Send relevant messages
        sendServerMultiplierChangedNotice(player, serverMultiplier.getMultiplier(), serverMultiplier.getTime(), notice);

        return true;
    }

    /**
     * Remove from the multiplier for the server.
     * @apiNote This multiplier is added to the base and island multiplier.<br>
     * Example: 1.0 (Base) + 0.0 (Server) + 1.0 (Island) is a 2.0 multiplier.<br>
     * Example: 1.0 (Base) + 1.0 (Server) + 1.0 (Island) is a 3.0 multiplier.
     * @param player The {@link Player} that initiated the change or null.
     * @param multiplier The multiplier or null.
     * @param time The multiplier time or null.
     * @param notice Should the online players be told about the change?
     * @return true if successful, false if not.
     */
    public boolean removeServerMultiplier(@Nullable Player player, @Nullable Double multiplier, @Nullable Long time, boolean notice) {
        // If no change, return false
        if(multiplier == null && time == null) return false;

        // Update the multiplier
        if(multiplier != null && multiplier > 0.0) {
            serverMultiplier.removeMultiplier(multiplier);
        }

        if(time != null && time > 0) {
            serverMultiplier.removeTime(time);
        }

        // Send relevant messages
        sendServerMultiplierChangedNotice(player, serverMultiplier.getMultiplier(), serverMultiplier.getTime(), notice);

        return true;
    }

    /**
     * Clear the server's multiplier and multiplier time.
     * @param player The {@link Player} that initiated the change or null.
     * @param notice Should the online player's be told about the change?
     * @return true if successful, false if not.
     */
    public boolean clearServerMultiplier(@Nullable Player player, boolean notice) {
        // Update the multiplier
        serverMultiplier.setMultiplier(0);
        // Update the multiplier time
        serverMultiplier.setTime(0);

        // Send relevant messages
        sendServerMultiplierClearedNotice(player, notice);

        return true;
    }

    /**
     * Get the current multiplier for the server.
     * @apiNote This is not the effective multiplier due to islands also having their own multiplier.<br>
     * See {@link #getIslandMultiplier(Island)} {@link #getMultiplier(Island)} {@link #getMultiplier(IslandData)}.
     * @return The multiplier. 0.0 means the server has no multiplier.
     */
    public double getServerMultiplier() {
        return serverMultiplier.getMultiplier();
    }

    /**
     * Get the time in seconds the server multiplier lasts for.
     * @return The time in seconds or -1 if no time limit.
     */
    public long getServerMultiplierTime() {
        return serverMultiplier.getTime();
    }

    /**
     * Get the multiplier for the island.
     * @param island The {@link Island}.
     * @return The island's multiplier.
     */
    public double getIslandMultiplier(@NotNull Island island) {
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) return 0;

        return islandData.getMultiplier();
    }

    /**
     * Get the multiplier for the island.
     * @param islandData The {@link IslandData}.
     * @return The island's multiplier.
     */
    public double getIslandMultiplier(@NotNull IslandData islandData) {
        return islandData.getMultiplier();
    }

    /**
     * Get the time in seconds the island multiplier lasts for.
     * @param island The {@link Island}.
     * @return The time in seconds or -1 if no time limit.
     */
    public long getIslandMultiplierTime(@NotNull Island island) {
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        if(islandData == null) return 0;

        return islandData.getMultiplierTime();
    }

    /**
     * Set the multiplier for the island.
     * @apiNote This multiplier is added to the base and server multiplier.<br>
     * Example: 1.0 (Base) + 0.0 (Server) + 1.0 (Island) is a 2.0 multiplier.<br>
     * Example: 1.0 (Base) + 1.0 (Server) + 1.0 (Island) is a 3.0 multiplier.
     * @param player The {@link Player} that initiated the change or null.
     * @param island The {@link Island}.
     * @param multiplier The multiplier or null.
     * @param time The multiplier time or null.
     * @param notice Should the island member's be told about the change?
     * @return true if successful, false if not.
     */
    public boolean setIslandMultiplier(@Nullable Player player, @NotNull Island island, @Nullable Double multiplier, @Nullable Long time, boolean notice) {
        // Get the Island's IslandData
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        // If the IslandData is null, return false
        if(islandData == null) return false;
        // If no change, return false
        if(multiplier == null && time == null) return false;

        // Update the multiplier
        if(multiplier != null) {
            islandData.setMultiplier(multiplier);
        }

        // Update the multiplier time
        if(time != null) {
            islandData.setMultiplierTime(time);
        }

        // Send relevant messages
        sendIslandMultiplierChangedNotice(player, island, islandData.getMultiplier(), islandData.getMultiplierTime(), notice);

        return true;
    }

    /**
     * Add to the multiplier for the island.
     * @apiNote This multiplier is added to the base and server multiplier.<br>
     * Example: 1.0 (Base) + 0.0 (Server) + 1.0 (Island) is a 2.0 multiplier.<br>
     * Example: 1.0 (Base) + 1.0 (Server) + 1.0 (Island) is a 3.0 multiplier.
     * @param player The {@link Player} that initiated the change or null.
     * @param island The {@link Island}.
     * @param multiplier The multiplier or null.
     * @param time The multiplier time or null.
     * @param notice Should the island member's be told about the change?
     * @return true if successful, false if not.
     */
    public boolean addIslandMultiplier(@Nullable Player player, @NotNull Island island, @Nullable Double multiplier, @Nullable Long time, boolean notice) {
        // Get the Island's IslandData
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        // If the IslandData is null, return false
        if(islandData == null) return false;
        // If no change, return false
        if(multiplier == null && time == null) return false;

        // Update the multiplier
        if(multiplier != null) {
            islandData.addMultiplier(multiplier);
        }

        // Update the multiplier time
        if(time != null) {
            islandData.addMultiplierTime(time);
        }

        // Send relevant messages
        sendIslandMultiplierChangedNotice(player, island, islandData.getMultiplier(), islandData.getMultiplierTime(), notice);

        return true;
    }

    /**
     * Remove from the multiplier for the island.
     * @apiNote This multiplier is added to the base and server multiplier.<br>
     * Example: 1.0 (Base) + 0.0 (Server) + 1.0 (Island) is a 2.0 multiplier.<br>
     * Example: 1.0 (Base) + 1.0 (Server) + 1.0 (Island) is a 3.0 multiplier.
     * @param player The {@link Player} that initiated the change or null.
     * @param island The {@link Island}.
     * @param multiplier The multiplier or null.
     * @param time The multiplier time or null.
     * @param notice Should the island member's be told about the change?
     * @return true if successful, false if not.
     */
    public boolean removeIslandMultiplier(@Nullable Player player, @NotNull Island island, @Nullable Double multiplier, @Nullable Long time, boolean notice) {
        // Get the Island's IslandData
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        // If the IslandData is null, return false
        if(islandData == null) return false;
        // If no change, return false
        if(multiplier == null && time == null) return false;

        // Update the multiplier
        if(multiplier != null) {
            islandData.removeMultiplier(multiplier);
        }

        // Update the multiplier time
        if(time != null) {
            islandData.removeMultiplierTime(time);
        }

        // Send relevant messages
        sendIslandMultiplierChangedNotice(player, island, islandData.getMultiplier(), islandData.getMultiplierTime(), notice);

        return true;
    }

    /**
     * Clear the island's multiplier and multiplier time.
     * @param player The {@link Player} that initiated the change or null.
     * @param island The {@link Island}.
     * @param notice Should the island member's be told about the change?
     * @return true if successful, false if not.
     */
    public boolean clearIslandMultiplier(@Nullable Player player, @NotNull Island island, boolean notice) {
        // Get the Island's IslandData
        @Nullable IslandData islandData = islandDataManager.getData(island.getUniqueId());
        // If the IslandData is null, return false
        if(islandData == null) return false;

        // Update the multiplier
        islandData.setMultiplier(0);
        // Update the multiplier time
        islandData.setMultiplierTime(0);

        // Send relevant messages
        sendIslandMultiplierClearedNotice(player, island, notice);

        return true;
    }

    /**
     * Get the {@link Component} for the time placeholder.
     * @param timeMessage The {@link TimeFormat} to use.
     * @param timeInSeconds The time in seconds to format.
     * @return A {@link Component}.
     */
    @NotNull
    public Component getTimePlaceholder(@NotNull TimeFormat timeMessage, long timeInSeconds) {
        boolean firstUnit = true;
        Time timeRecord = TimeUtil.millisToTime(timeInSeconds * 1000L);
        StringBuilder messageBuilder = new StringBuilder();

        if(!timeMessage.prefix().isEmpty()) messageBuilder.append(timeMessage.prefix());

        if(timeRecord.years() > 0) {
            messageBuilder.append(timeMessage.years());
            firstUnit = false;
        }

        if(timeRecord.months() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(timeMessage.months());
            firstUnit = false;
        }

        if(timeRecord.weeks() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(timeMessage.weeks());
            firstUnit = false;
        }

        if(timeRecord.days() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(timeMessage.days());
            firstUnit = false;
        }

        if(timeRecord.hours() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(timeMessage.hours());
            firstUnit = false;
        }

        if(timeRecord.minutes() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(timeMessage.minutes());
            firstUnit = false;
        }

        if(timeRecord.seconds() > 0) {
            if(!firstUnit) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(timeMessage.seconds());
            firstUnit = false;
        }

        if(firstUnit) {
            messageBuilder.append(timeMessage.seconds());
        }

        if(!timeMessage.suffix().isEmpty()) messageBuilder.append(timeMessage.suffix());

        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("years", String.valueOf(timeRecord.years())),
                Placeholder.parsed("months", String.valueOf(timeRecord.months())),
                Placeholder.parsed("weeks", String.valueOf(timeRecord.weeks())),
                Placeholder.parsed("days", String.valueOf(timeRecord.days())),
                Placeholder.parsed("hours", String.valueOf(timeRecord.hours())),
                Placeholder.parsed("minutes", String.valueOf(timeRecord.minutes())),
                Placeholder.parsed("seconds", String.valueOf(timeRecord.seconds())));

        return AdventureUtil.deserialize(messageBuilder.toString(), placeholders);
    }

    /**
     * Send the relevant notices to online players and feedback to the command player.
     * @param player The {@link Player}.
     * @param multiplier The multiplier.
     * @param time The time.
     * @param notice If online players should be informed of the change.
     */
    private void sendServerMultiplierChangedNotice(
            @Nullable Player player,
            double multiplier,
            long time,
            boolean notice) {
        Locale locale = localeManager.getConfiguration();
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("multiplier", String.valueOf(multiplier)));
        if(time > -1) placeholders.add(Placeholder.component("time", getTimePlaceholder(locale.multiplier().multiplierTimePlaceholder(), time)));

        // Send notice to online players
        if(notice) {
            Component message = time != -1 ?
                    AdventureUtil.deserialize(locale.prefix() + locale.multiplier().serverMultiplierChangedTimeLimit(), placeholders) :
                    AdventureUtil.deserialize(locale.prefix() + locale.multiplier().serverMultiplierChangedNoTimeLimit(), placeholders);

            plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(message));
        }

        // If no Player, return
        if(player == null) return;

        // Send feedback to Player
        Component message = time != -1 ?
                AdventureUtil.deserialize(locale.prefix() + locale.multiplier().serverMultiplierTimeLimit(), placeholders) :
                AdventureUtil.deserialize(locale.prefix() + locale.multiplier().serverMultiplierNoTimeLimit(), placeholders);
        player.sendMessage(message);
    }

    /**
     * Send the relevant notices to online players and feedback to the command player.
     * @param player The {@link Player}.
     * @param notice If online players should be informed of the change.
     */
    private void sendServerMultiplierClearedNotice(
            @Nullable Player player,
            boolean notice) {
        Locale locale = localeManager.getConfiguration();

        // Send notice to online players
        if(notice) {
            Component message = AdventureUtil.deserialize(locale.prefix() + locale.multiplier().serverMultiplierClearedNotice());

            plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(message));
        }

        // If no Player, return
        if(player == null) return;

        // Send feedback to Player
        Component message = AdventureUtil.deserialize(locale.prefix() + locale.multiplier().serverMultiplierCleared());
        player.sendMessage(message);
    }

    /**
     * Send the relevant notices to island members and feedback to the command player.
     * @param player The {@link Player}.
     * @param island The {@link Island} whose multiplier was updated.
     * @param multiplier The multiplier.
     * @param time The time.
     * @param notice If island members should be informed of the change.
     */
    private void sendIslandMultiplierChangedNotice(
            @Nullable Player player,
            @NotNull Island island,
            double multiplier,
            long time,
            boolean notice) {
        Locale locale = localeManager.getConfiguration();
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("multiplier", String.valueOf(multiplier)));
        if(time > -1) placeholders.add(Placeholder.component("time", getTimePlaceholder(locale.multiplier().multiplierTimePlaceholder(), time)));

        // Send notice to island members
        if(notice) {
            Component message = time != -1 ?
                    AdventureUtil.deserialize(locale.prefix() + locale.multiplier().islandMultiplierChangedTimeLimit(), placeholders) :
                    AdventureUtil.deserialize(locale.prefix() + locale.multiplier().islandMultiplierChangedNoTimeLimit(), placeholders);

            island.getMemberSet().stream()
                    .map(memberId -> plugin.getServer().getPlayer(memberId))
                    .filter(Objects::nonNull)
                    .forEach(onlinePlayer -> onlinePlayer.sendMessage(message));
        }

        // If no Player, return
        if(player == null) return;

        // Send feedback to Player
        Component message = time != -1 ?
                AdventureUtil.deserialize(locale.prefix() + locale.multiplier().islandMultiplierTimeLimit(), placeholders) :
                AdventureUtil.deserialize(locale.prefix() + locale.multiplier().islandMultiplierNoTimeLimit(), placeholders);
        player.sendMessage(message);
    }

    /**
     * Send the relevant notices to island members and feedback to the command player.
     * @param player The {@link Player}.
     * @param island The {@link Island} whose multiplier was updated.
     * @param notice If island members should be informed of the change.
     */
    private void sendIslandMultiplierClearedNotice(
            @Nullable Player player,
            @NotNull Island island,
            boolean notice) {
        Locale locale = localeManager.getConfiguration();

        // Send notice to island members
        if(notice) {
            Component message = AdventureUtil.deserialize(locale.prefix() + locale.multiplier().islandMultiplierClearedNotice());

            island.getMemberSet().stream()
                    .map(memberId -> plugin.getServer().getPlayer(memberId))
                    .filter(Objects::nonNull)
                    .forEach(onlinePlayer -> onlinePlayer.sendMessage(message));
        }

        // If no Player, return
        if(player == null) return;

        // Send feedback to Player
        Component message = AdventureUtil.deserialize(locale.prefix() + locale.multiplier().islandMultiplierCleared());
        player.sendMessage(message);
    }
}