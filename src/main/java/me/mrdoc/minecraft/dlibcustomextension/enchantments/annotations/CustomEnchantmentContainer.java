package me.mrdoc.minecraft.dlibcustomextension.enchantments.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomEnchantmentContainer {

    String ANNOTATION_PATH = "me.mrdoc.minecraft.dlibcustomextension.enchantments.annotations.CustomEnchantmentContainer";

}
