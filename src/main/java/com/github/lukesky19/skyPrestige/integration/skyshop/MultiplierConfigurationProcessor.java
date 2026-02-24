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

import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.integration.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.multiplier.MultiplierManager;
import com.github.lukesky19.skyPrestige.util.enums.MultiplierType;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skyshop.api.configuration.TransactionConfiguration;
import com.github.lukesky19.skyshop.api.processor.TransactionProcessor;
import com.github.lukesky19.skyshop.api.result.TransactionResult;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import world.bentobox.bentobox.database.objects.Island;

/**
 * This class processes {@link ShopMultiplierConfiguration} for buying/selling prestige point multipliers.
 */
public class MultiplierConfigurationProcessor implements TransactionProcessor {
    private final @NonNull LocaleManager localeManager;
    private final @NonNull HookManager hookManager;
    private final @NonNull MultiplierManager multiplierManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param multiplierManager A {@link MultiplierManager}
     */
    public MultiplierConfigurationProcessor(
            @NonNull LocaleManager localeManager,
            @NonNull HookManager hookManager,
            @NonNull MultiplierManager multiplierManager) {
        this.localeManager = localeManager;
        this.hookManager = hookManager;
        this.multiplierManager = multiplierManager;
    }

    /**
     * Can the player buy the item with no errors?
     * @apiNote Prices are already checked.
     * @param player The {@link Player} buying the item(s).
     * @param configuration The configuration to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NonNull TransactionResult canBuy(@NonNull Player player, @NonNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof ShopMultiplierConfiguration shopMultiplierConfiguration)) return new TransactionResult("Wrong Type", true, true, false);

        MultiplierType multiplierType = shopMultiplierConfiguration.multiplierType();
        Double multiplier = shopMultiplierConfiguration.multiplier();
        boolean activeMultiplierPreventPurchase = shopMultiplierConfiguration.activeMultiplierPreventPurchase();
        boolean activeMultiplierHigherPreventPurchase = shopMultiplierConfiguration.activeMultiplierHigherPreventPurchase();
        boolean resetMultiplierTimeIfHigherMultiplier = shopMultiplierConfiguration.resetMultiplierTimeIfHigherMultiplier();
        Long time = shopMultiplierConfiguration.time();
        Long maxTime = shopMultiplierConfiguration.maxTime();

        if(multiplierType == null
                || ((multiplier == null || multiplier <= 0)
                && (time == null || time <= 0))) {
            return new TransactionResult("Not Configured", false, false, false);
        }

        Locale locale = localeManager.getConfiguration();
        Locale.MultiplierMessages multiplierMessages = locale.multiplierMessages();
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);

        // Check if BentoBox is hooked into
        if(!bentoBoxHook.isHooked()) {
            return new TransactionResult("BentoBox not hooked into", true, true, false);
        }

        long updatedTime;
        Component multiplierActiveMessage = AdventureUtil.deserialize(locale.prefix() + multiplierMessages.shopMultiplierMessages().multiplierActive());
        Component higherMultiplierActiveMessage = AdventureUtil.deserialize(locale.prefix() + multiplierMessages.shopMultiplierMessages().higherMultiplierActive());
        Component multiplierTimeMax = AdventureUtil.deserialize(locale.prefix() + multiplierMessages.shopMultiplierMessages().multiplierTimeMax());
        if(multiplierType.equals(MultiplierType.SERVER)) {
            if(activeMultiplierPreventPurchase) {
                if(multiplierManager.getServerMultiplier() > 0.0) {
                    player.sendMessage(multiplierActiveMessage);
                    return new TransactionResult("A server multiplier is already active", true, false, false);
                }
            }

            if(multiplier != null && activeMultiplierHigherPreventPurchase) {
                if(multiplierManager.getServerMultiplier() > multiplier) {
                    player.sendMessage(higherMultiplierActiveMessage);
                    return new TransactionResult("A higher server multiplier is already active", true, false, false);
                }
            }

            // Calculate base time
            long baseTime = multiplierManager.getServerMultiplierTime();
            if(multiplier != null
                    && multiplier > multiplierManager.getServerMultiplier()
                    && resetMultiplierTimeIfHigherMultiplier) {
                baseTime = 0;
            }

            // Calculate updated time
            if(time != null && time > 0) {
                updatedTime = baseTime + time;

                // Check max time
                if(maxTime != null && updatedTime > maxTime) {
                    player.sendMessage(multiplierTimeMax);
                    return new TransactionResult("The maximum multiplier time would be exceeded by this transaction", true, false, false);
                }
            }
        } else {
            // Get and validate the island
            Island island = bentoBoxHook.getIsland(player.getWorld(), player.getUniqueId());
            if(island == null) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + multiplierMessages.shopMultiplierMessages().notOnIsland()));
                return new TransactionResult("Player not on island", true, false, false);
            }

            if(activeMultiplierPreventPurchase) {
                if(multiplierManager.getIslandMultiplier(island) > 0.0) {
                    player.sendMessage(multiplierActiveMessage);
                    return new TransactionResult("An island multiplier is already active", true, false, false);
                }
            }

            if(multiplier != null && activeMultiplierHigherPreventPurchase) {
                if(multiplierManager.getIslandMultiplier(island) > multiplier) {
                    player.sendMessage(higherMultiplierActiveMessage);
                    return new TransactionResult("A higher island multiplier is already active", true, false, false);
                }
            }

            // Calculate base time
            long baseTime = multiplierManager.getIslandMultiplierTime(island);
            if(multiplier != null
                    && multiplier > multiplierManager.getIslandMultiplier(island)
                    && resetMultiplierTimeIfHigherMultiplier) {
                baseTime = 0;
            }

            // Calculate updated time
            if(time != null && time > 0) {
                updatedTime = baseTime + time;

                // Check max time
                if(maxTime != null && updatedTime > maxTime) {
                    player.sendMessage(multiplierTimeMax);
                    return new TransactionResult("The maximum multiplier time would be exceeded by this transaction", true, false, false);
                }
            }
        }

        return new TransactionResult("Success", false, false, false);
    }

    /**
     * Can the player sell the multiplier with no errors?
     * @param player The {@link Player} selling the multiplier.
     * @param configuration The configuration to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NonNull TransactionResult canSell(@NonNull Player player, @NonNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof ShopMultiplierConfiguration)) return new TransactionResult("Wrong Type", true, true, false);

        return new TransactionResult("Selling of prestige multiplier not supported", true, true, false);
    }

    /**
     * Process the configuration to buy prestige point multipliers.
     * @apiNote Prices are automatically taken from the player as necessary.<br>
     * If the buying fails, the rest of the transaction will still proceed.
     * @param player The {@link Player} to process data for.
     * @param configuration The configuration to process.
     * @param amount The amount being purchased.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NonNull TransactionResult buy(@NonNull Player player, @NonNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof ShopMultiplierConfiguration shopMultiplierConfiguration)) return new TransactionResult("Wrong Type", true, true, false);

        MultiplierType multiplierType = shopMultiplierConfiguration.multiplierType();
        Double multiplier = shopMultiplierConfiguration.multiplier();
        boolean activeMultiplierPreventPurchase = shopMultiplierConfiguration.activeMultiplierPreventPurchase();
        boolean activeMultiplierHigherPreventPurchase = shopMultiplierConfiguration.activeMultiplierHigherPreventPurchase();
        boolean resetMultiplierTimeIfHigherMultiplier = shopMultiplierConfiguration.resetMultiplierTimeIfHigherMultiplier();
        Long time = shopMultiplierConfiguration.time();
        Long maxTime = shopMultiplierConfiguration.maxTime();

        if(multiplierType == null
                || ((multiplier == null || multiplier <= 0)
                && (time == null || time <= 0))) {
            return new TransactionResult("Not Configured", false, false, false);
        }

        Locale locale = localeManager.getConfiguration();
        Locale.MultiplierMessages multiplierMessages = locale.multiplierMessages();
        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);

        // Check if BentoBox is hooked into
        if(!bentoBoxHook.isHooked()) {
            return new TransactionResult("BentoBox not hooked into", true, true, false);
        }

        Island island = null;
        Long updatedTime = null;
        String multiplierActiveMessage = locale.prefix() + multiplierMessages.shopMultiplierMessages().multiplierActive();
        String higherMultiplierActiveMessage = locale.prefix() + multiplierMessages.shopMultiplierMessages().higherMultiplierActive();
        String multiplierTimeMaxMessage = locale.prefix() + multiplierMessages.shopMultiplierMessages().multiplierTimeMax();
        if(multiplierType.equals(MultiplierType.SERVER)) {
            if(activeMultiplierPreventPurchase) {
                if(multiplierManager.getServerMultiplier() > 0.0) {
                    player.sendMessage(AdventureUtil.deserialize(multiplierActiveMessage));
                    return new TransactionResult("A server multiplier is already active", true, false, false);
                }
            }

            if(multiplier != null && activeMultiplierHigherPreventPurchase) {
                if(multiplierManager.getServerMultiplier() > multiplier) {
                    player.sendMessage(AdventureUtil.deserialize(higherMultiplierActiveMessage));
                    return new TransactionResult("A higher server multiplier is already active", true, false, false);
                }
            }

            // Calculate base time
            long baseTime = multiplierManager.getServerMultiplierTime();
            if(multiplier != null
                    && multiplier > multiplierManager.getServerMultiplier()
                    && resetMultiplierTimeIfHigherMultiplier) {
                baseTime = 0;
            }

            // Calculate updated time
            if(time != null && time > 0) {
                updatedTime = baseTime + time;

                // Check max time
                if(maxTime != null && updatedTime > maxTime) {
                    player.sendMessage(AdventureUtil.deserialize(multiplierTimeMaxMessage));
                    return new TransactionResult("The maximum multiplier time would be exceeded by this transaction", true, false, false);
                }
            }
        } else {
            // Get and validate the island
            island = bentoBoxHook.getIsland(player.getWorld(), player.getUniqueId());
            if(island == null) {
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + multiplierMessages.shopMultiplierMessages().notOnIsland()));
                return new TransactionResult("Player not on island", true, false, false);
            }

            if(activeMultiplierPreventPurchase) {
                if(multiplierManager.getIslandMultiplier(island) > 0.0) {
                    player.sendMessage(AdventureUtil.deserialize(multiplierActiveMessage));
                    return new TransactionResult("An island multiplier is already active", true, false, false);
                }
            }

            if(multiplier != null && activeMultiplierHigherPreventPurchase) {
                if(multiplierManager.getIslandMultiplier(island) > multiplier) {
                    player.sendMessage(AdventureUtil.deserialize(higherMultiplierActiveMessage));
                    return new TransactionResult("A higher island multiplier is already active", true, false, false);
                }
            }

            // Calculate base time
            long baseTime = multiplierManager.getIslandMultiplierTime(island);
            if(multiplier != null
                    && multiplier > multiplierManager.getIslandMultiplier(island)
                    && resetMultiplierTimeIfHigherMultiplier) {
                baseTime = 0;
            }

            // Calculate updated time
            if(time != null && time > 0) {
                updatedTime = baseTime + time;

                // Check max time
                if(maxTime != null && updatedTime > maxTime) {
                    player.sendMessage(AdventureUtil.deserialize(multiplierTimeMaxMessage));
                    return new TransactionResult("The maximum multiplier time would be exceeded by this transaction", true, false, false);
                }
            }
        }

        // Modify multiplier and multiplier time
        boolean result;
        if(multiplierType.equals(MultiplierType.SERVER)) {
            result = multiplierManager.setServerMultiplier(player, multiplier, updatedTime, true);
        } else {
            result = multiplierManager.setIslandMultiplier(player, island, multiplier, updatedTime, true);
        }

        return result
                ? new TransactionResult("The multiplier was updated successfully", false, false, false)
                : new TransactionResult("The multiplier failed to be updated", true, false, false);
    }

    /**
     * Process the configuration to sell the item(s).
     * @apiNote Prices are automatically added to the player as necessary.<br>
     * If the selling fails, the rest of the transaction will still proceed.
     * @param player The {@link Player} to process data for.
     * @param configuration The configuration to process.
     * @param amount The amount being sold.
     * @return A {@link TransactionResult}.
     */
    @Override
    public @NonNull TransactionResult sell(@NonNull Player player, @NonNull TransactionConfiguration configuration, int amount) {
        if(!(configuration instanceof ShopMultiplierConfiguration)) return new TransactionResult("Wrong Type", true, true, false);

        return new TransactionResult("Selling of prestige multiplier not supported", true, true, false);
    }
}
