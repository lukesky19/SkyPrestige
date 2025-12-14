package com.github.lukesky19.skyPrestige.configuration.data.island;

import com.github.lukesky19.skyPrestige.configuration.interfaces.IslandSettingsInterface;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * Settings related to keeping or resetting island data for non-prestige sources.
 * @param keepIslandSize Whether to keep island size or not.
 * @param keepGeneratorUpgrades Whether to keep generator upgrades or not.
 * @param resetPrestigePoints Whether to reset prestige points or not.
 * @param resetPrestigeLevel Whether to reset the prestige level of the island or not.
 * @param clearVault Whether to clear the island vault or not.
 */
@ConfigSerializable
public record NonPrestigeIslandSettings(
        boolean keepIslandSize,
        boolean keepGeneratorUpgrades,
        boolean resetPrestigePoints,
        boolean resetPrestigeLevel,
        boolean clearVault) implements IslandSettingsInterface {
    @Override
    public boolean keepIslandFlags() {
        return true;
    }

    @Override
    public boolean removeRequiredPrestigePoints() {
        return false;
    }
}
