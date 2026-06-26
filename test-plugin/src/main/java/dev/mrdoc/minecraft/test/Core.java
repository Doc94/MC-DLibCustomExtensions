package dev.mrdoc.minecraft.test;

import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

public class Core extends JavaPlugin {

    private static @Nullable Core instance;

    public static Core getInstance() {
        return Objects.requireNonNull(instance);
    }

    @Override
    public void onEnable() {
        instance = this;
        DLibCustomExtensionManager.getInstance().onEnable();
        this.getLogger().info("TestPlugin enabled!");
    }
}
