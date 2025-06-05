package dev.mrdoc.minecraft.dlibcustomextension.items.classes;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract non-sealed class AbstractCustomItem extends AbstractBaseCustomItem implements Listener {

    public AbstractCustomItem(CustomItemBuilder customItemBuilder) {
        super(customItemBuilder.getPlugin(), customItemBuilder.getInternalName(), customItemBuilder.getDisplayName(), customItemBuilder.getRarity(), customItemBuilder.isSpecial(), customItemBuilder.getItemModel(), customItemBuilder.getInventoryTypes(), customItemBuilder.getDescriptions());
        Bukkit.getServer().getPluginManager().registerEvents(this, customItemBuilder.getPlugin());
        LoggerUtils.info("Item registered " + this.getRecipeNamespace());
    }

    @Override
    public void unRegisterRecipe() {
        super.unRegisterRecipe();
        HandlerList.unregisterAll(this);
    }

    /**
     * Handles the event when a player consumes an item. This method is triggered when a player
     * in the game consumes/uses a consumable item (like food, potions, etc.) that matches the
     * item type. Implement additional conditions and logic within this method to handle specific
     * behaviors related to item consumption.
     *
     * @param event the PlayerItemConsumeEvent triggered when a player consumes an item
     */
    protected abstract void onPlayerConsumeItem(PlayerItemConsumeEvent event);

    /**
     * Handles the event when a player drops an item. This method is triggered whenever a player
     * in the game drops an item from their inventory. Implement additional conditions and logic
     * within this method to handle specific cases or behaviors related to item drop events.
     *
     * @param event the PlayerDropItemEvent triggered when a player drops an item
     */
    protected abstract void onPlayerDropItem(PlayerDropItemEvent event);

    /**
     * Handles the event when an entity picks up an item. This method is triggered when an entity in the game world
     * interacts with an item by picking it up. Implement additional logic within this method to handle specific
     * behaviors or conditions related to item pickup events.
     *
     * @param event the EntityPickupItemEvent triggered when an entity picks up an item
     */
    protected abstract void onEntityPickupItem(EntityPickupItemEvent event);

    /**
     * Handles player interaction events. This method is triggered when a player interacts
     * with an object in the game world, such as blocks or items. It is designed to handle
     * specific conditions and behaviors based on the interaction context and associated logic.
     *
     * @param event the PlayerInteractEvent triggered when a player interacts with an object
     */
    protected abstract void onPlayerInteract(PlayerInteractEvent event);

    /**
     * Handles the player's interaction with an entity. This method is triggered when a player right-clicks or interacts
     * with an entity while holding an item of the matching type. Additional conditions may need to be implemented
     * based on specific use cases to handle interactions appropriately.
     *
     * @param event the PlayerInteractEntityEvent triggered when a player interacts with an entity
     */
    protected abstract void onPlayerInteractEntity(PlayerInteractEntityEvent event);

    /**
     * Call when the item was replaced by {@link PlayerDropItemEvent}
     *
     * @param event event
     * @param itemReplace new item
     */
    protected abstract void onItemReplace(PlayerDropItemEvent event, ItemStack itemReplace);

    @EventHandler
    public void playerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand().toString().contains("HAND")) {
            if (this.isItem(event.getPlayer().getInventory().getItemInMainHand())) {
                this.onPlayerInteractEntity(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.PHYSICAL)) {
            if (event.getItem() != null && this.isItem(event.getItem())) {
                this.onPlayerInteract(event);
            }
        }
    }

    @EventHandler
    public void entityPickupItem(EntityPickupItemEvent event) {
        if (this.isItem(event.getItem().getItemStack())) {
            this.onEntityPickupItem(event);
        }
    }

    @EventHandler
    public void playerDropItem(PlayerDropItemEvent event) {
        if (this.isItem(event.getItemDrop().getItemStack())) {
            ItemStack itemReplace = event.getItemDrop().getItemStack();
            itemReplace.setData(DataComponentTypes.LORE, this.getItem().getDataOrDefault(DataComponentTypes.LORE, ItemLore.lore().build()));
            itemReplace.setData(DataComponentTypes.ITEM_NAME, this.getItem().getDataOrDefault(DataComponentTypes.ITEM_NAME, this.getItem().displayName()));
            if (this.getItem().hasData(DataComponentTypes.ITEM_MODEL)) {
                itemReplace.setData(DataComponentTypes.ITEM_MODEL, this.getItem().getData(DataComponentTypes.ITEM_MODEL));
            }
            if (this.getItem().hasData(DataComponentTypes.CUSTOM_MODEL_DATA)) {
                itemReplace.setData(DataComponentTypes.CUSTOM_MODEL_DATA, this.getItem().getData(DataComponentTypes.CUSTOM_MODEL_DATA));
            }

            event.getItemDrop().setItemStack(itemReplace); // Replace Item
            this.onItemReplace(event, itemReplace);
            if (event.isCancelled()) {
                return;
            }
            this.onPlayerDropItem(event);
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        ItemStack itemConsume = event.getItem();

        if (this.isItem(itemConsume)) {
            this.onPlayerConsumeItem(event);
        }

    }

}
