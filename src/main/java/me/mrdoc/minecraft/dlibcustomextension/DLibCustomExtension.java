package me.mrdoc.minecraft.dlibcustomextension;

import me.mrdoc.minecraft.dlibcustomextension.commands.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DLibCustomExtension {

    private static JavaPlugin PLUGIN_INSTANCE;
    private static ClassLoader CLASS_LOADER;

    /**
     * Sets the classloader for this lib.
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
     * Sets the plugin instance for this lib.
     * <b>Note:</b> use this in the first moment you can (like onEnable)
     *
     * @param pluginInstance the plugin instance
     */
    public static void setPluginInstance(JavaPlugin pluginInstance) {
        if (PLUGIN_INSTANCE != null) {
            throw new RuntimeException("Plugin is already set!");
        }
        PLUGIN_INSTANCE = pluginInstance;
        CommandManager.load(pluginInstance);
    }

    public static JavaPlugin getPluginInstance() {
        return PLUGIN_INSTANCE;
    }
}