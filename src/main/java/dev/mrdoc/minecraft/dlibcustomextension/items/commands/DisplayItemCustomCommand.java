package dev.mrdoc.minecraft.dlibcustomextension.items.commands;

import java.util.List;
import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.commands.BaseCommand;
import dev.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.parser.Parser;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

public class DisplayItemCustomCommand extends BaseCommand implements Listener {

    private final String TAG_INVENTORY_PREVIEW = "INVENTORY_PREVIEW_DISPLAY_ITEM";

    public DisplayItemCustomCommand() {
        super(DLibCustomExtensionManager.getPluginInstance());
        Bukkit.getPluginManager().registerEvents(this, DLibCustomExtensionManager.getPluginInstance());
    }

    @Command("displayitemcustom <item>")
    @CommandDescription("Comando para ver receta de un item custom")
    @Permission("dlibcustomextensions.items.command.displaycustom")
    public void executeDisplayCustom(PlayerSource playerSourceSender, @Argument(value = "item", parserName = "parser_itemcustom") ItemStack itemStack) {
        Player senderPlayer = playerSourceSender.source();
        CustomItemsManager.getCustomItem(CustomItemsManager.getInternalName(itemStack)).ifPresentOrElse(baseItem -> {
            if (baseItem.getRecipe() == null) {
                senderPlayer.sendMessage(Component.translatable("dlce.commands.displayitemcustom.failed.recipe_not_found", baseItem.getItem().displayName()).color(NamedTextColor.RED));
                return;
            }
            InventoryView baseItemInventoryView = baseItem.createDisplayCraft(senderPlayer);
            if (baseItemInventoryView != null) {
                senderPlayer.getScoreboardTags().add(TAG_INVENTORY_PREVIEW);
                senderPlayer.openInventory(baseItemInventoryView);
            } else {
                senderPlayer.sendMessage(Component.translatable("dlce.commands.displayitemcustom.failed.craft_inventory_invalid", baseItem.getItem().displayName()).color(NamedTextColor.RED));
            }

        }, () -> senderPlayer.sendMessage(Component.translatable("dlce.argument.item.notfound")));
    }

    @Suggestions("suggest_itemcustom")
    public List<String> suggestItemCustom(CommandContext<Source> ctx, String input) {
        return ItemCustomCommandHelper.suggestItemCustom(ctx, input);
    }

    @Parser(name = "parser_itemcustom", suggestions = "suggest_itemcustom")
    public ItemStack parserItemCustom(CommandContext<Source> ctx, CommandInput commandInput) {
        return ItemCustomCommandHelper.parserItemCustom(ctx, commandInput);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer().getScoreboardTags().contains(TAG_INVENTORY_PREVIEW)) {
            event.getInventory().clear();
            event.getPlayer().getScoreboardTags().remove(TAG_INVENTORY_PREVIEW);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getViewers().stream().anyMatch(humanEntity -> humanEntity.getScoreboardTags().contains(TAG_INVENTORY_PREVIEW))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getViewers().stream().anyMatch(humanEntity -> humanEntity.getScoreboardTags().contains(TAG_INVENTORY_PREVIEW))) {
            event.setCancelled(true);
        }
    }
}
