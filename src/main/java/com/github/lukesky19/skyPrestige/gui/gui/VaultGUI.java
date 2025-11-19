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

import com.github.lukesky19.skyPrestige.SkyPrestige;
import com.github.lukesky19.skyPrestige.config.data.gui.VaultGUIConfig;
import com.github.lukesky19.skyPrestige.config.data.gui.button.ButtonConfig;
import com.github.lukesky19.skyPrestige.config.data.locale.Locale;
import com.github.lukesky19.skyPrestige.config.manager.gui.GUIConfigManager;
import com.github.lukesky19.skyPrestige.config.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.config.manager.settings.SettingsManager;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.island.data.IslandData;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to allow island members to store items that persist across prestiges.
 */
public class VaultGUI extends ChestGUI {
    // Plugin Classes
    private final @NotNull SkyPrestige skyPrestige;
    private final @NotNull GUIManager guiManager;
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    // Island
    private final @NotNull String islandId;
    private final @NotNull IslandData islandData;
    // Config
    private final @Nullable VaultGUIConfig vaultGUIConfig;
    private @Nullable VaultGUIConfig.PageConfig pageConfig;
    // Page info
    private int pageNum = 0;

    /**
     * Constructor
     * @param skyPrestige A {@link SkyPrestige} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param islandId The island's unique id.
     * @param islandData The island's {@link IslandData}.
     * @param player The {@link Player} viewing the GUI.
     */
    public VaultGUI(
            @NotNull SkyPrestige skyPrestige,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull GUIManager guiManager,
            @NotNull DatabaseManager databaseManager,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull String islandId,
            @NotNull IslandData islandData,
            @NotNull Player player) {
        super(skyPrestige, guiManager, player);

        this.skyPrestige = skyPrestige;
        this.guiManager = guiManager;
        this.databaseManager = databaseManager;
        this.settingsManager = settingsManager;
        this.localeManager = localeManager;

        this.islandId = islandId;
        this.islandData = islandData;

        vaultGUIConfig = guiConfigManager.getVaultGUIConfig();
        if(vaultGUIConfig != null) pageConfig = vaultGUIConfig.pages().getFirst();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(vaultGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the vault GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = vaultGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the vault GUI due to an invalid GUIType."));
            return false;
        }

        String guiName = Objects.requireNonNullElse(vaultGUIConfig.guiName(), "");

        return create(guiType, guiName, List.of());
    }

    /**
     * Create all the buttons and decorate the GUI.
     * @return true if updated successfully, otherwise false.
     */
    @Override
    public boolean update() {
        clearButtons();

        if(vaultGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the vault GUI as the gui configuration is invalid."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the vault GUI as the InventoryView was not created."));
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        createFillerButtons(guiSize);

        createDummyButtons();

        createExitButton();

        if(pageNum < vaultGUIConfig.pages().size() - 1) {
            createNextPageButton();
        }

        if(pageNum > 0) {
            createPrevPageButton();
        }

        createStorageSlots();

        return super.update();
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

            guiManager.removeOpenGUI(uuid);

            guiManager.removeOpenGUI(islandId, uuid);
        }, 1L);

        // Then 1 tick later, open the GUI and track that it is open for the player.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            inventoryView.open();

            guiManager.addOpenGUI(uuid, this);

            guiManager.addOpenGUI(islandId, uuid, this);
        }, 2L);

        return true;
    }

    /**
     * Close the GUI with an UNLOADED {@link InventoryCloseEvent.Reason}.
     * Then stores the items stored inside the vault to the database.
     * You should use {@link #unload(boolean)} if the plugin is being disabled, and you are trying to close open GUIs.
     */
    @Override
    public void close() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

            guiManager.removeOpenGUI(uuid);

            guiManager.removeOpenGUI(islandId, uuid);

            databaseManager.getIslandVaultsTable().setVaultData(islandId, islandData.getVaultItems());
        }, 1L);
    }

    /**
     * Close the GUI with an UNLOADED {@link InventoryCloseEvent.Reason}.
     * Then stores the items stored inside the vault to the database.
     * If the plugin is being disabled, the scheduler won't be used as it is unavailable during server shutdown.
     * @param onDisable Is the plugin being disabled?
     */
    @Override
    public void unload(boolean onDisable) {
        if(!onDisable) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                guiManager.removeOpenGUI(uuid);

                guiManager.removeOpenGUI(islandId, uuid);

                databaseManager.getIslandVaultsTable().setVaultData(islandId, islandData.getVaultItems());
            }, 1L);
        } else {
            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

            guiManager.removeOpenGUI(uuid);

            guiManager.removeOpenGUI(islandId, uuid);

            databaseManager.getIslandVaultsTable().setVaultData(islandId, islandData.getVaultItems());
        }
    }

    /**
     * Handles when the inventory is closed. Ignores closures with reason UNLOADED and OPEN_NEW.
     * Then stores the items stored inside the vault to the database.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(uuid);

        guiManager.removeOpenGUI(islandId, uuid);

        databaseManager.getIslandVaultsTable().setVaultData(islandId, islandData.getVaultItems());
    }

    /**
     * Runs the button's action if a button was clicked.
     * If not a button, returns the item stored in the vault at that slot.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleTopClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
        int slot = inventoryClickEvent.getSlot();

        GUIButton button = slotButtons.get(slot);
        if(button != null) {
            inventoryClickEvent.setCancelled(true);
            button.action().accept(inventoryClickEvent);
        }
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
     * Handles when the player's inventory is clicked. Attempts to store the item clicked in the vault.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
        if(pageConfig == null) return;
        @NotNull Locale locale = localeManager.getLocale();
        @NotNull Map<Integer, ItemStack> vaultItems = islandData.getVaultItemsByPageNumber(pageNum);

        int clickedSlot = inventoryClickEvent.getSlot();
        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        if(itemStack == null || itemStack.isEmpty()) return;
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;

        // If the item is restricted, don't add the item to the vault and send the player an error message
        if(settingsManager.isItemDisallowed(itemType)) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.vaultItemNotAllowed()));
            return;
        }

        // Track the remaining amount to merge
        int remainingAmount = itemStack.getAmount();

        // Iterate over slot configs to merge or place items
        for(VaultGUIConfig.SlotConfig slotConfig : pageConfig.slots()) {
            Integer slot = slotConfig.slot();
            if (slot == null || slotConfig.prestigeLevel() == null
                    || islandData.getPrestigeLevel() < slotConfig.prestigeLevel()) {
                continue; // Skip if conditions aren't met
            }

            @Nullable ItemStack vaultItem = vaultItems.get(slot);
            // Check if the slot is empty
            if(vaultItem != null && !vaultItem.isEmpty()) {
                // Check if the item in the slot is similar
                if(itemStack.isSimilar(vaultItem)) {
                    // Calculate the total amount
                    int totalAmount = vaultItem.getAmount() + remainingAmount;

                    // If the amount fits in the max stack size, set the item in the vault at that slot
                    if(totalAmount <= vaultItem.getMaxStackSize()) {
                        vaultItem.setAmount(totalAmount);

                        remainingAmount = 0;

                        islandData.addVaultItem(pageNum, slot, vaultItem);

                        break;
                    } else {
                        // Fill the stack up to the max stack size
                        remainingAmount = totalAmount - vaultItem.getMaxStackSize();

                        vaultItem.setAmount(vaultItem.getMaxStackSize());

                        islandData.addVaultItem(pageNum, slot, vaultItem);
                    }
                }
            } else {
                ItemStack clonedStack = itemStack.clone();
                createVaultButton(clonedStack, slotConfig.unlockedItem(), slot);
                remainingAmount = 0;
                islandData.addVaultItem(pageNum, slot, clonedStack);
                break;
            }
        }

        if(remainingAmount > 0) {
            ItemStack returnItem = itemStack.clone();
            returnItem.setAmount(remainingAmount);

            player.getInventory().setItem(clickedSlot, returnItem);
        } else {
            player.getInventory().setItem(clickedSlot, ItemType.AIR.createItemStack());
        }

        guiManager.refreshVaultGUIs(islandId);
    }

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
        if(pageConfig == null) return;

        ItemStackConfig fillerConfig = pageConfig.filler();
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
        if(pageConfig == null) return;

        ButtonConfig nextPageConfig = pageConfig.nextPage();

        if(nextPageConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the next page button to the blueprints GUI due to an invalid slot."));
            return;
        }

        createActionButton(nextPageConfig, inventoryClickEvent -> {
            pageNum++;

            this.update();
        });
    }

    /**
     * Create the previous page button for the GUI.
     */
    private void createPrevPageButton() {
        if(pageConfig == null) return;

        ButtonConfig prevPageConfig = pageConfig.prevPage();

        if(prevPageConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the previous page button to the blueprints GUI due to an invalid slot."));
            return;
        }

        createActionButton(prevPageConfig, inventoryClickEvent -> {
            pageNum--;

            this.update();
        });
    }

    /**
     * Create the exit button for the GUI.
     */
    private void createExitButton() {
        if(pageConfig == null) return;
        ButtonConfig exitConfig = pageConfig.exit();

        if(exitConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the exit button to the blueprint GUI due to an invalid slot."));
            return;
        }

        createActionButton(exitConfig, inventoryClickEvent -> {
            skyPrestige.getServer().getScheduler().runTaskLater(skyPrestige, () ->
                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);

            guiManager.removeOpenGUI(uuid);
        });
    }

    /**
     * Create the buttons for the items inside the vault or the placeholder buttons.
     */
    private void createStorageSlots() {
        if (inventoryView == null || pageConfig == null) return;

        @NotNull Map<Integer, ItemStack> vaultItems = islandData.getVaultItemsByPageNumber(pageNum);
        int islandPrestigeLevel = islandData.getPrestigeLevel();

        pageConfig.slots().stream()
                .filter(slotConfig ->
                        slotConfig.slot() != null
                                && slotConfig.prestigeLevel() != null
                                && slotConfig.lockedItem().itemType() != null)
                .forEach(slotConfig ->
                        createStorageButton(
                                vaultItems,
                                slotConfig.slot(),
                                islandPrestigeLevel,
                                slotConfig.prestigeLevel(),
                                slotConfig.unlockedItem(),
                                slotConfig.lockedItem()));
    }

    /**
     * Create an individual storage button with either a vault item button or a placeholder button.
     * @param vaultItems The {@link Map} mapping slots to {@link ItemStack}s in the vault for the current page.
     * @param slot The slot the butotn should be placed at.
     * @param islandPrestigeLevel The island's current prestige level.
     * @param requiredPrestigeLevel The prestige level required to use this slot.
     * @param unlockedItem The {@link ItemStackConfig} for the placeholder item when the player has access to the slot, but no vault item is placed there.
     * @param lockedItem The {@link ItemStackConfig} for the placeholder item when the player doesn't have access to the slot.
     */
    private void createStorageButton(
            @NotNull Map<Integer, ItemStack> vaultItems,
            int slot,
            int islandPrestigeLevel,
            int requiredPrestigeLevel,
            @NotNull ItemStackConfig unlockedItem,
            @NotNull ItemStackConfig lockedItem) {
        slotButtons.remove(slot);

        if(islandPrestigeLevel >= requiredPrestigeLevel) {
            @Nullable ItemStack vaultItemStack = vaultItems.get(slot);
            if(vaultItemStack == null) {
                // Add the unlocked placeholder button to the GUI
                createUnlockedButton(unlockedItem, slot);
            } else {
                // Create a button for the vault item
                createVaultButton(vaultItemStack, unlockedItem, slot);
            }
        } else {
            // Otherwise display the locked button.
            createLockedButton(lockedItem, slot, islandPrestigeLevel, requiredPrestigeLevel);
        }
    }

    /**
     * Create the button that acts as a placeholder for where items can be placed inside the vault.
     * @param unlockedItem The {@link ItemStackConfig} to create the button's ItemStack with.
     * @param slot The slot to place the button at.
     */
    private void createUnlockedButton(
            @NotNull ItemStackConfig unlockedItem,
            int slot) {
        if(unlockedItem.itemType() != null) {
            // Create the button using the item config
            createDisplayButton(unlockedItem, slot, List.of());
        } else {
            // Create an empty button
            GUIButton unlockedButton = new GUIButton.Builder().setItemStack(ItemType.AIR.createItemStack()).build();

            // Add the button
            setButton(slot, unlockedButton);
        }
    }

    /**
     * Create the button that acts as a placeholder for where items can be placed inside the vault.
     * @param lockedItem The {@link ItemStackConfig} to create the button's ItemStack with.
     * @param slot The slot to place the button at.
     * @param islandPrestigeLevel The island's current prestige level.
     * @param requiredPrestigeLevel The prestige level required to access this slot.
     */
    private void createLockedButton(
            @NotNull ItemStackConfig lockedItem,
            int slot,
            int islandPrestigeLevel,
            int requiredPrestigeLevel) {
        createDisplayButton(lockedItem, slot,
                List.of(
                        Placeholder.parsed("island_prestige_level", String.valueOf(islandPrestigeLevel)),
                        Placeholder.parsed("required_prestige_level", String.valueOf(requiredPrestigeLevel))
                ));
    }

    /**
     * Create the button for the item stored inside the vault.
     * @param vaultItemStack The {@link ItemStack} to use for the button.
     * @param unlockedItem The {@link ItemStackConfig} used to replace the button with after the item is removed from the vault.
     * @param slot The slot to place the button at.
     */
    private void createVaultButton(
            @NotNull ItemStack vaultItemStack,
            @NotNull ItemStackConfig unlockedItem,
            int slot) {
        // Create a button for the vault item
        createActionButton(vaultItemStack, slot, inventoryClickEvent -> {
            // Update the GUI with the unlocked placeholder button for the slot
            createUnlockedButton(unlockedItem, slot);

            // Remove the item from the vault data
            islandData.removeVaultItem(pageNum, slot);

            // Give the player the item
            PlayerUtil.giveItem(player.getInventory(), vaultItemStack.clone(), vaultItemStack.getAmount(), player.getLocation());

            // Refresh the vault guis open for the island id
            guiManager.refreshVaultGUIs(islandId);
        });
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(pageConfig == null) return;

        pageConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a dummy button to the blueprint GUI due to an invalid slot."));
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
            logger.warn(AdventureUtil.deserialize("Unable to add an action button to the vault GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyPrestige.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, List.of());

        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        optionalItemStack.ifPresent(itemStack -> createActionButton(itemStack, buttonConfig.slot(), action));
    }

    /**
     * Create a button with a custom action to take when clicked.
     * @param itemStack The {@link ItemStack} to use for the button.
     * @param slot The slot to place the button at.
     * @param action A {@link Consumer} that takes an {@link InventoryClickEvent} to execute when the button is clicked.
     */
    private void createActionButton(@NotNull ItemStack itemStack, int slot, @NotNull Consumer<InventoryClickEvent> action) {
        GUIButton.Builder builder = new GUIButton.Builder();

        builder.setItemStack(itemStack);

        builder.setAction(action);

        setButton(slot, builder.build());
    }

    /**
     * Create a button that has no action associated with it.
     * @param buttonConfig The {@link ButtonConfig}.
     * @param placeholders A {@link List} of {@link TagResolver.Single} of placeholders for the button's ItemStack.
     */
    private void createDisplayButton(@NotNull ButtonConfig buttonConfig, @NotNull List<TagResolver.Single> placeholders) {
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add a display button to the vault GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyPrestige.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, placeholders);
        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(itemStack);

            setButton(buttonConfig.slot(), builder.build());
        });
    }

    /**
     * Create a button that has no action associated with it.
     * @param itemStackConfig The {@link ItemStackConfig}.
     * @param slot The slot number to place the button.
     */
    private void createDisplayButton(@NotNull ItemStackConfig itemStackConfig, int slot, @NotNull List<TagResolver.Single> placeholders) {
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(skyPrestige.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, placeholders);
        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(itemStack);

            setButton(slot, builder.build());
        });
    }
}
