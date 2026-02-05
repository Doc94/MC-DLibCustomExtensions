package dev.mrdoc.minecraft.dlibcustomextension.items.commands;

import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
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

/**
 * Command for displaying the recipe of a custom item in a preview inventory.
 * <p>
 * This command allows players to see how to craft a specific custom item
 * and prevents interaction with the preview inventory.
 * </p>
 */
public class DisplayItemCustomCommand extends BaseCommand implements Listener {

    private final String TAG_INVENTORY_PREVIEW = "INVENTORY_PREVIEW_DISPLAY_ITEM";

    public DisplayItemCustomCommand() {
        super(DLibCustomExtensionManager.getPluginInstance());
        Bukkit.getPluginManager().registerEvents(this, DLibCustomExtensionManager.getPluginInstance());
    }

    /**
     * Executes the display custom item command.
     *
     * @param playerSourceSender the player who executed the command
     * @param customItemClass    the class of the custom item to display
     */
    @Command("displayitemcustom <item>")
    @CommandDescription("Comando para ver receta de un item custom")
    @Permission("dlibcustomextensions.items.command.displaycustom")
    public void executeDisplayCustom(PlayerSource playerSourceSender, @Argument(value = "item", parserName = "parser_itemcustomclass") Class<? extends AbstractCustomItem> customItemClass) {
        Player senderPlayer = playerSourceSender.source();
        CustomItemsManager.getCustomItem(customItemClass).ifPresentOrElse(baseItem -> {
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

    /**
     * Provides suggestions for custom item names.
     *
     * @param ctx   the command context
     * @param input the current input string
     * @return a list of suggested custom item names
     */
    @Suggestions("suggest_itemcustom")
    public List<String> suggestItemCustom(CommandContext<Source> ctx, String input) {
        return ItemCustomCommandHelper.suggestItemCustom(ctx, input);
    }

    /**
     * Parses the custom item class from the command input.
     *
     * @param ctx          the command context
     * @param commandInput the command input to parse
     * @return the parsed custom item class
     */
    @Parser(name = "parser_itemcustomclass", suggestions = "suggest_itemcustom")
    public Class<? extends AbstractCustomItem> parserItemCustom(CommandContext<Source> ctx, CommandInput commandInput) {
        return ItemCustomCommandHelper.parserItemCustomClass(ctx, commandInput);
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
