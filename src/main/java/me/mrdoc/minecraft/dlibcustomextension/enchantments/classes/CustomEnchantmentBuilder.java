package me.mrdoc.minecraft.dlibcustomextension.enchantments.classes;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import java.util.Set;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;

@Getter
public class CustomEnchantmentBuilder {

    private final String internalName;
    private Set<TagEntry<ItemType>> tagSupportedItems = Set.of();
    private Set<TagKey<Enchantment>> tagEnchantments = Set.of();

    private CustomEnchantmentBuilder(String internalName) {
        this.internalName = internalName;
    }

    /**
     * Set the supported items for this enchantment.
     *
     * @param itemsValidForEnchantment ItemType tag of items
     * @return the builder
     */
    public CustomEnchantmentBuilder supportedItems(TagEntry<ItemType>... itemsValidForEnchantment) {
        return this.supportedItems(Set.of(itemsValidForEnchantment));
    }

    /**
     * Set the supported items for this enchantment.
     *
     * @param itemsValidForEnchantment ItemType tag of items
     * @return the builder
     */
    public CustomEnchantmentBuilder supportedItems(Set<TagEntry<ItemType>> itemsValidForEnchantment) {
        this.tagSupportedItems = itemsValidForEnchantment;
        return this;
    }

    /**
     * Set the tags where this enchantment need to be added.
     * <br>
     * ex: {@link EnchantmentTagKeys#IN_ENCHANTING_TABLE} for the enchantment be available in enchantment table
     *
     * @param enchantmentTags Enchantment tags
     * @return the builder
     */
    public CustomEnchantmentBuilder tags(TagKey<Enchantment>... enchantmentTags) {
        return this.tags(Set.of(enchantmentTags));
    }

    /**
     * Set the tags where this enchantment need to be added.
     * <br>
     * ex: {@link EnchantmentTagKeys#IN_ENCHANTING_TABLE} for the enchantment be available in enchantment table
     *
     * @param enchantmentTags Enchantment tags
     * @return the builder
     */
    public CustomEnchantmentBuilder tags(Set<TagKey<Enchantment>> enchantmentTags) {
        this.tagEnchantments = enchantmentTags;
        return this;
    }

    /**
     * Create a new builder for {@link AbstractCustomEnchantment}.
     *
     * @param internalName the unique and internal name to be used
     * @return the builder
     */
    public static CustomEnchantmentBuilder create(String internalName) {
        return new CustomEnchantmentBuilder(internalName);
    }
}
