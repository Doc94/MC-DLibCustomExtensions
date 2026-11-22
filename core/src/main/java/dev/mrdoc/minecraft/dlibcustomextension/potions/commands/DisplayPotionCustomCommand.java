package dev.mrdoc.minecraft.dlibcustomextension.potions.commands;

import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.commands.BaseCommand;
import dev.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.AbstractCustomPotion;
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
 * Command for displaying the recipe of a custom potion.
 * <p>
 * Without arguments it opens a dialog to pick the potion, with a potion it opens the preview directly.
 * </p>
 */
public class DisplayPotionCustomCommand extends BaseCommand {

    /**
     * Constructs a new DisplayPotionCustomCommand.
     */
    public DisplayPotionCustomCommand() {
        super(DLibCustomExtensionManager.getPluginInstance());
    }

    @Command("displaypotioncustom <item>")
    @CommandDescription("Comando para ver receta de un item custom")
    @Permission("dlibcustomextensions.potions.command.displaycustom")
    public void executeDisplayCustom(final PlayerSource playerSourceSender, final @Argument(value = "item", parserName = "parser_itempotioncustomclass") Class<? extends AbstractCustomPotion> customPotionClass) {
        CustomPotionsManager.getCustomPotion(customPotionClass).ifPresentOrElse(
                basePotion -> RecipePreview.open(playerSourceSender.source(), basePotion),
                () -> playerSourceSender.source().sendMessage(Component.translatable("dlce.argument.potion.notfound")));
    }

    @Command("displaypotioncustom")
    @CommandDescription("Comando para elegir y ver la receta de una potion custom")
    @Permission("dlibcustomextensions.potions.command.displaycustom")
    public void executeBrowseCustom(final PlayerSource playerSourceSender) {
        RecipeBrowser.openPotionDialog(playerSourceSender.source());
    }

    @Suggestions("suggest_itempotioncustom")
    public List<String> suggestCustom(final CommandContext<Source> ctx, final String input) {
        return PotionCustomCommandHelper.suggestItemCustom(ctx, input);
    }

    @Parser(name = "parser_itempotioncustomclass", suggestions = "suggest_itempotioncustom")
    public Class<? extends AbstractCustomPotion> parserCustom(final CommandContext<Source> ctx, final CommandInput commandInput) {
        return PotionCustomCommandHelper.parserItemCustomClass(ctx, commandInput);
    }
}
