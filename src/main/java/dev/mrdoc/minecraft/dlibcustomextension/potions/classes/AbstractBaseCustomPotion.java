package dev.mrdoc.minecraft.dlibcustomextension.potions.classes;

import com.google.common.base.Preconditions;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import dev.mrdoc.minecraft.dlibcustomextension.utils.item.RecipeChoiceUtils;
import dev.mrdoc.minecraft.dlibcustomextension.utils.persistence.PersistentDataKey;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.potion.PotionMix;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.Getter;
import dev.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.view.BrewingStandView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Base class for custom potions.
 * <p>
 * Manages the base item, namespace keys, visual components, and potion mixing,
 * including utilities for registering/unregistering and displaying the recipe in a brewing stand.
 * </p>
 */
@NullMarked
public abstract sealed class AbstractBaseCustomPotion permits AbstractCustomPotion {

    /**
     * Gets the instance of the plugin that owns this potion.
     */
    @Getter
    private final Plugin instance;
    /**
     * Gets the unique internal name of the potion (logical identifier).
     */
    @Getter
    private final String internalName;
    /**
     * Gets the unique Adventure key used as a full internal identifier.
     */
    @Getter
    private final Key key;
    /**
     * The base input for the brewing recipe (bottle/base).
     */
    private final RecipeChoice recipeInput;
    /**
     * A list of example input items used for defining the recipe of a custom potion.
     * This variable stores a collection of {@link ItemStack} instances representing possible
     * inputs for the potion's brewing recipe.
     * It serves as a reference for displaying or validating recipe configurations.
     */
    private final List<ItemStack> recipeInputExamples = new ArrayList<>();
    /**
     * The ingredient added to the brewing recipe.
     */
    private final RecipeChoice recipeIngredient;
    /**
     * A collection of example ingredient items used for creating custom potion recipes.
     * This list is used to register and display potential recipe ingredients
     * in various components of the custom potion system, such as crafting displays
     * or registration logic.
     */
    private final List<ItemStack> recipeIngredientExamples = new ArrayList<>();
    /**
     *  Gets the base Item used for recipe registration and validations.
     *  The item is final and should not be modified.
     *
     * @return Potion Item Custom
     */
    @Getter
    private final ItemStack item;
    /**
     * Gets the potion mix definition used by the Paper brewer.
     */
    @Getter
    private final PotionMix potionMix;

    /**
     * Creates a new base custom potion.
     *
     * @param plugin        the owning plugin instance
     * @param internalName  the unique internal name
     * @param displayName   the display name of the item (can be {@link Component#empty()})
     * @param descriptions  descriptive lines for the lore
     */
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

    /**
     * Creates a {@link RecipeChoice} based on a predicate and example input items.
     * This method is utilized to define a custom recipe input for a potion.
     *
     * @param stackPredicate the predicate to test {@link ItemStack} instances, determining whether they match the recipe criteria
     * @param items          example {@link ItemStack} inputs to be added for reference or visualization
     * @return a {@link RecipeChoice} object representing the choice created from the given predicate
     */
    public RecipeChoice createRecipeInputPredicateChoice(final Predicate<? super ItemStack> stackPredicate, ItemStack... items) {
        this.recipeInputExamples.addAll(List.of(items));
        return PotionMix.createPredicateChoice(stackPredicate);
    }

    /**
     * Creates a {@link RecipeChoice} for the recipe ingredient based on a predicate and example input items.
     * This method is used to define a custom ingredient for a potion recipe.
     *
     * @param stackPredicate the predicate to evaluate {@link ItemStack} instances, determining whether they match the recipe criteria
     * @param items          example {@link ItemStack} inputs to be added for reference or visualization
     * @return a {@link RecipeChoice} object representing the choice created from the provided predicate
     */
    public RecipeChoice createRecipeIngredientPredicateChoice(final Predicate<? super ItemStack> stackPredicate, ItemStack... items) {
        this.recipeIngredientExamples.addAll(List.of(items));
        return PotionMix.createPredicateChoice(stackPredicate);
    }

    /**
     * Creates the potion mix to be registered in the brewing system.
     *
     * @return the prepared potion mix
     */
    private PotionMix createPotionMix() {
        return new PotionMix(this.getNamespaceKey(), this.item, this.recipeInput, this.recipeIngredient);
    }

    public List<ItemStack> getRecipeInputExamples() {
        if (this.recipeInputExamples.isEmpty()) {
            return RecipeChoiceUtils.getRecipeChoiceItemStacks(this.getPotionMix().getInput());
        }

        return this.recipeInputExamples;
    }

    public List<ItemStack> getRecipeIngredientExamples() {
        if (this.recipeIngredientExamples.isEmpty()) {
            return RecipeChoiceUtils.getRecipeChoiceItemStacks(this.getPotionMix().getIngredient());
        }

        return this.recipeIngredientExamples;
    }

    /**
     * Creates a brewing stand view to display the recipe to the player.
     *
     * @param player the player for whom the view is created
     * @return a brewing stand view, or {@code null} if it could not be created
     */
    @Nullable
    public InventoryView createDisplayCraft(Player player) {
        Component titleInventoryView = Component.translatable("dlce.potions.recipe.display", this.getItem().displayName());
        BrewingStandView brewingStandView = MenuType.BREWING_STAND.create(player, titleInventoryView);

        List<ItemStack> inputVariants = this.getRecipeInputExamples();
        List<ItemStack> ingredientVariants = this.getRecipeIngredientExamples();

        inputVariants.forEach(itemStack -> {
            System.out.println("Input: " + itemStack.displayName());
        });

        ingredientVariants.forEach(itemStack -> {
            System.out.println("Ingredient: " + itemStack.displayName());
        });

        if (!inputVariants.isEmpty() || !ingredientVariants.isEmpty()) {
            new BukkitRunnable() {
                int tick = 0;

                @Override
                public void run() {
                    if (brewingStandView.getTopInventory().getViewers().isEmpty()) {
                        LoggerUtils.debug("Player " + player.getName() + " closed the brewing view, remove animation for choices recipes");
                        this.cancel();
                        return;
                    }

                    if (!inputVariants.isEmpty()) {
                        ItemStack inputItem = inputVariants.get((tick / 20) % inputVariants.size());
                        for (int basePos = 0; basePos < 3; basePos++) {
                            brewingStandView.setItem(basePos, inputItem);
                        }
                    }

                    if (!ingredientVariants.isEmpty()) {
                        ItemStack ingredientItem = ingredientVariants.get((tick / 20) % ingredientVariants.size());
                        brewingStandView.setItem(3, ingredientItem);
                    }

                    tick += 20;
                }
            }.runTaskTimer(this.instance, 0L, 20L);
        }

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

    /**
     * Registers the potion mix into the Bukkit/Paper PotionBrewer.
     */
    public void registerPotionMix() {
        LoggerUtils.info("Adding PotionMix " + this.getKey());
        Bukkit.getPotionBrewer().addPotionMix(this.getPotionMix());
    }

    /**
     * Unregisters the potion mix from the PotionBrewer.
     */
    public void unRegisterPotionMix() {
        LoggerUtils.info("Removing PotionMix " + this.getKey());
        Bukkit.getPotionBrewer().removePotionMix(this.getNamespaceKey());
    }

}
