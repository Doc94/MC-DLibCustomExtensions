package me.mrdoc.minecraft.dlibcustomextension.items.commands;

import java.util.Collection;
import java.util.List;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import me.mrdoc.minecraft.dlibcustomextension.commands.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.parser.Parser;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;

public class GiveItemCustomCommand extends BaseCommand {

    public GiveItemCustomCommand() {
        super(DLibCustomExtensionManager.getPluginInstance());
    }

    @Command("givecustom <target> <item> [size]")
    @CommandDescription("Comando para dar items customs a un jugador")
    @Permission("cdlibcustomextensions.items.command.givecustomitem")
    public void executeGiveItemCustom(Source sourceSender, @Argument("target") MultiplePlayerSelector playerTargetsArgument, @Argument(value = "item", parserName = "parser_itemcustom") ItemStack itemStack, @Argument("size") Integer size) {
        int amount = (size != null) ? size : 1;
        itemStack.setAmount(amount);

        final Collection<Player> playersTargets = playerTargetsArgument.values();

        if (playersTargets.isEmpty()) {
            sourceSender.source().sendMessage(Component.translatable("argument.entity.notfound.player", NamedTextColor.RED));
            return;
        }

        playersTargets.forEach(player -> player.getInventory().addItem(itemStack));

        Component componentMessage;
        if (playersTargets.size() > 1) {
            componentMessage = Component.translatable("commands.give.success.multiple", Component.text(amount), itemStack.displayName(), Component.text(playersTargets.size()));
        } else {
            componentMessage = Component.translatable("commands.give.success.single", Component.text(amount), itemStack.displayName(), playersTargets.iterator().next().teamDisplayName());
        }

        sourceSender.source().sendMessage(componentMessage);
    }

    @Suggestions("suggest_itemcustom")
    public List<String> suggestItemCustom(CommandContext<Source> ctx, String input) {
        return ItemCustomCommandHelper.suggestItemCustom(ctx, input);
    }

    @Parser(name = "parser_itemcustom", suggestions = "suggest_itemcustom")
    public ItemStack parserItemCustom(CommandContext<Source> ctx, CommandInput commandInput) {
        return ItemCustomCommandHelper.parserItemCustom(ctx, commandInput);
    }
}
