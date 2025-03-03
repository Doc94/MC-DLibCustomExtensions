package me.mrdoc.minecraft.dlibcustomextension.potions.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;

public class PotionCustomCommandHelper {
    static List<String> suggestItemCustom(CommandContext<Source> ctx, String input) {
        List<String> completions = new ArrayList<>();

        StringUtil.copyPartialMatches(input, CustomPotionsManager.getNamespacedKeys().stream().map(NamespacedKey::getKey).collect(Collectors.toList()), completions);

        Collections.sort(completions);

        return completions;
    }

    static ItemStack parserItemCustom(CommandContext<Source> ctx, CommandInput commandInput) {
        final String input = commandInput.readString();

        return CustomPotionsManager.getItem(input.toUpperCase()).orElseThrow(() -> new IllegalArgumentException("El item potion con ID \"%s\" no es valido.".formatted(input)));
    }
}
