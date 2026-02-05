package dev.mrdoc.minecraft.dlibcustomextension.enchantments.classes;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.experimental.Accessors;
import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.enchantments.CustomEnchantmentManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

/**
 * Class for implementation of custom enchantments
 */
@NullMarked
@Getter
public abstract sealed class AbstractBaseCustomEnchantment permits AbstractCustomEnchantment {

    /**
     * Gets the key for this enchantment.
     *
     * @return key
     */
    private final Key key;
    /**
     * Gets the internal name.
     *
     * @return name
     */
    private final String name;
    /**
     * Gets the display name.
     *
     * @return display name
     */
    private final Component displayName;
    /**
     * Gets the weight of this enchantment used by the weighted random when selecting enchantments.
     *
     * @return the weight value.
     * @see <a href="https://minecraft.wiki/w/Enchanting">https://minecraft.wiki/w/Enchanting</a> for examplary weights.
     */
    private final @Range(from = 1, to = 1024) int weight;
    /**
     * Gets the maximum level this enchantment can have when applied.
     *
     * @return the maximum level.
     */
    private final @Range(from = 1, to = 255) int maxLevel;
    /**
     * Gets the minimum cost needed to enchant an item with this enchantment.
     * <p>
     * Note that a cost is not directly related to the consumed xp.
     *
     * @return the enchantment cost.
     * @see <a href="https://minecraft.wiki/w/Enchanting/Levels">https://minecraft.wiki/w/Enchanting/Levels</a> for
     * examplary costs.
     */
    private final EnchantmentRegistryEntry.EnchantmentCost minimumCost;

    /**
     * Gets the maximum cost allowed to enchant an item with this enchantment.
     * <p>
     * Note that a cost is not directly related to the consumed xp.
     *
     * @return the enchantment cost.
     * @see <a href="https://minecraft.wiki/w/Enchanting/Levels">https://minecraft.wiki/w/Enchanting/Levels</a> for
     * examplary costs.
     */
    private final EnchantmentRegistryEntry.EnchantmentCost maximumCost;
    /**
     * Gets the cost of applying this enchantment using an anvil.
     * <p>
     * Note that this is halved when using an enchantment book, and is multiplied by the level of the enchantment.
     * See <a href="https://minecraft.wiki/w/Anvil_mechanics">https://minecraft.wiki/w/Anvil_mechanics</a> for more
     * information.
     * </p>
     *
     * @return the anvil cost of this enchantment
     */
    private final @Range(from = 0, to = Integer.MAX_VALUE) int anvilCost;

    /**
     * Gets a list of slot groups this enchantment may be active in.
     * <p>
     * If the item enchanted with this enchantment is equipped in a slot not covered by the returned list and its
     * groups, the enchantment's effects, like attribute modifiers, will not activate.
     *
     * @return a list of equipment slot groups.
     * @see Enchantment#getActiveSlotGroups()
     */
    private final List<EquipmentSlotGroup> activeSlots;

    /**
     * Gets the registry key set of enchantments that this enchantment is exclusive with.
     * <p>
     * Exclusive enchantments prohibit the application of this enchantment to an item if they are already present on
     * said item.
     *
     * @return a registry set of enchantments exclusive to this one.
     */
    private final RegistryKeySet<Enchantment> exclusiveWith;
    /**
     * Gets the tags for this enchantment.
     *
     * @return tags
     */
    private final Set<TagKey<Enchantment>> tagsEnchantments = new HashSet<>();
    /**
     * Gets the item types valid for this enchantment.
     *
     * @return entry tags
     */
    private final Set<TagEntry<ItemType>> tagsItemTypes = new HashSet<>();
    /**
     * Gets the primary item types valid for this enchantment.
     * <br>
     * <b>Note:</b> primary are items who can appear with this enchantment (ex: loot)
     *
     * @return entry tags
     */
    private final Set<TagEntry<ItemType>> tagsItemPrimaryTypes = new HashSet<>();
    /**
     * Gets whether primary items are based on valid items.
     *
     * @return {@code true} if {@link #getTagsItemPrimaryTypes()} need to be {@link #getTagsItemTypes()}
     */
    @Accessors(fluent = true)
    private final boolean useSupportedItemsForPrimaryItems;

    /**
     * Constructor
     *
     * @param customEnchantmentBuilder the builder
     */
    @ApiStatus.Internal
    public AbstractBaseCustomEnchantment(CustomEnchantmentBuilder customEnchantmentBuilder) {
        String keyName = DLibCustomExtensionManager.getPluginNamespace().concat(":").concat(customEnchantmentBuilder.getInternalName());
        this.key = Key.key(keyName);
        this.name = customEnchantmentBuilder.getInternalName();
        this.displayName = customEnchantmentBuilder.getDisplayName();
        this.weight = customEnchantmentBuilder.getWeight();
        this.maxLevel = customEnchantmentBuilder.getMaxLevel();
        this.minimumCost = customEnchantmentBuilder.getMinimumCost();
        this.maximumCost = customEnchantmentBuilder.getMaximumCost();
        this.anvilCost = customEnchantmentBuilder.getAnvilCost();
        this.activeSlots = customEnchantmentBuilder.getActiveSlots();
        this.exclusiveWith = customEnchantmentBuilder.getExclusiveWith();
        this.tagsItemTypes.addAll(customEnchantmentBuilder.getTagSupportedItems());
        this.useSupportedItemsForPrimaryItems = customEnchantmentBuilder.getTagPrimaryItems() == null;
        if (!this.useSupportedItemsForPrimaryItems) {
            this.tagsItemPrimaryTypes.addAll(customEnchantmentBuilder.getTagPrimaryItems());
        }
        this.tagsEnchantments.addAll(customEnchantmentBuilder.getTagEnchantments());
    }

    /**
     * Gets if this item is enabled by config.
     * <br>
     * Only apply if config is enabled for use.
     * <br>
     * <b>Note: </b> this not avoid register for avoid minecraft remove this enchantment for not load
     *
     * @return if is enabled
     */
    public boolean isEnabled() {
        return CustomEnchantmentManager.isEnchantmentEnabled(this.getName());
    }

}
