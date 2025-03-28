package me.mrdoc.minecraft.dlibcustomextension.enchantments.classes;

import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Getter
public abstract sealed class AbstractBaseCustomEnchantment permits AbstractCustomEnchantment {

    private final Key key;
    private final String name;
    private final Set<TagKey<Enchantment>> tagsEnchantments = new HashSet<>();
    private final Set<TagEntry<ItemType>> tagsItemTypes = new HashSet<>();
    private final Set<TagEntry<ItemType>> tagsItemPrimaryTypes = new HashSet<>();
    @Accessors(fluent = true)
    private final boolean useSupportedItemsForPrimaryItems;

    @ApiStatus.Internal
    public AbstractBaseCustomEnchantment(CustomEnchantmentBuilder customEnchantmentBuilder) {
        String keyName = DLibCustomExtensionManager.getPluginNamespace().concat(":").concat(customEnchantmentBuilder.getInternalName());
        this.key = Key.key(keyName);
        this.name = customEnchantmentBuilder.getInternalName();
        this.tagsItemTypes.addAll(customEnchantmentBuilder.getTagSupportedItems());
        this.useSupportedItemsForPrimaryItems = customEnchantmentBuilder.getTagPrimaryItems() == null;
        if (!this.useSupportedItemsForPrimaryItems) {
            this.tagsItemPrimaryTypes.addAll(customEnchantmentBuilder.getTagPrimaryItems());
        }
        this.tagsEnchantments.addAll(customEnchantmentBuilder.getTagEnchantments());
    }

}
