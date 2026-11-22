package dev.mrdoc.minecraft.dlibcustomextension.recipes;

import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import dev.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.AbstractCustomPotion;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

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
                .map(item -> new Entry(item.getItem(), item.getCategory(), player -> RecipePreview.open(player, item)))
                .toList();
        return buildDialog(Component.translatable("dlce.recipes.browser.items.title"), entries);
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
                .map(potion -> new Entry(potion.getItem(), potion.getCategory(), player -> RecipePreview.open(player, potion)))
                .toList();
        return buildDialog(Component.translatable("dlce.recipes.browser.potions.title"), entries);
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

    private record Entry(ItemStack item, @Nullable RecipeCategory category, Consumer<Player> onSelect) {
    }

    private static Dialog buildDialog(Component title, List<Entry> entries) {
        warnConflictingCategories(entries);
        return createLevel(title, entries, null, null);
    }

    // Categories are matched by key, so two different definitions with the same key are reported
    private static void warnConflictingCategories(List<Entry> entries) {
        Map<Key, RecipeCategory> seen = new HashMap<>();
        for (Entry entry : entries) {
            if (entry.category() == null) {
                continue;
            }
            for (RecipeCategory category : entry.category().path()) {
                RecipeCategory previous = seen.putIfAbsent(category.key(), category);
                if (previous != null && !Objects.equals(parentKey(previous), parentKey(category))) {
                    LoggerUtils.warn("Recipe category [%s] is defined with different parents, using the first one found".formatted(category.key().asString()));
                }
            }
        }
    }

    private static @Nullable Key parentKey(RecipeCategory category) {
        return category.parent() == null ? null : category.parent().key();
    }

    /**
     * Creates the dialog for a category level: its sub categories first and then its own entries.
     *
     * @param current the category to show, or {@code null} for the root
     * @param parent  creates the dialog of the previous level, or {@code null} for the root
     */
    private static Dialog createLevel(Component rootTitle, List<Entry> entries, @Nullable RecipeCategory current, @Nullable Supplier<Dialog> parent) {
        Map<Key, RecipeCategory> children = new TreeMap<>(Comparator.comparing(Key::asString));
        List<Entry> direct = new ArrayList<>();
        for (Entry entry : entries) {
            List<RecipeCategory> path = entry.category() == null ? List.of() : entry.category().path();
            int index = -1;
            if (current != null) {
                index = indexOfKey(path, current.key());
                if (index < 0) {
                    continue;
                }
            }
            if (index + 1 >= path.size()) {
                direct.add(entry);
            } else {
                RecipeCategory child = path.get(index + 1);
                children.putIfAbsent(child.key(), child);
            }
        }

        Component title = rootTitle;
        if (current != null) {
            title = rootTitle.append(Component.text(" - ")).append(Component.join(JoinConfiguration.separator(Component.text(" > ")), current.path().stream().map(RecipeCategory::displayName).toList()));
        }

        if (children.isEmpty() && direct.isEmpty()) {
            DialogBase emptyBase = DialogBase.builder(title)
                    .body(List.of(DialogBody.plainMessage(Component.translatable("dlce.recipes.browser.empty"))))
                    .build();
            return Dialog.create(factory -> factory.empty().base(emptyBase).type(DialogType.notice()));
        }

        Supplier<Dialog> self = () -> createLevel(rootTitle, entries, current, parent);
        List<ActionButton> buttons = new ArrayList<>();
        for (RecipeCategory child : children.values()) {
            long count = entries.stream().filter(entry -> entry.category() != null && indexOfKey(entry.category().path(), child.key()) >= 0).count();
            DialogAction action = DialogAction.customClick((view, audience) -> {
                if (audience instanceof Player player) {
                    player.showDialog(createLevel(rootTitle, entries, child, self));
                }
            }, CALLBACK_OPTIONS);
            buttons.add(ActionButton.builder(child.displayName().append(Component.text(" (" + count + ")")))
                    .width(BUTTON_WIDTH)
                    .action(action)
                    .build());
        }
        direct.forEach(entry -> buttons.add(toButton(entry, self)));

        ActionButton exitButton;
        if (parent == null) {
            exitButton = ActionButton.builder(Component.translatable("dlce.recipes.browser.close")).build();
        } else {
            exitButton = ActionButton.builder(Component.translatable("dlce.recipes.browser.back"))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player player) {
                            player.showDialog(parent.get());
                        }
                    }, CALLBACK_OPTIONS))
                    .build();
        }

        DialogBase dialogBase = DialogBase.builder(title).build();
        return Dialog.create(factory -> factory.empty().base(dialogBase)
                .type(DialogType.multiAction(buttons).columns(COLUMNS).exitAction(exitButton).build()));
    }

    private static int indexOfKey(List<RecipeCategory> path, Key key) {
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).key().equals(key)) {
                return i;
            }
        }
        return -1;
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
