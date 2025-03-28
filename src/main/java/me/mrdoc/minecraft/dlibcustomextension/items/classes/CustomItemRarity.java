package me.mrdoc.minecraft.dlibcustomextension.items.classes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemRarity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record CustomItemRarity(
        TextColor color,
        String name,
        @Nullable ItemRarity vanillaRarity
) {

    public Component generateTag() {
        return Component.text(this.name, this.color).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

}
