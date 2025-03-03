package me.mrdoc.minecraft.dlibcustomextension.potions.commands;

import java.util.List;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtension;
import me.mrdoc.minecraft.dlibcustomextension.commands.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class GivePotionCustomCommand extends BaseCommand {

    public GivePotionCustomCommand() {
        super(DLibCustomExtension.getPluginInstance());
    }

    @Command("givepotion <target> <item> [size]")
    @CommandDescription("Comando para dar items customs a un jugador")
    @Permission("dlibcustomextensions.potions.command.givecustomitem")
    public void executeGiveItemCustom(Source sourceSender, @Argument("target") MultiplePlayerSelector playerTargetsArgument, @Argument(value = "item", parserName = "parser_itempotioncustom") ItemStack itemStack, @Argument("size") Integer size) {
        int amount = (size != null) ? size : 1;
        itemStack.setAmount(amount);

        if (playerTargetsArgument.values().isEmpty()) {
            sourceSender.source().sendMessage(Component.text("No hay target para entregar items.", NamedTextColor.RED));
            return;
        }

        playerTargetsArgument.values().forEach(player -> player.getInventory().addItem(itemStack));

        Component targetComponentResponse = playerTargetsArgument.values().size() == 1 ? playerTargetsArgument.values().stream().findFirst().map(player -> player.teamDisplayName().hoverEvent(player)).orElse(Component.empty()) : Component.text(playerTargetsArgument.values().size());

        Component componentMessage = Component.text("Entregado " + amount)
                .append(Component.space())
                .append(itemStack.displayName().hoverEvent(itemStack))
                .append(Component.space())
                .append(Component.text("a").append(Component.space()).append(targetComponentResponse));
        sourceSender.source().sendMessage(componentMessage);
    }

    @Suggestions("suggest_itempotioncustom")
    public List<String> suggestItemCustom(CommandContext<Source> ctx, String input) {
        return PotionCustomCommandHelper.suggestItemCustom(ctx, input);
    }

    @Parser(name = "parser_itempotioncustom", suggestions = "suggest_itempotioncustom")
    public ItemStack parserItemCustom(CommandContext<Source> ctx, CommandInput commandInput) {
        return PotionCustomCommandHelper.parserItemCustom(ctx, commandInput);
    }

}
