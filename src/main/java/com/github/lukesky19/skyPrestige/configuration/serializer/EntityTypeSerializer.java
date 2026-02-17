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
package com.github.lukesky19.skyPrestige.configuration.serializer;

import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.serialize.TypeSerializer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * Serializes and deserializes {@link ItemType}.
 */
public class EntityTypeSerializer implements TypeSerializer<EntityType> {
    private final @NotNull ComponentLogger logger;

    /**
     * Constructor
     * @param logger A {@link ComponentLogger}.
     */
    public EntityTypeSerializer(@NotNull ComponentLogger logger) {
        this.logger = logger;
    }

    /**
     * Deserializes the {@link String} representing the {@link NamespacedKey} to an {@link EntityType}.
     * @param type The {@link Type}.
     * @param node The {@link ConfigurationNode} to deserialize.
     * @return The {@link EntityType} or null.
     * @throws SerializationException If serialization fails.
     */
    @Override
    public @Nullable EntityType deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        @Nullable String key = node.getString();
        if(key == null) return null;

        return RegistryUtil.getEntityType(logger, key).orElse(null);
    }

    /**
     * Serializes the {@link EntityType} to it's {@link NamespacedKey} as a {@link String}.
     * @param type The {@link Type}.
     * @param entityType The {@link EntityType}.
     * @param node The {@link ConfigurationNode} to write to.
     * @throws SerializationException If serialization fails.
     */
    @Override
    public void serialize(@NotNull Type type, @Nullable EntityType entityType, @NotNull ConfigurationNode node) throws SerializationException {
        if(entityType == null) {
            node.raw(null);
            return;
        }

        node.set(String.class, entityType.getKey().toString());
    }
}