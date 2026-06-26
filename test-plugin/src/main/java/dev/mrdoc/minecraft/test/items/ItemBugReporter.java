package dev.mrdoc.minecraft.test.items;

import dev.mrdoc.minecraft.dlibcustomextension.items.annotations.CustomItemContainer;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.CustomItemBuilder;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.CustomItemRarity;
import dev.mrdoc.minecraft.test.Core;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.Recipe;
import org.jspecify.annotations.Nullable;

@CustomItemContainer
public class ItemBugReporter extends AbstractCustomItem {

    public ItemBugReporter() {
        super(CustomItemBuilder.create(Core.getInstance(), "bug_reporter", Component.text("Bug Reporter", TextColor.fromHexString("#ff3b33"), TextDecoration.BOLD))
                .special()
                .rarity(new CustomItemRarity(NamedTextColor.GOLD, "WOW", ItemRarity.EPIC))
                .description(Component.text("Un reconocimiento para los bug reporters", NamedTextColor.WHITE))
        );
    }

    @Override
    public ItemStack createItem() {
        final ItemStack itemBugReporter = ItemType.BARRIER.createItemStack();
        itemBugReporter.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(Map.of(Enchantment.FORTUNE, 10)));
        itemBugReporter.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD).build());
        return itemBugReporter;
    }

    @Override
    public @Nullable Recipe createRecipe() {
        return null;
    }

    @Override
    protected void onPlayerConsumeItem(final PlayerItemConsumeEvent event) {
    }

    @Override
    protected void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        event.setCancelled(true);
        player.sendMessage(Component.text("You cannot drop this", NamedTextColor.RED));
    }

    @Override
    protected void onEntityPickupItem(final EntityPickupItemEvent event) {
    }

    @Override
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
        }
    }

    @Override
    protected void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
    }

    @Override
    public void onItemReplace(final PlayerDropItemEvent event, final ItemStack itemReplace) {
    }
}
