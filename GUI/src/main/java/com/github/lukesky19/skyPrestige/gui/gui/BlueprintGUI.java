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

import com.github.lukesky19.skyPrestige.configuration.data.gui.BlueprintGUIConfig;
import com.github.lukesky19.skyPrestige.configuration.data.gui.common.ButtonConfig;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.manager.gui.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.locale.LocaleManager;
import com.github.lukesky19.skyPrestige.core.abstracts.SkyPlugin;
import com.github.lukesky19.skyPrestige.data.island.IslandResetData;
import com.github.lukesky19.skyPrestige.gui.manager.GUIManager;
import com.github.lukesky19.skyPrestige.hook.hooks.BentoBoxHook;
import com.github.lukesky19.skyPrestige.hook.manager.HookManager;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.abstracts.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
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

import java.util.*;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to select a blueprint for island prestige.
 */
public class BlueprintGUI extends ChestGUI {
    // Plugin Classes
    private final @NotNull SkyPlugin plugin;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull GUIConfigManager guiConfigManager;
    private final @NotNull HookManager hookManager;

    // Island Reset Data
    private final @NotNull IslandResetData islandResetData;
    private final @NotNull Consumer<IslandResetData> consumer;

    private final @Nullable BlueprintGUIConfig blueprintGUIConfig;
    // Page info
    private int blueprintsPerPage;
    private int pageNum = 0;
    private int currentBlueprintKey = 0;
    private int numOfBlueprintsAdded = 0;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager A {@link GUIManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param islandResetData An {@link IslandResetData} instance.
     * @param consumer The consumer that will prestige the island once the player confirms it.
     */
    public BlueprintGUI(
            @NotNull SkyPlugin plugin,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull GUIManager guiManager,
            @NotNull HookManager hookManager,
            @NotNull IslandResetData islandResetData,
            @NotNull Consumer<IslandResetData> consumer) {
        super(plugin, guiManager, islandResetData.getPlayer());

        this.plugin = plugin;
        this.localeManager = localeManager;
        this.guiConfigManager = guiConfigManager;
        this.hookManager = hookManager;

        this.islandResetData = islandResetData;
        this.consumer = consumer;

        blueprintGUIConfig = guiConfigManager.getBlueprintGUIConfig();
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(blueprintGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the blueprint GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = blueprintGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the blueprint GUI due to an invalid GUIType."));
            return false;
        }

        switch(guiType) {
            case CHEST_27 -> blueprintsPerPage = 7;

            case CHEST_36 -> blueprintsPerPage = 14;

            case CHEST_45 -> blueprintsPerPage = 21;

            case CHEST_54 -> blueprintsPerPage = 28;

            default -> {
                logger.error(AdventureUtil.deserialize("Unsupported GUI Type in blueprints GUI config. Allowed Types: CHEST_27, CHEST_36, CHEST_45, CHEST_54"));
                return false;
            }
        }

        String guiName = Objects.requireNonNullElse(blueprintGUIConfig.guiName(), "");

        return create(guiType, guiName, List.of());
    }

    /**
     * Create all the buttons and decorate the GUI.
     * @return true if updated successfully, otherwise false.
     */
    @Override
    public boolean update() {
        clearButtons();

        if(blueprintGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the GUI as the gui configuration is invalid."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        createFillerButtons(guiSize);

        createDummyButtons();

        createExitButton();

        BentoBoxHook bentoBoxHook = hookManager.getHook(BentoBoxHook.class);
        if(bentoBoxHook.isHooked()) {
            @Nullable Map<String, BlueprintBundle> blueprints = bentoBoxHook.getBlueprints(islandResetData.getGameModeAddon());
            if(blueprints != null) {
                List<Map.Entry<String, BlueprintBundle>> blueprintList = blueprints.entrySet()
                        .stream()
                        .filter(entry -> {
                            BlueprintBundle blueprint = entry.getValue();

                            if (blueprint.isRequirePermission()) {
                                String permission = islandResetData.getGameModeAddon().getPermissionPrefix() + "island.create." + blueprint.getUniqueId();

                                return player.hasPermission(permission);
                            }

                            return true;
                        })
                        .sorted(Map.Entry.comparingByValue(Comparator.comparing(BlueprintBundle::getSlot)))
                        .toList();

                createBlueprintButtons(blueprintList);

                if(numOfBlueprintsAdded >= blueprintsPerPage && (blueprintList.size() - 1) >= currentBlueprintKey) {
                    createNextPageButton();
                }
            }
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
        currentBlueprintKey = currentBlueprintKey - numOfBlueprintsAdded;
        if(currentBlueprintKey < 0) currentBlueprintKey = 0;

        numOfBlueprintsAdded = 0;

        return this.update();
    }

    /**
     * Handles when the inventory is closed. Ignores closures with reason UNLOADED and OPEN_NEW.
     * @param inventoryCloseEvent An {@link InventoryCloseEvent}
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED) || inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) return;

        guiManager.removeOpenGUI(inventoryCloseEvent.getPlayer().getUniqueId());
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
        if(blueprintGUIConfig == null) return;

        ItemStackConfig fillerConfig = blueprintGUIConfig.filler();
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
     * Create buttons to display the blueprints that the player can select for their island.
     * @param blueprintList A {@link List} of {@link Map.Entry} mapping a blueprint name/id as a {@link String} to a {@link BlueprintBundle}.
     */
    private void createBlueprintButtons(@NotNull List<Map.Entry<String, BlueprintBundle>> blueprintList) {
        int blueprintListSize = blueprintList.size();
        while(numOfBlueprintsAdded < blueprintsPerPage) {
            if(currentBlueprintKey >= blueprintListSize) break;

            Map.Entry<String, BlueprintBundle> entry = blueprintList.get(currentBlueprintKey);
            BlueprintBundle blueprint = entry.getValue();

            GUIButton.Builder builder = new GUIButton.Builder();

            ItemStack itemStack = ItemStack.of(blueprint.getIcon());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(AdventureUtil.deserialize(blueprint.getDisplayName()));
            List<Component> lore = blueprint.getDescription().stream().map(AdventureUtil::deserialize).toList();
            itemMeta.lore(lore);
            itemStack.setItemMeta(itemMeta);

            builder.setItemStack(itemStack);

            builder.setAction(inventoryClickEvent -> {
                @NotNull Locale locale = localeManager.getLocale();
                Player player = (Player) inventoryClickEvent.getWhoClicked();

                islandResetData.setBlueprint(blueprint);

                this.close();

                ConfirmPrestigeGUI gui = new ConfirmPrestigeGUI(plugin, localeManager, guiConfigManager, guiManager, islandResetData, consumer);

                boolean creationResult = gui.create();
                if(!creationResult) {
                    logger.error(AdventureUtil.deserialize("Unable to create the InventoryView for the blueprints GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean updateResult = gui.update();
                if(!updateResult) {
                    logger.error(AdventureUtil.deserialize("Unable to decorate the blueprints GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                boolean openResult = gui.open();
                if(!openResult) {
                    logger.error(AdventureUtil.deserialize("Unable to open the blueprints GUI for player " + player.getName() + " due to a configuration error."));
                    player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.guiOpenError()));
                    return;
                }

                gui.open();
            });

            setButton(getBlueprintSlot(), builder.build());
            currentBlueprintKey++;
            numOfBlueprintsAdded++;
        }
    }

    /**
     * Create the next page button for the GUI.
     */
    private void createNextPageButton() {
        if(blueprintGUIConfig == null) return;

        ButtonConfig nextPageConfig = blueprintGUIConfig.nextPage();

        if(nextPageConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the next page button to the blueprints GUI due to an invalid slot."));
            return;
        }

        createActionButton(nextPageConfig, inventoryClickEvent -> {
            numOfBlueprintsAdded = 0;
            pageNum++;

            this.update();
        });
    }

    /**
     * Create the previous page button for the GUI.
     */
    private void createPrevPageButton() {
        if(blueprintGUIConfig == null) return;

        ButtonConfig prevPageConfig = blueprintGUIConfig.prevPage();

        if(prevPageConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the previous page button to the blueprints GUI due to an invalid slot."));
            return;
        }

        createActionButton(prevPageConfig, inventoryClickEvent -> {
            currentBlueprintKey = currentBlueprintKey - (numOfBlueprintsAdded + blueprintsPerPage);
            if(currentBlueprintKey < 0) currentBlueprintKey = 0;

            numOfBlueprintsAdded = 0;
            pageNum--;

            this.update();
        });
    }

    /**
     * Create the exit button for the GUI.
     */
    private void createExitButton() {
        if(blueprintGUIConfig == null) return;
        ButtonConfig exitConfig = blueprintGUIConfig.exit();

        if(exitConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the exit button to the blueprint GUI due to an invalid slot."));
            return;
        }

        createActionButton(exitConfig, inventoryClickEvent -> {
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);

            guiManager.removeOpenGUI(uuid);
        });
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(blueprintGUIConfig == null) return;

        blueprintGUIConfig.dummyButtons().forEach(buttonConfig -> {
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
            logger.warn(AdventureUtil.deserialize("Unable to add an action button to the blueprints GUI due to an invalid slot."));
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
            logger.warn(AdventureUtil.deserialize("Unable to add a display button to the blueprints GUI due to an invalid slot."));
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

    /**
     * Get the slot to place a blueprint button at based on the current number of blueprints added.
     * @return A slot number as an int.
     * @throws RuntimeException If the number of blueprints would exceed the size of the GUI.
     */
    private int getBlueprintSlot() {
        return switch(numOfBlueprintsAdded) {
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