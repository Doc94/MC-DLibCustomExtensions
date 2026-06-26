package dev.mrdoc.minecraft.test;

import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import lombok.SneakyThrows;

public class CoreBootstrap implements PluginBootstrap {

    @SneakyThrows
    @Override
    public void bootstrap(BootstrapContext context) {
        try {
            LoggerUtils.info("Loading DLIB boostrap for test plugin");
            DLibCustomExtensionManager.buildWithBoostrap(DLibCustomExtensionManager.ContextBoostrap.Builder.create(context).withEnchantments().withItems().withPotions().build());
            DLibCustomExtensionManager.getInstance().onBoostrap();
        } catch (Exception exception) {
            LoggerUtils.error("Ocurrio un error al cargar el CoreBootstrap, el servidor sera apagado por seguridad", exception);
            System.exit(1);
        }
    }

}
