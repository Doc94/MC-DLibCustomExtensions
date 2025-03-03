package me.mrdoc.minecraft.dlibcustomextension.potions.potion.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtension;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class CustomPotionBuilder {

    private final JavaPlugin plugin;
    private final String internalName;
    private final Component displayName;
    private List<Component> descriptions = new ArrayList<>();

    private CustomPotionBuilder(String internalName, Component displayName) {
        this(DLibCustomExtension.getPluginInstance(), internalName, displayName);
    }

    private CustomPotionBuilder(JavaPlugin plugin, String internalName, Component displayName) {
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
    public static CustomPotionBuilder create(JavaPlugin plugin, String internalName, Component displayName) {
        return new CustomPotionBuilder(plugin, internalName, displayName);
    }
}
