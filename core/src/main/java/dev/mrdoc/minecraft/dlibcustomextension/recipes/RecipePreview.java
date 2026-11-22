package dev.mrdoc.minecraft.dlibcustomextension.recipes;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.AbstractCustomPotion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

/**
 * Opens read-only recipe previews of custom items and potions and protects them from any interaction.
 */
public final class RecipePreview implements Listener {

    private static final String TAG_INVENTORY_PREVIEW = "DLCE_RECIPE_PREVIEW";

    private static boolean registered = false;

    private RecipePreview() {
    }

    /**
     * Registers the listeners that protect the preview inventories. Safe to call multiple times.
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        Bukkit.getPluginManager().registerEvents(new RecipePreview(), DLibCustomExtensionManager.getPluginInstance());
    }

    /**
     * Opens the recipe preview of a custom item.
     *
     * @param player the player who will see the preview
     * @param item   the custom item
     * @return {@code true} if the preview was opened
     */
    public static boolean open(Player player, AbstractCustomItem item) {
        if (item.getRecipe() == null) {
            player.sendMessage(Component.translatable("dlce.commands.displayitemcustom.failed.recipe_not_found", item.getItem().displayName()).color(NamedTextColor.RED));
            return false;
        }
        InventoryView view = item.createDisplayCraft(player);
        if (view == null) {
            player.sendMessage(Component.translatable("dlce.commands.displayitemcustom.failed.craft_inventory_invalid", item.getItem().displayName()).color(NamedTextColor.RED));
            return false;
        }
        show(player, view);
        return true;
    }

    /**
     * Opens the recipe preview of a custom potion.
     *
     * @param player the player who will see the preview
     * @param potion the custom potion
     * @return {@code true} if the preview was opened
     */
    public static boolean open(Player player, AbstractCustomPotion potion) {
        InventoryView view = potion.createDisplayCraft(player);
        if (view == null) {
            player.sendMessage(Component.translatable("dlce.commands.displaypotioncustom.failed.craft_inventory_invalid", potion.getItem().displayName()).color(NamedTextColor.RED));
            return false;
        }
        show(player, view);
        return true;
    }

    private static void show(Player player, InventoryView view) {
        register();
        player.openInventory(view);
        // Added after opening because replacing a view fires the close event, which removes the tag
        player.getScoreboardTags().add(TAG_INVENTORY_PREVIEW);
    }

    private static boolean isInPreview(HumanEntity entity) {
        return entity.getScoreboardTags().contains(TAG_INVENTORY_PREVIEW);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isInPreview(event.getPlayer())) {
            event.getInventory().clear();
            event.getPlayer().getScoreboardTags().remove(TAG_INVENTORY_PREVIEW);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getViewers().stream().anyMatch(RecipePreview::isInPreview)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getViewers().stream().anyMatch(RecipePreview::isInPreview)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getViewers().stream().anyMatch(RecipePreview::isInPreview)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRecipeBookClick(PlayerRecipeBookClickEvent event) {
        if (isInPreview(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.translatable("dlce.commands.displayitemcustom.failed.recipe_change_not_allowed").color(NamedTextColor.RED));
        }
    }
}
