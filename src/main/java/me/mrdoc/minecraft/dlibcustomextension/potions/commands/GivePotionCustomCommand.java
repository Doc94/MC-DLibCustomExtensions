package me.mrdoc.minecraft.dlibcustomextension.potions.commands;

import java.util.Collection;
import java.util.List;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtension;
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

        final Collection<Player> playersTargets = playerTargetsArgument.values();
        if (playersTargets.isEmpty()) {
            sourceSender.source().sendMessage(Component.translatable("argument.entity.notfound.player", NamedTextColor.RED));
            return;
        }

        playersTargets.forEach(player -> player.getInventory().addItem(itemStack));

        Component componentMessage;
        if (playersTargets.size() > 1) {
            componentMessage = Component.translatable("commands.give.success.multiple", Component.text(amount), itemStack.displayName(), playersTargets.iterator().next().teamDisplayName());
        } else {
            componentMessage = Component.translatable("commands.give.success.single", Component.text(amount), itemStack.displayName(), Component.text(playersTargets.size()));
        }

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
