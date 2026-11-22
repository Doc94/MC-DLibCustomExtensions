package dev.mrdoc.minecraft.dlibcustomextension.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.object.ObjectContents;
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
 * @param icon   the optional sprite shown before the name, not inherited by sub categories
 * @param atlas  the atlas of the sprite, {@code null} for the default one
 */
public record RecipeCategory(Key key, Component name, @Nullable RecipeCategory parent, @Nullable Key icon, @Nullable Key atlas) {

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
     * Creates a category without icon.
     *
     * @param key    the unique key
     * @param name   the display name
     * @param parent the parent category, or {@code null}
     */
    public RecipeCategory(Key key, Component name, @Nullable RecipeCategory parent) {
        this(key, name, parent, null, null);
    }

    /**
     * Creates a copy of this category with a sprite shown before the name.
     *
     * @param sprite the sprite key, from the default atlas
     * @return the new category
     */
    public RecipeCategory withIcon(Key sprite) {
        return new RecipeCategory(key, name, parent, sprite, null);
    }

    /**
     * Creates a copy of this category with a sprite from a specific atlas shown before the name.
     *
     * @param atlas  the atlas key
     * @param sprite the sprite key
     * @return the new category
     */
    public RecipeCategory withIcon(Key atlas, Key sprite) {
        return new RecipeCategory(key, name, parent, sprite, atlas);
    }

    /**
     * Gets the name to display, with the icon before it when defined.
     *
     * @return the display component
     */
    public Component displayName() {
        if (icon == null) {
            return name;
        }
        ObjectContents sprite = atlas == null ? ObjectContents.sprite(icon) : ObjectContents.sprite(atlas, icon);
        return Component.textOfChildren(Component.object(sprite), Component.space(), name);
    }

    /**
     * Creates a root category.
     *
     * @param key  the unique key
     * @param name the display name
     * @return the category
     */
    public static RecipeCategory of(Key key, Component name) {
        return new RecipeCategory(key, name, null, null, null);
    }

    /**
     * Creates a sub category of this one.
     *
     * @param key  the unique key
     * @param name the display name
     * @return the category
     */
    public RecipeCategory child(Key key, Component name) {
        return new RecipeCategory(key, name, this, null, null);
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
