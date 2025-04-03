package me.mrdoc.minecraft.dlibcustomextension.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import me.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jspecify.annotations.NullMarked;

/**
 * Helper related to recipes
 */
public class CustomItemRecipeHelper {

    /**
     * Check the ingredients for a custom item.
     *
     * @param customItem the custom item
     * @param matrixCraft the matrix of ingredients
     * @return {@code true} if is valid
     */
    static boolean validateRecipeIngredients(AbstractCustomItem customItem, ItemStack[] matrixCraft) {
        return validateRecipeIngredients(customItem.getRecipe(), matrixCraft);
    }

    /**
     * Check the ingredients for a custom item.
     *
     * @param recipe the custom recipe
     * @param matrixCraft the matrix of ingredients
     * @return {@code true} if is valid
     */
    static boolean validateRecipeIngredients(Recipe recipe, ItemStack[] matrixCraft) {
        // We validate that the matrix is always of size 9 (3x3)
        if (matrixCraft == null || matrixCraft.length != 9) {
            return false;
        }

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            String[] shape = shapedRecipe.getShape();
            Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();

            int rows = shape.length; // Recipe rows
            int columns = shape[0].length(); // Recipe columns (we assume well-formed recipe)

            // We calculate the offset to center the recipe in the 3x3 matrix
            int rowOffset = (3 - rows) / 2;
            int colOffset = (3 - columns) / 2;

            for (int row = 0; row < rows; row++) {
                String shapeRow = shape[row];

                for (int col = 0; col < columns; col++) {
                    char slot = shapeRow.charAt(col);
                    int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset); // We adjust by offset

                    if (slot == ' ' || !ingredientMap.containsKey(slot)) {
                        // Empty spaces in the recipe must correspond to empty slots in the array
                        if (matrixCraft[matrixIndex] != null && !matrixCraft[matrixIndex].isEmpty()) {
                            return false;
                        }
                        continue;
                    }

                    ItemStack requiredItem = ingredientMap.get(slot);
                    ItemStack matrixItem = matrixCraft[matrixIndex];

                    if (!validateIngredient(matrixItem, requiredItem)) {
                        return false;
                    }
                }
            }
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            List<ItemStack> ingredients = new ArrayList<>(shapelessRecipe.getIngredientList());

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
    private static boolean validateIngredient(ItemStack itemMatrix, ItemStack itemInRecipe) {
        if (itemInRecipe == null || itemInRecipe.isEmpty()) {
            return true; // No se requiere este slot
        }

        if (itemMatrix == null || itemMatrix.isEmpty() || itemMatrix.getAmount() < itemInRecipe.getAmount()) {
            return false;
        }

        if (!CustomItemsManager.getInternalName(itemInRecipe).equals(CustomItemsManager.getInternalName(itemMatrix))) {
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
    static Pair<ItemStack[], Integer> reduceMatrix(CraftingRecipe craftingRecipe, final ItemStack[] matrix, boolean processAll) {
        // We clone the matrix to work without modifying the original
        ItemStack[] matrixResult = cloneMatrix(matrix);
        int maxCrafts = Integer.MAX_VALUE; // We initialize with a very high value to reduce

        // We get the maximum allowed per stack from the recipe result
        int maxPerStack = craftingRecipe.getResult().getMaxStackSize();
        int craftsToProcess = 0;

        if (craftingRecipe instanceof ShapedRecipe shapedRecipe) {
            Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();
            String[] shape = shapedRecipe.getShape();

            int rows = shape.length; // Recipe rows
            int columns = shape[0].length(); // Recipe columns (we assume well-formed recipe)

            // Calculamos los desplazamientos (offset) para centrar la receta en la matriz 3x3
            int rowOffset = (3 - rows) / 2;
            int colOffset = (3 - columns) / 2;

            // Determinate the max crafts based in any slot
            for (int row = 0; row < rows; row++) {
                String shapeRow = shape[row];

                for (int col = 0; col < columns; col++) {
                    char slot = shapeRow.charAt(col);
                    int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset); // Adjust with offsets

                    if (slot == ' ' || !ingredientMap.containsKey(slot)) {
                        continue; // Ignore empty spaces
                    }

                    ItemStack requiredItem = ingredientMap.get(slot);
                    if (requiredItem == null || requiredItem.isEmpty()) {
                        continue; // Ignore ingredients not defined
                    }

                    ItemStack matrixItem = matrixResult[matrixIndex];
                    if (matrixItem == null || !validateIngredient(matrixItem, requiredItem)) {
                        maxCrafts = 0; // If not has a requirement ingredient then cannot craft
                    } else {
                        int possibleCrafts = matrixItem.getAmount() / requiredItem.getAmount();
                        maxCrafts = Math.min(maxCrafts, possibleCrafts);
                    }
                }
            }

            // We limit the max crafts for avoid more than an stack
            maxCrafts = Math.min(maxCrafts, maxPerStack);

            // If cannot process all then limit to 1 craft
            craftsToProcess = processAll ? maxCrafts : 1;

            // Reduce the matrix based in the craft allowed
            if (craftsToProcess > 0) {
                for (int row = 0; row < rows; row++) {
                    String shapeRow = shape[row];

                    for (int col = 0; col < columns; col++) {
                        char slot = shapeRow.charAt(col);
                        int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset); // Adjust with offsets

                        if (slot == ' ' || !ingredientMap.containsKey(slot)) {
                            continue; // Ignore empty spaces
                        }

                        ItemStack requiredItem = ingredientMap.get(slot);
                        ItemStack matrixItem = matrixResult[matrixIndex];
                        if (matrixItem != null && validateIngredient(matrixItem, requiredItem)) {
                            int amountToSubtract = requiredItem.getAmount() * craftsToProcess;
                            matrixResult[matrixIndex] = matrixItem.clone().subtract(amountToSubtract);
                        }
                    }
                }
            }
        } else if (craftingRecipe instanceof ShapelessRecipe shapelessRecipe) {
            // ShapelessRecipe Process
            for (ItemStack ingredient : shapelessRecipe.getIngredientList()) {
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
            for (ItemStack ingredient : shapelessRecipe.getIngredientList()) {
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
    private static ItemStack[] cloneMatrix(ItemStack[] matrix) {
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
