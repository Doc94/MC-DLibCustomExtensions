package dev.mrdoc.minecraft.dlibcustomextension.recipes;

/**
 * Defines what happens when an entry of the {@link RecipeBrowser} is selected.
 */
public enum SelectMode {

    /**
     * Opens a detail dialog with the item and its description, from where the recipe can be opened.
     */
    DETAIL,

    /**
     * Opens the recipe preview directly.
     */
    RECIPE
}
