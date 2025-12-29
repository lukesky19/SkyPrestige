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

import com.github.lukesky19.skyPrestige.configuration.data.gui.ConfirmPrestigeGUIConfig;
import com.github.lukesky19.skyPrestige.configuration.data.gui.common.ButtonConfig;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.playtime.PlayTimeSettings;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandResetData;
import com.github.lukesky19.skyPrestige.gui.abstracts.ConfirmGUI;
import com.github.lukesky19.skyPrestige.processor.reward.RewardsProcessor;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.interfaces.IGUIManager;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.blueprints.dataobjects.BlueprintBundle;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to confirm an island prestige.
 */
public class ConfirmPrestigeGUI extends ConfirmGUI {
    // Plugin Classes
    private final @NotNull SkyPlugin plugin;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull RewardsProcessor rewardsProcessor;

    private final @NotNull IslandResetData islandResetData;
    private final @NotNull Consumer<IslandResetData> consumer;

    private final @Nullable ConfirmPrestigeGUIConfig confirmPrestigeGUIConfig;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager A {@link IGUIManager} instance.
     * @param identifier The {@link IslandIdUUIDKey} this GUI is tied to.
     * @param rewardsProcessor A {@link RewardsProcessor} instance.
     * @param islandResetData The {@link IslandResetData}
     * @param consumer The consumer that will prestige the island once the player confirms it.
     */
    public ConfirmPrestigeGUI(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull IGUIManager<IslandIdUUIDKey> guiManager,
            @NotNull IslandIdUUIDKey identifier,
            @NotNull RewardsProcessor rewardsProcessor,
            @NotNull IslandResetData islandResetData,
            @NotNull Consumer<IslandResetData> consumer) {
        super(plugin, guiManager, identifier, islandResetData.getPlayer());

        this.plugin = plugin;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.rewardsProcessor = rewardsProcessor;

        this.islandResetData = islandResetData;
        this.consumer = consumer;

        confirmPrestigeGUIConfig = guiConfigManager.getConfirmPrestigeGUIConfig();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(!islandResetData.isPrestige()) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the confirm prestige GUI due to invalid prestige data."));
            return false;
        }

        if(confirmPrestigeGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the confirm prestige GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = confirmPrestigeGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the confirm prestige GUI due to an invalid GUIType."));
            return false;
        }

        switch(guiType) {
            case CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54 -> {}

            default -> {
                logger.error(AdventureUtil.deserialize("Unsupported GUI Type in confirm prestige GUI config. Allowed Types: CHEST_9, CHEST_18, CHEST_27, CHEST_36, CHEST_45, CHEST_54"));
                return false;
            }
        }

        String guiName = Objects.requireNonNullElse(confirmPrestigeGUIConfig.guiName(), "");

        return create(guiType, guiName, List.of());
    }

    /**
     * Create all the buttons and decorate the GUI.
     * @return true if updated successfully, otherwise false.
     */
    @Override
    public boolean update() {
        if(confirmPrestigeGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the confirm prestige GUI due to invalid gui configuration."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the GUI as the InventoryView was not created."));
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
        createDisplayButton(confirmPrestigeGUIConfig.keepMembers(), emptyList);
        createDisplayButton(confirmPrestigeGUIConfig.keepFlags(), emptyList);
        createDisplayButton(confirmPrestigeGUIConfig.keepCommandRanks(), emptyList);

        // Conditional Buttons
        createConditionalButtons();

        return super.update();
    }

    @Override
    public void cancel() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

            guiManager.removeOpenGUI(identifier);

            rewardsProcessor.revertEarlyRewards(islandResetData.getOldIsland().getMemberSet());
        }, 1L);
    }

    /**
     * Handles when items are dragged across the player's inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when items are dragged across the entire inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when the player's inventory is clicked. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Handles when a click occurs in either inventory. This method does nothing.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create the filler buttons for the GUI.
     * @param guiSize The size of the GUI.
     */
    private void createFillerButtons(int guiSize) {
        if(confirmPrestigeGUIConfig == null) return;

        ItemStackConfig fillerConfig = confirmPrestigeGUIConfig.filler();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(fillerConfig, player, null, List.of());

        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
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
        assert confirmPrestigeGUIConfig != null;
        ButtonConfig confirmConfig = confirmPrestigeGUIConfig.confirmButton();

        if(confirmConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the confirm button to the confirm prestige GUI due to an invalid slot."));
            return;
        }

        createActionButton(confirmConfig, inventoryClickEvent -> {
            consumer.accept(islandResetData);

            close();
        });
    }

    /**
     * Create the cancel button for the GUI.
     */
    private void createCancelButton() {
        if(confirmPrestigeGUIConfig == null) return;
        ButtonConfig cancelConfig = confirmPrestigeGUIConfig.cancelButton();

        if(cancelConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the cancel button to the confirm prestige GUI due to an invalid slot."));
            return;
        }

        createActionButton(cancelConfig, inventoryClickEvent -> this.cancel());
    }

    /**
     * Created the selected blueprint button that will be used when the island is prestiged.
     */
    private void createSelectedBlueprintButton() {
        assert confirmPrestigeGUIConfig != null;
        @Nullable BlueprintBundle blueprint = islandResetData.getBlueprint();
        if(blueprint == null) return;
        GUIButton.Builder builder = new GUIButton.Builder();

        ItemStack itemStack = ItemStack.of(blueprint.getIcon());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(AdventureUtil.deserialize(blueprint.getDisplayName()));
        List<Component> lore = blueprint.getDescription().stream().map(AdventureUtil::deserialize).toList();
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);

        builder.setItemStack(itemStack);
        setButton(confirmPrestigeGUIConfig.blueprintBundleSlot(), builder.build());
    }

    /**
     * Create the rewards button for the GUI.
     */
    private void createRewardsButton() {
        if(confirmPrestigeGUIConfig == null) return;
        ButtonConfig rewardsConfig = confirmPrestigeGUIConfig.rewardsButton();

        if(rewardsConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the rewards button to the confirm prestige GUI due to an invalid slot."));
            return;
        }

        createActionButton(rewardsConfig, inventoryClickEvent -> {
            Locale locale = localeManager.getConfiguration();

            close();

            if(islandResetData.getPrestigeConfig() == null) return;

            RewardsGUI rewardsGUI = new RewardsGUI(plugin, guiConfigManager, guiManager, identifier, player, islandResetData.getPrestigeConfig(), this);

            boolean creationResult = rewardsGUI.create();
            if(!creationResult) {
                logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the rewards GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                return;
            }

            boolean updateResult = rewardsGUI.update();
            if(!updateResult) {
                logger.error(AdventureUtil.deserialize("Unable to decorate the rewards GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                return;
            }

            boolean openResult = rewardsGUI.open();
            if(!openResult) {
                logger.error(AdventureUtil.deserialize("Unable to open the rewards GUI for player " + player.getName() + " due to a configuration error."));
                player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
            }
        });
    }

    /**
     * Create the conditional buttons for the GUI depending on prestige settings.
     */
    private void createConditionalButtons() {
        if(confirmPrestigeGUIConfig == null) return;
        if(islandResetData.getPrestigeConfig() == null) return;
        PrestigeConfig.PrestigeSettings prestigeSettings = islandResetData.getPrestigeConfig().prestigeSettings();
        List<TagResolver.Single> emptyList = List.of();

        if(!prestigeSettings.playerSettings().playerInventorySettings().clearInventory()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepInventory(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().clearInventory(), emptyList);
        }

        if(prestigeSettings.islandSettings().keepGeneratorUpgrades()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepGeneratorUpgrades(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetGeneratorUpgrades(), emptyList);
        }

        if(!prestigeSettings.playerSettings().enderChestSettings().resetEnderChest()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepEnderChest(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().clearEnderChest(), emptyList);
        }

        if(!prestigeSettings.playerSettings().resetExp()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepExp(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetExp(), emptyList);
        }

        if(!prestigeSettings.playerSettings().resetMoney()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepMoney(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetMoney(), emptyList);
        }

        if(!prestigeSettings.playerSettings().resetAuctionItems()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepAuctionItems(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetAuctionItems(), emptyList);
        }

        if(prestigeSettings.startingMoney() > 0) {
            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("amount", String.valueOf(prestigeSettings.startingMoney())));

            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().startingMoney(), placeholders);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().noStartingMoney(), emptyList);
        }

        PlayTimeSettings playTimeSettings = prestigeSettings.playerSettings().playTimeSettings();

        if(!playTimeSettings.resetSession()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepSessionPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetSessionPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetDaily()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepDailyPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetDailyPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetWeekly()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepWeeklyPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetWeeklyPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetMonthly()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepMonthlyPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetMonthlyPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetYearly()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepYearlyPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetYearlyPlayTime(), emptyList);
        }

        if(!playTimeSettings.resetTotal()) {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().keepTotalPlayTime(), emptyList);
        } else {
            createDisplayButton(confirmPrestigeGUIConfig.conditionalButtons().resetTotalPlayTime(), emptyList);
        }
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(confirmPrestigeGUIConfig == null) return;

        confirmPrestigeGUIConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a dummy button to the confirm prestige GUI due to an invalid slot."));
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
    private void createActionButton(@NotNull ButtonConfig buttonConfig, @NotNull Consumer<InventoryClickEvent> action) {
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add an action button to the confirm prestige GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, List.of());
        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
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
    private void createDisplayButton(@NotNull ButtonConfig buttonConfig, @NotNull List<TagResolver.Single> placeholders) {
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a display button to the confirm prestige GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, placeholders);
        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(itemStack);

            setButton(buttonConfig.slot(), builder.build());
        });
    }
}