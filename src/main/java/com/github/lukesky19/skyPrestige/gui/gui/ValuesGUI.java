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

import com.github.lukesky19.skyPrestige.configuration.data.gui.ValuesGUIConfig;
import com.github.lukesky19.skyPrestige.configuration.data.gui.common.ButtonConfig;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to view the prestige points earned for certain actions.
 */
public class ValuesGUI extends ChestGUI<IslandIdUUIDKey> {
    // Plugin Classes
    private final @NotNull GUIManager guiManager;
    // Config
    private final @Nullable ValuesGUIConfig valuesGUIConfig;
    // Page info
    private int pageNum = 0;
    private @Nullable ValuesGUIConfig.PageConfig pageConfig;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param identifier The {@link IslandIdUUIDKey} this GUI is tied to.
     * @param player The {@link Player} viewing the GUI.
     */
    public ValuesGUI(
            @NotNull SkyPlugin plugin,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull GUIManager guiManager,
            @NotNull IslandIdUUIDKey identifier,
            @NotNull Player player) {
        super(plugin, guiManager, identifier, player);

        this.guiManager = guiManager;

        valuesGUIConfig = guiConfigManager.getValuesGUIConfig();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(valuesGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the values GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = valuesGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the values GUI due to an invalid GUIType."));
            return false;
        }

        String guiName = Objects.requireNonNullElse(valuesGUIConfig.guiName(), "");

        return create(guiType, guiName, List.of());
    }

    @Override
    public boolean open() {
        if(inventoryView == null) {
            // If the InventoryView was not created, log a warning and return false.
            logger.warn(AdventureUtil.deserialize("Unable to open the InventoryView as it was not created."));
            return false;
        }

        // Close the current Inventory the player has open (if any)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

            guiManager.removeOpenGUI(identifier);
        }, 1L);

        // Then 1 tick later, open the GUI and track that it is open for the player.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            inventoryView.open();

            guiManager.addOpenGUI(identifier, this);
        }, 2L);

        return true;
    }

    /**
     * Create all the buttons and decorate the GUI.
     * @return true if updated successfully, otherwise false.
     */
    @Override
    public boolean update() {
        if(valuesGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the GUI as the gui configuration is invalid."));
            return false;
        }

        pageConfig = valuesGUIConfig.pages().get(pageNum);
        if(pageConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the GUI as the page configuration is invalid."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        clearButtons();

        createFillerButtons(guiSize);

        createDummyButtons();

        createExitButton();

        createNextPageButton();

        createPrevPageButton();

        return super.update();
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
        if(valuesGUIConfig == null) return;
        if(pageConfig == null) return;

        ItemStackConfig fillerConfig = pageConfig.filler();
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
     * Create the next page button for the GUI.
     */
    private void createNextPageButton() {
        if(valuesGUIConfig == null) return;
        if(pageConfig == null) return;
        ButtonConfig nextPageButtonConfig = pageConfig.nextPage();

        if(nextPageButtonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the next page button to the values GUI due to an invalid slot."));
            return;
        }

        int nextPageNum = pageNum + 1;
        if(nextPageNum >= valuesGUIConfig.pages().size()) {
            logger.warn(AdventureUtil.deserialize("Unable to add the next page button to the values GUI due to no next page configured."));
            return;
        }

        @Nullable ValuesGUIConfig.PageConfig nextPageConfig = valuesGUIConfig.pages().get(nextPageNum);
        if(nextPageConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the next page button to the values GUI due to no next page configured."));
            return;
        }

        createActionButton(nextPageButtonConfig, inventoryClickEvent -> {
            pageNum++;

            this.update();
        });
    }

    /**
     * Create the previous page button for the GUI.
     */
    private void createPrevPageButton() {
        if(pageNum <= 0) return;
        if(valuesGUIConfig == null) return;
        if(pageConfig == null) return;
        ButtonConfig prevPageButtonConfig = pageConfig.prevPage();

        if(prevPageButtonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the previous page button to the values GUI due to an invalid slot."));
            return;
        }

        int previousPageNum = pageNum - 1;
        @Nullable ValuesGUIConfig.PageConfig previousPageConfig = valuesGUIConfig.pages().get(previousPageNum);
        if(previousPageConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the previous page button to the values GUI due to no previous page configured."));
            return;
        }

        createActionButton(prevPageButtonConfig, inventoryClickEvent -> {
            pageNum--;

            this.update();
        });
    }

    /**
     * Create the exit button for the GUI.
     */
    private void createExitButton() {
        if(valuesGUIConfig == null) return;
        if(pageConfig == null) return;
        ButtonConfig exitConfig = pageConfig.exit();

        if(exitConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the exit button to the values GUI due to an invalid slot."));
            return;
        }

        createActionButton(exitConfig, inventoryClickEvent -> close());
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(valuesGUIConfig == null) return;
        if(pageConfig == null) return;

        pageConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a dummy button to the values GUI due to an invalid slot."));
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
            logger.warn(AdventureUtil.deserialize("Unable to add an action button to the values GUI due to an invalid slot."));
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
            logger.warn(AdventureUtil.deserialize("Unable to add a display button to the values GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, placeholders);

        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> createDisplayButton(itemStack, buttonConfig.slot()));
    }

    /**
     * Create a button that has no action associated with it.
     * @param itemStack The {@link ItemStack} for the button.
     * @param slot The slot to place the button at.
     */
    private void createDisplayButton(@NotNull ItemStack itemStack, int slot) {
        GUIButton.Builder builder = new GUIButton.Builder();

        builder.setItemStack(itemStack);

        setButton(slot, builder.build());
    }
}