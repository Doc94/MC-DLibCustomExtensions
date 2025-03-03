package me.mrdoc.minecraft.dlibcustomextension.items.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomItemContainer {

    String ANNOTATION_PATH = "me.mrdoc.minecraft.dlibcustomextension.items.annotations.CustomItemContainer";

    /**
     * Indica las clases de las que depende este item
     * @return
     */
    Class<? extends AbstractCustomItem>[] depends() default {};

    boolean strongDependency() default true;

}
