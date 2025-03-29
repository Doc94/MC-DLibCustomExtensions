package me.mrdoc.minecraft.dlibcustomextension.items.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
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

    private final Plugin plugin;
    private final String internalName;
    private final Component displayName;
    @Nullable
    private CustomItemRarity rarity = null;
    private boolean isSpecial = false;
    @Nullable
    private Key itemModel = null;
    private List<InventoryType> inventoryTypes = new ArrayList<>();
    private List<Component> descriptions = new ArrayList<>();

    private CustomItemBuilder(String internalName, Component displayName) {
        this(DLibCustomExtensionManager.getPluginInstance(), internalName, displayName);
    }

    private CustomItemBuilder(Plugin plugin, String internalName, Component displayName) {
        this.plugin = plugin;
        this.internalName = internalName;
        this.displayName = displayName;
    }

    public CustomItemBuilder rarity(CustomItemRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public CustomItemBuilder special() {
        return this.special(true);
    }

    public CustomItemBuilder special(boolean isSpecial) {
        this.isSpecial = isSpecial;
        return this;
    }

    /**
     * Sets the item model using key.
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

    public CustomItemBuilder inventoryTypes(InventoryType... inventoryTypes) {
        return this.inventoryTypes(Arrays.asList(inventoryTypes));
    }

    public CustomItemBuilder inventoryTypes(List<InventoryType> inventoryTypes) {
        this.inventoryTypes = inventoryTypes;
        return this;
    }

    public CustomItemBuilder description(Component... descriptions) {
        return this.description(Arrays.asList(descriptions));
    }

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
