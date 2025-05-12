package dev.mrdoc.minecraft.dlibcustomextension.enchantments;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CustomEnchantmentBoostrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        LoggerUtils.info("Loading Custom Enchantment Boostrap...");
        CustomEnchantmentManager.load(context);
    }

}