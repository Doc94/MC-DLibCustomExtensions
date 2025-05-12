package dev.mrdoc.minecraft.dlibcustomextension.items.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import dev.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;

public class ItemCustomCommandHelper {

    public static List<String> suggestItemCustom(CommandContext<Source> ctx, String input) {
        List<String> completions = new ArrayList<>();

        StringUtil.copyPartialMatches(input, CustomItemsManager.getNamespacedKeys().stream().map(NamespacedKey::getKey).collect(Collectors.toList()), completions);

        Collections.sort(completions);

        return completions;
    }

    public static ItemStack parserItemCustom(CommandContext<Source> ctx, CommandInput commandInput) {
        final String input = commandInput.readString();

        Optional<ItemStack> optionalItemStack = CustomItemsManager.getItem(input.toUpperCase());

        return optionalItemStack.orElseThrow(() -> new IllegalArgumentException("Custom Item ID \"%s\" invalid.".formatted(input)));
    }



}
