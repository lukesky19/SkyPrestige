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

import com.github.lukesky19.skyPrestige.configuration.data.gui.RequirementsGUIConfig;
import com.github.lukesky19.skyPrestige.configuration.data.gui.common.ButtonConfig;
import com.github.lukesky19.skyPrestige.configuration.data.prestige.PrestigeConfig;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.InventoryRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.MoneyRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.PrestigePointsRequirement;
import com.github.lukesky19.skyPrestige.configuration.data.requirement.QuestRequirement;
import com.github.lukesky19.skyPrestige.configuration.manager.GUIConfigManager;
import com.github.lukesky19.skyPrestige.data.data.island.IslandData;
import com.github.lukesky19.skyPrestige.integration.hooks.EconomyHook;
import com.github.lukesky19.skyPrestige.integration.hooks.LMBQuestHook;
import com.github.lukesky19.skyPrestige.integration.manager.HookManager;
import com.github.lukesky19.skyPrestige.requirements.RequirementsManager;
import com.github.lukesky19.skyPrestige.util.key.IslandIdUUIDKey;
import com.github.lukesky19.skyPrestige.util.number.NumberUtils;
import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.format.FormatUtil;
import com.github.lukesky19.skylib.paper.api.gui.GUIButton;
import com.github.lukesky19.skylib.paper.api.gui.GUIType;
import com.github.lukesky19.skylib.paper.api.gui.interfaces.IGUIManager;
import com.github.lukesky19.skylib.paper.api.gui.templates.ChestGUI;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.paper.api.plugin.SkyPlugin;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.quest.Quest;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;
import java.util.function.Consumer;

/**
 * This class is used to create the GUI to view requirements for the next prestige level.
 */
public class RequirementsGUI extends ChestGUI<IslandIdUUIDKey> {
    private final @NonNull RequirementsManager requirementsManager;
    private final @NonNull HookManager hookManager;

    // Config
    private final @Nullable RequirementsGUIConfig requirementsGUIConfig;
    private final @NonNull PrestigeConfig prestigeConfig;

    // Island Data
    private final @NonNull Island island;
    private final @NonNull IslandData islandData;

    // Page info
    private int requirementsPerPage;
    private int pageNum = 0;
    private int currentRequirementKey = 0;
    private int numOfRequirementsAdded = 0;
    private int numOfRequirementsErrored = 0;
    private final @NonNull Map<Integer, Integer> requirementsAddedPerPage = new HashMap<>();
    private final @NonNull Map<Integer, Integer> errorCountsPerPage = new HashMap<>();

    /**
     * Constructor
     * @param plugin A {@link JavaPlugin} instance.
     * @param guiManager An {@link IGUIManager} instance.
     * @param identifier The {@link IslandIdUUIDKey} this GUI is tied to.
     * @param player The {@link Player} viewing the GUI.
     * @param guiConfigManager A {@link GUIConfigManager} instance.
     * @param requirementsManager A {@link RequirementsManager} instance.
     * @param hookManager A {@link HookManager} instance.
     * @param prestigeConfig The {@link PrestigeConfig} to display rewards for.
     * @param island The player's {@link Island}.
     * @param islandData The island's {@link IslandData}.
     */
    public RequirementsGUI(
            @NonNull SkyPlugin plugin,
            @NonNull IGUIManager<IslandIdUUIDKey> guiManager,
            @NonNull IslandIdUUIDKey identifier,
            @NonNull Player player,
            @NonNull GUIConfigManager guiConfigManager,
            @NonNull RequirementsManager requirementsManager,
            @NonNull HookManager hookManager,
            @NonNull PrestigeConfig prestigeConfig,
            @NonNull Island island,
            @NonNull IslandData islandData) {
        super(plugin, guiManager, identifier, player);

        this.requirementsManager = requirementsManager;
        this.hookManager = hookManager;

        this.requirementsGUIConfig = guiConfigManager.getRequirementsGUIConfig();

        this.prestigeConfig = prestigeConfig;
        this.island = island;
        this.islandData = islandData;
    }

    /**
     * Create the {@link InventoryView} for this GUI.
     * @return true if created successfully, otherwise false.
     */
    public boolean create() {
        if(requirementsGUIConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the rewards GUI due to invalid gui configuration."));
            return false;
        }

        GUIType guiType = requirementsGUIConfig.guiType();
        if(guiType == null) {
            logger.warn(AdventureUtility.plain("Unable to create the InventoryView for the rewards GUI due to an invalid GUIType."));
            return false;
        }

        switch(guiType) {
            case CHEST_27 -> requirementsPerPage = 7;

            case CHEST_36 -> requirementsPerPage = 14;

            case CHEST_45 -> requirementsPerPage = 21;

            case CHEST_54 -> requirementsPerPage = 28;

            default -> {
                logger.error(AdventureUtility.plain("Unsupported GUI Type in rewards GUI config. Allowed Types: CHEST_27, CHEST_36, CHEST_45, CHEST_54"));
                return false;
            }
        }

        String guiName = Objects.requireNonNullElse(requirementsGUIConfig.guiName(), "");

        return create(guiType, guiName, List.of());
    }

    /**
     * Create all the buttons and decorate the GUI.
     * @return true if updated successfully, otherwise false.
     */
    @Override
    public boolean update() {
        if(requirementsGUIConfig == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the GUI as the gui configuration is invalid."));
            return false;
        }

        // If the InventoryView was not created, log a warning and return false.
        if(inventoryView == null) {
            logger.warn(AdventureUtility.plain("Unable to add buttons to the GUI as the InventoryView was not created."));
            return false;
        }

        int guiSize = inventoryView.getTopInventory().getSize();

        clearButtons();

        createFillerButtons(guiSize);

        createDummyButtons();

        createExitButton();

        int totalRequirementCount = getTotalRequirementsCount();
        createRequirementButtons(totalRequirementCount);

        requirementsAddedPerPage.put(pageNum, numOfRequirementsAdded);
        errorCountsPerPage.put(pageNum, numOfRequirementsErrored);

        if(numOfRequirementsAdded >= requirementsPerPage && currentRequirementKey < totalRequirementCount) {
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
        int reqAddedCurrentPage = requirementsAddedPerPage.get(pageNum);

        currentRequirementKey = currentRequirementKey - (errorCountCurrentPage + reqAddedCurrentPage);
        if(currentRequirementKey < 0) currentRequirementKey = 0;

        numOfRequirementsAdded = 0;
        numOfRequirementsErrored = 0;

        return this.update();
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
        assert requirementsGUIConfig != null;
        ItemStackConfig fillerConfig = requirementsGUIConfig.filler();
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
        if(requirementsGUIConfig == null) return;
        ButtonConfig nextPageConfig = requirementsGUIConfig.nextPage();

        if(nextPageConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add the next page button to the rewards GUI due to an invalid slot."));
            return;
        }

        createActionButton(nextPageConfig, inventoryClickEvent -> {
            pageNum++;
            numOfRequirementsAdded = 0;
            numOfRequirementsErrored = 0;

            this.update();
        });
    }

    /**
     * Create the previous page button for the GUI.
     */
    private void createPrevPageButton() {
        if(requirementsGUIConfig == null) return;
        ButtonConfig prevPageConfig = requirementsGUIConfig.prevPage();

        if(prevPageConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add the previous page button to the requirements GUI due to an invalid slot."));
            return;
        }

        createActionButton(prevPageConfig, inventoryClickEvent -> {
            currentRequirementKey = currentRequirementKey - ((errorCountsPerPage.get(pageNum) + requirementsAddedPerPage.get(pageNum) + (errorCountsPerPage.get(pageNum - 1) + requirementsAddedPerPage.get(pageNum - 1))));

            numOfRequirementsAdded = 0;
            numOfRequirementsErrored = 0;
            pageNum--;

            this.update();
        });
    }

    /**
     * Create the exit button for the GUI.
     */
    private void createExitButton() {
        if(requirementsGUIConfig == null) return;
        ButtonConfig exitConfig = requirementsGUIConfig.exit();

        if(exitConfig.slot() == null) {
            logger.warn(AdventureUtility.plain("Unable to add the exit button to the requirements GUI due to an invalid slot."));
            return;
        }

        createActionButton(exitConfig, inventoryClickEvent -> close());
    }

    /**
     * Create the requirements button.
     * @param totalRequirementCount The total requirements count.
     */
    private void createRequirementButtons(int totalRequirementCount) {
        if(currentRequirementKey == 0) {
            createPrestigePointsButton(prestigeConfig.prestigePointsRequirement());
        } else {
            currentRequirementKey++;
        }

        if(currentRequirementKey == 1) {
            createMoneyButton(prestigeConfig.moneyRequirement());
        } else {
            currentRequirementKey++;
        }

        if(!prestigeConfig.inventoryRequirements().isEmpty()
                && currentRequirementKey == 2
                && currentRequirementKey <= (2 + prestigeConfig.inventoryRequirements().size())) {
            createInventoryButtons(prestigeConfig.inventoryRequirements(), totalRequirementCount);
        } else {
            currentRequirementKey++;
        }

        if(!prestigeConfig.questRequirements().isEmpty()
                && currentRequirementKey == 3
                && currentRequirementKey <= (3 + prestigeConfig.questRequirements().size())) {
            createQuestButtons(prestigeConfig.questRequirements(), totalRequirementCount);
        } else {
            currentRequirementKey++;
        }
    }

    /**
     * Create the prestige points requirement button.
     * @param prestigePointsRequirement The {@link PrestigePointsRequirement}.
     */
    private void createPrestigePointsButton(@NonNull PrestigePointsRequirement prestigePointsRequirement) {
        if(prestigePointsRequirement.prestigePoints() <= 0) {
            currentRequirementKey++;
            return;
        }
        double requiredPoints = requirementsManager.calculateRequiredPrestigePoints(island.getMemberSet().size(), islandData, prestigePointsRequirement);
        if(requiredPoints <= 0) {
            currentRequirementKey++;
            return;
        }

        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("current_points", NumberUtils.formatDecimal(islandData.getPrestigePoints())),
                Placeholder.parsed("required_points", NumberUtils.formatDecimal(requiredPoints)));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        if(islandData.getPrestigePoints() >= requiredPoints) {
            itemStackBuilder.fromItemStackConfig(prestigePointsRequirement.completedStack(), player, placeholders);
        } else {
            itemStackBuilder.fromItemStackConfig(prestigePointsRequirement.incompleteStack(), player, placeholders);
        }

        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isPresent()) {
            if(createDisplayButton(optionalItemStack.get(), getRequirementSlot())) {
                numOfRequirementsAdded++;
            } else {
                numOfRequirementsErrored++;
            }
        } else {
            logger.warn(AdventureUtility.plain("Failed to create the display ItemStack for a prestige points requirement in the requirements GUI."));
            numOfRequirementsErrored++;
        }

        currentRequirementKey++;
    }

    private void createMoneyButton(@NonNull MoneyRequirement moneyRequirement) {
        if(moneyRequirement.money() <= 0) {
            currentRequirementKey++;
            return;
        }

        Server server = plugin.getServer();
        EconomyHook economyHook = hookManager.getHook(EconomyHook.class);

        double requiredMoney = requirementsManager.scale(
                moneyRequirement.scaleFormula(),
                moneyRequirement.money(),
                island.getMemberSet().size(),
                moneyRequirement.scaleFactor());
        if(requiredMoney <= 0) {
            currentRequirementKey++;
            return;
        }

        double balance = island.getMemberSet().stream()
                .map(server::getOfflinePlayer)
                .map(economyHook::getBalance)
                .reduce(Double::sum)
                .orElse(0.0);

        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("required_money", NumberUtils.formatDecimal(requiredMoney)),
                Placeholder.parsed("balance", NumberUtils.formatDecimal(balance)));

        ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
        if(balance >= requiredMoney) {
            itemStackBuilder.fromItemStackConfig(moneyRequirement.completedStack(), player,  placeholders);
        } else {
            itemStackBuilder.fromItemStackConfig(moneyRequirement.incompleteStack(), player, placeholders);
        }

        Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
        if(optionalItemStack.isPresent()) {
            if(createDisplayButton(optionalItemStack.get(), getRequirementSlot())) {
                numOfRequirementsAdded++;
            } else {
                numOfRequirementsErrored++;
            }
        } else {
            logger.warn(AdventureUtility.plain("Failed to create the display ItemStack for the money requirement in the requirements GUI."));
            numOfRequirementsErrored++;
        }

        currentRequirementKey++;
    }

    private void createInventoryButtons(@NonNull List<InventoryRequirement> inventoryRequirements, int totalRequirementCount) {
        int inventoryCount = inventoryRequirements.size();
        if(inventoryCount == 0) {
            currentRequirementKey++;
            return;
        }

        for(int invKey = 0; invKey < inventoryCount && numOfRequirementsAdded < requirementsPerPage && currentRequirementKey < totalRequirementCount; invKey++) {
            InventoryRequirement inventoryRequirement = inventoryRequirements.get(invKey);
            int requiredAmount = requirementsManager.scale(
                    inventoryRequirement.scaleFormula(),
                    inventoryRequirement.item().amount() != null ? inventoryRequirement.item().amount() : 1,
                    island.getMemberSet().size(),
                    inventoryRequirement.scaleFactor());

            ItemStackBuilder requiredItemBuilder = new ItemStackBuilder(logger);
            requiredItemBuilder.fromItemStackConfig(inventoryRequirement.item(), player, List.of());
            requiredItemBuilder.setAmount(requiredAmount);

            Optional<ItemStack> optionalRequiredItem = requiredItemBuilder.buildItemStack();
            if(optionalRequiredItem.isEmpty()) {
                logger.warn(AdventureUtility.plain("Failed to create the required ItemStack for an inventory requirement in the requirements GUI."));
                numOfRequirementsErrored++;
                continue;
            }
            ItemStack requiredStack = optionalRequiredItem.get();
            ItemType itemType = requiredStack.getType().asItemType();
            if(itemType == null) {
                logger.warn(AdventureUtility.plain("Failed to get the ItemType from the required ItemStack for an inventory requirement in the requirements GUI."));
                numOfRequirementsErrored++;
                continue;
            }

            Server server = plugin.getServer();
            int playersAmount = island.getMemberSet().stream()
                    .map(server::getPlayer)
                    .filter(Objects::nonNull)
                    .map(member -> getInventoryAmount(member.getInventory(), requiredStack))
                    .reduce(Integer::sum)
                    .orElse(0);

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("item_name", FormatUtil.formatItemTypeName(itemType)));
            placeholders.add(Placeholder.parsed("current_amount", String.valueOf(playersAmount)));
            placeholders.add(Placeholder.parsed("required_amount", String.valueOf(requiredStack.getAmount())));

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            if(playersAmount >= requiredAmount) {
                itemStackBuilder.fromItemStackConfig(inventoryRequirement.completedStack(), player, placeholders);
            } else {
                itemStackBuilder.fromItemStackConfig(inventoryRequirement.incompleteStack(), player, placeholders);
            }

            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            if(optionalItemStack.isPresent()) {
                if(createDisplayButton(optionalItemStack.get(), getRequirementSlot())) {
                    numOfRequirementsAdded++;
                } else {
                    numOfRequirementsErrored++;
                }
            } else {
                logger.warn(AdventureUtility.plain("Failed to create the display ItemStack for the money requirement in the requirements GUI."));
                numOfRequirementsErrored++;
            }

            currentRequirementKey++;
        }
    }

    /**
     * Get the amount of items inside the player's inventory that match the provided stack.
     * @param inventory The player's {@link PlayerInventory}.
     * @param itemStack The {@link ItemStack} to check for.
     * @return The amount of items that match the current stack inside the inventory.
     */
    private int getInventoryAmount(@NonNull PlayerInventory inventory, @NonNull ItemStack itemStack) {
        return Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(invItem -> invItem.isSimilar(itemStack))
                .map(ItemStack::getAmount)
                .reduce(Integer::sum)
                .orElse(0);
    }

    /**
     * Create the quest requirements buttons.
     * @param questRequirements The {@link List} of {@link QuestRequirement}s.
     * @param totalRequirementCount The total requirements count.
     */
    private void createQuestButtons(@NonNull List<QuestRequirement> questRequirements, int totalRequirementCount) {
        int questSize = questRequirements.size();
        if(questSize == 0) {
            currentRequirementKey++;
            return;
        }

        LMBQuestHook lmbQuestHook = hookManager.getHook(LMBQuestHook.class);
        if(!lmbQuestHook.isHooked()) {
            logger.warn(AdventureUtility.plain("Failed to create the display ItemStacks for a quest requirements in the requirements GUI due to the quests plugin not being hooked into."));
            numOfRequirementsErrored += questSize;
            currentRequirementKey += questSize;
            return;
        }

        List<QPlayer> qPlayerList = lmbQuestHook.getQPlayerList(island.getMemberSet());

        for(int questKey = 0; questKey < questSize && numOfRequirementsAdded < requirementsPerPage && currentRequirementKey < totalRequirementCount; questKey++) {
            QuestRequirement questRequirement = prestigeConfig.questRequirements().get(questKey);
            if(questRequirement.questId() == null) {
                logger.warn(AdventureUtility.plain("Failed to create the display ItemStack for a quest requirement in the requirements GUI due to a null quest id."));
                numOfRequirementsErrored++;
                currentRequirementKey++;
                continue;
            }

            Quest quest = lmbQuestHook.getQuestById(questRequirement.questId());
            if(quest == null) {
                logger.warn(AdventureUtility.plain("Failed to create the display ItemStack for a quest requirement in the requirements GUI due to no quest found for quest id " + questRequirement.questId() + "."));
                numOfRequirementsErrored++;
                currentRequirementKey++;
                continue;
            }

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("quest_id", questRequirement.questId()));

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(logger);
            if(lmbQuestHook.isQuestComplete(qPlayerList, quest)) {
                itemStackBuilder.fromItemStackConfig(questRequirement.completedStack(), player, placeholders);
            } else {
                itemStackBuilder.fromItemStackConfig(questRequirement.incompleteStack(), player, placeholders);
            }

            Optional<ItemStack> optionalItemStack = itemStackBuilder.buildItemStack();
            if(optionalItemStack.isPresent()) {
                if (createDisplayButton(optionalItemStack.get(), getRequirementSlot())) {
                    numOfRequirementsAdded++;
                } else {
                    numOfRequirementsErrored++;
                }
            } else {
                logger.warn(AdventureUtility.plain("Failed to create the display ItemStack for a quest requirement in the requirements GUI."));
                numOfRequirementsErrored++;
            }

            currentRequirementKey++;
        }
    }

    /**
     * Get the total number of requirements.
     * @return The total number of requirements.
     */
    private int getTotalRequirementsCount() {
        int count = 3; // Starts at 3 because of the prestige points, money, and inventory requirements

        if(!prestigeConfig.questRequirements().isEmpty()) {
            count += prestigeConfig.questRequirements().size();
        }

        return count;
    }

    /**
     * Create the dummy buttons for the GUI.
     */
    private void createDummyButtons() {
        if(requirementsGUIConfig == null) return;

        requirementsGUIConfig.dummyButtons().forEach(buttonConfig -> {
            if(buttonConfig.slot() == null) {
                logger.warn(AdventureUtility.plain("Unable to add a dummy button to the requirements GUI due to an invalid slot."));
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
    private boolean createDisplayButton(@NonNull ItemStack itemStack, int slot) {
        GUIButton.Builder builder = new GUIButton.Builder();

        builder.setItemStack(itemStack);

        return setButton(slot, builder.build());
    }

    /**
     * Get the slot to place a requirement button at based on the current number of requirements added.
     * @return A slot number as an int.
     * @throws RuntimeException If the number of requirements would exceed the size of the GUI.
     */
    private int getRequirementSlot() {
        return switch(numOfRequirementsAdded) {
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