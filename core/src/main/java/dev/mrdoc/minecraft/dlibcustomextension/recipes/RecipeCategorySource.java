package dev.mrdoc.minecraft.dlibcustomextension.recipes;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Defines how the {@link RecipeCategory} of a custom item or potion is obtained once its class is known.
 */
@FunctionalInterface
public interface RecipeCategorySource {

    /**
     * Uses the category declared with {@link RecipeCategoryOf} in the package of the class, if any.
     */
    RecipeCategorySource PACKAGE = RecipeCategoryResolver::resolve;

    /**
     * Gets the category.
     *
     * @param owner the class of the item or potion
     * @return the category, or {@code null} for none
     */
    @Nullable RecipeCategory resolve(Class<?> owner);

    /**
     * Uses always the given category, ignoring the package.
     *
     * @param category the category
     * @return the source
     */
    static RecipeCategorySource of(RecipeCategory category) {
        return owner -> category;
    }

    /**
     * Uses a new category that is a child of the one declared by the package, or a root category if the package has none.
     *
     * @param key  the unique key of the new category
     * @param name the display name
     * @return the source
     */
    static RecipeCategorySource underPackage(Key key, Component name) {
        return owner -> new RecipeCategory(key, name, RecipeCategoryResolver.resolve(owner));
    }
}
