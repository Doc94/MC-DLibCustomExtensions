package dev.mrdoc.minecraft.dlibcustomextension.items.commands;

import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.commands.BaseCommand;
import dev.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import dev.mrdoc.minecraft.dlibcustomextension.recipes.RecipeBrowser;
import dev.mrdoc.minecraft.dlibcustomextension.recipes.RecipePreview;
import java.util.List;
import net.kyori.adventure.text.Component;
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
 * Without arguments it opens a dialog to pick the item, with an item it opens the preview directly.
 * </p>
 */
public class DisplayItemCustomCommand extends BaseCommand {

    public DisplayItemCustomCommand() {
        super(DLibCustomExtensionManager.getPluginInstance());
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
    public void executeDisplayCustom(final PlayerSource playerSourceSender, final @Argument(value = "item", parserName = "parser_itemcustomclass") Class<? extends AbstractCustomItem> customItemClass) {
        CustomItemsManager.getCustomItem(customItemClass).ifPresentOrElse(
                baseItem -> RecipePreview.open(playerSourceSender.source(), baseItem),
                () -> playerSourceSender.source().sendMessage(Component.translatable("dlce.argument.item.notfound")));
    }

    /**
     * Opens a dialog to choose the custom item to display.
     *
     * @param playerSourceSender the player who executed the command
     */
    @Command("displayitemcustom")
    @CommandDescription("Comando para elegir y ver la receta de un item custom")
    @Permission("dlibcustomextensions.items.command.displaycustom")
    public void executeBrowseCustom(final PlayerSource playerSourceSender) {
        RecipeBrowser.openItemDialog(playerSourceSender.source());
    }

    /**
     * Provides suggestions for custom item names.
     *
     * @param ctx   the command context
     * @param input the current input string
     * @return a list of suggested custom item names
     */
    @Suggestions("suggest_itemcustom")
    public List<String> suggestItemCustom(final CommandContext<Source> ctx, final String input) {
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
    public Class<? extends AbstractCustomItem> parserItemCustom(final CommandContext<Source> ctx, final CommandInput commandInput) {
        return ItemCustomCommandHelper.parserItemCustomClass(ctx, commandInput);
    }
}
