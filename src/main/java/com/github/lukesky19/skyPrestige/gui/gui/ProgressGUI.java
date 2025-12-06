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

import com.github.lukesky19.skyPrestige.configuration.data.gui.ProgressGUIConfig;
import com.github.lukesky19.skyPrestige.configuration.data.gui.common.ButtonConfig;
import com.github.lukesky19.skyPrestige.configuration.data.locale.Locale;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.data.settings.Settings;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.configuration.manager.LocaleManager;
import com.github.lukesky19.skyPrestige.configuration.manager.SettingsManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skyPrestige.util.number.NumberUtils;
import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.common.abstracts.SkyPlugin;
import com.github.lukesky19.skylib.api.gui.GUIButton;
import com.github.lukesky19.skylib.api.gui.GUIType;
import com.github.lukesky19.skylib.api.gui.interfaces.IGUIManager;
import com.github.lukesky19.skylib.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.api.math.EquationUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to view progress to the next prestige level.
 */
public class ProgressGUI extends ChestGUI<IslandIdUUIDKey> {
    // Plugin Classes
    private final @NotNull SettingsManager settingsManager;
    private final @NotNull LocaleManager localeManager;
    // BentoBox
    private final @NotNull Island island;
    private final @NotNull IslandData islandData;
    // Config
    private final @NotNull PrestigeConfig prestigeConfig;
    private final @Nullable ProgressGUIConfig progressGUIConfig;

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param guiManager An {@link IGUIManager} instance.
     * @param identifier The {@link IslandIdUUIDKey} this GUI is tied to.
     * @param player The {@link Player} viewing the GUI.
     * @param island The player's {@link Island}.
     * @param islandData The {@link IslandData} for the island.
     * @param prestigeConfig The {@link PrestigeConfig} for the next prestige level.
     */
    public ProgressGUI(
            @NotNull SkyPlugin plugin,
            @NotNull SettingsManager settingsManager,
            @NotNull LocaleManager localeManager,
            @NotNull GUIConfigManager guiConfigManager,
            @NotNull IGUIManager<IslandIdUUIDKey> guiManager,
            @NotNull IslandIdUUIDKey identifier,
            @NotNull Player player,
            @NotNull Island island,
            @NotNull IslandData islandData,
            @NotNull PrestigeConfig prestigeConfig) {
        super(plugin, guiManager, identifier, player);

        this.settingsManager = settingsManager;
        this.localeManager = localeManager;
        this.prestigeConfig = prestigeConfig;

        progressGUIConfig = guiConfigManager.getProgressGUIConfig();

        this.island = island;
        this.islandData = islandData;
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(progressGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the progress GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = progressGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtil.deserialize("Unable to create the InventoryView for the progress GUI due to an invalid GUIType."));
            return false;
        }

        String guiName = Objects.requireNonNullElse(progressGUIConfig.guiName(), "");

        return create(guiType, guiName, List.of());
    }

    /**
     * Create all the buttons and decorate the GUI.
     * @return true if updated successfully, otherwise false.
     */
    @Override
    public boolean update() {
        if(progressGUIConfig == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the GUI as the gui configuration is invalid."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        Locale locale = localeManager.getConfiguration();
        Settings settings = settingsManager.getConfiguration();

        // Check for invalid settings
        if(settings == null || settings.scaleFormula() == null) {
            player.sendMessage(AdventureUtil.deserialize(locale.prefix() + locale.prestigeConfigError()));
            logger.error(AdventureUtil.deserialize("Unable to display prestige requirement due to invalid plugin settings or scale formula."));
            return false;
        }

        clearButtons();

        createFillerButtons(guiSize);

        createDummyButtons();

        createExitButton();

        createProgressButtons();

        return super.update();
    }

    /**
     * Refresh all the requirement button in the GUI.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean refresh() {
        createProgressButtons();

        return super.update();
    }

    /**
     * Handles when items are dragged across the player's inventory. The event is always cancelled.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleBottomDrag(@NotNull InventoryDragEvent inventoryDragEvent) {
        inventoryDragEvent.setCancelled(true);
    }

    /**
     * Handles when items are dragged across the entire inventory. The event is always cancelled.
     * @param inventoryDragEvent An {@link InventoryDragEvent}
     */
    @Override
    public void handleGlobalDrag(@NotNull InventoryDragEvent inventoryDragEvent) {
        inventoryDragEvent.setCancelled(true);
    }

    /**
     * Handles when the player's inventory is clicked. The event is always cancelled.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleBottomClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
    }

    /**
     * Handles when a click occurs in either inventory. The event is always cancelled.
     * @param inventoryClickEvent An {@link InventoryClickEvent}
     */
    @Override
    public void handleGlobalClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        inventoryClickEvent.setCancelled(true);
    }

    /**
     * Create the filler buttons for the GUI.
     * @param guiSize The size of the GUI.
     */
    private void createFillerButtons(int guiSize) {
        if(progressGUIConfig == null) return;
        ItemStackConfig fillerConfig = progressGUIConfig.filler();
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(fillerConfig, player, null, List.of());

        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> {
            GUIButton.Builder builder = new GUIButton.Builder();
            builder.setItemStack(itemStack);

            for(int i = 0; i <= guiSize - 1; i++) {
                setButton(i, builder.build());
            }
        });
    }

    /**
     * Create the exit button for the GUI.
     */
    private void createExitButton() {
        if(progressGUIConfig == null) return;
        ButtonConfig exitConfig = progressGUIConfig.exit();

        if(exitConfig.slot() == null) {
            logger.warn(AdventureUtil.deserialize("Unable to add the exit button to the progress GUI due to an invalid slot."));
            return;
        }

        createActionButton(exitConfig, inventoryClickEvent -> close());
    }

    /**
     * Create the buttons that display the island's prestige progress.
     */
    private void createProgressButtons() {
        if(progressGUIConfig == null ||
                progressGUIConfig.progressSlots().isEmpty() ||
                progressGUIConfig.progressButtons().incomplete().itemType() == null ||
                progressGUIConfig.progressButtons().complete().itemType() == null) return;

        Settings settings = settingsManager.getConfiguration();
        if(settings == null || settings.scaleFormula() == null) return;

        if(prestigeConfig.requiredPrestigePoints() == null) return;

        double requiredPoints;
        if (prestigeConfig.scaleFactor() != null && prestigeConfig.scaleFactor() != 0) {
            HashMap<String, String> variables = new HashMap<>();
            variables.put("r", String.valueOf(prestigeConfig.requiredPrestigePoints()));
            variables.put("p", String.valueOf(island.getMemberSet().size()));
            variables.put("k", String.valueOf(prestigeConfig.scaleFactor()));

            requiredPoints = EquationUtil.evaluateEquation(settings.scaleFormula(), variables).intValue();
        } else {
            requiredPoints = prestigeConfig.requiredPrestigePoints();
        }

        // Calculate current progress percentage
        double currentPercentage;
        if(requiredPoints > 0) {
            currentPercentage = Math.min(islandData.getPrestigePoints() / requiredPoints * 100, 100);
        } else {
            currentPercentage = 0.0;
        }

        // Create list of placeholders
        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("current_percentage", String.valueOf(currentPercentage)),
                Placeholder.parsed("needed_percentage", String.valueOf((100 - currentPercentage))),
                Placeholder.parsed("needed_points", NumberUtils.formatDecimal(Math.max(requiredPoints - islandData.getPrestigePoints(), 0))),
                Placeholder.parsed("island_points", NumberUtils.formatDecimal(islandData.getPrestigePoints())),
                Placeholder.parsed("required_points", String.valueOf(requiredPoints)));

        // Calculate the percentage of progress per button
        double percentagePerButton = (double) 100 / progressGUIConfig.progressSlots().size();
        // Loop through configured button slots and display the appropriate buttons based on the player's current percentage and the button's percentage
        for(int buttonCount = 0; buttonCount < progressGUIConfig.progressSlots().size(); buttonCount++) {
            double percentageForButton = percentagePerButton * (buttonCount + 1);
            int slot = progressGUIConfig.progressSlots().get(buttonCount);

            if(currentPercentage >= percentageForButton) {
                createDisplayButton(progressGUIConfig.progressButtons().complete(), slot, placeholders);
            } else {
                createDisplayButton(progressGUIConfig.progressButtons().incomplete(), slot, placeholders);
            }
        }
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(progressGUIConfig == null) return;

        progressGUIConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtil.deserialize("Unable to add a dummy button in the progress gui due to an invalid slot."));
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
            logger.warn(AdventureUtil.deserialize("Unable to add an action button to the progress GUI due to an invalid slot."));
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
            logger.warn(AdventureUtil.deserialize("Unable to add a display button to the progress GUI due to an invalid slot."));
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
     * @param itemStackConfig The {@link ItemStackConfig}.
     * @param placeholders A {@link List} of {@link TagResolver.Single} of placeholders for the button's ItemStack.
     */
    private void createDisplayButton(@NotNull ItemStackConfig itemStackConfig, int slot, @NotNull List<TagResolver.Single> placeholders) {
        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(plugin.getComponentLogger());
        itemStackBuilder.fromItemStackConfig(itemStackConfig, player, null, placeholders);

        Optional<@NotNull ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        optionalItemStack.ifPresent(itemStack -> createDisplayButton(itemStack, slot));
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
