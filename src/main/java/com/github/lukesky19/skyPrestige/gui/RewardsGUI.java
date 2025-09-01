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
package com.github.lukesky19.skyPrestige.gui;

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.PrestigeConfig;
import com.github.lukesky19.skyPrestige.config.gui.RewardsGUIConfig;
import com.github.lukesky19.skyPrestige.config.gui.button.ButtonConfig;
import com.github.lukesky19.skyPrestige.manager.config.GUIConfigManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.AbstractGUIManager;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to view rewards for the next prestige level.
 */
public class RewardsGUI extends ChestGUI {
    // Plugin Classes
    private final @NotNull SkyPrestige skyPrestige;
    // The ConfirmPrestigeGUI to open after this one is closed. This GUI can also be opened via a command so they don't always have a ConfirmPrestigeGUI to go back to.
    private final @Nullable ConfirmPrestigeGUI confirmPrestigeGUI;
    // Config
    private final @NotNull PrestigeConfig prestigeConfig;
    private final @Nullable RewardsGUIConfig rewardsGUIConfig;
    // Page info
    private int rewardsPerPage;
    private int pageNum = 0;
    private int currentRewardKey = 0;
    private int numOfRewardsAdded = 0;
    private int numOfRewardsErrored = 0;
    private final @NotNull Map<Integer, Integer> rewardsAddedPerPage = new HashMap<>();
    private final @NotNull Map<Integer, Integer> errorCountsPerPage = new HashMap<>();

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager An {@link AbstractGUIManager} instance.
     * @param player The {@link Player} viewing the GUI.
     * @param confirmPrestigeGUI The {@link ConfirmPrestigeGUI} the player came from, if any.
     * @param prestigeConfig The {@link PrestigeConfig} for the next prestige level.
     */
    public RewardsGUI(
            @NotNull SkyPrestige skyPrestige,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull AbstractGUIManager guiManager,
            @NotNull Player player,
            @Nullable ConfirmPrestigeGUI confirmPrestigeGUI,
            @NotNull PrestigeConfig prestigeConfig) {
        super(skyPrestige, guiManager, player);

        this.skyPrestige = skyPrestige;
        this.confirmPrestigeGUI = confirmPrestigeGUI;
        this.prestigeConfig = prestigeConfig;

        rewardsGUIConfig = guiConfigManager.getRewardsGUIConfig();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(rewardsGUIConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the rewards GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = rewardsGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.serialize("Unable to create the InventoryView for the rewards GUI due to an invalid GUIType."));
            return false;
        }

        switch(guiType) {
            case CHEST_27 -> rewardsPerPage = 7;

            case CHEST_36 -> rewardsPerPage = 14;

            case CHEST_45 -> rewardsPerPage = 21;

            case CHEST_54 -> rewardsPerPage = 28;

            default -> {
                logger.error(AdventureUtil.serialize("Unsupported GUI Type in rewards GUI config. Allowed Types: CHEST_27, CHEST_36, CHEST_45, CHEST_54"));
                return false;
            }
        }

        String guiName = Objects.requireNonNullElse(rewardsGUIConfig.guiName(), "");

        return create(guiType, guiName, List.of());
    }

    /**
     * Create all the buttons and decorate the GUI.
     * @return true if updated successfully, otherwise false.
     */
    @Override
    public boolean update() {
        if(rewardsGUIConfig == null) {
            logger.warn(AdventureUtil.serialize("Unable to add buttons to the GUI as the gui configuration is invalid."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.serialize("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        clearButtons();

        createFillerButtons(guiSize);

        createDummyButtons();

        createExitButton();

        createRewardsButtons();

        rewardsAddedPerPage.put(pageNum, numOfRewardsAdded);
        errorCountsPerPage.put(pageNum, numOfRewardsErrored);

        if(numOfRewardsAdded >= rewardsPerPage && currentRewardKey <= (prestigeConfig.rewards().size() - 1)) {
            createNextPageButton();
        }

        if(pageNum > 0) {
            createPrevPageButton();
        }

        return super.update();
    }

    /**
     * Refresh all the buttons in the GUI.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean refresh() {
        int errorCountCurrentPage = errorCountsPerPage.get(pageNum);
        int reqAddedCurrentPage = rewardsAddedPerPage.get(pageNum);

        currentRewardKey = currentRewardKey - (errorCountCurrentPage + reqAddedCurrentPage);
        if(currentRewardKey < 0) currentRewardKey = 0;

        numOfRewardsAdded = 0;
        numOfRewardsErrored = 0;

        return this.update();
    }

    /**
     * Closes the GUI and opens the confirm prestige GUI if the player came form that GUI.
     */
    @Override
    public void close() {
        if(confirmPrestigeGUI != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

                guiManager.removeOpenGUI(uuid);
            }, 1L);

            confirmPrestigeGUI.open();
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

                guiManager.removeOpenGUI(uuid);
            }, 1L);
        }
    }

    /**
     * Handles when the GUI is closed by the player.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        Player player = (Player) inventoryCloseEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        guiManager.removeOpenGUI(uuid);

        if(confirmPrestigeGUI != null) confirmPrestigeGUI.open();
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
        assert rewardsGUIConfig != null;
        ItemStackConfig fillerConfig = rewardsGUIConfig.filler();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyPrestige.getComponentLogger());
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
        if(rewardsGUIConfig == null) return;
        ButtonConfig nextPageConfig = rewardsGUIConfig.nextPage();

        if(nextPageConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to add the next page button to the rewards GUI due to an invalid slot."));
            return;
        }

        createActionButton(nextPageConfig, inventoryClickEvent -> {
            pageNum++;
            numOfRewardsAdded = 0;
            numOfRewardsErrored = 0;

            this.update();
        });
    }

    /**
     * Create the previous page button for the GUI.
     */
    private void createPrevPageButton() {
        if(rewardsGUIConfig == null) return;
        ButtonConfig prevPageConfig = rewardsGUIConfig.prevPage();

        if(prevPageConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to add the previous page button to the rewards GUI due to an invalid slot."));
            return;
        }

        createActionButton(prevPageConfig, inventoryClickEvent -> {
            currentRewardKey = currentRewardKey - ((errorCountsPerPage.get(pageNum) + rewardsAddedPerPage.get(pageNum) + (errorCountsPerPage.get(pageNum - 1) + rewardsAddedPerPage.get(pageNum - 1))));

            numOfRewardsAdded = 0;
            numOfRewardsErrored = 0;
            pageNum--;

            this.update();
        });
    }

    /**
     * Create the exit button for the GUI.
     */
    private void createExitButton() {
        if(rewardsGUIConfig == null) return;
        ButtonConfig exitConfig = rewardsGUIConfig.exit();

        if(exitConfig.slot() == null) {
            logger.warn(AdventureUtil.serialize("Unable to add the exit button to the rewards GUI due to an invalid slot."));
            return;
        }

        createActionButton(exitConfig, inventoryClickEvent ->
                skyPrestige.getServer().getScheduler().runTaskLater(skyPrestige, () -> {
                    if(confirmPrestigeGUI != null) {
                        player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

                        guiManager.removeOpenGUI(uuid);

                        confirmPrestigeGUI.open();
                    } else {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                        guiManager.removeOpenGUI(uuid);
                    }
                }, 1L));
    }

    /**
     * Create the buttons to display rewards for the GUI.
     */
    private void createRewardsButtons() {
        while(numOfRewardsAdded < rewardsPerPage && currentRewardKey < prestigeConfig.rewards().size()) {
            PrestigeConfig.Reward reward = prestigeConfig.rewards().get(currentRewardKey);

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyPrestige.getComponentLogger());
            itemStackBuilder.fromItemStackConfig(reward.displayItem(), player, null, List.of());

            Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            if(optionalItemStack.isPresent()) {
                createDisplayButton(optionalItemStack.get(), getRewardSlot());
                numOfRewardsAdded++;
            } else {
                logger.warn(AdventureUtil.serialize("Unable to add a reward display item to the Rewards GUI due to a null ItemStack."));
                numOfRewardsErrored++;
            }

            currentRewardKey++;
        }
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(rewardsGUIConfig == null) return;

        rewardsGUIConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.serialize("Unable to add a dummy button to the rewards GUI due to an invalid slot."));
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
            logger.warn(AdventureUtil.serialize("Unable to add an action button to the requirements GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyPrestige.getComponentLogger());
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
            logger.warn(AdventureUtil.serialize("Unable to add a display button to the requirements GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyPrestige.getComponentLogger());
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

    /**
     * Get the slot to place a reward button at based on the current number of rewards added.
     * @return A slot number as an int.
     * @throws RuntimeException If the number of rewards would exceed the size of the GUI.
     */
    private int getRewardSlot() {
        return switch(numOfRewardsAdded) {
            case 0 -> 10;
            case 1 -> 11;
            case 2 -> 12;
            case 3 -> 13;
            case 4 -> 14;
            case 5 -> 15;
            case 6 -> 16;
            case 7 -> 19;
            case 8 -> 20;
            case 9 -> 21;
            case 10 -> 22;
            case 11 -> 23;
            case 12 -> 24;
            case 13 -> 25;
            case 14 -> 28;
            case 15 -> 29;
            case 16 -> 30;
            case 17 -> 31;
            case 18 -> 32;
            case 19 -> 33;
            case 20 -> 34;
            case 21 -> 37;
            case 22 -> 38;
            case 23 -> 39;
            case 24 -> 40;
            case 25 -> 41;
            case 26 -> 42;
            case 27 -> 43;
            default -> throw new RuntimeException("Number of requirements added exceeds the size of the GUI!");
        };
    }
}
