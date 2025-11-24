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
package com.github.lukesky19.skyPrestige.points.context;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * This interface is used to define the function to extract data from an {@link E}
 * @param <E> An {@link Event}.
 */
@FunctionalInterface
public interface EventContextExtractor<E extends Event> {
    /**
     * Get the {@link EventContext} from the event.
     * @param event The event to extract data from.
     * @return An {@link EventContext}. May be null.
     */
    @Nullable EventContext extract(E event);
}
