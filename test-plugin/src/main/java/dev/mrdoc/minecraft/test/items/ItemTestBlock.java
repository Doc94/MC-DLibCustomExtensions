package dev.mrdoc.minecraft.test.items;

import dev.mrdoc.minecraft.dlibcustomextension.items.annotations.CustomItemContainer;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.CustomItemBuilder;
import dev.mrdoc.minecraft.test.Core;
import dev.mrdoc.minecraft.test.TestCategories;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

/**
 * Item based on a block, used to test how block items look in the recipe browser.
 */
@CustomItemContainer
public class ItemTestBlock extends AbstractCustomItem {

    public ItemTestBlock() {
        super(CustomItemBuilder.create(Core.getInstance(), "test_block", Component.text("Test Block", TextColor.fromHexString("#c06bff")))
                .category(TestCategories.BLOCKS)
                .description(Component.text("Item de bloque para probar el navegador de recetas", NamedTextColor.WHITE))
        );
    }

    @Override
    public ItemStack createItem() {
        return ItemType.LODESTONE.createItemStack();
    }

    @Override
    public Recipe createRecipe() {
        final ShapedRecipe recipe = new ShapedRecipe(this.getNamespacedKey(), this.getItem());
        recipe.shape("AAA", "ABA", "AAA");
        recipe.setIngredient('A', RecipeChoice.itemType(ItemType.CHISELED_STONE_BRICKS));
        recipe.setIngredient('B', RecipeChoice.itemType(ItemType.NETHERITE_INGOT));
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
    }

    @Override
    protected void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
    }

    @Override
    public void onItemReplace(final PlayerDropItemEvent event, final ItemStack itemReplace) {
    }
}
