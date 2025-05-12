package dev.mrdoc.minecraft.dlibcustomextension.items.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;

/**
 * Annotation for custom item class.
 * <br>
 * Classes with this annotation are manage to load.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomItemContainer {

    /**
     * Path for save classes using this annotation.
     */
    String ANNOTATION_PATH = "dev.mrdoc.minecraft.dlibcustomextension.items.annotations.CustomItemContainer";

    /**
     * An array of dependency items.
     *
     * @return the class items
     */
    Class<? extends AbstractCustomItem>[] depends() default {};

    /**
     * Sets a strong dependency where true means this fails if the dependency fail to load.
     *
     * @return {@code true} if this is a strong dependency
     */
    boolean strongDependency() default true;

}
