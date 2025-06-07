package dev.mrdoc.minecraft.dlibcustomextension.potions.commands;

import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.commands.BaseCommand;
import dev.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.AbstractCustomPotion;
import java.util.List;
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

public class DisplayPotionCustomCommand extends BaseCommand implements Listener {

    private final String TAG_INVENTORY_PREVIEW = "INVENTORY_PREVIEW_DISPLAY_POTION";

    public DisplayPotionCustomCommand() {
        super(DLibCustomExtensionManager.getPluginInstance());
        Bukkit.getPluginManager().registerEvents(this, DLibCustomExtensionManager.getPluginInstance());
    }

    @Command("displaypotioncustom <item>")
    @CommandDescription("Comando para ver receta de un item custom")
    @Permission("dlibcustomextensions.potions.command.displaycustom")
    public void executeDisplayCustom(PlayerSource playerSourceSender, @Argument(value = "item", parserName = "parser_itempotioncustomclass") Class<? extends AbstractCustomPotion> customPotionClass) {
        Player senderPlayer = playerSourceSender.source();
        CustomPotionsManager.getCustomPotion(customPotionClass).ifPresentOrElse(baseItem -> {
            InventoryView baseItemInventoryView = baseItem.createDisplayCraft(senderPlayer);
            if (baseItemInventoryView != null) {
                senderPlayer.getScoreboardTags().add(TAG_INVENTORY_PREVIEW);
                senderPlayer.openInventory(baseItemInventoryView);
            } else {
                senderPlayer.sendMessage(Component.translatable("dlce.commands.displaypotioncustom.failed.craft_inventory_invalid", baseItem.getItem().displayName()).color(NamedTextColor.RED));
            }

        }, () -> senderPlayer.sendMessage(Component.translatable("dlce.argument.potion.notfound")));
    }

    @Suggestions("suggest_itempotioncustom")
    public List<String> suggestCustom(CommandContext<Source> ctx, String input) {
        return PotionCustomCommandHelper.suggestItemCustom(ctx, input);
    }

    @Parser(name = "parser_itempotioncustomclass", suggestions = "suggest_itempotioncustom")
    public Class<? extends AbstractCustomPotion> parserCustom(CommandContext<Source> ctx, CommandInput commandInput) {
        return PotionCustomCommandHelper.parserItemCustomClass(ctx, commandInput);
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
