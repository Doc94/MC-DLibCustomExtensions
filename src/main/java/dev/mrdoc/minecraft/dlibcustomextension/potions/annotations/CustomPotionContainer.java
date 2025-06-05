package dev.mrdoc.minecraft.dlibcustomextension.potions.annotations;

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

}
