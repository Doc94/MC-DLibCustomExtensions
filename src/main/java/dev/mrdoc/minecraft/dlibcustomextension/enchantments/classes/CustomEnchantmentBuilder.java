package dev.mrdoc.minecraft.dlibcustomextension.enchantments.classes;

import com.google.common.collect.Lists;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
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
     * The display name.
     *
     * @return display name
     */
    private final Component displayName;
    /**
     * Provides the weight of this enchantment used by the weighted random when selecting enchantments.
     *
     * @return the weight value.
     * @see <a href="https://minecraft.wiki/w/Enchanting">https://minecraft.wiki/w/Enchanting</a> for examplary weights.
     */
    private @Range(from = 1, to = 1024) int weight;
    /**
     * Provides the maximum level this enchantment can have when applied.
     *
     * @return the maximum level.
     */
    private @Range(from = 1, to = 255) int maxLevel;
    /**
     * Provides the minimum cost needed to enchant an item with this enchantment.
     * <p>
     * Note that a cost is not directly related to the consumed xp.
     *
     * @return the enchantment cost.
     * @see <a href="https://minecraft.wiki/w/Enchanting/Levels">https://minecraft.wiki/w/Enchanting/Levels</a> for
     * examplary costs.
     */
    private EnchantmentRegistryEntry.EnchantmentCost minimumCost;

    /**
     * Provides the maximum cost allowed to enchant an item with this enchantment.
     * <p>
     * Note that a cost is not directly related to the consumed xp.
     *
     * @return the enchantment cost.
     * @see <a href="https://minecraft.wiki/w/Enchanting/Levels">https://minecraft.wiki/w/Enchanting/Levels</a> for
     * examplary costs.
     */
    private EnchantmentRegistryEntry.EnchantmentCost maximumCost;
    /**
     * Provides the cost of applying this enchantment using an anvil.
     * <p>
     * Note that this is halved when using an enchantment book, and is multiplied by the level of the enchantment.
     * See <a href="https://minecraft.wiki/w/Anvil_mechanics">https://minecraft.wiki/w/Anvil_mechanics</a> for more
     * information.
     * </p>
     *
     * @return the anvil cost of this enchantment
     */
    private @Range(from = 0, to = Integer.MAX_VALUE) int anvilCost;

    /**
     * Provides a list of slot groups this enchantment may be active in.
     * <p>
     * If the item enchanted with this enchantment is equipped in a slot not covered by the returned list and its
     * groups, the enchantment's effects, like attribute modifiers, will not activate.
     *
     * @return a list of equipment slot groups.
     * @see Enchantment#getActiveSlotGroups()
     */
    private List<EquipmentSlotGroup> activeSlots;

    /**
     * Provides the registry key set of enchantments that this enchantment is exclusive with.
     * <p>
     * Exclusive enchantments prohibit the application of this enchantment to an item if they are already present on
     * said item.
     *
     * @return a registry set of enchantments exclusive to this one.
     */
    private RegistryKeySet<Enchantment> exclusiveWith;
    /**
     * The item types valid for this enchantment.
     *
     * @return entry tags
     */
    private Set<TagEntry<ItemType>> tagSupportedItems = Set.of();
    /**
     * The primary item types valid for this enchantment.
     * <br>
     * <b>Note:</b> primary are items that can appear with this enchantment (ex: loot)
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

    private CustomEnchantmentBuilder(String internalName, Component displayName) {
        this.internalName = internalName;
        this.displayName = displayName;
    }

    /**
     * Configures the weight of this enchantment used by the weighted random when selecting enchantments.
     *
     * @param weight the weight value.
     * @return this builder instance.
     * @see <a href="https://minecraft.wiki/w/Enchanting">https://minecraft.wiki/w/Enchanting</a> for examplary weights.
     */
    @Contract(value = "_ -> this", mutates = "this")
    public CustomEnchantmentBuilder weight(@Range(from = 1, to = 1024) int weight) {
        this.weight = weight;
        return this;
    }

    /**
     * Configures the maximum level this enchantment can have when applied.
     *
     * @param maxLevel the maximum level.
     * @return this builder instance.
     */
    @Contract(value = "_ -> this", mutates = "this")
    public CustomEnchantmentBuilder maxLevel(@Range(from = 1, to = 255) int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    /**
     * Configures the minimum cost needed to enchant an item with this enchantment.
     * <p>
     * Note that a cost is not directly related to the consumed xp.
     *
     * @param minimumCost the enchantment cost.
     * @return this builder instance.
     * @see <a href="https://minecraft.wiki/w/Enchanting/Levels">https://minecraft.wiki/w/Enchanting/Levels</a> for
     * examplary costs.
     */
    @Contract(value = "_ -> this", mutates = "this")
    public CustomEnchantmentBuilder minimumCost(EnchantmentRegistryEntry.EnchantmentCost minimumCost) {
        this.minimumCost = minimumCost;
        return this;
    }

    /**
     * Configures the maximum cost to enchant an item with this enchantment.
     * <p>
     * Note that a cost is not directly related to the consumed xp.
     *
     * @param maximumCost the enchantment cost.
     * @return this builder instance.
     * @see <a href="https://minecraft.wiki/w/Enchanting/Levels">https://minecraft.wiki/w/Enchanting/Levels</a> for
     * examplary costs.
     */
    @Contract(value = "_ -> this", mutates = "this")
    public CustomEnchantmentBuilder maximumCost(EnchantmentRegistryEntry.EnchantmentCost maximumCost) {
        this.maximumCost = maximumCost;
        return this;
    }

    /**
     * Configures the cost of applying this enchantment using an anvil.
     * <p>
     * Note that this is halved when using an enchantment book, and is multiplied by the level of the enchantment.
     * See <a href="https://minecraft.wiki/w/Anvil_mechanics">https://minecraft.wiki/w/Anvil_mechanics</a> for more information.
     * </p>
     *
     * @param anvilCost the anvil cost of this enchantment
     * @return this builder instance.
     * @see Enchantment#getAnvilCost()
     */
    @Contract(value = "_ -> this", mutates = "this")
    public CustomEnchantmentBuilder anvilCost(@Range(from = 0, to = Integer.MAX_VALUE) int anvilCost) {
        this.anvilCost = anvilCost;
        return this;
    }

    /**
     * Configures the list of slot groups this enchantment may be active in.
     * <p>
     * If the item enchanted with this enchantment is equipped in a slot not covered by the returned list and its
     * groups, the enchantment's effects, like attribute modifiers, will not activate.
     *
     * @param activeSlots a list of equipment slot groups.
     * @return this builder instance.
     * @see Enchantment#getActiveSlotGroups()
     */
    @Contract(value = "_ -> this", mutates = "this")
    public CustomEnchantmentBuilder activeSlots(final EquipmentSlotGroup... activeSlots) {
        return this.activeSlots(List.of(activeSlots));
    }

    /**
     * Configures the list of slot groups this enchantment may be active in.
     * <p>
     * If the item enchanted with this enchantment is equipped in a slot not covered by the returned list and its
     * groups, the enchantment's effects, like attribute modifiers, will not activate.
     *
     * @param activeSlots a list of equipment slot groups.
     * @return this builder instance.
     * @see Enchantment#getActiveSlotGroups()
     */
    @Contract(value = "_ -> this", mutates = "this")
    public CustomEnchantmentBuilder activeSlots(Iterable<EquipmentSlotGroup> activeSlots) {
        this.activeSlots = Lists.newArrayList(activeSlots);
        return this;
    }

    /**
     * Configures the registry key set of enchantments that this enchantment is exclusive with.
     * <p>
     * Exclusive enchantments prohibit the application of this enchantment to an item if they are already present on
     * said item.
     * <p>
     * Defaults to an empty set allowing this enchantment to be applied regardless of other enchantments.
     *
     * @param exclusiveWith a registry set of enchantments exclusive to this one.
     * @return this builder instance.
     * @see RegistrySet#keySet(RegistryKey, TypedKey[])
     * @see io.papermc.paper.registry.event.RegistryFreezeEvent#getOrCreateTag(TagKey)
     */
    @Contract(value = "_ -> this", mutates = "this")
    public CustomEnchantmentBuilder exclusiveWith(RegistryKeySet<Enchantment> exclusiveWith) {
        this.exclusiveWith = exclusiveWith;
        return this;
    }

    /**
     * Set the supported items for this enchantment.
     *
     * @param itemsValidForEnchantment ItemType tag of items
     * @return the builder
     */
    @Contract(value = "_ -> this", mutates = "this")
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
    @Contract(value = "_ -> this", mutates = "this")
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
    @Contract(value = "_ -> this", mutates = "this")
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
    @Contract(value = "_ -> this", mutates = "this")
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
    @Contract(mutates = "this")
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
    @Contract(value = "_ -> this", mutates = "this")
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
    @Contract(value = "_ -> this", mutates = "this")
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
    @Deprecated(forRemoval = true)
    public static CustomEnchantmentBuilder create(String internalName) {
        return new CustomEnchantmentBuilder(internalName, Component.text(internalName));
    }

    /**
     * Create a new builder for {@link AbstractCustomEnchantment}.
     *
     * @param internalName the unique and internal name to be used
     * @return the builder
     */
    public static CustomEnchantmentBuilder create(String internalName, Component displayName) {
        return new CustomEnchantmentBuilder(internalName, displayName);
    }
}
