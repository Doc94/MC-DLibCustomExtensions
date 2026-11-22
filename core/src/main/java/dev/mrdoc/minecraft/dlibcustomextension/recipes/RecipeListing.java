package dev.mrdoc.minecraft.dlibcustomextension.recipes;

import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import org.jspecify.annotations.Nullable;

/**
 * How an item or potion is presented in the {@link RecipeBrowser}.
 * <p>
 * Every value is optional: without icon only the name is shown, without select mode the one used by the
 * browser call applies, and without detail the lore of the item is shown in the detail dialog.
 * </p>
 *
 * @param icon       the sprite shown before the name in the list, or {@code null}
 * @param atlas      the atlas of the sprite, {@code null} for the default one
 * @param selectMode overrides the select mode of the browser for this entry, or {@code null}
 * @param detail     lines shown in the detail dialog instead of the lore, empty to use the lore
 */
public record RecipeListing(@Nullable Key icon, @Nullable Key atlas, @Nullable SelectMode selectMode, List<Component> detail) {

    /**
     * A listing without customizations.
     */
    public static final RecipeListing DEFAULT = new RecipeListing(null, null, null, List.of());

    /**
     * Makes the detail list immutable.
     */
    public RecipeListing {
        detail = List.copyOf(detail);
    }

    /**
     * Creates a copy with a sprite from the default atlas.
     *
     * @param sprite the sprite key
     * @return the new listing
     */
    public RecipeListing withIcon(@Nullable Key sprite) {
        return new RecipeListing(sprite, null, selectMode, detail);
    }

    /**
     * Creates a copy with a sprite from a specific atlas.
     *
     * @param atlas  the atlas key
     * @param sprite the sprite key
     * @return the new listing
     */
    public RecipeListing withIcon(Key atlas, Key sprite) {
        return new RecipeListing(sprite, atlas, selectMode, detail);
    }

    /**
     * Creates a copy with a select mode for this entry.
     *
     * @param mode the mode, or {@code null} to use the one of the browser
     * @return the new listing
     */
    public RecipeListing withSelectMode(@Nullable SelectMode mode) {
        return new RecipeListing(icon, atlas, mode, detail);
    }

    /**
     * Creates a copy with the lines shown in the detail dialog.
     *
     * @param lines the lines
     * @return the new listing
     */
    public RecipeListing withDetail(Component... lines) {
        return new RecipeListing(icon, atlas, selectMode, Arrays.asList(lines));
    }

    /**
     * Adds the icon, when defined, before a name.
     *
     * @param name the name
     * @return the name with the icon
     */
    public Component decorate(Component name) {
        if (icon == null) {
            return name;
        }
        ObjectContents sprite = atlas == null ? ObjectContents.sprite(icon) : ObjectContents.sprite(atlas, icon);
        return Component.textOfChildren(Component.object(sprite), Component.space(), name);
    }
}
