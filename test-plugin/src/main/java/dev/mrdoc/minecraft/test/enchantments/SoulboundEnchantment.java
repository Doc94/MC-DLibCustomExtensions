package dev.mrdoc.minecraft.test.enchantments;
import dev.mrdoc.minecraft.dlibcustomextension.enchantments.annotations.CustomEnchantmentContainer;
import dev.mrdoc.minecraft.dlibcustomextension.enchantments.classes.AbstractCustomEnchantment;
import dev.mrdoc.minecraft.dlibcustomextension.enchantments.classes.CustomEnchantmentBuilder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.tag.TagEntry;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

@CustomEnchantmentContainer
public class SoulboundEnchantment extends AbstractCustomEnchantment {

    public SoulboundEnchantment() {
        super(CustomEnchantmentBuilder.create("souldbound", Component.text("Soulbound", TextColor.fromHexString("#9e9190")))
                .anvilCost(15)
                .maxLevel(1)
                .weight(1)
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 25))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 25))
                .activeSlots(EquipmentSlotGroup.ANY)
                .exclusiveWith(RegistrySet.keySet(RegistryKey.ENCHANTMENT, EnchantmentKeys.BINDING_CURSE, EnchantmentKeys.VANISHING_CURSE))
                .supportedItems(
                        TagEntry.tagEntry(ItemTypeTagKeys.SPEARS),
                        TagEntry.tagEntry(ItemTypeTagKeys.SWORDS),
                        TagEntry.tagEntry(ItemTypeTagKeys.PICKAXES),
                        TagEntry.tagEntry(ItemTypeTagKeys.AXES),
                        TagEntry.tagEntry(ItemTypeTagKeys.SHOVELS),
                        TagEntry.tagEntry(ItemTypeTagKeys.HOES),
                        TagEntry.tagEntry(ItemTypeTagKeys.HEAD_ARMOR),
                        TagEntry.tagEntry(ItemTypeTagKeys.CHEST_ARMOR),
                        TagEntry.tagEntry(ItemTypeTagKeys.LEG_ARMOR),
                        TagEntry.tagEntry(ItemTypeTagKeys.FOOT_ARMOR),
                        TagEntry.tagEntry(ItemTypeTagKeys.BUNDLES),
                        TagEntry.tagEntry(ItemTypeTagKeys.SHULKER_BOXES),
                        TagEntry.valueEntry(ItemTypeKeys.ELYTRA),
                        TagEntry.valueEntry(ItemTypeKeys.MACE),
                        TagEntry.valueEntry(ItemTypeKeys.TRIDENT),
                        TagEntry.valueEntry(ItemTypeKeys.FISHING_ROD),
                        TagEntry.valueEntry(ItemTypeKeys.BOW),
                        TagEntry.valueEntry(ItemTypeKeys.CROSSBOW),
                        TagEntry.valueEntry(ItemTypeKeys.SHEARS),
                        TagEntry.valueEntry(ItemTypeKeys.SHIELD),
                        TagEntry.valueEntry(ItemTypeKeys.CARVED_PUMPKIN)
                ));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeathListener(PlayerDeathEvent event) {
        final List<ItemStack> dropItems = event.getDrops();
        final List<ItemStack> soulboundItems = new ArrayList<>();
        final Player player = event.getEntity();
        if (event.getKeepInventory()) {
            return;
        }

        for (final ItemStack item : dropItems) {
            if (item.getEnchantmentLevel(this.getEnchantment()) > 0) {
                soulboundItems.add(item);
            }
        }

        if (soulboundItems.isEmpty()) {
            return;
        }

        for (final ItemStack item : soulboundItems) {
            event.getDrops().remove(item);
            event.getItemsToKeep().add(item);
        }
        player.sendMessage(Component.text("Items Found: " + soulboundItems.size(), NamedTextColor.AQUA));
    }
}
