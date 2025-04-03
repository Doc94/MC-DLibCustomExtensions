package me.mrdoc.minecraft.dlibcustomextension.enchantments.classes;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import java.util.Set;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Builder class
 */
@NullMarked
@Getter
public class CustomEnchantmentBuilder {

    /**
     * The internal name.
     *
     * @return name
     */
    private final String internalName;
    /**
     * The item types valid for this enchantment.
     *
     * @return entry tags
     */
    private Set<TagEntry<ItemType>> tagSupportedItems = Set.of();
    /**
     * The primary item types valid for this enchantment.
     * <br>
     * <b>Note:</b> primary are items who can appear with this enchantment (ex: loot)
     *
     * @return entry tags
     */
    @Nullable
    private Set<TagEntry<ItemType>> tagPrimaryItems = Set.of();
    /**
     * The tags for this enchantment
     *
     * @return tags
     */
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
    @SafeVarargs
    public final CustomEnchantmentBuilder supportedItems(TagEntry<ItemType>... itemsValidForEnchantment) {
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
     * Set the primary items for this enchantment.
     * <br>
     * For make primary items just use the {@link #getTagSupportedItems()} just use {@link #useSupportedItemsInPrimaryItems()}
     *
     * @param itemsValidNaturalForEnchantment ItemType tag of items
     * @return the builder
     * @see EnchantmentRegistryEntry.Builder#primaryItems()
     */
    @SafeVarargs
    public final CustomEnchantmentBuilder primaryItems(TagEntry<ItemType>... itemsValidNaturalForEnchantment) {
        return this.primaryItems(Set.of(itemsValidNaturalForEnchantment));
    }

    /**
     * Set the primary items for this enchantment.
     * <br>
     * For make primary items just use the {@link #getTagSupportedItems()} just use {@link #useSupportedItemsInPrimaryItems()}
     *
     * @param itemsValidNaturalForEnchantment ItemType tag of items
     * @return the builder
     * @see EnchantmentRegistryEntry.Builder#primaryItems()
     */
    public CustomEnchantmentBuilder primaryItems(Set<TagEntry<ItemType>> itemsValidNaturalForEnchantment) {
        this.tagPrimaryItems = itemsValidNaturalForEnchantment;
        return this;
    }

    /**
     * Set the primary items use {@link #supportedItems(TagEntry[])} for this enchantment.
     *
     * @return the builder
     * @see EnchantmentRegistryEntry.Builder#primaryItems()
     */
    public CustomEnchantmentBuilder useSupportedItemsInPrimaryItems() {
        this.tagPrimaryItems = null;
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
    @SafeVarargs
    public final CustomEnchantmentBuilder tags(TagKey<Enchantment>... enchantmentTags) {
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
