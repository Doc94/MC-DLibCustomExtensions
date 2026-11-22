package dev.mrdoc.minecraft.test;

import dev.mrdoc.minecraft.dlibcustomextension.recipes.RecipeCategory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

/**
 * Example categories for the recipe browser.
 */
public final class TestCategories {

    public static final RecipeCategory TOOLS = RecipeCategory.of(Key.key("dlcetest", "tools"), Component.text("Tools"));
    public static final RecipeCategory STORAGE = TOOLS.child(Key.key("dlcetest", "storage"), Component.text("Storage"));
    // Translatable without translation, shows the name generated from the key
    public static final RecipeCategory BLOCKS = RecipeCategory.of(Key.key("dlcetest", "decoration_blocks"), Component.translatable("dlcetest.category.blocks"));
    public static final RecipeCategory POTIONS = RecipeCategory.of(Key.key("dlcetest", "potions"), Component.text("Potions"));

    private TestCategories() {
    }
}
