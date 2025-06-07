package dev.mrdoc.minecraft.dlibcustomextension.items;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
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
                humanWhoClicked.sendMessage(Component.translatable("dlce.items.craftitem.failed.crafting_in_invalid_inventory", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }

            final CraftingRecipe craftingRecipe = ((CraftingRecipe) customItem.getRecipe());

            if (!CustomItemRecipeHelper.validateRecipeIngredients(customItem, event.getInventory().getMatrix())) {
                humanWhoClicked.sendMessage(Component.translatable("dlce.items.craftitem.failed.crafting_invalid_for_recipe", NamedTextColor.RED));
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
            } else if (Objects.equals(CustomItemsManager.getInternalKey(humanWhoClicked.getItemOnCursor()), CustomItemsManager.getInternalKey(itemResult))) {
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
                event.getViewers().forEach(viewer -> viewer.sendActionBar(Component.translatable("dlce.items.precraftitem.failed.crafting_disabled", customItem.getItem().displayName()).color(NamedTextColor.RED)));
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
            if (!customItem.getInventoryTypes().contains(event.getInventory().getType())) {
                event.getViewers().forEach(viewer -> viewer.sendActionBar(Component.translatable("dlce.items.precraftitem.failed.crafting_in_invalid_inventory", customItem.getItem().displayName(), Component.text(event.getInventory().getType().name()), Component.text(customItem.getInventoryTypes().stream().map(Objects::toString).collect(Collectors.joining(", ")))).color(NamedTextColor.RED)));
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
            if (!CustomItemRecipeHelper.validateRecipeIngredients(customItem, event.getInventory().getMatrix())) {
                event.getViewers().forEach(viewer -> viewer.sendActionBar(Component.translatable("dlce.items.precraftitem.failed.crafting_invalid_for_recipe", customItem.getItem().displayName()).color(NamedTextColor.RED)));
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }

            Pair<ItemStack[], Integer> resultCraft = CustomItemRecipeHelper.reduceMatrix(craftingRecipe, event.getInventory().getMatrix(), true);
            Component messagePossibleCraftsComponent = Component.translatable("dlce.items.precraftitem.message.crafting_size_result", Component.text(resultCraft.getRight(), TextColor.fromHexString("#69b5ff"))).color(TextColor.fromHexString("#ff9a1c"));
            event.getViewers().forEach(viewer -> viewer.sendActionBar(messagePossibleCraftsComponent));
        } else {
            // Bloquear crafteo si algún ingrediente es un item custom no permitido
            if (Arrays.stream(event.getInventory().getMatrix()).anyMatch(itemStack ->
                    CustomItemsManager.getCustomItem(itemStack).isPresent())) {
                if (!CustomItemRecipeHelper.validateRecipeIngredients(event.getRecipe(), event.getInventory().getMatrix())) {
                    event.getViewers().forEach(viewer -> viewer.sendActionBar(Component.translatable("dlce.items.precraftitem.failed.crafting_contains_unsupported_custom_item", NamedTextColor.RED)));
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
                        viewer.sendActionBar(Component.translatable("dlce.items.crafter.failed.crafting_invalid_for_recipe", NamedTextColor.RED)));
            }
        }
    }

}
