package dev.mrdoc.minecraft.dlibcustomextension.potions.potion.classes;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract non-sealed class AbstractCustomPotion extends AbstractBaseCustomPotion implements Listener {

    public AbstractCustomPotion(CustomPotionBuilder customPotionBuilder) {
        super(customPotionBuilder.getPlugin(), customPotionBuilder.getInternalName(), customPotionBuilder.getDisplayName(), customPotionBuilder.getDescriptions());
        Bukkit.getServer().getPluginManager().registerEvents(this, customPotionBuilder.getPlugin());
        customPotionBuilder.getPlugin().getSLF4JLogger().info("Potion {} registered", this.getPotionNamespace().toString());
    }
}
