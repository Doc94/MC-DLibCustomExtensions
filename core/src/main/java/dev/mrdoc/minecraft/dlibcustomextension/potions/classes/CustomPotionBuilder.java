package dev.mrdoc.minecraft.dlibcustomextension.potions.classes;

import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.recipes.RecipeCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

@Getter
public class CustomPotionBuilder {

    private final Plugin plugin;
    private final String internalName;
    private final Component displayName;
    private List<Component> descriptions = new ArrayList<>();
    private @Nullable RecipeCategory category = null;

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
     * Sets the category used to group the potion in the recipe browser.
     *
     * @param category the category
     * @return the updated builder
     */
    public CustomPotionBuilder category(@Nullable RecipeCategory category) {
        this.category = category;
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
