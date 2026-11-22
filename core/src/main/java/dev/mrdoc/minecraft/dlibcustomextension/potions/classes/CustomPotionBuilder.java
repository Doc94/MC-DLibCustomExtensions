package dev.mrdoc.minecraft.dlibcustomextension.potions.classes;

import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.recipes.RecipeCategory;
import dev.mrdoc.minecraft.dlibcustomextension.recipes.RecipeCategorySource;
import dev.mrdoc.minecraft.dlibcustomextension.recipes.RecipeListing;
import dev.mrdoc.minecraft.dlibcustomextension.recipes.SelectMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

@Getter
public class CustomPotionBuilder {

    private final Plugin plugin;
    private final String internalName;
    private final Component displayName;
    private List<Component> descriptions = new ArrayList<>();
    private RecipeCategorySource categorySource = RecipeCategorySource.PACKAGE;
    private RecipeListing listing = RecipeListing.DEFAULT;

    private CustomPotionBuilder(String internalName, Component displayName) {
        this(DLibCustomExtensionManager.getPluginInstance(), internalName, displayName);
    }

    private CustomPotionBuilder(Plugin plugin, String internalName, Component displayName) {
        this.plugin = plugin;
        this.internalName = internalName;
        this.displayName = displayName;
    }

    public CustomPotionBuilder descriptions(Component... descriptions) {
        this.descriptions = Arrays.asList(descriptions);
        return this;
    }

    /**
     * Sets the category used to group the potion in the recipe browser, ignoring the one declared by the package.
     *
     * @param category the category, or {@code null} to use the one declared by the package
     * @return the updated builder
     */
    public CustomPotionBuilder category(final @Nullable RecipeCategory category) {
        this.categorySource = category == null ? RecipeCategorySource.PACKAGE : RecipeCategorySource.of(category);
        return this;
    }

    /**
     * Sets a new category that is a child of the one declared by the package.
     *
     * @param key  the unique key of the category
     * @param name the display name
     * @return the updated builder
     */
    public CustomPotionBuilder categoryUnderPackage(final Key key, final Component name) {
        this.categorySource = RecipeCategorySource.underPackage(key, name);
        return this;
    }

    /**
     * Sets a sprite shown before the name in the recipe browser list.
     *
     * @param sprite the sprite key from the default atlas
     * @return the updated builder
     */
    public CustomPotionBuilder icon(final Key sprite) {
        this.listing = this.listing.withIcon(sprite);
        return this;
    }

    /**
     * Sets a sprite from a specific atlas shown before the name in the recipe browser list.
     *
     * @param atlas  the atlas key
     * @param sprite the sprite key
     * @return the updated builder
     */
    public CustomPotionBuilder icon(final Key atlas, final Key sprite) {
        this.listing = this.listing.withIcon(atlas, sprite);
        return this;
    }

    /**
     * Sets what happens when the potion is selected in the recipe browser, overriding the mode of the browser.
     *
     * @param mode the select mode
     * @return the updated builder
     */
    public CustomPotionBuilder selectMode(final @Nullable SelectMode mode) {
        this.listing = this.listing.withSelectMode(mode);
        return this;
    }

    /**
     * Sets the lines shown in the recipe browser detail dialog instead of the potion lore.
     *
     * @param lines the lines
     * @return the updated builder
     */
    public CustomPotionBuilder browserDescription(final Component... lines) {
        this.listing = this.listing.withDetail(lines);
        return this;
    }

    /**
     * Create a new builder for {@link AbstractCustomPotion}.
     *
     * @param internalName the unique and internal name to be used
     * @param displayName the name to display
     * @return the builder
     */
    public static CustomPotionBuilder create(String internalName, Component displayName) {
        return new CustomPotionBuilder(internalName, displayName);
    }

    /**
     * Create a new builder for {@link AbstractCustomPotion}.
     *
     * @param plugin the plugin instance
     * @param internalName the unique and internal name to be used
     * @param displayName the name to display
     * @return the builder
     */
    public static CustomPotionBuilder create(Plugin plugin, String internalName, Component displayName) {
        return new CustomPotionBuilder(plugin, internalName, displayName);
    }
}
