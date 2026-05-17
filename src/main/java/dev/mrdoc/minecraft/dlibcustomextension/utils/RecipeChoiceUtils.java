package dev.mrdoc.minecraft.dlibcustomextension.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.RecipeChoice;
import org.jspecify.annotations.Nullable;

public class RecipeChoiceUtils {

    /**
     * Gets a list of {@link ItemStack} representations from a {@link RecipeChoice}.
     *
     * @param recipeChoice the recipe choice to convert
     * @return a list of corresponding item stacks
     */
    @SuppressWarnings("UnstableApiUsage")
    public static List<ItemStack> getRecipeChoiceItemStacks(@Nullable RecipeChoice recipeChoice) {
        if (recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
            return exactChoice.getChoices();
        } else if (recipeChoice instanceof RecipeChoice.ItemTypeChoice itemTypeChoice) {
            List<ItemStack> itemStacks = new ArrayList<>();
            itemTypeChoice.itemTypes().forEach(itemTypeTypedKey -> {
                ItemType itemType = Registry.ITEM.get(itemTypeTypedKey);
                if (itemType != null) {
                    itemStacks.add(itemType.createItemStack());
                }
            });
            return itemStacks;
        } else if (recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
            return materialChoice.getChoices().stream()
                    .map(material -> Objects.requireNonNull(material.asItemType()).createItemStack())
                    .toList();
        }
        return List.of();
    }

}
