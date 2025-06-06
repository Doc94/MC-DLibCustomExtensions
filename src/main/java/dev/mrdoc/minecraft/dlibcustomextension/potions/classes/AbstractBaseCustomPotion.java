package dev.mrdoc.minecraft.dlibcustomextension.potions.classes;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.potion.PotionMix;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import dev.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.persistence.PersistentDataType;
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
    private final NamespacedKey potionNamespace;
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

        this.potionNamespace = new NamespacedKey(this.instance, internalName);
        this.item = createItem();
        Validate.notNull(this.item, "The item for this potion cannot be null.");

        this.item.editPersistentDataContainer(persistentDataContainer -> persistentDataContainer.set(CustomPotionsManager.getNamespacedKey(), PersistentDataType.STRING, this.potionNamespace.toString()));

        if (displayName != null && !displayName.equals(Component.empty())) {
            this.item.setData(DataComponentTypes.ITEM_NAME, displayName);
            if (this.item.hasData(DataComponentTypes.POTION_CONTENTS) || this.item.getType().asItemType() == ItemType.POTION || this.item.getType().asItemType() == ItemType.SPLASH_POTION || this.item.getType().asItemType() == ItemType.LINGERING_POTION) {
                this.item.setData(DataComponentTypes.CUSTOM_NAME, displayName.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }
        }

        ArrayList<Component> lore = new ArrayList<>();

        if (!descriptions.isEmpty()) {
            lore.add(Component.empty());
            lore.addAll(descriptions);
        }

        this.item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

        this.recipeInput = createRecipeInput();
        this.recipeIngredient = createRecipeIngredient();
        this.potionMix = createPotionMix();
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
        return new PotionMix(this.potionNamespace, this.item, this.recipeInput, this.recipeIngredient);
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
        String data = itemToCheck.getPersistentDataContainer().getOrDefault(CustomPotionsManager.getNamespacedKey(), PersistentDataType.STRING, "");
        return itemToCheck.getType().equals(getItem().getType()) && data.equals(getPotionNamespace().toString());
    }

}
