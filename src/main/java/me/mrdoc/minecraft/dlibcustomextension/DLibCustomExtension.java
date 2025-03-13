package me.mrdoc.minecraft.dlibcustomextension;

import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import me.mrdoc.minecraft.dlibcustomextension.i18n.TranslatesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DLibCustomExtension {

    private static JavaPlugin PLUGIN_INSTANCE;
    private static ClassLoader CLASS_LOADER;

    /**
     * Sets the classloader for this lib.
     * <br>
     * <b>Note:</b> use this in the first moment you can (like boostrap or onEnable)
     *
     * @param classLoader the classLoader
     */
    public static void setClassLoader(ClassLoader classLoader) {
        CLASS_LOADER = classLoader;
    }

    public static ClassLoader getClassLoader() {
        return (CLASS_LOADER != null) ? CLASS_LOADER : DLibCustomExtension.class.getClassLoader();
    }

    /**
     * Handle the logic for boostrap enable.
     * <br>
     * <b>Note:</b> use this in the first moment you can (like bootstrap)
     *
     * @param pluginBootstrap the plugin boostrap instance
     */
    public static void onBoostrapEnable(PluginBootstrap pluginBootstrap) {
        if (CLASS_LOADER != null) {
            DLibCustomExtension.setClassLoader(pluginBootstrap.getClass().getClassLoader());
        }
    }

    /**
     * Handle the logic for plugin enable.
     * <br>
     * <b>Note:</b> use this in the first moment you can (like onEnable)
     *
     * @param pluginInstance the plugin instance
     */
    public static void onPluginEnable(JavaPlugin pluginInstance) {
        if (PLUGIN_INSTANCE != null) {
            throw new RuntimeException("Plugin is already set!");
        }
        PLUGIN_INSTANCE = pluginInstance;
        if (CLASS_LOADER != null) {
            DLibCustomExtension.setClassLoader(pluginInstance.getClass().getClassLoader());
        }
        TranslatesManager.load();
    }

    public static JavaPlugin getPluginInstance() {
        return PLUGIN_INSTANCE;
    }
}