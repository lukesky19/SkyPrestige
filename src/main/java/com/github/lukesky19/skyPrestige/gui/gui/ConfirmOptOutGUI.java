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
package com.github.lukesky19.skyPrestige.gui.gui;

import com.github.lukesky19.skyPrestige.configuration.data.gui.ConfirmGUIConfig;
import com.github.lukesky19.skyPrestige.configuration.data.gui.common.ButtonConfig;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.opt_in_out.OptInOutConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reset.PrestigeResetSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.island.IslandSettings;
import com.github.lukesky19.skyPrestige.configuration.data.reset.playtime.PlayTimeSettings;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.OptOutConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.gui.abstracts.ConfirmGUI;
import com.github.lukesky19.skyPrestige.prestige.PrestigeExemptionManager;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.gui.GUIButton;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.gui.interfaces.IGUIManager;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.blueprints.dataobjects.BlueprintBundle;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to confirm opting out of prestige.
 */
public class ConfirmOptOutGUI extends ConfirmGUI {
    private final @NonNull SkyPlugin plugin;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull GUIConfigManager guiConfigManager;
    private final @NonNull PrestigeExemptionManager prestigeExemptionManager;

    private final @NonNull RewardsProcessor rewardsProcessor;

    private final @NonNull Island oldIsland;
    private final @NonNull IslandData oldIslandData;
    private final @NonNull GameModeAddon gameModeAddon;
    private final @Nullable BlueprintBundle blueprint;

    private final @Nullable ConfirmGUIConfig confirmOptOutGUIConfig;
    private final @Nullable OptInOutConfig optOutConfig;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager A {@link IGUIManager} instance.
     * @param identifier The {@link IslandIdUUIDKey} this GUI is tied to.
     * @param optOutConfigManager A {@link OptOutConfigManager} instance.
     * @param rewardsProcessor A {@link RewardsProcessor} instance.
     * @param prestigeExemptionManager A {@link PrestigeExemptionManager} instance.
     * @param player The {@link Player}.
     * @param oldIsland The old {@link Island}.
     * @param oldIslandData The old {@link IslandData}.
     * @param gameModeAddon The {@link GameModeAddon}.
     * @param blueprint The blueprint to use.
     */
    public ConfirmOptOutGUI(
            @NonNull SkyPlugin plugin,
            @NonNull LocaleManager localeManager,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull IGUIManager<IslandIdUUIDKey> guiManager,
            @NonNull IslandIdUUIDKey identifier,
            @NonNull OptOutConfigManager optOutConfigManager,
            @NonNull RewardsProcessor rewardsProcessor,
            @NonNull PrestigeExemptionManager prestigeExemptionManager,
            @NonNull Player player,
            @NonNull Island oldIsland,
            @NonNull IslandData oldIslandData,
            @NonNull GameModeAddon gameModeAddon,
            @Nullable BlueprintBundle blueprint) {
        super(plugin, guiManager, identifier, player);

        this.plugin = plugin;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;

        this.rewardsProcessor = rewardsProcessor;
        this.prestigeExemptionManager = prestigeExemptionManager;

        // Island Reset Data
        this.oldIsland = oldIsland;
        this.oldIslandData = oldIslandData;
        this.gameModeAddon = gameModeAddon;
        this.blueprint = blueprint;

        this.confirmOptOutGUIConfig = guiConfigManager.getConfirmOptOutGUIConfig();
        this.optOutConfig = optOutConfigManager.getConfiguration();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    @Override
    public boolean create() {
        if(confirmOptOutGUIConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the confirm opt-out GUI due to invalid gui configuration."));
            return false;
        }

        if(optOutConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the confirm opt-out GUI due to invalid opt out config."));
            return false;
        }

        GUIType guiType = confirmOptOutGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the confirm opt-out GUI due to an invalid GUIType."));
            return false;
        }

        switch(guiType) {
            case CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54 -> {}

            default -> {
                logger.error(AdventureUtility.plain("Unsupported GUI Type in confirm opt-out GUI config. Allowed Types: CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54"));
                return false;
            }
        }

        String guiName = Objects.requireNonNullElse(confirmOptOutGUIConfig.guiName(), "");

        return create(guiType, guiName, List.of());
    }

    /**
     * Create all the buttons and decorate the GUI.
     * @return true if updated successfully, otherwise false.
     */
    @Override
    public boolean update() {
        if(confirmOptOutGUIConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the confirm opt-out GUI due to invalid gui configuration."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        // Static Buttons
        createFillerButtons(guiSize);

        createDummyButtons();

        createConfirmButton();
        createCancelButton();

        createSelectedBlueprintButton();
        createRewardsButton();

        List<TagResolver.Single> emptyList = List.of();
        createDisplayButton(confirmOptOutGUIConfig.keepMembers(), emptyList);
        createDisplayButton(confirmOptOutGUIConfig.keepCommandRanks(), emptyList);

        // Conditional Buttons
        createConditionalButtons();

        return super.update();
    }

    @Override
    public void cancel() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

            guiManager.removeOpenGUI(identifier);

            rewardsProcessor.revertEarlyRewards(oldIsland.getMemberSet());

            prestigeExemptionManager.removeExempting(oldIsland.getUniqueId());
        }, 1L);
    }

    @Override
    public void handleClose(@NonNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED)
                || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(identifier);

        // Remove early rewards given
        rewardsProcessor.revertEarlyRewards(oldIsland.getMemberSet());

        prestigeExemptionManager.removeExempting(oldIsland.getUniqueId());
    }

    /**
     * Handles when items are dragged across the player's inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleBottomDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when items are dragged across the entire inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when the player's inventory is clicked. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NonNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Handles when a click occurs in either inventory. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleGlobalClick(@NonNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create the filler buttons for the GUI.
     * @param guiSize The size of the GUI.
     */
    private void createFillerButtons(int guiSize) {
        if(confirmOptOutGUIConfig == null) return;

        ItemStackConfig fillerConfig = confirmOptOutGUIConfig.filler();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(fillerConfig, player, List.of());

        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(itemStack);

            for(int i = 0; i <= guiSize - 1; i++) {
                setButton(i, builder.build());
            }
        });
    }

    /**
     * Create the confirm button for the GUI.
     */
    private void createConfirmButton() {
        assert confirmOptOutGUIConfig != null;
        ButtonConfig confirmConfig = confirmOptOutGUIConfig.confirmButton();

        if(confirmConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add the confirm button to the confirm opt-out GUI due to an invalid slot."));
            return;
        }

        createActionButton(confirmConfig, _ -> {
            prestigeExemptionManager.toggleIslandPrestigeStatus(player, oldIsland, oldIslandData, gameModeAddon, blueprint != null ? blueprint.getUniqueId() : null);

            close();
        });
    }

    /**
     * Create the cancel button for the GUI.
     */
    private void createCancelButton() {
        if(confirmOptOutGUIConfig == null) return;
        ButtonConfig cancelConfig = confirmOptOutGUIConfig.cancelButton();

        if(cancelConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add the cancel button to the confirm opt-out GUI due to an invalid slot."));
            return;
        }

        createActionButton(cancelConfig, _ -> this.close());
    }

    /**
     * Created the selected blueprint button that will be used when the island is reset.
     */
    private void createSelectedBlueprintButton() {
        assert confirmOptOutGUIConfig != null;
        if(blueprint == null) return;
        GUIButton.Builder builder = new GUIButton.Builder();

        ItemStack itemStack = ItemStack.of(blueprint.getIcon());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(AdventureUtility.deserialize(blueprint.getDisplayName()));
        List<Component> lore = blueprint.getDescription().stream().map(AdventureUtility::deserialize).toList();
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);

        builder.setItemStack(itemStack);
        setButton(confirmOptOutGUIConfig.blueprintButtonSlot(), builder.build());
    }

    /**
     * Create the rewards button for the GUI.
     */
    private void createRewardsButton() {
        if(optOutConfig == null || confirmOptOutGUIConfig == null) return;
        ButtonConfig rewardsConfig = confirmOptOutGUIConfig.rewardsButton();

        if(rewardsConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add the rewards button to the confirm opt-out GUI due to an invalid slot."));
            return;
        }

        createActionButton(rewardsConfig, _ -> {
            Locale locale = localeManager.getConfiguration();

            close();

            OptOutRewardsGUI rewardsGUI = new OptOutRewardsGUI(plugin, guiManager, identifier, player, guiConfigManager, optOutConfig, this);

            boolean creationResult = rewardsGUI.create();
            if(!creationResult) {
                logger.error(AdventureUtility.plain("Unable to create the InventoryView for the opt-out rewards GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                return;
            }

            boolean updateResult = rewardsGUI.update();
            if(!updateResult) {
                logger.error(AdventureUtility.plain("Unable to decorate the opt-out rewards GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
                return;
            }

            boolean openResult = rewardsGUI.open();
            if(!openResult) {
                logger.error(AdventureUtility.plain("Unable to open the opt-out rewards GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.guiOpenError()));
            }
        });
    }

    /**
     * Create the conditional buttons for the GUI depending on opt out settings.
     */
    private void createConditionalButtons() {
        if(confirmOptOutGUIConfig == null || optOutConfig == null) return;
        PrestigeResetSettings resetSettings = optOutConfig.resetSettings();
        IslandSettings islandSettings = resetSettings.islandSettings();
        PlayTimeSettings playTimeSettings = resetSettings.playerSettings().playTimeSettings();
        List<TagResolver.Single> emptyList = List.of();

        if(islandSettings.keepIsland()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepIsland(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetIsland(), emptyList);
        }

        if(islandSettings.keepIslandSize()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepIslandSize(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetIslandSize(), emptyList);
        }

        if(islandSettings.keepIslandFlags()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepFlags(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetFlags(), emptyList);
        }

        if(islandSettings.clearVault()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().clearVaultItems(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepVaultItems(), emptyList);
        }

        if(resetSettings.islandSettings().keepGeneratorUpgrades()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepGeneratorUpgrades(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetGeneratorUpgrades(), emptyList);
        }

        if(!resetSettings.playerSettings().inventorySettings().resetInventory()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepInventory(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().clearInventory(), emptyList);
        }

        if(!resetSettings.playerSettings().enderChestSettings().resetInventory()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepEnderChest(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().clearEnderChest(), emptyList);
        }

        if(!resetSettings.playerSettings().resetExp()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepExp(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetExp(), emptyList);
        }

        if(!resetSettings.playerSettings().resetMoney()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepMoney(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetMoney(), emptyList);
        }

        if(resetSettings.startingMoney() > 0) {
            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("amount", String.valueOf(resetSettings.startingMoney())));

            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().startingMoney(), placeholders);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().noStartingMoney(), emptyList);
        }

        if(!resetSettings.playerSettings().resetAuctionItems()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepAuctionItems(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetAuctionItems(), emptyList);
        }

        if(!resetSettings.playerSettings().resetQuestProgress()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepQuestProgress(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetQuestProgress(), emptyList);
        }

        if(!playTimeSettings.resetSession()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepSessionPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetSessionPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetDaily()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepDailyPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetDailyPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetWeekly()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepWeeklyPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetWeeklyPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetMonthly()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepMonthlyPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetMonthlyPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetYearly()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepYearlyPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetYearlyPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetTotal()) {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().keepTotalPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmOptOutGUIConfig.conditionalButtons().resetTotalPlayTime(), emptyList);
        }
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(confirmOptOutGUIConfig == null) return;

        confirmOptOutGUIConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtility.plain("Unable to add a dummy button to the confirm opt-out GUI due to an invalid slot."));
                return;
            }

            createDisplayButton(buttonConfig, List.of());
        });
    }

    /**
     * Create a button with a custom action to take when clicked.
     * @param buttonConfig The {@link ButtonConfig}.
     * @param action A {@link Consumer} that takes an {@link InventoryClickEvent} to execute when the button is clicked.
     */
    private void createActionButton(@NonNull ButtonConfig buttonConfig, @NonNull Consumer<InventoryClickEvent> action) {
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add an action button to the confirm opt-out GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());
        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(itemStack);

            builder.setAction(action);

            setButton(buttonConfig.slot(), builder.build());
        });
    }

    /**
     * Create a button that has no action associated with it.
     * @param buttonConfig The {@link ButtonConfig}.
     * @param placeholders A {@link List} of {@link TagResolver.Single} of placeholders for the button's ItemStack.
     */
    private void createDisplayButton(@NonNull ButtonConfig buttonConfig, @NonNull List<TagResolver.Single> placeholders) {
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a display button to the confirm opt-out GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player,placeholders);
        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(itemStack);

            setButton(buttonConfig.slot(), builder.build());
        });
    }
}