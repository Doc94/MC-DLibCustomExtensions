package me.mrdoc.minecraft.dlibcustomextension.enchantments.classes;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import java.util.function.Consumer;
import me.mrdoc.minecraft.dlibcustomextension.enchantments.CustomEnchantmentManager;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;

public abstract non-sealed class AbstractCustomEnchantment extends AbstractBaseCustomEnchantment implements Listener {

    public AbstractCustomEnchantment(CustomEnchantmentBuilder customEnchantmentBuilder) {
        super(customEnchantmentBuilder.getInternalName(), customEnchantmentBuilder.getTagSupportedItems(), customEnchantmentBuilder.getTagEnchantments());
    }

    public void registerListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    final public Key getEnchantableKey() {
        String enchantmentKey = CustomEnchantmentManager.ENCHANTMENT_PREFIX.concat("enchantable/").concat(this.getName());
        return Key.key(enchantmentKey);
    }

    public Enchantment getEnchantment() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(this.getKey());
    }

    public ItemStack generateEnchantmentBook() {
        return this.generateEnchantmentBook(this.getEnchantment().getStartLevel());
    }

    public ItemStack generateEnchantmentBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.editMeta(EnchantmentStorageMeta.class, enchantmentStorageMeta -> enchantmentStorageMeta.addStoredEnchant(this.getEnchantment(), level, true));
        return book;
    }

    public TypedKey<Enchantment> getTypedKey() {
        return TypedKey.create(RegistryKey.ENCHANTMENT, this.getKey());
    }

    public abstract Consumer<EnchantmentRegistryEntry.Builder> generateConsumerEREB(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry. Builder> registryFreezeEvent);

}
