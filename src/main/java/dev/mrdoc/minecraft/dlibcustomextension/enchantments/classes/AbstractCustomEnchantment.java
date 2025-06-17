package dev.mrdoc.minecraft.dlibcustomextension.enchantments.classes;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import java.util.function.Consumer;
import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;

/**
 * Basic class for implementation of custom enchantments
 */
public abstract non-sealed class AbstractCustomEnchantment extends AbstractBaseCustomEnchantment implements Listener {

    /**
     * Constructor
     *
     * @param customEnchantmentBuilder the builder
     */
    public AbstractCustomEnchantment(CustomEnchantmentBuilder customEnchantmentBuilder) {
        super(customEnchantmentBuilder);
    }

    /**
     * Register the listeners for this enchantment
     *
     * @param plugin a plugin instance
     */
    public void registerListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Get the key tag for items valid to this enchantment.
     *
     * @return a Key
     */
    final public Key getEnchantableKey() {
        String enchantmentKey = DLibCustomExtensionManager.getPluginNamespace().concat(":enchantable/").concat(this.getName());
        return Key.key(enchantmentKey);
    }

    /**
     * Get the key tag for primary items valid to this enchantment.
     * <br>
     * <b>Note:</b> Primary Items are items can appear in vanilla with this enchantment.
     *
     * @return a Key
     */
    final public Key getEnchantablePrimaryKey() {
        String enchantmentKey = DLibCustomExtensionManager.getPluginNamespace().concat(":enchantable/primary_").concat(this.getName());
        return Key.key(enchantmentKey);
    }

    /**
     * Get the Bukkit enchantment for being used.
     *
     * @return bukkit enchantment
     */
    public Enchantment getEnchantment() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(this.getKey());
    }

    /**
     * Generate a book with this enchantment.
     * <br>
     * <b>Note:</b> the level is the min available.
     *
     * @return an {@link ItemStack} of {@link org.bukkit.inventory.ItemType#ENCHANTED_BOOK} with this enchantment.
     */
    public ItemStack generateEnchantmentBook() {
        return this.generateEnchantmentBook(this.getEnchantment().getStartLevel());
    }

    /**
     * Generate a book with this enchantment.
     * <br>
     * <b>Note:</b> the level is the min available.
     *
     * @param level the level of enchantment
     * @return an {@link ItemStack} of {@link org.bukkit.inventory.ItemType#ENCHANTED_BOOK} with this enchantment.
     */
    public ItemStack generateEnchantmentBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.editMeta(EnchantmentStorageMeta.class, enchantmentStorageMeta -> enchantmentStorageMeta.addStoredEnchant(this.getEnchantment(), level, true));
        return book;
    }

    /**
     * Get the TypeKey for this enchantment.
     *
     * @return a TypeKey
     */
    public TypedKey<Enchantment> getTypedKey() {
        return TypedKey.create(RegistryKey.ENCHANTMENT, this.getKey());
    }

    /**
     * Util method to define and get the builder for EnchantmentRegistryEntry.
     * <br>
     * With this builder are defined all parameters for use this enchantment like:
     * <ul>
     *     <li>Costs</li>
     *     <li>Levels</li>
     *     <li>Exclusive enchantments</li>
     *     <li>Items valid</li>
     *     <li>Items that can have this enchantment in generation</li>
     * </ul>
     *
     * @param registryFreezeEvent the event for register the enchantment
     * @return a consumer builder for EnchantmentRegistryEntry
     */
    public Consumer<EnchantmentRegistryEntry.Builder> generateConsumerEREB(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry. Builder> registryFreezeEvent) {
        return builder -> {
            builder.description(this.getDisplayName());
            builder.weight(this.getWeight());
            builder.maxLevel(this.getMaxLevel());
            builder.minimumCost(this.getMinimumCost());
            builder.maximumCost(this.getMaximumCost());
            builder.anvilCost(this.getAnvilCost());
            builder.activeSlots(this.getActiveSlots());
            builder.exclusiveWith(this.getExclusiveWith());
            builder.supportedItems(registryFreezeEvent.getOrCreateTag(ItemTypeTagKeys.create(this.getEnchantableKey())));
            if (this.useSupportedItemsForPrimaryItems()) {
                builder.primaryItems(null);
            } else if(!this.getTagsItemPrimaryTypes().isEmpty()) {
                builder.primaryItems(registryFreezeEvent.getOrCreateTag(ItemTypeTagKeys.create(this.getEnchantablePrimaryKey())));
            }
        };
    }

}
