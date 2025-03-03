package me.mrdoc.minecraft.dlibcustomextension.items;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import me.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.block.Crafter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CrafterInventory;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CustomItemsListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        CustomItemsManager.handleAvailableRecipes(player);
    }

    @EventHandler
    public void onCrafterCraftItem(CrafterCraftEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ItemStack itemClick = event.getResult();
        Optional<AbstractCustomItem> baseItemOptional = CustomItemsManager.getCustomItem(itemClick);

        if (baseItemOptional.isPresent()) {
            AbstractCustomItem customItem = baseItemOptional.get();
            if (!customItem.isEnabled()) {
                event.setCancelled(true);
                return;
            }

            if (event.getBlock().getState() instanceof Crafter crafter) {
                Inventory inventory = crafter.getSnapshotInventory();
                ItemStack[] matrix = inventory.getContents().clone();
                final CraftingRecipe craftingRecipe = ((CraftingRecipe) customItem.getRecipe());
                if (CustomItemRecipeHelper.validateRecipeIngredients(customItem, matrix)) {
                    inventory.setContents(CustomItemRecipeHelper.reduceMatrix(craftingRecipe, matrix).getKey());
                    crafter.update(true);
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ItemStack itemClick = event.getCurrentItem();
        Optional<AbstractCustomItem> baseItemOptional = CustomItemsManager.getCustomItem(itemClick);

        if (baseItemOptional.isPresent()) {
            final AbstractCustomItem customItem = baseItemOptional.get();
            final HumanEntity humanWhoClicked = event.getWhoClicked();

            if (!customItem.isEnabled()) {
                event.setCancelled(true);
                return;
            }
            if (!customItem.getInventoryTypes().contains(event.getInventory().getType())) {
                humanWhoClicked.sendMessage(Component.text("Este crafteo es invalido para el tipo de inventario (Podria ser un bug)", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }

            final CraftingRecipe craftingRecipe = ((CraftingRecipe) customItem.getRecipe());

            if (!CustomItemRecipeHelper.validateRecipeIngredients(customItem, event.getInventory().getMatrix())) {
                humanWhoClicked.sendMessage(Component.text("Este crafteo no es valido segun su receta (Podria ser un bug)", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true); // Necesito cancelar el evento porque sino va a procesar igualmente la matrix 1 vez

            Pair<ItemStack[], Integer> resultCraft = CustomItemRecipeHelper.reduceMatrix(craftingRecipe, event.getInventory().getMatrix(), event.isShiftClick());
            event.getInventory().setMatrix(resultCraft.getLeft()); // Reemplazamos matrix

            final ItemStack itemResult = customItem.getItemForPlayer(resultCraft.getRight());

            if (event.isShiftClick()) {
                humanWhoClicked.getInventory().addItem(itemResult).forEach((integer, itemStack) -> humanWhoClicked.getWorld().dropItem(humanWhoClicked.getLocation(), itemStack));
            } else if (humanWhoClicked.getItemOnCursor().isEmpty()) {
                humanWhoClicked.setItemOnCursor(itemResult);
            } else if (CustomItemsManager.getInternalName(humanWhoClicked.getItemOnCursor()).equals(CustomItemsManager.getInternalName(itemResult))) {
                if (humanWhoClicked.getItemOnCursor().getAmount() >= humanWhoClicked.getItemOnCursor().getMaxStackSize()) {
                    humanWhoClicked.getInventory().addItem(itemResult).forEach((integer, itemStack) -> humanWhoClicked.getWorld().dropItem(humanWhoClicked.getLocation(), itemStack));
                } else {
                    humanWhoClicked.getItemOnCursor().add(itemResult.getAmount());
                }
            } else {
                humanWhoClicked.getInventory().addItem(itemResult).forEach((integer, itemStack) -> humanWhoClicked.getWorld().dropItem(humanWhoClicked.getLocation(), itemStack));
            }
        }

    }

    @EventHandler
    public void preCraftItem(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) {
            return;
        }

        Recipe recipeEvent = event.getRecipe();

        Optional<AbstractCustomItem> baseItemOptional = CustomItemsManager.getCustomItem(recipeEvent.getResult());

        if (baseItemOptional.isPresent()) {
            AbstractCustomItem customItem = baseItemOptional.get();
            final CraftingRecipe craftingRecipe = ((CraftingRecipe) customItem.getRecipe());

            if (!customItem.isEnabled()) {
                event.getViewers().forEach(viewer -> viewer.sendActionBar(Component.text().append(Component.text("El pre-crafteo [", NamedTextColor.RED)).append(customItem.getItem().displayName()).append(Component.text("] no esta activado", NamedTextColor.RED))));
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
            if (!customItem.getInventoryTypes().contains(event.getInventory().getType())) {
                event.getViewers().forEach(viewer -> viewer.sendActionBar(Component.text().append(Component.text("El pre-crafteo [", NamedTextColor.RED)).append(customItem.getItem().displayName()).append(Component.text("] esta siendo procesado en (" + event.getInventory().getType() + ") cuando deberia ser: " + customItem.getInventoryTypes().stream().map(Objects::toString).collect(Collectors.joining(", ")), NamedTextColor.RED))));
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
            if (!CustomItemRecipeHelper.validateRecipeIngredients(customItem, event.getInventory().getMatrix())) {
                event.getViewers().forEach(viewer -> viewer.sendActionBar(Component.text().append(Component.text("El pre-crafteo [", NamedTextColor.RED)).append(customItem.getItem().displayName()).append(Component.text("] no es valido segun su receta (Podria ser un bug)", NamedTextColor.RED))));
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }

            Pair<ItemStack[], Integer> resultCraft = CustomItemRecipeHelper.reduceMatrix(craftingRecipe, event.getInventory().getMatrix(), true);
            Component messagePossibleCraftsComponent = Component.text().append(Component.text("Cantidad Posible a Craftear:", TextColor.fromHexString("#ff9a1c"))).appendSpace().append(Component.text(resultCraft.getRight(), TextColor.fromHexString("#69b5ff"))).build();
            event.getViewers().forEach(viewer -> viewer.sendActionBar(messagePossibleCraftsComponent));
        } else {
            // Bloquear crafteo si algún ingrediente es un item custom no permitido
            if (Arrays.stream(event.getInventory().getMatrix()).anyMatch(itemStack ->
                    CustomItemsManager.getCustomItem(itemStack).isPresent())) {
                if (!CustomItemRecipeHelper.validateRecipeIngredients(event.getRecipe(), event.getInventory().getMatrix())) {
                    event.getViewers().forEach(viewer -> viewer.sendActionBar(Component.text("Este pre-crafteo contiene un item custom y no deberias usarlo aqui", NamedTextColor.RED)));
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                }
            }
        }
    }

    @EventHandler
    public void onCrafterView(InventoryOpenEvent event) {
        if (!(event.getInventory() instanceof CrafterInventory crafterInventory)) {
            return;
        }

        final Inventory inventory = event.getInventory();
        final ItemStack itemResult = crafterInventory.getItem(9);

        Optional<AbstractCustomItem> baseItemOptional = CustomItemsManager.getCustomItem(itemResult);

        if (baseItemOptional.isPresent()) {
            AbstractCustomItem customItem = baseItemOptional.get();

            // Por defecto, bloquear resultado
            inventory.setItem(9, new ItemStack(Material.BARRIER));

            if (!customItem.isEnabled() ||
                    !CustomItemRecipeHelper.validateRecipeIngredients(customItem, inventory.getContents())) {
                inventory.setItem(9, new ItemStack(Material.AIR));
                inventory.getViewers().forEach(viewer ->
                        viewer.sendActionBar(Component.text("Esta receta es incompleta o inválida.", NamedTextColor.RED)));
            }
        }
    }

}
