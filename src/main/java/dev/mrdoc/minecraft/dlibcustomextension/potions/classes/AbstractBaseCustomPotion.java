package dev.mrdoc.minecraft.dlibcustomextension.potions.classes;

import com.google.common.base.Preconditions;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import dev.mrdoc.minecraft.dlibcustomextension.utils.persistence.PersistentDataKey;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.potion.PotionMix;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import dev.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.view.BrewingStandView;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract sealed class AbstractBaseCustomPotion permits AbstractCustomPotion {

    @Getter
    private final Plugin instance;
    @Getter
    private final String internalName;
    @Getter
    private final Key key;
    private final RecipeChoice recipeInput;
    private final RecipeChoice recipeIngredient;
    /**
     *  Gets the base Item used for recipe registration and validations.
     *  The item is final and should not be modified.
     *
     * @return Potion Item Custom
     */
    @Getter
    private final ItemStack item;
    @Getter
    private final PotionMix potionMix;

    public AbstractBaseCustomPotion(Plugin plugin, String internalName, Component displayName, List<Component> descriptions) {
        this.instance = plugin;
        this.internalName = internalName;

        this.key = new NamespacedKey(this.instance, this.internalName);

        this.item = createItem();
        Preconditions.checkState(this.item != null, "The potion item for %s is null", internalName);

        this.item.editPersistentDataContainer(persistentDataContainer -> persistentDataContainer.set(CustomPotionsManager.getNamespacedKey(), PersistentDataKey.KEY_CONTAINER, this.key));

        if (!Component.empty().equals(displayName)) {
            this.item.setData(DataComponentTypes.ITEM_NAME, displayName);
            // Patch because potion contents and POTION/SPLASH has a diff behavior for displayName
            if (this.item.hasData(DataComponentTypes.POTION_CONTENTS) || this.item.getType().asItemType() == ItemType.POTION || this.item.getType().asItemType() == ItemType.SPLASH_POTION || this.item.getType().asItemType() == ItemType.LINGERING_POTION) {
                this.item.setData(DataComponentTypes.CUSTOM_NAME, displayName.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }
        }

        ArrayList<Component> loreComponents = new ArrayList<>();

        if (!descriptions.isEmpty()) {
            loreComponents.add(Component.empty());
            List<Component> descriptionProcessed = descriptions.stream().map(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList();
            loreComponents.addAll(descriptionProcessed);
        }

        this.item.setData(DataComponentTypes.LORE, ItemLore.lore(loreComponents));

        this.recipeInput = this.createRecipeInput();
        this.recipeIngredient = this.createRecipeIngredient();
        this.potionMix = this.createPotionMix();
    }

    /**
     * Retrieves the {@link NamespacedKey} associated with this potion.
     *
     * @return the {@link NamespacedKey} representing the namespace and value for this potion.
     */
    public NamespacedKey getNamespaceKey() {
        return new NamespacedKey(this.key.namespace(), this.key.value());
    }

    /**
     * Definition of the item.
     *
     * @return item to create
     */
    public abstract ItemStack createItem();

    /**
     * The input (button) for the potion recipe.
     *
     * @return input
     */
    public abstract RecipeChoice createRecipeInput();

    /**
     * The ingredient (item button being added) for the potion recipe.
     *
     * @return ingredient
     */
    public abstract RecipeChoice createRecipeIngredient();

    private PotionMix createPotionMix() {
        return new PotionMix(this.getNamespaceKey(), this.item, this.recipeInput, this.recipeIngredient);
    }

    @Nullable
    public InventoryView createDisplayCraft(Player player) {
        final ItemStack unknownItem = ItemType.STRUCTURE_VOID.createItemStack(itemMeta -> itemMeta.itemName(Component.text("???").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));

        Component titleInventoryView = Component.translatable("dlce.potions.recipe.display", this.getItem().displayName());
        BrewingStandView brewingStandView = MenuType.BREWING_STAND.create(player, titleInventoryView);
        for (int basePos = 0; basePos < 3; basePos++) {
            ItemStack inputItem = unknownItem.clone();
            final RecipeChoice recipeInput = this.getPotionMix().getInput();
            if (recipeInput instanceof RecipeChoice.ExactChoice exactChoice) {
                inputItem = exactChoice.getChoices().getFirst();
            } else if (recipeInput instanceof RecipeChoice.ItemTypeChoice itemTypeChoice) {
                inputItem = Registry.ITEM.get(itemTypeChoice.itemTypes().iterator().next()).createItemStack();
            } else if (recipeInput instanceof RecipeChoice.MaterialChoice materialChoice) {
                inputItem = Objects.requireNonNull(materialChoice.getChoices().getFirst().asItemType()).createItemStack();
            }
            brewingStandView.setItem(basePos, inputItem);
        }

        ItemStack ingredientItem = unknownItem.clone();
        if (this.getPotionMix().getIngredient() instanceof RecipeChoice.ExactChoice exactChoice) {
            ingredientItem = exactChoice.getItemStack();
        } else if (this.getPotionMix().getIngredient() instanceof RecipeChoice.MaterialChoice materialChoice) {
            ingredientItem = materialChoice.getItemStack();
        }
        brewingStandView.setItem(3, ingredientItem);

        return brewingStandView;
    }

    /**
     * Gets the item for give to the player.
     *
     * @return the item
     */
    public ItemStack getItemForPlayer() {
        return getItem().clone();
    }

    /**
     * Generate a copy of the item to give to player.
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
     * Checks if the provided item matches this custom potion item
     *
     * @param itemToCheck Item to validate
     * @return {@code true} if it matches the custom potion item
     */
    public boolean isItem(@Nullable ItemStack itemToCheck) {
        if (itemToCheck == null || itemToCheck.isEmpty() || itemToCheck.getAmount() <= 0) {
            return false;
        }
        if (!itemToCheck.getPersistentDataContainer().has(CustomPotionsManager.getNamespacedKey(), PersistentDataKey.KEY_CONTAINER)) {
            return false;
        }
        return Objects.equals(itemToCheck.getPersistentDataContainer().get(CustomPotionsManager.getNamespacedKey(), PersistentDataKey.KEY_CONTAINER), this.getKey());
    }

    public void registerPotionMix() {
        LoggerUtils.info("Adding PotionMix " + this.getKey());
        Bukkit.getPotionBrewer().addPotionMix(this.getPotionMix());
    }

    public void unRegisterPotionMix() {
        LoggerUtils.info("Removing PotionMix " + this.getKey());
        Bukkit.getPotionBrewer().removePotionMix(this.getNamespaceKey());
    }

}
