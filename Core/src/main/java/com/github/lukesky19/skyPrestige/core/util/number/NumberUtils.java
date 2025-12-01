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
package com.github.lukesky19.skyPrestige.core.util.number;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * This contains utility methods used throughout the plugin.
 */
public class NumberUtils {
    /**
     * Default Constructor. All methods in this class are static.
     * @deprecated All methods in this class are static.
     * @throws RuntimeException if this method is used.
     */
    @Deprecated
    public NumberUtils() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Format the provided value to 4 decimal places.
     * @param value The value to format.
     * @return A {@link String} for the decimal formatted to 4 decimal places.
     */
    public static @NotNull String formatDecimal(double value) {
        value = round(value);

        DecimalFormat decimalFormat = new DecimalFormat("#.####");
        return decimalFormat.format(value);
    }

    /**
     * Round the provided value to 4 decimal places.
     * @param value The value to round.
     * @return The rounded value.
     */
    private static double round(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(4, RoundingMode.FLOOR);
        return bd.doubleValue();
    }
}
