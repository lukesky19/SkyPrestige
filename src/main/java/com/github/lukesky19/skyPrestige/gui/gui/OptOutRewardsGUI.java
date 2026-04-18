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

import com.github.lukesky19.skyPrestige.configuration.data.gui.RewardsGUIConfig;
import com.github.lukesky19.skyPrestige.configuration.data.gui.common.ButtonConfig;
import com.github.lukesky19.skyPrestige.configuration.data.opt_in_out.OptInOutConfig;
import com.github.lukesky19.skyPrestige.configuration.data.reward.CommandReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.ItemReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.MoneyReward;
import com.github.lukesky19.skyPrestige.configuration.data.reward.RewardConfig;
import com.github.lukesky19.skyPrestige.configuration.interfaces.IReward;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.OptOutConfigManager;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.gui.GUIButton;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.gui.interfaces.IGUIManager;
import com.github.lukesky19.skylib.paper.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to view rewards for opting out.
 */
public class OptOutRewardsGUI extends ChestGUI<IslandIdUUIDKey> {
    // The ConfirmOptOutGUI to open after this one is closed.
    // This GUI can also be opened via a command so they don't always have a ConfirmOptOutGUI to go back to.
    private final @Nullable ConfirmOptOutGUI confirmOptOutGUI;

    // Config
    private final @Nullable RewardsGUIConfig rewardsGUIConfig;
    private final @Nullable OptInOutConfig optOutConfig;

    // Page info
    private int rewardsPerPage;
    private int pageNum = 0;
    private int currentRewardKey = 0;
    private int numOfRewardsAdded = 0;
    private int numOfRewardsErrored = 0;
    private final @NonNull Map<Integer, Integer> rewardsAddedPerPage = new HashMap<>();
    private final @NonNull Map<Integer, Integer> errorCountsPerPage = new HashMap<>();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager An {@link IGUIManager} instance.
     * @param identifier The {@link IslandIdUUIDKey} this GUI is tied to.
     * @param player The {@link Player} viewing the GUI.
     * @param optOutConfigManager An {@link OptOutConfigManager} instance.
     * @param confirmOptOutGUI The {@link ConfirmOptOutGUI} the player came from, if any.
     */
    public OptOutRewardsGUI(
            @NonNull SkyPlugin plugin,
            @NonNull IGUIManager<IslandIdUUIDKey> guiManager,
            @NonNull IslandIdUUIDKey identifier,
            @NonNull Player player,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull OptOutConfigManager optOutConfigManager,
            @Nullable ConfirmOptOutGUI confirmOptOutGUI) {
        super(plugin, guiManager, identifier, player);

        this.rewardsGUIConfig = guiConfigManager.getOptOutRewardsGUIConfig();

        this.confirmOptOutGUI = confirmOptOutGUI;
        this.optOutConfig = optOutConfigManager.getConfiguration();
    }

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param guiManager An {@link IGUIManager} instance.
     * @param identifier The {@link IslandIdUUIDKey} this GUI is tied to.
     * @param player The {@link Player} viewing the GUI.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param optOutConfig The {@link OptInOutConfig} for opt-out.
     * @param confirmOptOutGUI The {@link ConfirmOptOutGUI} the player came from, if any.
     */
    public OptOutRewardsGUI(
            @NonNull SkyPlugin plugin,
            @NonNull IGUIManager<IslandIdUUIDKey> guiManager,
            @NonNull IslandIdUUIDKey identifier,
            @NonNull Player player,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull OptInOutConfig optOutConfig,
            @Nullable ConfirmOptOutGUI confirmOptOutGUI) {
        super(plugin, guiManager, identifier, player);

        this.rewardsGUIConfig = guiConfigManager.getOptOutRewardsGUIConfig();

        this.confirmOptOutGUI = confirmOptOutGUI;
        this.optOutConfig = optOutConfig;
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(optOutConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the opt-out rewards GUI due to invalid opt-out configuration."));
            return false;
        }

        if(rewardsGUIConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the opt-out rewards GUI due to invalid opt-out rewards GUI configuration."));
            return false;
        }

        GUIType guiType = rewardsGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the opt-out rewards GUI due to an invalid GUIType."));
            return false;
        }

        switch(guiType) {
            case CHEST_27 -> rewardsPerPage = 7;

            case CHEST_36 -> rewardsPerPage = 14;

            case CHEST_45 -> rewardsPerPage = 21;

            case CHEST_54 -> rewardsPerPage = 28;

            default -> {
                logger.error(AdventureUtility.plain("Unsupported GUI Type in opt-in rewards GUI config. Allowed Types: CHEST_27, CHEST_36, CHEST_45, CHEST_54"));
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
        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        if(rewardsGUIConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the GUI as the gui configuration is invalid."));
            return false;
        }

        if(optOutConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the GUI due to invalid opt-in configuration."));
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        clearButtons();

        createFillerButtons(guiSize);

        createDummyButtons();

        createExitButton();

        createRewardsButtons(optOutConfig.rewardConfig());

        rewardsAddedPerPage.put(pageNum, numOfRewardsAdded);
        errorCountsPerPage.put(pageNum, numOfRewardsErrored);

        int totalRewardsCount = getTotalRewardsCount(optOutConfig.rewardConfig());
        if(numOfRewardsAdded >= rewardsPerPage && currentRewardKey < totalRewardsCount) {
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
     * Handles when the GUI is closed by the player.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NonNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(identifier);

        if(confirmOptOutGUI != null) confirmOptOutGUI.open();
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
        assert rewardsGUIConfig != null;
        ItemStackConfig fillerConfig = rewardsGUIConfig.filler();
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
     * Create the next page button for the GUI.
     */
    private void createNextPageButton() {
        if(rewardsGUIConfig == null) return;
        ButtonConfig nextPageConfig = rewardsGUIConfig.nextPage();

        if(nextPageConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add the next page button to the opt-in rewards GUI due to an invalid slot."));
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
            logger.warn(AdventureUtility.plain("Unable to add the previous page button to the opt-in rewards GUI due to an invalid slot."));
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
            logger.warn(AdventureUtility.plain("Unable to add the exit button to the opt-in rewards GUI due to an invalid slot."));
            return;
        }

        createActionButton(exitConfig, inventoryClickEvent ->
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);

                    guiManager.removeOpenGUI(identifier);

                    if(confirmOptOutGUI != null) {
                        confirmOptOutGUI.open();
                    }
                }, 1L));
    }

    /**
     * Create the buttons to display rewards for the GUI.
     * @param rewardConfig The {@link RewardConfig}.
     */
    private void createRewardsButtons(@NonNull RewardConfig rewardConfig) {
        int totalRewardsCount = getTotalRewardsCount(rewardConfig);

        while(numOfRewardsAdded < rewardsPerPage && currentRewardKey < totalRewardsCount) {
            // Determine the current reward based on the current key
            IReward currentReward = getCurrentReward(rewardConfig);

            if(currentReward != null) {
                addRewardButton(currentReward);
                currentRewardKey++;
            }
        }
    }

    /**
     * Get the total number of rewards.
     * @param rewardConfig The {@link RewardConfig}.
     * @return The total number of rewards.
     */
    private int getTotalRewardsCount(@NonNull RewardConfig rewardConfig) {
        int count = rewardConfig.itemRewards().size() +
                rewardConfig.commandRewards().size() +
                rewardConfig.permissionRewards().size() +
                rewardConfig.groupRewards().size() +
                rewardConfig.moneyRewards().size();

        if (rewardConfig.islandSizeReward().islandSize() > 0) {
            count++;
        }

        return count;
    }

    /**
     * Create a reward button that displays a reward.
     * @param reward The {@link ItemReward}, {@link CommandReward}, or {@link MoneyReward} to process. May be null.
     */
    private void addRewardButton(@NonNull IReward reward) {
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());

        ItemStackConfig itemStackConfig = reward.displayItem();
        if(itemStackConfig.itemType() != null) {
            itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());

            Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            if(optionalItemStack.isPresent()) {
                createDisplayButton(optionalItemStack.get(), getRewardSlot());
                numOfRewardsAdded++;
            } else {
                logger.warn(AdventureUtility.plain("Unable to add a reward display item to the opt-in rewards GUI due to a null ItemStack."));
                numOfRewardsErrored++;
            }
        }
    }

    /**
     * Get the current reward based on the {@link #currentRewardKey}.
     * @param rewardConfig The {@link RewardConfig}.
     * @return A {@link IReward} to process. May be null.
     */
    private @Nullable IReward getCurrentReward(@NonNull RewardConfig rewardConfig) {
        int itemRewardsCount = rewardConfig.itemRewards().size();
        int commandRewardsCount = rewardConfig.commandRewards().size();
        int permissionRewardsCount = rewardConfig.permissionRewards().size();
        int groupRewardsCount = rewardConfig.groupRewards().size();
        int moneyRewardsCount = rewardConfig.moneyRewards().size();

        if(currentRewardKey < itemRewardsCount) {
            return rewardConfig.itemRewards().get(currentRewardKey);
        } else if(currentRewardKey < itemRewardsCount + commandRewardsCount) {
            return rewardConfig.commandRewards().get(currentRewardKey - itemRewardsCount);
        } else if(currentRewardKey < itemRewardsCount + commandRewardsCount + permissionRewardsCount) {
            return rewardConfig.permissionRewards().get(currentRewardKey - itemRewardsCount - commandRewardsCount);
        } else if(currentRewardKey < itemRewardsCount + commandRewardsCount + permissionRewardsCount + groupRewardsCount) {
            return rewardConfig.groupRewards().get(currentRewardKey - itemRewardsCount - commandRewardsCount - permissionRewardsCount);
        } else if(currentRewardKey < itemRewardsCount + commandRewardsCount + permissionRewardsCount + groupRewardsCount + moneyRewardsCount) {
            return rewardConfig.moneyRewards().get(currentRewardKey - itemRewardsCount - commandRewardsCount - permissionRewardsCount - groupRewardsCount);
        } else if(rewardConfig.islandSizeReward().islandSize() > 0
                && currentRewardKey == itemRewardsCount + commandRewardsCount + permissionRewardsCount + groupRewardsCount + moneyRewardsCount) {
            return rewardConfig.islandSizeReward();
        }

        return null;
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(rewardsGUIConfig == null) return;

        rewardsGUIConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtility.plain("Unable to add a dummy button to the opt-in rewards GUI due to an invalid slot."));
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
            logger.warn(AdventureUtility.plain("Unable to add an action button to the requirements GUI due to an invalid slot."));
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
            logger.warn(AdventureUtility.plain("Unable to add a display button to the requirements GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, placeholders);

        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> createDisplayButton(itemStack, buttonConfig.slot()));
    }

    /**
     * Create a button that has no action associated with it.
     * @param itemStack The {@link ItemStack} for the button.
     * @param slot The slot to place the button at.
     */
    private void createDisplayButton(@NonNull ItemStack itemStack, int slot) {
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
