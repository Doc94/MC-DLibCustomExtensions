package me.mrdoc.minecraft.dlibcustomextension.potions.potion.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;

@Getter
public class CustomPotionBuilder {

    private final Plugin plugin;
    private final String internalName;
    private final Component displayName;
    private List<Component> descriptions = new ArrayList<>();

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
