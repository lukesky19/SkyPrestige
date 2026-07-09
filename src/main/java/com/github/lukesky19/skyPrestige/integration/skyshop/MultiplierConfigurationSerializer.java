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
package com.github.lukesky19.skyPrestige.integration.skyshop;

import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skyshop.api.serializer.ConfigurationSerializer;
import org.jspecify.annotations.NonNull;

/**
 * Serializes/deserializes {@link ShopMultiplierConfiguration}.
 */
public class MultiplierConfigurationSerializer extends ConfigurationSerializer<ShopMultiplierConfiguration> {
    /**
     * Constructor
     */
    public MultiplierConfigurationSerializer() {
        super(ShopMultiplierConfiguration.class);
    }

    /**
     * Currently nothing to migrate.
     * @param root The root {@link ConfigurationNode}.
     */
    @Override
    public void migrate(@NonNull ConfigurationNode root) {}
}