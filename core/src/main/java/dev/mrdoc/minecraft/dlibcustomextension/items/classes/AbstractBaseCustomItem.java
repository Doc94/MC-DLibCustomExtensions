package dev.mrdoc.minecraft.dlibcustomextension.items.classes;

import dev.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import dev.mrdoc.minecraft.dlibcustomextension.utils.item.RecipeChoiceUtils;
import dev.mrdoc.minecraft.dlibcustomextension.utils.persistence.PersistentDataKey;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Base class for custom items.
 * <p>
 * This class provides the foundation for creating custom items with support for recipes,
 * visual components, and integration with the custom items manager.
 * </p>
 */
public abstract sealed class AbstractBaseCustomItem permits AbstractCustomItem {

    /**
     * Gets the instance of the plugin that owns this custom item.
     */
    @Getter
    private final Plugin instance;
    /**
     * Gets the unique internal name of the custom item (logical identifier).
     */
    @Getter
    private final String internalName;
    /**
     * Gets the unique Adventure key used as a full internal identifier.
     */
    @Getter
    private final Key key;
    /**
     * Gets the recipe associated with this custom item, if any.
     */
    @Getter
    private final @Nullable Recipe recipe;
    /**
     * The base item for recipes and validations.
     */
    @Getter
    private final ItemStack item;
    /**
     * The key for the item model.
     */
    @Getter
    private final @Nullable Key itemModelKey;
    /**
     * The sprite component for the item model, if available.
     */
    @Getter
    private final @Nullable ObjectComponent itemModelSprite;
    /**
     * Gets whether this item is considered special (e.g., has special visual effects or tags).
     */
    @Getter
    private final boolean special;
    /**
     * Gets whether the player should automatically discover the recipe.
     */
    @Getter
    private final boolean autoDiscoverRecipe;
    /**
     * Gets the rarity of the custom item, if assigned.
     */
    @Getter
    private final @Nullable CustomItemRarity rarity;
    /**
     * Gets the set of inventory types where this item is permitted.
     */
    @Getter
    private final HashSet<InventoryType> inventoryTypes = new HashSet<>();

    /**
     * Creates a new base custom item.
     *
     * @param instance           the owning plugin instance
     * @param internalName       the unique internal name
     * @param displayName        the display name of the item (can be {@link Component#empty()})
     * @param rarity             the rarity of the item
     * @param isSpecial          whether the item is marked as special
     * @param autoDiscoverRecipe whether to automatically discover the recipe
     * @param modelNameKey       the key for the item model
     * @param inventoryTypes     the permitted inventory types
     * @param descriptions       descriptive lines for the lore
     */
    @ApiStatus.Internal
    public AbstractBaseCustomItem(Plugin instance, String internalName, Component displayName, @Nullable CustomItemRarity rarity, boolean isSpecial, boolean autoDiscoverRecipe, @Nullable final Key modelNameKey, List<InventoryType> inventoryTypes, List<Component> descriptions) {
        this.inventoryTypes.addAll(inventoryTypes);
        this.instance = instance;
        this.internalName = internalName;

        this.autoDiscoverRecipe = autoDiscoverRecipe;

        this.key = new NamespacedKey(this.instance, this.internalName);

        this.item = Objects.requireNonNull(this.createItem(), "The ItemStack for %s is null".formatted(internalName));

        this.item.editPersistentDataContainer(persistentDataContainer -> persistentDataContainer.set(CustomItemsManager.getNamespacedKey(), PersistentDataKey.KEY_CONTAINER, this.key));

        if (!Component.empty().equals(displayName)) {
            this.item.setData(DataComponentTypes.ITEM_NAME, displayName.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        this.itemModelKey = modelNameKey;
        Key spriteKey = null;
        if (this.itemModelKey != null) {
            this.item.setData(DataComponentTypes.ITEM_MODEL, modelNameKey);
            spriteKey = Key.key(this.itemModelKey.namespace(), "item/".concat(this.itemModelKey.value()));
        }
        this.itemModelSprite = (spriteKey != null) ? Component.object(ObjectContents.sprite(Key.key("items"), spriteKey)) : null;

        ArrayList<Component> loreComponents = new ArrayList<>();

        this.special = isSpecial;
        if (this.special) {
            loreComponents.add(Component.text("✦ ✦ ✦ ✦ ✦", TextColor.fromHexString("#ac3fff")).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        this.rarity = rarity;
        if (this.rarity != null) {
            loreComponents.add(this.rarity.generateTag());
            if (this.rarity.vanillaRarity() != null) {
                this.item.setData(DataComponentTypes.RARITY, this.rarity.vanillaRarity());
            }
        }

        if (!descriptions.isEmpty()) {
            loreComponents.add(Component.empty());
            List<Component> descriptionProcessed = descriptions.stream().map(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList();
            loreComponents.addAll(descriptionProcessed);
        }

        this.item.setData(DataComponentTypes.LORE, ItemLore.lore().addLines(loreComponents).build());

        this.recipe = this.createRecipe();
    }

    /**
     * Returns the {@link NamespacedKey} associated with this custom item.
     *
     * @return the namespaced key representing the namespace and value of the custom item
     */
    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(this.key.namespace(), this.key.value());
    }

    /**
     * Determines whether the custom item is enabled.
     *
     * @return {@code true} if the item is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return CustomItemsManager.isItemEnable(getInternalName());
    }

    /**
     * The definition of the item.
     *
     * @return item to register
     */
    public abstract ItemStack createItem();

    /**
     * The definition of recipe
     *
     * @return recipe to register
     */
    public abstract @Nullable Recipe createRecipe();

    /**
     * Creates a crafting view to display the recipe to the player.
     *
     * @param player the player for whom the view is created
     * @return an inventory view representing the recipe, or {@code null} if no recipe exists
     */
    public @Nullable InventoryView createDisplayCraft(Player player) {
        Component titleInventoryView = Component.translatable("dlce.items.recipe.display", this.getItem().displayName());
        final Recipe recipe = this.getRecipe();
        if (recipe == null) return null;

        final InventoryView inventoryView;
        final List<Map<Integer, List<ItemStack>>> animatedSlots = new ArrayList<>();

        switch (recipe) {
            case ShapedRecipe shapedRecipe -> {
                inventoryView = MenuType.CRAFTING.create(player, titleInventoryView);
                String[] shape = shapedRecipe.getShape();
                Map<Character, RecipeChoice> choiceMap = shapedRecipe.getChoiceMap();

                int rows = shape.length;
                int columns = shape[0].length();
                int rowOffset = (3 - rows) / 2;
                int colOffset = (3 - columns) / 2;

                Map<Integer, List<ItemStack>> slots = new HashMap<>();
                for (int row = 0; row < rows; row++) {
                    String shapeRow = shape[row];
                    for (int col = 0; col < columns; col++) {
                        char slotChar = shapeRow.charAt(col);
                        if (slotChar == ' ' || !choiceMap.containsKey(slotChar)) continue;

                        int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset);
                        List<ItemStack> variants = RecipeChoiceUtils.getRecipeChoiceItemStacks(choiceMap.get(slotChar));
                        if (!variants.isEmpty()) {
                            slots.put(matrixIndex + 1, variants);
                        }
                    }
                }
                animatedSlots.add(slots);
            }
            case ShapelessRecipe shapelessRecipe -> {
                inventoryView = MenuType.CRAFTING.create(player, titleInventoryView);
                Map<Integer, List<ItemStack>> slots = new HashMap<>();
                List<RecipeChoice> choices = shapelessRecipe.getChoiceList();
                for (int i = 0; i < choices.size(); i++) {
                    List<ItemStack> variants = RecipeChoiceUtils.getRecipeChoiceItemStacks(choices.get(i));
                    if (!variants.isEmpty()) {
                        slots.put(i + 1, variants);
                    }
                }
                animatedSlots.add(slots);
            }
            case SmithingTransformRecipe smithingTransformRecipe -> {
                inventoryView = MenuType.SMITHING.create(player, titleInventoryView);
                Map<Integer, List<ItemStack>> slots = new HashMap<>();

                List<ItemStack> templateVariants = RecipeChoiceUtils.getRecipeChoiceItemStacks(smithingTransformRecipe.getTemplate());
                if (!templateVariants.isEmpty()) slots.put(0, templateVariants);

                List<ItemStack> baseVariants = RecipeChoiceUtils.getRecipeChoiceItemStacks(smithingTransformRecipe.getBase());
                if (!baseVariants.isEmpty()) slots.put(1, baseVariants);

                List<ItemStack> additionVariants = RecipeChoiceUtils.getRecipeChoiceItemStacks(smithingTransformRecipe.getAddition());
                if (!additionVariants.isEmpty()) slots.put(2, additionVariants);

                animatedSlots.add(slots);
            }
            default -> {
                return null;
            }
        }

        if (!animatedSlots.isEmpty()) {
            new BukkitRunnable() {
                int tick = 0;

                @Override
                public void run() {
                    if (inventoryView.getTopInventory().getViewers().isEmpty()) {
                        LoggerUtils.debug("Player " + player.getName() + " closed the crafting view, remove animation for choices recipes");
                        this.cancel();
                        return;
                    }

                    for (Map<Integer, List<ItemStack>> slots : animatedSlots) {
                        slots.forEach((slot, variants) -> {
                            ItemStack item = variants.get((tick / 20) % variants.size());
                            inventoryView.getTopInventory().setItem(slot, item);
                        });
                    }
                    tick += 20;
                }
            }.runTaskTimer(this.instance, 0L, 20L);
        }

        return inventoryView;
    }

    /**
     * Generate a copy of the item to give to the player.
     *
     * @return item to give
     */
    public ItemStack getItemForPlayer() {
        return this.getItem().clone();
    }

    /**
     * Generate a copy of the item to give to the player.
     *
     * @param quantity amount of item
     * @return item to give
     */
    public ItemStack getItemForPlayer(int quantity) {
        ItemStack itemStack = this.getItemForPlayer();
        itemStack.setAmount(quantity);
        return itemStack;
    }

    /**
     * Discovers the recipe for the specified player.
     *
     * @param player the player who discovers the recipe
     */
    public void discoverRecipe(Player player) {
        if (this.recipe != null && !player.hasDiscoveredRecipe(this.getNamespacedKey())) {
            player.discoverRecipe(this.getNamespacedKey());
        }
    }

    /**
     * Undiscovers the recipe for the specified player.
     *
     * @param player the player who undiscovers the recipe
     */
    public void undiscoverRecipe(Player player) {
        if (this.recipe != null && player.hasDiscoveredRecipe(this.getNamespacedKey())) {
            player.undiscoverRecipe(this.getNamespacedKey());
        }
    }

    /**
     * Registers the recipe into the server.
     */
    public void registerRecipe() {
        if (this.recipe != null) {
            LoggerUtils.info("Adding recipe " + this.getKey());
            Bukkit.getServer().addRecipe(this.getRecipe());
        }
    }

    /**
     * Unregisters the recipe from the server.
     */
    public void unRegisterRecipe() {
        if (this.recipe != null) {
            LoggerUtils.info("Removing recipe " + this.getKey());
            Bukkit.removeRecipe(this.getNamespacedKey());
        }
    }

    /**
     * Checks if the provided item stack matches this custom item.
     *
     * @param itemToCheck the item stack to validate
     * @return {@code true} if it matches, {@code false} otherwise
     */
    public boolean isItem(@Nullable ItemStack itemToCheck) {
        if (itemToCheck == null || itemToCheck.isEmpty() || itemToCheck.getAmount() <= 0) {
            return false;
        }
        if (!itemToCheck.getPersistentDataContainer().has(CustomItemsManager.getNamespacedKey(), PersistentDataKey.KEY_CONTAINER)) {
            return false;
        }
        return Objects.equals(itemToCheck.getPersistentDataContainer().get(CustomItemsManager.getNamespacedKey(), PersistentDataKey.KEY_CONTAINER), this.getKey());
    }
}
