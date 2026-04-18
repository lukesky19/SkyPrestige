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

import com.github.lukesky19.skyPrestige.configuration.data.gui.VaultGUIConfig;
import com.github.lukesky19.skyPrestige.configuration.data.gui.common.ButtonConfig;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.VaultConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.database.DatabaseManager;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.gui.GUIButton;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.paper.api.player.PlayerUtil;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to allow island members to store items that persist across prestiges.
 */
public class VaultGUI extends ChestGUI<IslandIdUUIDKey> {
    // Plugin Classes
    private final @NonNull GUIManager guiManager;
    private final @NonNull DatabaseManager databaseManager;
    private final @NonNull LocaleManager localeManager;
    private final @NonNull VaultConfigManager vaultConfigManager;
    // Island
    private final @NonNull String islandId;
    private final @NonNull IslandData islandData;
    // Config
    private final @Nullable VaultGUIConfig vaultGUIConfig;
    private VaultGUIConfig.@Nullable PageConfig pageConfig;
    // Page info
    private int pageNum = 0;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param identifier The {@link IslandIdUUIDKey} this GUI is tied to.
     * @param databaseManager A {@link DatabaseManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param vaultConfigManager A {@link VaultConfigManager} instance.
     * @param islandId The island's unique id.
     * @param islandData The island's {@link IslandData}.
     * @param player The {@link Player} viewing the GUI.
     */
    public VaultGUI(
            @NonNull SkyPlugin plugin,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull GUIManager guiManager,
            @NonNull IslandIdUUIDKey identifier,
            @NonNull DatabaseManager databaseManager,
            @NonNull LocaleManager localeManager,
            @NonNull VaultConfigManager vaultConfigManager,
            @NonNull String islandId,
            @NonNull IslandData islandData,
            @NonNull Player player) {
        super(plugin, guiManager, identifier, player);

        this.guiManager = guiManager;
        this.databaseManager = databaseManager;
        this.localeManager = localeManager;
        this.vaultConfigManager = vaultConfigManager;

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
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the vault GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = vaultGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the vault GUI due to an invalid GUIType."));
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
            logger.warn(AdventureUtility.plain("Unable to add buttons to the vault GUI as the gui configuration is invalid."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the vault GUI as the InventoryView was not created."));
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

    /**
     * Close the GUI with an UNLOADED {@link InventoryCloseEvent.Reason}.
     * Then stores the items stored inside the vault to the database.
     * You should use {@link #unload(boolean)} if the plugin is being disabled, and you are trying to close open GUIs.
     */
    @Override
    public void close() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

            guiManager.removeOpenGUI(identifier);

            databaseManager.getIslandDataTable().saveIslandData(islandId, islandData);
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

                guiManager.removeOpenGUI(identifier);

                databaseManager.getIslandDataTable().saveIslandData(islandId, islandData);
            }, 1L);
        } else {
            player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

            guiManager.removeOpenGUI(identifier);

            databaseManager.getIslandDataTable().saveIslandData(islandId, islandData);
        }
    }

    /**
     * Handles when the inventory is closed. Ignores closures with reason UNLOADED and OPEN_NEW.
     * Then stores the items stored inside the vault to the database.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NonNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(identifier);

        databaseManager.getIslandDataTable().saveIslandData(islandId, islandData);
    }

    /**
     * Runs the button's action if a button was clicked.
     * If not a button, returns the item stored in the vault at that slot.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleTopClick(@NonNull InventoryClickEvent inventoryClickEvent) {
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
    public void handleBottomDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when items are dragged across the entire inventory. This method does nothing.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NonNull InventoryDragEvent inventoryDragEvent) {}

    /**
     * Handles when the player's inventory is clicked. Attempts to store the item clicked in the vault.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NonNull InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
        if(pageConfig == null) return;
        Locale locale = localeManager.getConfiguration();
        Map<Integer, ItemStack> vaultItems = islandData.getVaultItemsByPageNumber(pageNum);

        int clickedSlot = inventoryClickEvent.getSlot();
        ItemStack itemStack = inventoryClickEvent.getCurrentItem();
        if(itemStack == null || itemStack.isEmpty()) return;
        ItemType itemType = itemStack.getType().asItemType();
        if(itemType == null) return;

        // If the item is restricted, don't add the item to the vault and send the player an error message
        if(vaultConfigManager.isVaultItemTypeDisallowed(itemType)) {
            player.sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.vaultMessages().itemNotAllowed()));
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

            ItemStack vaultItem = vaultItems.get(slot);
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
                clonedStack.setAmount(remainingAmount);
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
    public void handleGlobalClick(@NonNull InventoryClickEvent inventoryClickEvent) {}

    /**
     * Create the filler buttons for the GUI.
     * @param guiSize The size of the GUI.
     */
    private void createFillerButtons(int guiSize) {
        if(pageConfig == null) return;

        ItemStackConfig fillerConfig = pageConfig.filler();
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
        if(pageConfig == null) return;

        ButtonConfig nextPageConfig = pageConfig.nextPage();

        if(nextPageConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add the next page button to the blueprints GUI due to an invalid slot."));
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
            logger.warn(AdventureUtility.plain("Unable to add the previous page button to the blueprints GUI due to an invalid slot."));
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
            logger.warn(AdventureUtility.plain("Unable to add the exit button to the blueprint GUI due to an invalid slot."));
            return;
        }

        createActionButton(exitConfig, inventoryClickEvent -> close());
    }

    /**
     * Create the buttons for the items inside the vault or the placeholder buttons.
     */
    private void createStorageSlots() {
        if (inventoryView == null || pageConfig == null) return;

        Map<Integer, ItemStack> vaultItems = islandData.getVaultItemsByPageNumber(pageNum);
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
            @NonNull Map<Integer, ItemStack> vaultItems,
            int slot,
            int islandPrestigeLevel,
            int requiredPrestigeLevel,
            @NonNull ItemStackConfig unlockedItem,
            @NonNull ItemStackConfig lockedItem) {
        slotButtons.remove(slot);

        if(islandPrestigeLevel >= requiredPrestigeLevel) {
            ItemStack vaultItemStack = vaultItems.get(slot);
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
            @NonNull ItemStackConfig unlockedItem,
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
            @NonNull ItemStackConfig lockedItem,
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
            @NonNull ItemStack vaultItemStack,
            @NonNull ItemStackConfig unlockedItem,
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
                logger.warn(AdventureUtility.plain("Unable to add a dummy button to the blueprint GUI due to an invalid slot."));
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
            logger.warn(AdventureUtility.plain("Unable to add an action button to the vault GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, List.of());

        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        optionalItemStack.ifPresent(itemStack -> createActionButton(itemStack, buttonConfig.slot(), action));
    }

    /**
     * Create a button with a custom action to take when clicked.
     * @param itemStack The {@link ItemStack} to use for the button.
     * @param slot The slot to place the button at.
     * @param action A {@link Consumer} that takes an {@link InventoryClickEvent} to execute when the button is clicked.
     */
    private void createActionButton(@NonNull ItemStack itemStack, int slot, @NonNull Consumer<InventoryClickEvent> action) {
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
    private void createDisplayButton(@NonNull ButtonConfig buttonConfig, @NonNull List<TagResolver.Single> placeholders) {
        if(buttonConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add a display button to the vault GUI due to an invalid slot."));
            return;
        }

        ItemStackConfig itemStackConfig = buttonConfig.item();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, placeholders);
        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
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
    private void createDisplayButton(@NonNull ItemStackConfig itemStackConfig, int slot, @NonNull List<TagResolver.Single> placeholders) {
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, placeholders);
        Optional<@NonNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();

        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();

            builder.setItemStack(itemStack);

            setButton(slot, builder.build());
        });
    }
}
