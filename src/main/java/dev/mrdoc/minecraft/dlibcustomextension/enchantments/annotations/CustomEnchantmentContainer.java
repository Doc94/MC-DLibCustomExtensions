package dev.mrdoc.minecraft.dlibcustomextension.enchantments.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for custom enchantment class.
 * <br>
 * Classes with this annotation are manage to load.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomEnchantmentContainer {

    /**
     * Path for save classes using this annotation.
     */
    String ANNOTATION_PATH = "dev.mrdoc.minecraft.dlibcustomextension.enchantments.annotations.CustomEnchantmentContainer";

}
