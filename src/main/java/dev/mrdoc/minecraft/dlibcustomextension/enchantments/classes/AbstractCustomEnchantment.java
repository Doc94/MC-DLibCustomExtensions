package dev.mrdoc.minecraft.dlibcustomextension.enchantments.classes;

import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryComposeEvent;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PreFlattenTagRegistrar;
import io.papermc.paper.tag.TagEntry;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

/**
 * Basic class for implementation of custom enchantments
 */
@NullMarked
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
     * @return an {@link ItemStack} of {@link ItemType#ENCHANTED_BOOK} with this enchantment.
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
     * @return an {@link ItemStack} of {@link ItemType#ENCHANTED_BOOK} with this enchantment.
     */
    public ItemStack generateEnchantmentBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments(Map.of(this.getEnchantment(), level)));
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
     *     <li>Items that can have this enchantment in a generation</li>
     * </ul>
     *
     * @param registryComposeEvent the event for register the enchantment
     * @return a consumer builder for EnchantmentRegistryEntry
     */
    public Consumer<EnchantmentRegistryEntry.Builder> generateConsumerEREB(RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder> registryComposeEvent) {
        return builder -> {
            builder.description(this.getDisplayName());
            builder.weight(this.getWeight());
            builder.maxLevel(this.getMaxLevel());
            builder.minimumCost(this.getMinimumCost());
            builder.maximumCost(this.getMaximumCost());
            builder.anvilCost(this.getAnvilCost());
            builder.activeSlots(this.getActiveSlots());
            builder.exclusiveWith(this.getExclusiveWith());
            builder.supportedItems(registryComposeEvent.getOrCreateTag(ItemTypeTagKeys.create(this.getEnchantableKey())));
            if (this.useSupportedItemsForPrimaryItems()) {
                builder.primaryItems(null);
            } else if (!this.getTagsItemPrimaryTypes().isEmpty()) {
                builder.primaryItems(registryComposeEvent.getOrCreateTag(ItemTypeTagKeys.create(this.getEnchantablePrimaryKey())));
            }
        };
    }

    public void registerEnchantment(BootstrapContext context) {
        context.getLifecycleManager()
                .registerEventHandler(RegistryEvents.ENCHANTMENT.compose()
                        .newHandler(registryComposeEvent -> registryComposeEvent.registry()
                                .register(this.getTypedKey(), this.generateConsumerEREB(registryComposeEvent))
                        ));

        // Registry in Vanilla TAG
        if (!this.getTagsEnchantments().isEmpty()) {
            LoggerUtils.info("The custom enchantment " + this.getKey().asString() + " has the following tags to be added: " + this.getTagsEnchantments().stream().map(enchantmentTagKey -> enchantmentTagKey.key().asString()).collect(Collectors.joining(", ")));
            context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT), event -> {
                final PreFlattenTagRegistrar<Enchantment> registrar = event.registrar();
                this.getTagsEnchantments().forEach(enchantmentTagKey -> registrar.addToTag(enchantmentTagKey, List.of(TagEntry.valueEntry(this.getTypedKey()))));
            });
        }

        // Registry for items valid to be enchanted with abstractCustomEnchantment
        LoggerUtils.info("The custom enchantment " + this.getKey().asString() + " has the following Supported ItemType available to be added: " + this.getTagsItemTypes().stream().map(itemTypeTagEntry -> itemTypeTagEntry.key().asString()).collect(Collectors.joining(", ")));
        if (this.useSupportedItemsForPrimaryItems()) {
            LoggerUtils.info("The custom enchantment " + this.getKey().asString() + " is going to use the previous list of supported items for Primary ItemType used in natural enchantment");
        }
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM), event -> {
            final PreFlattenTagRegistrar<ItemType> registrar = event.registrar();
            registrar.addToTag(TagKey.create(RegistryKey.ITEM, this.getEnchantableKey()), this.getTagsItemTypes());
        });

        if (!this.getTagsItemPrimaryTypes().isEmpty()) {
            LoggerUtils.info("The custom enchantment " + this.getKey().asString() + " has the following Primary ItemType available to be added: " + this.getTagsItemPrimaryTypes().stream().map(itemTypeTagEntry -> itemTypeTagEntry.key().asString()).collect(Collectors.joining(", ")));
            context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM), event -> {
                final PreFlattenTagRegistrar<ItemType> registrar = event.registrar();
                registrar.addToTag(TagKey.create(RegistryKey.ITEM, this.getEnchantablePrimaryKey()), this.getTagsItemPrimaryTypes());
            });
        }
    }

}
