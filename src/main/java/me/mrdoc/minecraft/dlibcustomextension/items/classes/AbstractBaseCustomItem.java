package me.mrdoc.minecraft.dlibcustomextension.items.classes;

import com.google.common.base.Preconditions;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;
import me.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import me.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract sealed class AbstractBaseCustomItem permits AbstractCustomItem {

    @Getter
    private final Plugin instance;
    @Getter
    private final String internalName;
    private final NamespacedKey recipe_namespace;
    @Getter
    private final Recipe recipe;
    /**
     * El Item base usado para registro de recetas y validaciones.
     */
    @Getter
    private final ItemStack item;
    @Getter
    private boolean special;
    @Getter
    private CustomItemRarity rarity;
    @Getter
    private final HashSet<InventoryType> inventoryTypes = new HashSet<>();

    @ApiStatus.Internal
    public AbstractBaseCustomItem(Plugin instance, String internalName, Component displayName, CustomItemRarity rarity, boolean isSpecial, @Nullable final String modelName, List<InventoryType> inventoryTypes, List<Component> descriptions) {
        this.inventoryTypes.addAll(inventoryTypes);

        this.instance = instance;

        this.internalName = internalName;

        recipe_namespace = new NamespacedKey(instance, internalName);
        this.item = this.createItem();
        Validate.notNull(this.item, "El item creado no puede ser null");

        this.item.editPersistentDataContainer(persistentDataContainer -> persistentDataContainer.set(CustomItemsManager.getNamespacedKey(), PersistentDataType.STRING, recipe_namespace.toString()));

        if (displayName != null && !Component.empty().equals(displayName)) {
            this.item.setData(DataComponentTypes.ITEM_NAME, displayName.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }
        if (modelName != null && !modelName.isBlank()) {
            NamespacedKey namespacedModel = new NamespacedKey(instance, modelName);
            this.item.setData(DataComponentTypes.ITEM_MODEL, namespacedModel);
        }

        ArrayList<Component> loreComponents = new ArrayList<>();

        if (isSpecial) {
            this.special = true;
            loreComponents.add(Component.text("Item Especial", TextColor.fromHexString("#ac3fff")).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        if (!rarity.equals(CustomItemRarity.NONE)) {
            this.rarity = rarity;
            loreComponents.add(this.rarity.generateTag());
        }

        if (!descriptions.isEmpty()) {
            loreComponents.add(Component.empty());
            List<Component> descriptionProcessed = descriptions.stream().map(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList();
            loreComponents.addAll(descriptionProcessed);
        }

        this.item.setData(DataComponentTypes.LORE, ItemLore.lore().addLines(loreComponents).build());

        this.recipe = this.createRecipe();
    }

    public NamespacedKey getRecipeNamespace() {
        return recipe_namespace;
    }

    public boolean isEnabled() {
        return CustomItemsManager.isItemEnable(getInternalName());
    }

    /**
     * Metodo donde debe definirse el item
     *
     * @return Item a registrar.
     */
    public abstract ItemStack createItem();

    /**
     * Metodo donde debe definirse la receta
     *
     * @return Recipe valida o null para que no sea crafteable
     */
    public abstract Recipe createRecipe();

    public InventoryView createDisplayCraft(Player player) {
        Component titleInventoryView = Component.text().append(Component.text("Crafteo de")).appendSpace().append(this.getItem().displayName()).build();
        return switch (this.getRecipe()) {
            case null -> null;
            case ShapedRecipe shapedRecipe -> {
                InventoryView inventoryView = MenuType.CRAFTING.create(player, titleInventoryView);
                int pos = 1;
                for (String lineStr : shapedRecipe.getShape()) {
                    for (char character : lineStr.toCharArray()) {
                        RecipeChoice recipeChoice = shapedRecipe.getChoiceMap().get(character);
                        if (recipeChoice != null && recipeChoice.getItemStack() != null) {
                            inventoryView.getTopInventory().setItem(pos, recipeChoice.getItemStack());
                        }
                        //player.sendMessage(lineStr + " -> " + character + " " + pos + " " + shapedRecipe.getChoiceMap().get(character).getItemStack().getType());
                        pos = pos + 1;
                    }
                }
                yield inventoryView;
            }
            case ShapelessRecipe shapelessRecipe -> {
                InventoryView inventoryView = MenuType.CRAFTING.create(player, titleInventoryView);
                for (int pos = 1; pos <= shapelessRecipe.getChoiceList().size(); pos++) {
                    inventoryView.getTopInventory().setItem(pos, shapelessRecipe.getChoiceList().get(pos - 1).getItemStack());
                }
                yield inventoryView;
            }
            case SmithingTransformRecipe smithingTransformRecipe -> {
                InventoryView inventoryView = MenuType.SMITHING.create(player, titleInventoryView);
                if (!smithingTransformRecipe.getTemplate().equals(RecipeChoice.empty())) {
                    inventoryView.setItem(0, smithingTransformRecipe.getTemplate().getItemStack());
                }
                if (!smithingTransformRecipe.getBase().equals(RecipeChoice.empty())) {
                    inventoryView.setItem(1, smithingTransformRecipe.getBase().getItemStack());
                }
                if (!smithingTransformRecipe.getAddition().equals(RecipeChoice.empty())) {
                    inventoryView.setItem(2, smithingTransformRecipe.getAddition().getItemStack());
                }
                yield inventoryView;
            }
            default -> null;
        };
    }

    /**
     * Genera una copia del item para dar a jugadores.
     *
     * @return Item Custom
     */
    public ItemStack getItemForPlayer() {
        return this.getItem().clone();
    }

    /**
     * Genera una copia del item para dar a jugadores.
     *
     * @return Item Custom
     */
    public ItemStack getItemForPlayer(int quantity) {
        ItemStack itemStack = this.getItemForPlayer();
        itemStack.setAmount(quantity);
        return itemStack;
    }

    public void discoverRecipe(Player player) {
        if (this.recipe != null && !player.hasDiscoveredRecipe(this.getRecipeNamespace())) {
            player.discoverRecipe(this.getRecipeNamespace());
        }
    }

    public void undiscoverRecipe(Player player) {
        if (this.recipe != null && player.hasDiscoveredRecipe(this.getRecipeNamespace())) {
            player.undiscoverRecipe(this.getRecipeNamespace());
        }
    }

    public void registerRecipe() {
        if (this.recipe != null) {
            LoggerUtils.info("Adding recipe " + getRecipeNamespace().toString());
            Bukkit.getServer().addRecipe(getRecipe());
        }
    }

    public void unRegisterRecipe() {
        if (this.recipe != null) {
            LoggerUtils.info("Removing recipe " + getRecipeNamespace().toString());
            Bukkit.removeRecipe(getRecipeNamespace());
        }
    }

    /**
     * Revisa si el item ingresado corresponde al de la receta
     *
     * @param itemToCheck Item a validar
     * @return TRUE si corresponde al item custom
     */
    public boolean isItem(ItemStack itemToCheck) {
        if (itemToCheck == null || itemToCheck.getType().equals(Material.AIR) || itemToCheck.getAmount() <= 0) {
            return false;
        }
        String data = itemToCheck.getPersistentDataContainer().getOrDefault(CustomItemsManager.getNamespacedKey(), PersistentDataType.STRING, "");
        return itemToCheck.getType().equals(getItem().getType()) && data.equals(getRecipeNamespace().toString());
    }

    /**
     * Metodo de ayuda para saber si un item esta roto o no luego de aplicar daño.
     * <br>
     * <b>Nota:</b> Se considera que estara roto si el daño que recibe hace que supere el maximo de daño del item
     * @param itemToCheck Item a revisar
     * @param damage daño a recibir
     * @return TRUE si esta roto
     */
    public boolean isItemBroken(@NotNull ItemStack itemToCheck, int damage) {
        Preconditions.checkState(!itemToCheck.isEmpty(), "Item cannot be empty");
        return itemToCheck.hasData(DataComponentTypes.DAMAGE) && (itemToCheck.getData(DataComponentTypes.DAMAGE) + damage >= itemToCheck.getData(DataComponentTypes.MAX_DAMAGE));
    }

    /**
     * Metodo de ayuda para saber si un item esta roto o no.
     * <br>
     * <b>Nota:</b> Se considera roto si los usos restantes es menor o igual a 1
     * @param itemToCheck Item a revisar
     * @return TRUE si esta roto
     */
    public boolean isItemBroken(ItemStack itemToCheck) {
        return (itemToCheck == null || itemToCheck.isEmpty()) || (itemToCheck.hasData(DataComponentTypes.DAMAGE) && (itemToCheck.getData(DataComponentTypes.MAX_DAMAGE) - itemToCheck.getData(DataComponentTypes.DAMAGE) <= 1));
    }

    /**
     * Marca este item como roto dejando su daño al maximo
     * @param itemToBreak item a romper
     */
    public void brokeItem(@NotNull ItemStack itemToBreak) {
        Preconditions.checkState(!itemToBreak.isEmpty(), "Item cannot be empty");
        if (!itemToBreak.hasData(DataComponentTypes.MAX_DAMAGE)) {
            return;
        }
        itemToBreak.setData(DataComponentTypes.DAMAGE, itemToBreak.getData(DataComponentTypes.MAX_DAMAGE));
    }
}
