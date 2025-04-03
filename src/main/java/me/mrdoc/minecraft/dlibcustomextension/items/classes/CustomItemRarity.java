package me.mrdoc.minecraft.dlibcustomextension.items.classes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemRarity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Record for the custom item rarity.
 *
 * @param color color
 * @param name name
 * @param vanillaRarity the vanilla rarity to be used
 */
@NullMarked
public record CustomItemRarity(
        TextColor color,
        String name,
        @Nullable ItemRarity vanillaRarity
) {

    /**
     * Generate the tag display for the rarity.
     *
     * @return a Component
     */
    public Component generateTag() {
        return Component.text(this.name, this.color).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

}
