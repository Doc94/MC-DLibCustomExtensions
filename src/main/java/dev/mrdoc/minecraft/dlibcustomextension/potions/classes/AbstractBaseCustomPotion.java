package dev.mrdoc.minecraft.dlibcustomextension.potions.classes;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.potion.PotionMix;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import dev.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public abstract sealed class AbstractBaseCustomPotion permits AbstractCustomPotion {

    @Getter
    private final Plugin instance;
    @Getter
    private final String internalName;
    @Getter
    private final NamespacedKey potionNamespace;
    private final RecipeChoice recipeInput;
    private final RecipeChoice recipeIngredient;
    private final ItemStack item;
    @Getter
    private final PotionMix potionMix;

    public AbstractBaseCustomPotion(Plugin plugin, String internalName, Component displayName, List<Component> descriptions) {
        this.instance = plugin;
        this.internalName = internalName;

        potionNamespace = new NamespacedKey(instance, internalName);
        this.item = createItem();
        Validate.notNull(this.item, "El item creado para BasePotion no puede ser null");

        this.item.editPersistentDataContainer(persistentDataContainer -> persistentDataContainer.set(CustomPotionsManager.getNamespacedKey(), PersistentDataType.STRING, this.potionNamespace.toString()));

        if (displayName != null && !displayName.equals(Component.empty())) {
            this.item.setData(DataComponentTypes.ITEM_NAME, displayName);
        }

        ArrayList<Component> lore = new ArrayList<>();

        if (!descriptions.isEmpty()) {
            lore.add(Component.empty());
            lore.addAll(descriptions);
        }

        this.item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

        this.recipeInput = createRecipeInput();
        this.recipeIngredient = createRecipeIngredient();
        this.potionMix = createPotionMix();
    }

    /**
     * Metodo donde debe definirse el item
     * @return Item a registrar.
     */
    public abstract ItemStack createItem();

    /**
     * Lo que debe estar en los 3 slots del fondo
     * @return Input
     */
    public abstract RecipeChoice createRecipeInput();

    /**
     * El ingrediente de entrada
     * @return Ingredient
     */
    public abstract RecipeChoice createRecipeIngredient();

    private PotionMix createPotionMix() {
        return new PotionMix(this.potionNamespace, this.item, this.recipeInput, this.recipeIngredient);
    }

    /**
     * Obtiene el Item base usado para registro de recetas y validaciones.
     * El item es final por lo que no debería modificarse.
     * @return Item Custom
     */
    public ItemStack getItem() {
        return item;
    }

    public ItemStack getItemForPlayer() {
        return getItem().clone();
    }

}
