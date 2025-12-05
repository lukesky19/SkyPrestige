package com.github.lukesky19.skyPrestige.configuration.data.island;

import com.github.lukesky19.skyPrestige.configuration.interfaces.island.IslandSettingsInterface;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * Settings related to keeping or resetting island data.
 * @param keepIslandSize Whether to keep island size on prestige or not.
 * @param keepGeneratorUpgrades Whether to keep generator upgrades on prestige or not.
 * @param resetPrestigePoints Whether to reset prestige points on prestige or not.
 * @param removeRequiredPrestigePoints Whether to remove the required prestige points or not from the island's total.
 */
@ConfigSerializable
public record PrestigeIslandSettings(
        boolean keepIslandSize,
        boolean keepGeneratorUpgrades,
        boolean resetPrestigePoints,
        boolean removeRequiredPrestigePoints) implements IslandSettingsInterface {
    @Override
    public boolean keepIslandSize() {
        return keepIslandSize;
    }
    @Override
    public boolean keepGeneratorUpgrades() {
        return keepGeneratorUpgrades;
    }
    @Override
    public boolean keepIslandFlags() {
        return true;
    }
    @Override
    public boolean resetPrestigePoints() {
        return resetPrestigePoints;
    }
    @Override
    public boolean removeRequiredPrestigePoints() {
        return removeRequiredPrestigePoints;
    }
    @Override
    public boolean resetPrestigeLevel() {
        return false;
    }
    @Override
    public boolean clearVault() {
        return false;
    }
}
