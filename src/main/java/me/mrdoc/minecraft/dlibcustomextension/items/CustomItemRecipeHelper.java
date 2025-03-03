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

public class CustomItemRecipeHelper {

    static boolean validateRecipeIngredients(AbstractCustomItem customItem, ItemStack[] matrixCraft) {
        return validateRecipeIngredients(customItem.getRecipe(), matrixCraft);
    }

    static boolean validateRecipeIngredients(Recipe recipe, ItemStack[] matrixCraft) {
        // Validamos que la matriz siempre sea de tamaño 9 (3x3)
        if (matrixCraft == null || matrixCraft.length != 9) {
            return false;
        }

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            String[] shape = shapedRecipe.getShape();
            Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();

            int rows = shape.length; // Filas de la receta
            int columns = shape[0].length(); // Columnas de la receta (asumimos receta bien formada)

            // Calculamos el desplazamiento (offset) para centrar la receta en la matriz 3x3
            int rowOffset = (3 - rows) / 2;
            int colOffset = (3 - columns) / 2;

            for (int row = 0; row < rows; row++) {
                String shapeRow = shape[row];

                for (int col = 0; col < columns; col++) {
                    char slot = shapeRow.charAt(col);
                    int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset); // Ajustamos por offset

                    if (slot == ' ' || !ingredientMap.containsKey(slot)) {
                        // Espacios vacíos en la receta deben corresponder a slots vacíos en la matriz
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
                return false; // Si sobran ingredientes sin usar, la matriz no es válida
            }
        }

        return true;
    }

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

    static Pair<ItemStack[], Integer> reduceMatrix(CraftingRecipe craftingRecipe, ItemStack[] matrix) {
        return reduceMatrix(craftingRecipe, matrix, false);
    }

    static Pair<ItemStack[], Integer> reduceMatrix(CraftingRecipe craftingRecipe, final ItemStack[] matrix, boolean processAll) {
        // Función para calcular el desplazamiento (offset) necesario para centrar los ingredientes
        final Function<Integer, Integer> calculateOffset = length -> (3 - length) / 2;

        // Clonamos la matriz para trabajar sin modificar el original
        ItemStack[] matrixResult = cloneMatrix(matrix);
        int maxCrafts = Integer.MAX_VALUE; // Inicializamos con un valor muy alto para minimizarlo.

        // Obtenemos el máximo permitido por pila del resultado de la receta
        int maxPerStack = craftingRecipe.getResult().getMaxStackSize();
        int craftsToProcess = 0;

        if (craftingRecipe instanceof ShapedRecipe shapedRecipe) {
            Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();
            String[] shape = shapedRecipe.getShape();

            int rows = shape.length; // Filas de la receta
            int columns = shape[0].length(); // Columnas de la receta (asumimos receta bien formada)

            // Calculamos los desplazamientos (offset) para centrar la receta en la matriz 3x3
            int rowOffset = (3 - rows) / 2;
            int colOffset = (3 - columns) / 2;

            // Determinamos el número máximo de crafteos posibles respetando cada slot
            for (int row = 0; row < rows; row++) {
                String shapeRow = shape[row];

                for (int col = 0; col < columns; col++) {
                    char slot = shapeRow.charAt(col);
                    int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset); // Ajustamos con offsets

                    if (slot == ' ' || !ingredientMap.containsKey(slot)) {
                        continue; // Ignoramos espacios vacíos
                    }

                    ItemStack requiredItem = ingredientMap.get(slot);
                    if (requiredItem == null || requiredItem.isEmpty()) {
                        continue; // Ignoramos ingredientes no definidos
                    }

                    ItemStack matrixItem = matrixResult[matrixIndex];
                    if (matrixItem == null || !validateIngredient(matrixItem, requiredItem)) {
                        maxCrafts = 0; // Si no hay el ingrediente requerido, no se puede craftear
                    } else {
                        int possibleCrafts = matrixItem.getAmount() / requiredItem.getAmount();
                        maxCrafts = Math.min(maxCrafts, possibleCrafts);
                    }
                }
            }

            // Aseguramos que el número de crafteos no exceda el máximo permitido por pila
            maxCrafts = Math.min(maxCrafts, maxPerStack);

            // Si no se permite procesar todo, limitamos a 1 crafteo
            craftsToProcess = processAll ? maxCrafts : 1;

            // Reducimos la matriz según la cantidad de crafteos posibles
            if (craftsToProcess > 0) {
                for (int row = 0; row < rows; row++) {
                    String shapeRow = shape[row];

                    for (int col = 0; col < columns; col++) {
                        char slot = shapeRow.charAt(col);
                        int matrixIndex = ((row + rowOffset) * 3) + (col + colOffset); // Ajustamos con offsets

                        if (slot == ' ' || !ingredientMap.containsKey(slot)) {
                            continue; // Ignoramos espacios vacíos
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
            // Procesamos las recetas sin forma
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

            // Aseguramos que el número de crafteos no exceda el máximo permitido por pila
            maxCrafts = Math.min(maxCrafts, maxPerStack);

            // Si no se permite procesar todo, limitamos a 1 crafteo
            craftsToProcess = processAll ? maxCrafts : 1;

            // Reducimos la matriz según la cantidad de crafteos permitidos
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

        // Si no hay crafteos posibles, devolvemos la matriz original y 0
        if (craftsToProcess == 0) {
            return Pair.of(matrix, 0);
        }

        // Retornamos la matriz actualizada y el número de crafteos permitidos
        return Pair.of(matrixResult, craftsToProcess);
    }

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
