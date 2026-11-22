package dev.mrdoc.minecraft.dlibcustomextension.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TextComponent;
import org.jspecify.annotations.Nullable;

/**
 * A category used to group custom items and potions in the {@link RecipeBrowser}.
 * <p>
 * Categories can be nested using a parent. Being immutable, a parent must exist before its children,
 * so cycles cannot be created. The nesting depth is limited to {@link #MAX_DEPTH}.
 * Categories are identified by their {@code key}.
 * </p>
 *
 * @param key    the unique key of the category
 * @param name   the display name, a translatable component without translation uses a name generated from the key
 * @param parent the parent category, or {@code null} for a root category
 */
public record RecipeCategory(Key key, Component name, @Nullable RecipeCategory parent) {

    /**
     * Maximum nesting depth of categories (a root category has depth 1).
     */
    public static final int MAX_DEPTH = 5;

    /**
     * Validates the category and ensures that the name is always displayable.
     *
     * @throws IllegalArgumentException if the nesting is deeper than {@link #MAX_DEPTH}
     */
    public RecipeCategory {
        int depth = 1;
        for (RecipeCategory ancestor = parent; ancestor != null; ancestor = ancestor.parent()) {
            depth++;
        }
        if (depth > MAX_DEPTH) {
            throw new IllegalArgumentException("Category [%s] exceeds the maximum depth of %d".formatted(key.asString(), MAX_DEPTH));
        }

        if (name instanceof TranslatableComponent translatable && translatable.fallback() == null) {
            name = translatable.fallback(humanize(key));
        } else if (name instanceof TextComponent text && text.content().isBlank() && text.children().isEmpty()) {
            name = Component.text(humanize(key));
        }
    }

    /**
     * Creates a root category.
     *
     * @param key  the unique key
     * @param name the display name
     * @return the category
     */
    public static RecipeCategory of(Key key, Component name) {
        return new RecipeCategory(key, name, null);
    }

    /**
     * Creates a sub category of this one.
     *
     * @param key  the unique key
     * @param name the display name
     * @return the category
     */
    public RecipeCategory child(Key key, Component name) {
        return new RecipeCategory(key, name, this);
    }

    /**
     * Gets the chain of categories from the root to this one.
     *
     * @return the path, starting at the root and ending in this category
     */
    public List<RecipeCategory> path() {
        List<RecipeCategory> path = new ArrayList<>();
        for (RecipeCategory current = this; current != null; current = current.parent()) {
            path.addFirst(current);
        }
        return path;
    }

    private static String humanize(Key key) {
        StringBuilder builder = new StringBuilder();
        for (String word : key.value().split("[/_.-]+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return builder.isEmpty() ? key.asString() : builder.toString();
    }
}
