package dev.mrdoc.minecraft.test.items;

import dev.mrdoc.minecraft.dlibcustomextension.items.annotations.CustomItemContainer;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.CustomItemBuilder;
import dev.mrdoc.minecraft.test.Core;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

@CustomItemContainer
public class ItemEnderBundle extends AbstractCustomItem {

    public ItemEnderBundle() {
        super(CustomItemBuilder.create(Core.getInstance(), "ender_bundle", Component.text("Ender Bundle", TextColor.fromHexString("#316364")))
                .special()
                .inventoryTypes(InventoryType.WORKBENCH)
                .description(Component.text("Una bolsa con poderes del Ender", NamedTextColor.WHITE))
        );
    }

    @Override
    public ItemStack createItem() {
        ItemStack itemStack = ItemType.BUNDLE.createItemStack();
        itemStack.unsetData(DataComponentTypes.BUNDLE_CONTENTS);
        return itemStack;
    }

    @Override
    public Recipe createRecipe() {
        final ShapedRecipe recipe = new ShapedRecipe(this.getNamespacedKey(), this.getItem());
        recipe.shape("AAA", "ABA", "CCC");
        recipe.setIngredient('A', RecipeChoice.itemType(ItemType.OBSIDIAN));
        recipe.setIngredient('B', RecipeChoice.itemType(ItemType.ENDER_EYE));
        recipe.setIngredient('C', RecipeChoice.itemType(ItemType.NETHERITE_INGOT));

        return recipe;
    }

    @Override
    protected void onPlayerConsumeItem(final PlayerItemConsumeEvent event) {
    }

    @Override
    protected void onPlayerDropItem(final PlayerDropItemEvent event) {
    }

    @Override
    protected void onEntityPickupItem(final EntityPickupItemEvent event) {
    }

    @Override
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (event.getAction().isRightClick()) {
            player.playSound(Sound.sound().type(org.bukkit.Sound.BLOCK_ENDER_CHEST_OPEN).volume(1).pitch(1.2f).build());
            player.openInventory(player.getEnderChest());
        }
    }

    @Override
    protected void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
    }

    @Override
    public void onItemReplace(final PlayerDropItemEvent event, final ItemStack itemReplace) {
    }
}
