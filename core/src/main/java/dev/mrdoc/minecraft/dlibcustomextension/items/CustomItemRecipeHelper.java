package dev.mrdoc.minecraft.dlibcustomextension.items;

import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import dev.mrdoc.minecraft.dlibcustomextension.utils.item.RecipeChoiceUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Helper class for handling custom item recipes and crafting validations.
 * <p>
 * Provides utilities for extracting items from recipe choices, validating crafting matrices
 * against recipes, and calculating the results of recipe applications.
 * </p>
 */
@NullMarked
public class CustomItemRecipeHelper {

    /**
     * Check the ingredients for a custom item.
     *
     * @param customItem  the custom item
     * @param matrixCraft the matrix of ingredients
     * @return {@code true} if is valid
     */
    static boolean validateRecipeIngredients(final AbstractCustomItem customItem, final @Nullable ItemStack @Nullable [] matrixCraft) {
        if (customItem.getRecipe() == null) {
            return false;
        }
        return validateRecipeIngredients(customItem.getRecipe(), matrixCraft);
    }

    /**
     * Check the ingredients for a custom item.
     *
     * @param recipe      the custom recipe
     * @param matrixCraft the matrix of ingredients
     * @return {@code true} if is valid
     */
    @SuppressWarnings("ConstantConditions")
    static boolean validateRecipeIngredients(final Recipe recipe, final @Nullable ItemStack @Nullable [] matrixCraft) {
        // We validate that the matrix is always of size 9 (3x3)
        if (matrixCraft == null || matrixCraft.length != 9) {
            return false;
        }

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            String[] shape = shapedRecipe.getShape();
            Map<Character, RecipeChoice> ingredientMap = shapedRecipe.getChoiceMap();

            int recipeRows = shape.length;
            int recipeCols = shape[0].length(); // Se asume que todas las filas tienen la misma longitud

            for (int rowOffset = 0; rowOffset <= 3 - recipeRows; rowOffset++) {
                for (int colOffset = 0; colOffset <= 3 - recipeCols; colOffset++) {
                    boolean matched = true;

                    outerLoop:
                    for (int row = 0; row < recipeRows; row++) {
                        String shapeRow = shape[row];
                        for (int col = 0; col < recipeCols; col++) {
                            char slot = shapeRow.charAt(col);
                            int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset);

                            ItemStack matrixItem = matrixCraft[matrixIndex];

                            if (slot == ' ' || !ingredientMap.containsKey(slot)) {
                                if (matrixItem != null && !matrixItem.isEmpty()) {
                                    matched = false;
                                    break outerLoop;
                                }
                            } else {
                                RecipeChoice requiredChoice = ingredientMap.get(slot);
                                if (!validateIngredient(matrixItem, requiredChoice)) {
                                    matched = false;
                                    break outerLoop;
                                }
                            }
                        }
                    }

                    // Si alguna disposición hace match, la receta es válida
                    if (matched) return true;
                }
            }

            return false;
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            List<RecipeChoice> ingredients = new ArrayList<>(shapelessRecipe.getChoiceList());

            for (ItemStack itemMatrix : matrixCraft) {
                if (itemMatrix != null && !itemMatrix.isEmpty()) {
                    boolean matched = false;

                    Iterator<RecipeChoice> iterator = ingredients.iterator();
                    while (iterator.hasNext()) {
                        RecipeChoice requiredChoice = iterator.next();
                        if (validateIngredient(itemMatrix, requiredChoice)) {
                            iterator.remove();
                            matched = true;
                            break;
                        }
                    }

                    if (!matched) {
                        return false;
                    }
                }
            }

            return ingredients.isEmpty(); // If there are any unused ingredients left over, the array is invalid
        }

        return true;
    }

    /**
     * Validate and compare an item with the recipe item ingredient.
     *
     * @param itemMatrix   the item in craft matrix
     * @param recipeChoice the recipe choice to compare
     * @return {@code true} if is valid
     */
    private static boolean validateIngredient(@Nullable ItemStack itemMatrix, @Nullable RecipeChoice recipeChoice) {
        if (recipeChoice == null) {
            return true; // Slot not required
        }

        if (itemMatrix == null || itemMatrix.isEmpty()) {
            return false;
        }

        if (recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
            for (ItemStack choice : exactChoice.getChoices()) {
                if (validateIngredient(itemMatrix, choice)) {
                    return true;
                }
            }
            return false;
        }

        return recipeChoice.test(itemMatrix);
    }

    /**
     * Validate and compare an item with the recipe item ingredient.
     *
     * @param itemMatrix   the item in craft matrix
     * @param itemInRecipe the item in the recipe
     * @return {@code true} if is valid
     */
    @SuppressWarnings("UnstableApiUsage")
    private static boolean validateIngredient(@Nullable ItemStack itemMatrix, @Nullable ItemStack itemInRecipe) {
        if (itemInRecipe == null || itemInRecipe.isEmpty()) {
            return true; // Slot not required
        }

        if (itemMatrix == null || itemMatrix.isEmpty() || itemMatrix.getAmount() < itemInRecipe.getAmount()) {
            return false;
        }

        if (itemMatrix.getType() != itemInRecipe.getType()) {
            return false;
        }

        if (!Objects.equals(CustomItemsManager.getInternalKey(itemInRecipe), CustomItemsManager.getInternalKey(itemMatrix))) {
            return false;
        }

        if (itemMatrix.hasData(DataComponentTypes.ITEM_MODEL) && itemInRecipe.hasData(DataComponentTypes.ITEM_MODEL)) {
            if (!Objects.equals(itemMatrix.getData(DataComponentTypes.ITEM_MODEL), itemInRecipe.getData(DataComponentTypes.ITEM_MODEL))) {
                return false;
            }
        } else if (itemMatrix.hasData(DataComponentTypes.ITEM_MODEL) || itemInRecipe.hasData(DataComponentTypes.ITEM_MODEL)) {
            return false;
        }

        if (itemMatrix.hasData(DataComponentTypes.CUSTOM_MODEL_DATA) && itemInRecipe.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            return Objects.equals(itemMatrix.getData(DataComponentTypes.CUSTOM_MODEL_DATA), itemInRecipe.getData(DataComponentTypes.CUSTOM_MODEL_DATA));
        } else {
            return !itemMatrix.hasData(DataComponentTypes.CUSTOM_MODEL_DATA) && !itemInRecipe.hasData(DataComponentTypes.CUSTOM_MODEL_DATA);
        }
    }

    /**
     * Get the amount of an item in a recipe choice.
     *
     * @param itemMatrix   the item in craft matrix
     * @param recipeChoice the recipe choice
     * @return the amount
     */
    private static int getRecipeChoiceAmount(@Nullable ItemStack itemMatrix, @Nullable RecipeChoice recipeChoice) {
        if (recipeChoice == null || itemMatrix == null || itemMatrix.isEmpty()) {
            return 0;
        }

        for (ItemStack itemStackChoice : RecipeChoiceUtils.getRecipeChoiceItemStacks(recipeChoice)) {
            if (validateIngredient(itemMatrix, itemStackChoice)) {
                return itemStackChoice.getAmount();
            }
        }

        return 0;
    }

    /**
     * Reduce a matrix using a recipe.
     *
     * @param craftingRecipe the craft recipe
     * @param matrix         the matrix to reduce
     * @return a pair with the matrix result and the item result size
     */
    static Pair<@Nullable ItemStack @Nullable [], Integer> reduceMatrix(final CraftingRecipe craftingRecipe, final @Nullable ItemStack @Nullable [] matrix) {
        return reduceMatrix(craftingRecipe, matrix, false);
    }

    /**
     * Reduce a matrix using a recipe.
     *
     * @param craftingRecipe the craft recipe
     * @param matrix         the matrix to reduce
     * @param processAll     true for try the max of reduction
     * @return a pair with the matrix result and the item result size
     */
    static Pair<@Nullable ItemStack @Nullable [], Integer> reduceMatrix(final CraftingRecipe craftingRecipe, final @Nullable ItemStack @Nullable [] matrix, final boolean processAll) {
        Objects.requireNonNull(matrix);
        // We clone the matrix to work without modifying the original
        ItemStack[] matrixResult = Objects.requireNonNull(cloneMatrix(matrix));
        int maxCrafts; // We initialize with a very high value to reduce

        // We get the maximum allowed per stack from the recipe result
        int maxPerStack = craftingRecipe.getResult().getMaxStackSize();
        int craftsToProcess = 0;

        if (craftingRecipe instanceof ShapedRecipe shapedRecipe) {
            String[] shape = shapedRecipe.getShape();
            Map<Character, RecipeChoice> ingredientMap = shapedRecipe.getChoiceMap();

            int recipeRows = shape.length;
            int recipeCols = shape[0].length();

            for (int rowOffset = 0; rowOffset <= 3 - recipeRows; rowOffset++) {
                for (int colOffset = 0; colOffset <= 3 - recipeCols; colOffset++) {
                    boolean matched = true;
                    int offsetMaxCrafts = Integer.MAX_VALUE;

                    outerLoop:
                    for (int row = 0; row < recipeRows; row++) {
                        String shapeRow = shape[row];
                        for (int col = 0; col < recipeCols; col++) {
                            char slot = shapeRow.charAt(col);
                            int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset);
                            ItemStack matrixItem = matrix[matrixIndex];

                            if (slot == ' ' || !ingredientMap.containsKey(slot)) {
                                if (matrixItem != null && !matrixItem.isEmpty()) {
                                    matched = false;
                                    break outerLoop;
                                }
                                continue;
                            }

                            RecipeChoice requiredChoice = ingredientMap.get(slot);
                            if (!validateIngredient(matrixItem, requiredChoice)) {
                                matched = false;
                                break outerLoop;
                            }

                            int recipeAmount = getRecipeChoiceAmount(matrixItem, requiredChoice);
                            if (recipeAmount > 0) {
                                int possibleCrafts = matrixItem.getAmount() / recipeAmount;
                                offsetMaxCrafts = Math.min(offsetMaxCrafts, possibleCrafts);
                            }
                        }
                    }

                    if (matched) {
                        maxCrafts = offsetMaxCrafts;
                        // We limit the max crafts for avoid more than an stack
                        maxCrafts = Math.min(maxCrafts, maxPerStack);

                        // If cannot process all then limit to 1 craft
                        craftsToProcess = processAll ? maxCrafts : 1;

                        for (int row = 0; row < recipeRows; row++) {
                            String shapeRow = shape[row];
                            for (int col = 0; col < recipeCols; col++) {
                                char slot = shapeRow.charAt(col);
                                if (slot == ' ' || !ingredientMap.containsKey(slot)) continue;

                                int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset);
                                RecipeChoice requiredChoice = ingredientMap.get(slot);
                                ItemStack matrixItem = matrixResult[matrixIndex];
                                // We need to validate which choice matched to subtract the correct amount
                                if (requiredChoice instanceof RecipeChoice.ExactChoice exactChoice) {
                                    for (ItemStack choice : exactChoice.getChoices()) {
                                        if (validateIngredient(matrixItem, choice)) {
                                            int amountToSubtract = choice.getAmount() * craftsToProcess;
                                            matrixResult[matrixIndex] = Objects.requireNonNull(matrixItem).clone().subtract(amountToSubtract);
                                            break;
                                        }
                                    }
                                } else {
                                    int amountToSubtract = getRecipeChoiceAmount(matrixItem, requiredChoice) * craftsToProcess;
                                    matrixResult[matrixIndex] = Objects.requireNonNull(matrixItem).clone().subtract(amountToSubtract);
                                }
                            }
                        }
                        return Pair.of(matrixResult, craftsToProcess);
                    }
                }
            }
        } else if (craftingRecipe instanceof ShapelessRecipe shapelessRecipe) {
            // ShapelessRecipe Process
            List<RecipeChoice> recipeIngredients = shapelessRecipe.getChoiceList();
            int possibleCraftsTotal = Integer.MAX_VALUE;

            // Accurate calculation of maxCrafts for Shapeless
            // For each ingredient in the recipe, we sum all matching items in the matrix
            // and divide by the amount required by that specific ingredient/variant match.
            for (RecipeChoice ingredient : recipeIngredients) {
                int totalAvailableForIngredient = 0;
                int requiredAmount = 0;

                for (ItemStack item : matrix) {
                    if (item != null && validateIngredient(item, ingredient)) {
                        totalAvailableForIngredient += item.getAmount();
                        if (requiredAmount == 0) {
                            requiredAmount = getRecipeChoiceAmount(item, ingredient);
                        }
                    }
                }

                if (requiredAmount > 0) {
                    possibleCraftsTotal = Math.min(possibleCraftsTotal, totalAvailableForIngredient / requiredAmount);
                } else {
                    possibleCraftsTotal = 0;
                    break;
                }
            }

            maxCrafts = Math.min(possibleCraftsTotal, maxPerStack);
            craftsToProcess = processAll ? maxCrafts : 1;

            if (craftsToProcess > 0) {
                boolean[] usedSlots = new boolean[matrixResult.length];
                for (RecipeChoice ingredient : recipeIngredients) {
                    for (int i = 0; i < matrixResult.length; i++) {
                        if (usedSlots[i]) {
                            continue;
                        }
                        ItemStack matrixItem = matrixResult[i];
                        if (matrixItem != null && validateIngredient(matrixItem, ingredient)) {
                            int amountRequired = getRecipeChoiceAmount(matrixItem, ingredient);
                            int amountToSubtract = amountRequired * craftsToProcess;
                            matrixResult[i] = matrixItem.clone().subtract(amountToSubtract);
                            usedSlots[i] = true;
                            break;
                        }
                    }
                }
            }
        }

        // If the max craft is zero then return the matrix with 0 size
        if (craftsToProcess == 0) {
            return Pair.of(matrix, 0);
        }

        // Return the matrix updated and the max craft allowed
        return Pair.of(matrixResult, craftsToProcess);
    }

    /**
     * Clone this matrix.
     *
     * @param matrix the original matrix
     * @return a copied matrix
     */
    private static @Nullable ItemStack @Nullable [] cloneMatrix(final @Nullable ItemStack @Nullable [] matrix) {
        Objects.requireNonNull(matrix);
        @Nullable ItemStack[] clonedMatrix = new ItemStack[matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                clonedMatrix[i] = null; // Si el elemento es null, mantener null en la nueva matriz
            } else {
                clonedMatrix[i] = Objects.requireNonNull(matrix[i]).clone(); // Clonar cada ItemStack individualmente
            }
        }

        return clonedMatrix;
    }

}
