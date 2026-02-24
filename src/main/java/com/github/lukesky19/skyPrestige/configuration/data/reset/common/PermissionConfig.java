package com.github.lukesky19.skyPrestige.configuration.data.reset.common;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jspecify.annotations.Nullable;

/**
 * This record contains the permission settings.
 * @param permission The permission.
 * @param removePermission Whether the permission should be removed or not.
 * @param negatePermission Whether the permission should be negated or not.
 */
@ConfigSerializable
public record PermissionConfig(
        @Nullable String permission,
        boolean removePermission,
        boolean negatePermission) {}