package dev.mrdoc.minecraft.dlibcustomextension.items.classes;

import com.google.common.base.Preconditions;
import dev.mrdoc.minecraft.dlibcustomextension.utils.persistence.PersistentDataKey;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import dev.mrdoc.minecraft.dlibcustomextension.items.CustomItemRecipeHelper;
import dev.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract sealed class AbstractBaseCustomItem permits AbstractCustomItem {

    @Getter
    private final Plugin instance;
    @Getter
    private final String internalName;
    @Getter
    private final Key key;
    @Nullable
    @Getter
    private final Recipe recipe;
    /**
     * The base item for recipes and validations.
     */
    @Getter
    private final ItemStack item;
    @Getter
    private boolean special;
    @Nullable
    @Getter
    private final CustomItemRarity rarity;
    @Getter
    private final HashSet<InventoryType> inventoryTypes = new HashSet<>();

    @ApiStatus.Internal
    public AbstractBaseCustomItem(Plugin instance, String internalName, Component displayName, @Nullable CustomItemRarity rarity, boolean isSpecial, @Nullable final Key modelNameKey, List<InventoryType> inventoryTypes, List<Component> descriptions) {
        this.inventoryTypes.addAll(inventoryTypes);
        this.instance = instance;
        this.internalName = internalName;

        this.key = new NamespacedKey(this.instance, this.internalName);

        this.item = this.createItem();
        Preconditions.checkState(this.item != null, "The item for %s is null", internalName);

        this.item.editPersistentDataContainer(persistentDataContainer -> persistentDataContainer.set(CustomItemsManager.getNamespacedKey(), PersistentDataKey.KEY_CONTAINER, this.key));

        if (!Component.empty().equals(displayName)) {
            this.item.setData(DataComponentTypes.ITEM_NAME, displayName.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }
        if (modelNameKey != null) {
            this.item.setData(DataComponentTypes.ITEM_MODEL, modelNameKey);
        }

        ArrayList<Component> loreComponents = new ArrayList<>();

        if (isSpecial) {
            this.special = true;
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
    @Nullable
    public abstract Recipe createRecipe();

    @Nullable
    public InventoryView createDisplayCraft(Player player) {
        Component titleInventoryView = Component.translatable("dlce.items.recipe.display", this.getItem().displayName());
        return switch (this.getRecipe()) {
            case null -> null;
            case ShapedRecipe shapedRecipe -> {
                InventoryView inventoryView = MenuType.CRAFTING.create(player, titleInventoryView);

                String[] shape = shapedRecipe.getShape();
                Map<Character, @Nullable ItemStack> ingredientMap = CustomItemRecipeHelper.getIngredientMap(shapedRecipe.getChoiceMap());

                int rows = shape.length; // Recipe rows
                int columns = shape[0].length(); // Recipe columns (we assume well-formed recipe)

                // We calculate the offset to center the recipe in the 3x3 matrix
                int rowOffset = (3 - rows) / 2;
                int colOffset = (3 - columns) / 2;

                for (int row = 0; row < rows; row++) {
                    String shapeRow = shape[row];

                    for (int col = 0; col < columns; col++) {
                        char slot = shapeRow.charAt(col);
                        int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset); // We adjust by offset

                        if (slot == ' ' || !ingredientMap.containsKey(slot)) {
                            continue;
                        }

                        ItemStack requiredItem = ingredientMap.get(slot);
                        inventoryView.getTopInventory().setItem(matrixIndex + 1, requiredItem);
                    }
                }
                yield inventoryView;
            }
            case ShapelessRecipe shapelessRecipe -> {
                InventoryView inventoryView = MenuType.CRAFTING.create(player, titleInventoryView);
                for (int pos = 1; pos <= shapelessRecipe.getChoiceList().size(); pos++) {
                    inventoryView.getTopInventory().setItem(pos, CustomItemRecipeHelper.getRecipeChoiceItemStack(shapelessRecipe.getChoiceList().get(pos - 1)));
                }
                yield inventoryView;
            }
            case SmithingTransformRecipe smithingTransformRecipe -> {
                InventoryView inventoryView = MenuType.SMITHING.create(player, titleInventoryView);
                if (!smithingTransformRecipe.getTemplate().equals(RecipeChoice.empty())) {
                    inventoryView.setItem(0, CustomItemRecipeHelper.getRecipeChoiceItemStack(smithingTransformRecipe.getTemplate()));
                }
                if (!smithingTransformRecipe.getBase().equals(RecipeChoice.empty())) {
                    inventoryView.setItem(1, CustomItemRecipeHelper.getRecipeChoiceItemStack(smithingTransformRecipe.getBase()));
                }
                if (!smithingTransformRecipe.getAddition().equals(RecipeChoice.empty())) {
                    inventoryView.setItem(2, CustomItemRecipeHelper.getRecipeChoiceItemStack(smithingTransformRecipe.getAddition()));
                }
                yield inventoryView;
            }
            default -> null;
        };
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

    public void discoverRecipe(Player player) {
        if (this.recipe != null && !player.hasDiscoveredRecipe(this.getNamespacedKey())) {
            player.discoverRecipe(this.getNamespacedKey());
        }
    }

    public void undiscoverRecipe(Player player) {
        if (this.recipe != null && player.hasDiscoveredRecipe(this.getNamespacedKey())) {
            player.undiscoverRecipe(this.getNamespacedKey());
        }
    }

    public void registerRecipe() {
        if (this.recipe != null) {
            LoggerUtils.info("Adding recipe " + this.getKey());
            Bukkit.getServer().addRecipe(this.getRecipe());
        }
    }

    public void unRegisterRecipe() {
        if (this.recipe != null) {
            LoggerUtils.info("Removing recipe " + this.getKey());
            Bukkit.removeRecipe(this.getNamespacedKey());
        }
    }

    /**
     * Checks if the provided item matches this custom item
     *
     * @param itemToCheck Item to validate
     * @return {@code true} if it matches the custom item
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
