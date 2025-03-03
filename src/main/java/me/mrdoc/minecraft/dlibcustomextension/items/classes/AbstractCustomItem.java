package me.mrdoc.minecraft.dlibcustomextension.items.classes;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import me.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
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

public abstract non-sealed class AbstractCustomItem extends AbstractBaseCustomItem implements Listener {

    public AbstractCustomItem(CustomItemBuilder customItemBuilder) {
        super(customItemBuilder.getPlugin(), customItemBuilder.getInternalName(), customItemBuilder.getDisplayName(), customItemBuilder.getRarity(), customItemBuilder.isSpecial(), customItemBuilder.getItemModel(), customItemBuilder.getInventoryTypes(), customItemBuilder.getDescriptions());
        Bukkit.getServer().getPluginManager().registerEvents(this, customItemBuilder.getPlugin());
        LoggerUtils.info("Item registered " + this.getRecipeNamespace().toString());
    }

    @Override
    public void unRegisterRecipe() {
        super.unRegisterRecipe();
        HandlerList.unregisterAll(this);
    }

    /**
     * This fired when the Material of item match (you need add more conditionals for any cases)
     */
    protected abstract void onConsumeMaterialItem(PlayerItemConsumeEvent event);

    protected abstract void onPlayerDropItem(PlayerDropItemEvent event);

    protected abstract void onEntityPickupItem(EntityPickupItemEvent event);

    public abstract void onPlayerInteract(PlayerInteractEvent event);

    protected abstract void onPlayerInteractEntity(PlayerInteractEntityEvent event);

    public abstract void onItemReplace(PlayerDropItemEvent event, ItemStack itemReplace);

    @EventHandler
    public void playerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand().toString().contains("HAND")) {
            if (this.isItem(event.getPlayer().getInventory().getItemInMainHand())) {
                onPlayerInteractEntity(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.PHYSICAL)) {
            if (event.getItem() != null && this.isItem(event.getItem())) {
                onPlayerInteract(event);
            }
        }
    }

    @EventHandler
    public void entityPickupItem(EntityPickupItemEvent event) {
        if (this.isItem(event.getItem().getItemStack())) {
            onEntityPickupItem(event);
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
            onItemReplace(event, itemReplace);
            if (event.isCancelled()) {
                return;
            }
            onPlayerDropItem(event);
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        ItemStack itemConsume = event.getItem();

        if (this.isItem(itemConsume)) {
            onConsumeMaterialItem(event);
        }

    }

}
