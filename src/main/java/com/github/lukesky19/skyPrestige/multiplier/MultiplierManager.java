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

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.multiplier.MultiplierConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.MultiplierConfigManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the multiplier that is applied to prestige points earned.
 */
public class MultiplierManager {
    private final @NotNull SkyPlugin plugin;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull MultiplierConfigManager multiplierConfigManager;

    private double eventMultiplier = 0.0;
    private double additionalMultiplier = 0.0;
    private long eventDuration = -1;
    private long timeUntilNextEvent = -1;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param multiplierConfigManager A {@link MultiplierConfigManager} instance.
     */
    public MultiplierManager(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull MultiplierConfigManager multiplierConfigManager) {
        this.plugin = plugin;
        this.multiplierConfigManager = multiplierConfigManager;
        this.localeManager = localeManager;
    }

    /**
     * Re-calculate the cooldown time in seconds
     */
    public void reload() {
        eventMultiplier = 0.0;
        additionalMultiplier = 0.0;
        eventDuration = -1;
        timeUntilNextEvent = -1;

        startEvent(false);
    }

    /**
     * Get the current multiplier.
     * @return The current multiplier.
     */
    public double getMultiplier() {
        return 1.0 + eventMultiplier + additionalMultiplier;
    }

    /**
     * Add the multiplier to the additional multiplier.
     * @param multiplier The multiplier to add.
     */
    public void addAdditionalMultiplier(double multiplier) {
        additionalMultiplier += multiplier;
    }

    /**
     * Remove the multiplier from the additional multiplier.
     * @param multiplier The multiplier to remove.
     */
    public void removeAdditionalMultiplier(double multiplier) {
        additionalMultiplier -= multiplier;
    }

    /**
     * Set the additional multiplier to the multiplier provided.
     * @param multiplier The seconds to set.
     */
    public void setAdditionalMultiplier(double multiplier) {
        additionalMultiplier = multiplier;
    }

    /**
     * Get the current additional multiplier.
     * @return The current additional multiplier.
     */
    public double getAdditionalMultiplier() {
        return additionalMultiplier;
    }

    /**
     * Get the current scheduled multiplier.
     * @return The current scheduled multiplier.
     */
    public double getEventMultiplier() {
        return eventMultiplier;
    }

    /**
     * Remove the seconds from the event duration.
     * @param seconds The seconds to remove.
     */
    public void removeEventDurationSeconds(int seconds) {
        eventDuration -= seconds;
    }

    /**
     * Get the event duration in seconds until the scheduled multiplier is reset.
     * @return The event duration in seconds.
     */
    public long getEventDuration() {
        return eventDuration;
    }

    /**
     * Remove the seconds from the time until next event.
     * @param seconds The seconds to remove.
     */
    public void removeTimeUntilNextEvent(int seconds) {
        timeUntilNextEvent -= seconds;
    }

    /**
     * Get the time in seconds until the event starts.
     * @return The time in seconds until the event starts.
     */
    public long getTimeUntilNextEvent() {
        return timeUntilNextEvent;
    }

    /**
     * Get the {@link Component} for the time placeholder.
     * @param timeMessage The {@link Locale.TimeFormat} to use.
     * @param timeInSeconds The time in seconds to format.
     * @return A {@link Component}.
     */
    public @NotNull Component getTimePlaceholder(@NotNull Locale.TimeFormat timeMessage, long timeInSeconds) {
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
            messageBuilder.append("0 ").append(timeMessage.seconds());
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
     * Start the scheduled multiplier event if at or after the event time.
     * @param broadcast Should the event start be broadcasted to the players?
     */
    public void startEvent(boolean broadcast) {
        @NotNull Locale locale = localeManager.getConfiguration();

        if(eventDuration > 0) {
            plugin.getComponentLogger().warn(AdventureUtil.deserialize("Unable to start event because one is already running."));
            return;
        }

        calculateEventDuration();
        calculateNextEventSeconds();

        if(eventDuration <= 0 || eventMultiplier <= 0) return;

        if(broadcast) {
            List<TagResolver.Single> placeholders = List.of(
                    Placeholder.parsed("event_multiplier", String.valueOf(eventMultiplier + 1)),
                    Placeholder.parsed("current_multiplier", String.valueOf(getMultiplier())));
            Component message = AdventureUtil.deserialize(locale.prefix() + locale.multiplierEventStarted(), placeholders);

            plugin.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(message));
        }
    }

    /**
     * End the scheduled multiplier event.
     * @param broadcast Should the event end be broadcasted to the players?
     */
    public void endEvent(boolean broadcast) {
        @NotNull Locale locale = localeManager.getConfiguration();

        if(eventDuration < 0) return;

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("event_multiplier", String.valueOf(eventMultiplier + 1)));

        eventDuration = -1;
        eventMultiplier = 0;

        placeholders.add(Placeholder.parsed("current_multiplier", String.valueOf(getMultiplier())));

        calculateNextEventSeconds();

        if(broadcast) {
            Component message = AdventureUtil.deserialize(locale.prefix() + locale.multiplierEventEnded(), placeholders);

            plugin.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(message));
        }
    }

    /**
     * Calculate the number of seconds left in the event.
     * Also updates the scheduled multiplier if the calculated event duration is > 0.
     */
    private void calculateEventDuration() {
        MultiplierConfig multiplierConfig = multiplierConfigManager.getConfiguration();
        if(multiplierConfig == null) {
            timeUntilNextEvent = -1;
            return;
        }

        if(!multiplierConfig.enabled() || multiplierConfig.timezone() == null || multiplierConfig.day() == null || multiplierConfig.durationSeconds() <= 0) {
            timeUntilNextEvent = -1;
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(multiplierConfig.timezone()));

        DayOfWeek eventDay = DayOfWeek.valueOf(multiplierConfig.day().toUpperCase());
        int eventHour = multiplierConfig.hour();

        // Create ZonedDateTime for the event hour on the current day
        ZonedDateTime eventTimeToday = now.with(eventDay).withHour(eventHour).withMinute(0).withSecond(0);

        if(now.isEqual(eventTimeToday) || now.isAfter(eventTimeToday)) {
            Duration durationSinceEvent = Duration.between(now, eventTimeToday);
            long secondsPassed = durationSinceEvent.getSeconds();
            long calculatedDuration = multiplierConfig.durationSeconds() - secondsPassed;

            if(calculatedDuration > 0) {
                eventDuration = calculatedDuration;
                eventMultiplier = multiplierConfig.multiplier() - 1;
            }
        }
    }

    /**
     * Calculate the number of seconds until the next event.
     */
    private void calculateNextEventSeconds() {
        MultiplierConfig multiplierConfig = multiplierConfigManager.getConfiguration();
        if(multiplierConfig == null) {
            timeUntilNextEvent = -1;
            return;
        }

        if(!multiplierConfig.enabled() || multiplierConfig.timezone() == null || multiplierConfig.day() == null || multiplierConfig.durationSeconds() <= 0) {
            timeUntilNextEvent = -1;
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(multiplierConfig.timezone()));

        DayOfWeek eventDay = DayOfWeek.valueOf(multiplierConfig.day().toUpperCase());
        int eventHour = multiplierConfig.hour();

        ZonedDateTime nextEvent = now.with(eventDay).withHour(eventHour).withMinute(0).withSecond(0);

        if(nextEvent.isBefore(now) || (nextEvent.isEqual(now))) {
            nextEvent = nextEvent.plusWeeks(1);
        }

        // Calculate the remaining seconds until the next event
        timeUntilNextEvent = Duration.between(now, nextEvent).getSeconds();
    }
}