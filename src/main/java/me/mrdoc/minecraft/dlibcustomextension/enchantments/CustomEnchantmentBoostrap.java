package me.mrdoc.minecraft.dlibcustomextension.enchantments;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import me.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;

public class CustomEnchantmentBoostrap implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        LoggerUtils.info("Loading Custom Enchantment Boostrap...");
        CustomEnchantmentManager.load(context);
    }

}