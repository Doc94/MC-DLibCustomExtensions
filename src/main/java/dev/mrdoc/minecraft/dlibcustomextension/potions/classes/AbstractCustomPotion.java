package dev.mrdoc.minecraft.dlibcustomextension.potions.classes;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;

/**
 * Abstract class representing a custom potion with listener capabilities.
 */
@NullMarked
public abstract non-sealed class AbstractCustomPotion extends AbstractBaseCustomPotion implements Listener {

    /**
     * Constructs a new AbstractCustomPotion.
     *
     * @param customPotionBuilder the builder containing potion configuration
     */
    public AbstractCustomPotion(CustomPotionBuilder customPotionBuilder) {
        super(customPotionBuilder.getPlugin(), customPotionBuilder.getInternalName(), customPotionBuilder.getDisplayName(), customPotionBuilder.getDescriptions());
        Bukkit.getServer().getPluginManager().registerEvents(this, customPotionBuilder.getPlugin());
        customPotionBuilder.getPlugin().getSLF4JLogger().info("Potion {} registered", this.getKey().toString());
    }
}
