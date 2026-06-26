package dev.mrdoc.minecraft.dlibcustomextension.potions.annotations;

import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.AbstractCustomPotion;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for custom potion class.
 * <br>
 * Classes with this annotation are manage to load.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomPotionContainer {

    /**
     * Path for save classes using this annotation.
     */
    String ANNOTATION_PATH = "dev.mrdoc.minecraft.dlibcustomextension.potions.annotations.CustomPotionContainer";

    /**
     * An array of dependency potions.
     *
     * @return the class potions
     */
    Class<? extends AbstractCustomPotion>[] depends() default {};

    /**
     * Sets a strong dependency where true means this fails if the dependency fail to load.
     *
     * @return {@code true} if this is a strong dependency
     */
    boolean strongDependency() default true;

}
