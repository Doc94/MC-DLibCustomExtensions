package me.mrdoc.minecraft.dlibcustomextension.items.classes;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

@Getter
public enum CustomItemRarity {
    NONE(NamedTextColor.WHITE, ""),
    COMMON(NamedTextColor.GRAY, "Común"),
    UNCOMMON(NamedTextColor.GREEN, "Poco Común"),
    RARE(NamedTextColor.DARK_BLUE, "Rara"),
    EPIC(NamedTextColor.DARK_PURPLE, "Épico"),
    LEGENDARY(NamedTextColor.GOLD, "Legendaria"),
    MYTHICAL(NamedTextColor.YELLOW, "Mitico"),
    ADMIN(NamedTextColor.DARK_RED, "Admin");

    private final TextColor color;
    private final String name;

    CustomItemRarity(TextColor color, String name) {
        this.color = color;
        this.name = name;
    }

    public Component generateTag() {
        return Component.text(this.name, this.color).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
