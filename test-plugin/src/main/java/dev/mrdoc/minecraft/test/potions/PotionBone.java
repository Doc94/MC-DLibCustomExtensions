package dev.mrdoc.minecraft.test.potions;

import dev.mrdoc.minecraft.dlibcustomextension.potions.annotations.CustomPotionContainer;
import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.AbstractCustomPotion;
import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.CustomPotionBuilder;
import dev.mrdoc.minecraft.test.Core;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

@CustomPotionContainer
public class PotionBone extends AbstractCustomPotion {

    private static final PotionContents POTION_CONTENTS;

    static {
        POTION_CONTENTS = PotionContents.potionContents()
                .customColor(Color.BLACK)
                .addCustomEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 60 * 4, 1, false, true, true))
                .build();
    }

    protected static void applyPotionContents(ItemStack itemStack) {
        itemStack.setData(DataComponentTypes.POTION_CONTENTS, POTION_CONTENTS);
    }

    public PotionBone() {
        super(
                CustomPotionBuilder.create(
                        Core.getInstance(),
                        "resistance_bone",
                        Component.text("Resistance", TextColor.fromHexString("#444438"))
                )
        );
    }

    @Override
    public ItemStack createItem() {
        ItemStack itemStack = ItemType.POTION.createItemStack();
        itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, 64);
        applyPotionContents(itemStack);
        return itemStack;
    }

    @Override
    public RecipeChoice createRecipeInput() {
        ItemStack itemStack = ItemType.POTION.createItemStack();
        PotionContents potionContents = PotionContents.potionContents().potion(PotionType.WATER).build();
        itemStack.setData(DataComponentTypes.POTION_CONTENTS, potionContents);
        return RecipeChoice.exactChoice(itemStack);
    }

    @Override
    public RecipeChoice createRecipeIngredient() {
        return RecipeChoice.itemType(ItemType.BONE);
    }

}
