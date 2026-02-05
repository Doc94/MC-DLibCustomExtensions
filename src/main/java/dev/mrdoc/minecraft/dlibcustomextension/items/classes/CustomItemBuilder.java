package dev.mrdoc.minecraft.dlibcustomextension.items.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Getter
public class CustomItemBuilder {

    /**
     * The plugin instance.
     *
     * @return the plugin
     */
    private final Plugin plugin;
    /**
     * The unique internal name.
     *
     * @return the internal name
     */
    private final String internalName;
    /**
     * The display name of the item.
     *
     * @return the display name
     */
    private final Component displayName;
    @Nullable
    private CustomItemRarity rarity = null;
    /**
     * The special flag for the item.
     *
     * @return {@code true} if special
     */
    private boolean isSpecial = false;
    /**
     * The auto-discovery flag for the recipe.
     *
     * @return {@code true} if auto-discover
     */
    private boolean autoDiscoverRecipe = true;
    @Nullable
    private Key itemModel = null;
    /**
     * The inventory types where the item is permitted.
     *
     * @return the list of inventory types
     */
    private List<InventoryType> inventoryTypes = new ArrayList<>();
    /**
     * The descriptive lines for the item lore.
     *
     * @return the list of descriptions
     */
    private List<Component> descriptions = new ArrayList<>();

    /**
     * Constructs a new CustomItemBuilder with the default plugin instance.
     *
     * @param internalName the unique internal name
     * @param displayName  the display name
     */
    private CustomItemBuilder(String internalName, Component displayName) {
        this(DLibCustomExtensionManager.getPluginInstance(), internalName, displayName);
    }

    /**
     * Constructs a new CustomItemBuilder.
     *
     * @param plugin       the plugin instance
     * @param internalName the unique internal name
     * @param displayName  the display name
     */
    private CustomItemBuilder(Plugin plugin, String internalName, Component displayName) {
        this.plugin = plugin;
        this.internalName = internalName;
        this.displayName = displayName;
    }

    /**
     * Sets the rarity for the custom item.
     *
     * @param rarity the custom item rarity to set
     * @return the updated instance of the CustomItemBuilder
     */
    public CustomItemBuilder rarity(CustomItemRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    /**
     * Marks the custom item as special.
     *
     * @return the updated instance of the CustomItemBuilder
     */
    public CustomItemBuilder special() {
        return this.special(true);
    }

    /**
     * Sets whether the custom item is marked as special.
     *
     * @param isSpecial a boolean value indicating if the custom item should be marked as special
     * @return the updated instance of the CustomItemBuilder
     */
    public CustomItemBuilder special(boolean isSpecial) {
        this.isSpecial = isSpecial;
        return this;
    }

    /**
     * Sets whether the item's recipe should be auto-discovered.
     *
     * @param autoDiscover a boolean value indicating if the item should be auto-discovered
     * @return the updated instance of the CustomItemBuilder
     */
    public CustomItemBuilder autoDiscoverRecipe(boolean autoDiscover) {
        this.autoDiscoverRecipe = autoDiscover;
        return this;
    }

    /**
     * Sets the item model using a key.
     * <br>
     * <b>Note:</b> the namespace is get from the instance using this lib.
     *
     * @param itemModelKey the key
     * @return the builder
     */
    public CustomItemBuilder itemModel(String itemModelKey) {
        return this.itemModel(Key.key(DLibCustomExtensionManager.getPluginNamespace(), itemModelKey));
    }

    /**
     * Sets the item model using key.
     *
     * @param itemModelKey the key
     * @return the builder
     */
    public CustomItemBuilder itemModel(Key itemModelKey) {
        this.itemModel = itemModelKey;
        return this;
    }

    /**
     * Sets the inventory types where the item is permitted.
     *
     * @param inventoryTypes the inventory types
     * @return the builder
     */
    public CustomItemBuilder inventoryTypes(InventoryType... inventoryTypes) {
        return this.inventoryTypes(Arrays.asList(inventoryTypes));
    }

    /**
     * Sets the inventory types where the item is permitted.
     *
     * @param inventoryTypes the inventory types
     * @return the builder
     */
    public CustomItemBuilder inventoryTypes(List<InventoryType> inventoryTypes) {
        this.inventoryTypes = inventoryTypes;
        return this;
    }

    /**
     * Sets the descriptive lines for the item lore.
     *
     * @param descriptions the descriptions
     * @return the builder
     */
    public CustomItemBuilder description(Component... descriptions) {
        return this.description(Arrays.asList(descriptions));
    }

    /**
     * Sets the descriptive lines for the item lore.
     *
     * @param descriptions the descriptions
     * @return the builder
     */
    public CustomItemBuilder description(List<Component> descriptions) {
        this.descriptions = descriptions;
        return this;
    }

    /**
     * Create a new builder for {@link AbstractCustomItem}.
     *
     * @param internalName the unique and internal name to be used
     * @param displayName the name to display
     * @return the builder
     */
    public static CustomItemBuilder create(String internalName, Component displayName) {
        return new CustomItemBuilder(internalName, displayName);
    }

    /**
     * Create a new builder for {@link AbstractCustomItem}.
     *
     * @param plugin the plugin instance
     * @param internalName the unique and internal name to be used
     * @param displayName the name to display
     * @return the builder
     */
    public static CustomItemBuilder create(JavaPlugin plugin, String internalName, Component displayName) {
        return new CustomItemBuilder(plugin, internalName, displayName);
    }
}
