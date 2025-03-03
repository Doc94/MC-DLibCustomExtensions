package me.mrdoc.minecraft.dlibcustomextension.enchantments.classes;

import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import me.mrdoc.minecraft.dlibcustomextension.enchantments.CustomEnchantmentManager;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.ApiStatus;

@Getter
public abstract sealed class AbstractBaseCustomEnchantment permits AbstractCustomEnchantment {

    private final Key key;
    private final String name;
    private final Set<TagKey<Enchantment>> tagsEnchantments = new HashSet<>();
    private final Set<TagEntry<ItemType>> tagsItemTypes = new HashSet<>();

    @ApiStatus.Internal
    public AbstractBaseCustomEnchantment(String name) {
        this(name, Set.of(), Set.of());
    }

    @ApiStatus.Internal
    public AbstractBaseCustomEnchantment(String name, Set<TagEntry<ItemType>> itemsValidForEnchantment) {
        this(name, itemsValidForEnchantment, Set.of());
    }

    @ApiStatus.Internal
    public AbstractBaseCustomEnchantment(String name, Set<TagEntry<ItemType>> itemsValidForEnchantment, Set<TagKey<Enchantment>> enchantmentsTagsToAdd) {
        String keyName = CustomEnchantmentManager.ENCHANTMENT_PREFIX.concat(name);
        this.key = Key.key(keyName);
        this.name = name;
        this.tagsItemTypes.addAll(itemsValidForEnchantment);
        this.tagsEnchantments.addAll(enchantmentsTagsToAdd);
    }

}
