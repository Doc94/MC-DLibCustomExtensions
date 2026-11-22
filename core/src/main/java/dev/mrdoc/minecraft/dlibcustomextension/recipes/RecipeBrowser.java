package dev.mrdoc.minecraft.dlibcustomextension.recipes;

import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import dev.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.AbstractCustomPotion;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Builds and opens dialogs that list the custom items or potions with a recipe.
 * Selecting an entry opens its recipe preview through {@link RecipePreview}.
 */
public final class RecipeBrowser {

    private static final int BUTTON_WIDTH = 200;
    private static final int COLUMNS = 2;
    private static final int ITEM_SIZE = 64;
    private static final int DESCRIPTION_WIDTH = 200;

    private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
            .uses(ClickCallback.UNLIMITED_USES)
            .lifetime(Duration.ofMinutes(30))
            .build();

    private RecipeBrowser() {
    }

    /**
     * Creates, without opening it, a dialog with every enabled custom item that has a recipe.
     *
     * @return the dialog
     */
    public static Dialog createItemDialog() {
        return createItemDialog(item -> true);
    }

    /**
     * Creates, without opening it, a dialog with the enabled custom items that have a recipe and match the filter.
     *
     * @param filter additional filter for the listed items
     * @return the dialog
     */
    public static Dialog createItemDialog(Predicate<AbstractCustomItem> filter) {
        List<Entry> entries = CustomItemsManager.getCustomItems().stream()
                .filter(item -> item.getRecipe() != null && item.isEnabled())
                .filter(filter)
                .sorted(Comparator.comparing(item -> item.getKey().asString()))
                .map(item -> new Entry(item.getItem(), player -> RecipePreview.open(player, item)))
                .toList();
        return buildDialog(Component.translatable("dlce.recipes.browser.items.title"), entries, () -> createItemDialog(filter));
    }

    /**
     * Opens the item recipes dialog to a player.
     *
     * @param player the player
     */
    public static void openItemDialog(Player player) {
        player.showDialog(createItemDialog());
    }

    /**
     * Opens the item recipes dialog to a player using a filter.
     *
     * @param player the player
     * @param filter additional filter for the listed items
     */
    public static void openItemDialog(Player player, Predicate<AbstractCustomItem> filter) {
        player.showDialog(createItemDialog(filter));
    }

    /**
     * Creates, without opening it, a dialog with every enabled custom potion.
     *
     * @return the dialog
     */
    public static Dialog createPotionDialog() {
        return createPotionDialog(potion -> true);
    }

    /**
     * Creates, without opening it, a dialog with the enabled custom potions that match the filter.
     *
     * @param filter additional filter for the listed potions
     * @return the dialog
     */
    public static Dialog createPotionDialog(Predicate<AbstractCustomPotion> filter) {
        List<Entry> entries = CustomPotionsManager.getCustomPotions().stream()
                .filter(potion -> CustomPotionsManager.isItemEnable(potion.getKey().value()))
                .filter(filter)
                .sorted(Comparator.comparing(potion -> potion.getKey().asString()))
                .map(potion -> new Entry(potion.getItem(), player -> RecipePreview.open(player, potion)))
                .toList();
        return buildDialog(Component.translatable("dlce.recipes.browser.potions.title"), entries, () -> createPotionDialog(filter));
    }

    /**
     * Opens the potion recipes dialog to a player.
     *
     * @param player the player
     */
    public static void openPotionDialog(Player player) {
        player.showDialog(createPotionDialog());
    }

    /**
     * Opens the potion recipes dialog to a player using a filter.
     *
     * @param player the player
     * @param filter additional filter for the listed potions
     */
    public static void openPotionDialog(Player player, Predicate<AbstractCustomPotion> filter) {
        player.showDialog(createPotionDialog(filter));
    }

    private record Entry(ItemStack item, Consumer<Player> onSelect) {
    }

    private static Dialog buildDialog(Component title, List<Entry> entries, Supplier<Dialog> listSupplier) {
        if (entries.isEmpty()) {
            DialogBase emptyBase = DialogBase.builder(title)
                    .body(List.of(DialogBody.plainMessage(Component.translatable("dlce.recipes.browser.empty"))))
                    .build();
            return Dialog.create(factory -> factory.empty().base(emptyBase).type(DialogType.notice()));
        }

        List<ActionButton> buttons = entries.stream().map(entry -> toButton(entry, listSupplier)).toList();
        ActionButton exitButton = ActionButton.builder(Component.translatable("dlce.recipes.browser.close")).build();
        DialogBase dialogBase = DialogBase.builder(title).build();
        return Dialog.create(factory -> factory.empty().base(dialogBase)
                .type(DialogType.multiAction(buttons).columns(COLUMNS).exitAction(exitButton).build()));
    }

    private static ActionButton toButton(Entry entry, Supplier<Dialog> listSupplier) {
        DialogAction action = DialogAction.customClick((view, audience) -> {
            if (audience instanceof Player player) {
                player.showDialog(createDetailDialog(entry, listSupplier));
            }
        }, CALLBACK_OPTIONS);

        return ActionButton.builder(displayName(entry.item()))
                .width(BUTTON_WIDTH)
                .action(action)
                .build();
    }

    // Item bodies render the real item model, unlike button labels which can only use sprites
    private static Dialog createDetailDialog(Entry entry, Supplier<Dialog> listSupplier) {
        DialogBase base = DialogBase.builder(displayName(entry.item()))
                .body(List.of(DialogBody.item(entry.item())
                        .description(DialogBody.plainMessage(describe(entry.item()), DESCRIPTION_WIDTH))
                        .showDecorations(false)
                        .showTooltip(false)
                        .width(ITEM_SIZE)
                        .height(ITEM_SIZE)
                        .build()))
                .build();

        ActionButton viewButton = ActionButton.builder(Component.translatable("dlce.recipes.browser.view"))
                .width(BUTTON_WIDTH / 2)
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player player) {
                        // Next tick so the dialog closing does not also close the opened preview
                        Bukkit.getScheduler().runTask(DLibCustomExtensionManager.getPluginInstance(), () -> entry.onSelect().accept(player));
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton backButton = ActionButton.builder(Component.translatable("dlce.recipes.browser.back"))
                .width(BUTTON_WIDTH / 2)
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player player) {
                        player.showDialog(listSupplier.get());
                    }
                }, CALLBACK_OPTIONS))
                .build();

        return Dialog.create(factory -> factory.empty().base(base).type(DialogType.confirmation(viewButton, backButton)));
    }

    private static Component describe(ItemStack item) {
        Component description = displayName(item);
        ItemLore lore = item.getData(DataComponentTypes.LORE);
        if (lore != null) {
            for (Component line : lore.lines()) {
                description = description.appendNewline().append(line);
            }
        }
        return description;
    }

    private static Component displayName(ItemStack item) {
        Component name = item.getData(DataComponentTypes.CUSTOM_NAME);
        if (name == null) {
            name = item.getData(DataComponentTypes.ITEM_NAME);
        }
        return name != null ? name : Component.translatable(item.translationKey());
    }
}
