package dev.mrdoc.minecraft.dlibcustomextension.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
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
 * Helper related to recipes
 */
@NullMarked
public class CustomItemRecipeHelper {

    @Nullable
    public static ItemStack getRecipeChoiceItemStack(@Nullable RecipeChoice recipeChoice) {
        if (recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
            return exactChoice.getItemStack();
        } else if (recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
            return materialChoice.getItemStack();
        }
        return null;
    }

    public static Map<Character, @Nullable ItemStack> getIngredientMap(Map<Character, RecipeChoice> ingredients) {
        HashMap<Character, @Nullable ItemStack> result = new HashMap<Character, ItemStack>();
        for (Map.Entry<Character, RecipeChoice> ingredient : ingredients.entrySet()) {
            final ItemStack itemStack = getRecipeChoiceItemStack(ingredient.getValue());
            if (itemStack == null) {
                result.put(ingredient.getKey(), null);
            } else {
                result.put(ingredient.getKey(), itemStack.clone());
            }
        }
        return result;
    }

    /**
     * Check the ingredients for a custom item.
     *
     * @param customItem the custom item
     * @param matrixCraft the matrix of ingredients
     * @return {@code true} if is valid
     */
    static boolean validateRecipeIngredients(AbstractCustomItem customItem, @Nullable ItemStack @Nullable [] matrixCraft) {
        if (customItem.getRecipe() == null) {
            return false;
        }
        return validateRecipeIngredients(customItem.getRecipe(), matrixCraft);
    }

    /**
     * Check the ingredients for a custom item.
     *
     * @param recipe the custom recipe
     * @param matrixCraft the matrix of ingredients
     * @return {@code true} if is valid
     */
    static boolean validateRecipeIngredients(Recipe recipe, @Nullable ItemStack @Nullable [] matrixCraft) {
        // We validate that the matrix is always of size 9 (3x3)
        if (matrixCraft == null || matrixCraft.length != 9) {
            return false;
        }

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            String[] shape = shapedRecipe.getShape();
            Map<Character, @Nullable ItemStack> ingredientMap = getIngredientMap(shapedRecipe.getChoiceMap());

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
                                ItemStack requiredItem = ingredientMap.get(slot);
                                if (!validateIngredient(matrixItem, requiredItem)) {
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
            List<ItemStack> ingredients = new ArrayList<>(shapelessRecipe.getChoiceList().stream().map(CustomItemRecipeHelper::getRecipeChoiceItemStack).map(itemStack -> (itemStack == null) ? ItemStack.empty() : itemStack).toList());

            for (ItemStack itemMatrix : matrixCraft) {
                if (itemMatrix != null && !itemMatrix.isEmpty()) {
                    boolean matched = false;

                    Iterator<ItemStack> iterator = ingredients.iterator();
                    while (iterator.hasNext()) {
                        ItemStack requiredItem = iterator.next();
                        if (validateIngredient(itemMatrix, requiredItem)) {
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

            if (!ingredients.isEmpty()) {
                return false; // If there are any unused ingredients left over, the array is invalid
            }
        }

        return true;
    }

    /**
     * Validate and compare an item with the recipe item ingredient.
     *
     * @param itemMatrix the item in craft matrix
     * @param itemInRecipe the item in the recipe
     * @return {@code true} if is valid
     */
    private static boolean validateIngredient(@Nullable ItemStack itemMatrix, @Nullable ItemStack itemInRecipe) {
        if (itemInRecipe == null || itemInRecipe.isEmpty()) {
            return true; // Slot not required
        }

        if (itemMatrix == null || itemMatrix.isEmpty() || itemMatrix.getAmount() < itemInRecipe.getAmount()) {
            return false;
        }

        if (!Objects.equals(CustomItemsManager.getInternalKey(itemInRecipe), CustomItemsManager.getInternalKey(itemMatrix))) {
            return false;
        }

        if (itemMatrix.hasData(DataComponentTypes.CUSTOM_MODEL_DATA) &&
                itemInRecipe.hasData(DataComponentTypes.CUSTOM_MODEL_DATA) &&
                itemMatrix.getData(DataComponentTypes.CUSTOM_MODEL_DATA) != itemInRecipe.getData(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            return false;
        }

        return true;
    }

    /**
     * Reduce a matrix using a recipe.
     *
     * @param craftingRecipe the craft recipe
     * @param matrix the matrix to reduce
     * @return a pair with the matrix result and the item result size
     */
    static Pair<ItemStack[], Integer> reduceMatrix(CraftingRecipe craftingRecipe, ItemStack[] matrix) {
        return reduceMatrix(craftingRecipe, matrix, false);
    }

    /**
     * Reduce a matrix using a recipe.
     *
     * @param craftingRecipe the craft recipe
     * @param matrix the matrix to reduce
     * @param processAll true for try the max of reduction
     * @return a pair with the matrix result and the item result size
     */
    static Pair<ItemStack[], Integer> reduceMatrix(CraftingRecipe craftingRecipe, final ItemStack @Nullable [] matrix, boolean processAll) {
        // We clone the matrix to work without modifying the original
        ItemStack[] matrixResult = cloneMatrix(matrix);
        int maxCrafts = Integer.MAX_VALUE; // We initialize with a very high value to reduce

        // We get the maximum allowed per stack from the recipe result
        int maxPerStack = craftingRecipe.getResult().getMaxStackSize();
        int craftsToProcess = 0;

        if (craftingRecipe instanceof ShapedRecipe shapedRecipe) {
            String[] shape = shapedRecipe.getShape();
            Map<Character, @Nullable ItemStack> ingredientMap = getIngredientMap(shapedRecipe.getChoiceMap());

            int recipeRows = shape.length;
            int recipeCols = shape[0].length();

            for (int rowOffset = 0; rowOffset <= 3 - recipeRows; rowOffset++) {
                for (int colOffset = 0; colOffset <= 3 - recipeCols; colOffset++) {
                    boolean matched = true;

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

                            ItemStack requiredItem = ingredientMap.get(slot);
                            if (requiredItem == null || !validateIngredient(matrixItem, requiredItem)) {
                                matched = false;
                                break outerLoop;
                            }

                            int possibleCrafts = matrixItem.getAmount() / requiredItem.getAmount();
                            maxCrafts = Math.min(maxCrafts, possibleCrafts);
                        }
                    }

                    if (matched) {
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
                                ItemStack requiredItem = ingredientMap.get(slot);
                                assert matrixResult != null;
                                ItemStack matrixItem = matrixResult[matrixIndex];
                                if (requiredItem != null && validateIngredient(matrixItem, requiredItem)) {
                                    int amountToSubtract = requiredItem.getAmount() * craftsToProcess;
                                    matrixResult[matrixIndex] = matrixItem.clone().subtract(amountToSubtract);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } else if (craftingRecipe instanceof ShapelessRecipe shapelessRecipe) {
            // ShapelessRecipe Process
            for (ItemStack ingredient : shapelessRecipe.getChoiceList().stream().map(CustomItemRecipeHelper::getRecipeChoiceItemStack).map(itemStack -> (itemStack == null) ? ItemStack.empty() : itemStack).toList()) {
                int totalAvailable = 0;

                for (ItemStack matrixItem : matrixResult) {
                    if (matrixItem != null && validateIngredient(matrixItem, ingredient)) {
                        totalAvailable += matrixItem.getAmount();
                    }
                }

                int possibleCrafts = totalAvailable / ingredient.getAmount();
                maxCrafts = Math.min(maxCrafts, possibleCrafts);
            }

            // We limit the max crafts for avoid more than an stack
            maxCrafts = Math.min(maxCrafts, maxPerStack);

            // If cannot process all then limit to 1 craft
            craftsToProcess = processAll ? maxCrafts : 1;

            // Reduce the matrix based in the craft allowed
            for (ItemStack ingredient : shapelessRecipe.getChoiceList().stream().map(CustomItemRecipeHelper::getRecipeChoiceItemStack).map(itemStack -> (itemStack == null) ? ItemStack.empty() : itemStack).toList()) {
                for (int i = 0; i < matrixResult.length; i++) {
                    ItemStack matrixItem = matrixResult[i];
                    if (matrixItem != null && validateIngredient(matrixItem, ingredient)) {
                        int amountToSubtract = Math.min(matrixItem.getAmount(), ingredient.getAmount() * craftsToProcess);
                        matrixResult[i] = matrixItem.clone().subtract(amountToSubtract);
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
    private static ItemStack @Nullable [] cloneMatrix(ItemStack @Nullable [] matrix) {
        assert matrix != null;
        ItemStack[] clonedMatrix = new ItemStack[matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] != null) {
                clonedMatrix[i] = matrix[i].clone(); // Clonar cada ItemStack individualmente
            } else {
                clonedMatrix[i] = null; // Si el elemento es null, mantener null en la nueva matriz
            }
        }

        return clonedMatrix;
    }

}
