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
package com.github.lukesky19.skyPrestige.util.parameter;

import com.github.lukesky19.skylib.api.database.parameter.Parameter;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

/**
 * Takes a {@link Timestamp} and stores it as-is for use in a database.
 */
public class TimestampParameter implements Parameter<Timestamp> {
    private final @NotNull Timestamp value;

    /**
     * Stores a {@link Timestamp} to later use to replace a parameter with.
     * @param value The {@link Timestamp} to store.
     */
    public TimestampParameter(@NotNull Timestamp value) {
        this.value = value;
    }

    /**
     * Returns the {@link Timestamp} to use replace the parameter with.
     * @return A {@link Timestamp} to replace a parameter with.
     */
    @Override
    public @NotNull Timestamp getValue() {
        return value;
    }
}