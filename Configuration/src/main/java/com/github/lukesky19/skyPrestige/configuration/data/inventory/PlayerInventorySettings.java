package com.github.lukesky19.skyPrestige.configuration.data.inventory;

import com.github.lukesky19.skyPrestige.configuration.data.interfaces.InventorySettings;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

/**
 * Settings related to resetting a player's inventory.
 * @param resetInventory Whether to reset an island member's inventory on island prestige.
 * @param keepInfiniteSellWands Whether to keep infinite sell wands from SkySellWands or not.
 */
@ConfigSerializable
public record PlayerInventorySettings(
        boolean resetInventory,
        boolean keepInfiniteSellWands) implements InventorySettings {
    @Override
    public boolean clearInventory() {
        return resetInventory;
    }
}
