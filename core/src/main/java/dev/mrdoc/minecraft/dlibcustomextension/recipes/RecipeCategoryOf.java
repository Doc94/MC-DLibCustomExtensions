package dev.mrdoc.minecraft.dlibcustomextension.recipes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.kyori.adventure.key.KeyPattern;

/**
 * Declares, in a {@code package-info.java}, the {@link RecipeCategory} of the custom items and potions of that package
 * and its sub packages.
 * <p>
 * The nearest annotated package is the category of the item, and the annotated packages above it
 * are its parent categories. A category set in the builder always has priority.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface RecipeCategoryOf {

    /**
     * The unique key of the category, for example {@code myplugin:tools}.
     *
     * @return the key
     */
    @KeyPattern String key();

    /**
     * The translation key of the display name, empty to use a name generated from the key.
     *
     * @return the translation key
     */
    @KeyPattern String name() default "";

    /**
     * A literal display name in MiniMessage format, without translation support.
     * Has priority over {@link #name()}.
     *
     * @return the display name
     */
    String displayName() default "";

    /**
     * The sprite shown before the name, empty for none.
     *
     * @return the sprite key
     */
    @KeyPattern String icon() default "";

    /**
     * The atlas of the sprite, empty for the default one.
     *
     * @return the atlas key
     */
    @KeyPattern String atlas() default "";
}
