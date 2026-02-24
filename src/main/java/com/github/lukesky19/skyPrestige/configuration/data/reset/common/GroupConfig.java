package com.github.lukesky19.skyPrestige.configuration.data.reset.common;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.Nullable;

/**
 * This record contains the group settings.
 * @param groupName The permission.
 * @param removeGroup Whether the group should be removed or not.
 */
@ConfigSerializable
public record GroupConfig(
        @Nullable String groupName,
        boolean removeGroup) {}
