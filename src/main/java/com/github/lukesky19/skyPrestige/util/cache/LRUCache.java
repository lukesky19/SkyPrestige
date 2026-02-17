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
package com.github.lukesky19.skyPrestige.util.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class stores the last recently used key value pair.
 * @param <K> The key.
 * @param <V> The value.
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    /**
     * The capacity of the cache.
     */
    private final int capacity;

    /**
     * Constructor
     * @param capacity The capacity.
     */
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * Should the oldest entry be removed?
     * @param eldest The eldest entry
     * @return true if successful, false if not.
     */
    @Override
    protected boolean removeEldestEntry(@NotNull Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    /**
     * Cache the key value pair.
     * @param key The key.
     * @param value The value.
     * @return the inserted value.
     */
    public @Nullable V put(@NotNull K key, @NotNull V value) {
        return super.put(key, value);
    }

    /**
     * Get the value for the key.
     * @param key The key.
     * @return The value or null.
     */
    public @Nullable V get(@NotNull Object key) {
        return super.get(key);
    }
}