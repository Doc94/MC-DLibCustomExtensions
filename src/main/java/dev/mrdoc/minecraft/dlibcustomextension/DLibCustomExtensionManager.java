package dev.mrdoc.minecraft.dlibcustomextension;

import com.google.common.base.Preconditions;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import dev.mrdoc.minecraft.dlibcustomextension.enchantments.CustomEnchantmentManager;
import dev.mrdoc.minecraft.dlibcustomextension.i18n.TranslatesManager;
import dev.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import dev.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Manager class for the DLibCustomExtensions library.
 * <p>
 * This class handles the initialization and lifecycle of custom items, potions, and enchantments.
 * It follows the singleton pattern and provides utility methods to access the plugin instance and namespace.
 * </p>
 */
@Getter
public class DLibCustomExtensionManager {

    private static DLibCustomExtensionManager INSTANCE;

    /**
     * Gets the singleton instance of the manager.
     *
     * @return the manager instance
     */
    public static DLibCustomExtensionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the plugin instance from the current manager singleton.
     *
     * @return the plugin instance
     */
    public static Plugin getPluginInstance() {
        return INSTANCE.getPlugin();
    }

    /**
     * Gets the plugin name from the current manager singleton.
     *
     * @return the plugin name
     */
    public static String getPluginName() {
        return INSTANCE.getInstanceName();
    }

    /**
     * Gets the plugin namespace (lowercase plugin name) from the current manager singleton.
     *
     * @return the plugin namespace
     */
    public static String getPluginNamespace() {
        return INSTANCE.getInstanceName().toLowerCase();
    }

    /**
     * Builds and initializes the manager instance using a bootstrap context.
     *
     * @param boostrapContext the bootstrap context to use
     * @return the initialized manager instance
     */
    public static DLibCustomExtensionManager buildWithBoostrap(ContextBoostrap boostrapContext) {
        INSTANCE = new DLibCustomExtensionManager(boostrapContext);
        return INSTANCE;
    }

    /**
     * Builds and initializes the manager instance using a plugin enable context.
     *
     * @param pluginContext the plugin context to use
     * @return the initialized manager instance
     */
    public static DLibCustomExtensionManager buildWithEnable(ContextPlugin pluginContext) {
        INSTANCE = new DLibCustomExtensionManager(pluginContext);
        return INSTANCE;
    }

    private final Context context;

    private DLibCustomExtensionManager(Context context) {
        this.context = context;
    }

    /**
     * Gets the class loader used by the manager instance.
     *
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return INSTANCE.getClass().getClassLoader();
    }

    /**
     * Gets the plugin instance associated with the current context.
     *
     * @return the plugin instance
     * @throws RuntimeException if no context is set
     */
    public Plugin getPlugin() {
        if (this.context instanceof ContextPlugin contextPlugin) {
            return contextPlugin.pluginInstance();
        } else if (this.context instanceof ContextBoostrap contextBootstrap) {
            return Bukkit.getPluginManager().getPlugin(contextBootstrap.bootstrapContext().getPluginMeta().getName());
        }
        throw new RuntimeException("No context set!");
    }

    /**
     * Gets the name of the plugin instance associated with the current context.
     *
     * @return the plugin name
     * @throws RuntimeException if no context is set
     */
    public String getInstanceName() {
        if (this.context instanceof ContextPlugin contextPlugin) {
            return contextPlugin.pluginInstance().getPluginMeta().getName();
        } else if (this.context instanceof ContextBoostrap contextBootstrap) {
            return contextBootstrap.bootstrapContext().getPluginMeta().getName();
        }
        throw new RuntimeException("No context set!");
    }

    /**
     * For call in onEnable plugin step.
     */
    public void onEnable() {
        Preconditions.checkState(this.context != null, "Context is null!");
        TranslatesManager.load();
        if (this.context.items()) {
            CustomItemsManager.load();
        }
        if (this.context.potions()) {
            CustomPotionsManager.load();
        }
        if (this.context instanceof ContextBoostrap contextBoostrap) {
            if (contextBoostrap.enchantments()) {
                CustomEnchantmentManager.onEnable(this.getPlugin());
            }
        }
    }

    /**
     * For call in boostrap plugin step.
     */
    public void onBoostrap() {
        Preconditions.checkState(this.context != null, "Context is null!");
        if (this.context instanceof ContextBoostrap contextBootstrap) {
            TranslatesManager.load();
            if (contextBootstrap.enchantments()) {
                CustomEnchantmentManager.load(contextBootstrap.bootstrapContext());
            }
        } else {
            throw new RuntimeException("Not ContextBoostrap set for this!");
        }
    }

    @Accessors(fluent = true)
    @Getter(AccessLevel.PACKAGE)
    public static sealed class Context permits ContextBoostrap, ContextPlugin {
        private final ClassLoader classLoader;
        private final boolean items;
        private final boolean potions;

        public Context(BaseBuilder<?,?> builder) {
            this.classLoader = builder.classLoader;
            this.items = builder.items;
            this.potions = builder.potions;
        }

        public static abstract class BaseBuilder<T extends BaseBuilder<T, C>, C extends Context> {
            private ClassLoader classLoader;
            private boolean items = false;
            private boolean potions = false;

            public T withItems() {
                items = true;
                return this.self();
            }

            public T withPotions() {
                potions = true;
                return this.self();
            }

            protected abstract T self();

            public abstract C build();
        }
    }

    @Accessors(fluent = true)
    @Getter(AccessLevel.PACKAGE)
    public static final class ContextPlugin extends Context {
        private final Plugin pluginInstance;

        public ContextPlugin(ContextPlugin.Builder builder) {
            super(builder);
            this.pluginInstance = builder.pluginInstance;
        }

        public static class Builder extends BaseBuilder<Builder, ContextPlugin> {
            private final Plugin pluginInstance;

            public static Builder create(Plugin pluginInstance) {
                return new Builder(pluginInstance);
            }

            private Builder(Plugin pluginInstance) {
                this.pluginInstance = pluginInstance;
            }

            @Override
            protected Builder self() {
                return this;
            }

            @Override
            public ContextPlugin build() {
                return new ContextPlugin(this);
            }
        }
    }

    @Accessors(fluent = true)
    @Getter(AccessLevel.PACKAGE)
    public static final class ContextBoostrap extends Context {
        private final BootstrapContext bootstrapContext;

        private final boolean enchantments;

        public ContextBoostrap(ContextBoostrap.Builder builder) {
            super(builder);
            this.bootstrapContext = builder.bootstrapContext;
            this.enchantments = builder.enchantments;
        }

        public static class Builder extends BaseBuilder<Builder, ContextBoostrap> {
            private final BootstrapContext bootstrapContext;
            private boolean enchantments = false;

            public static Builder create(BootstrapContext bootstrapContext) {
                return new Builder(bootstrapContext);
            }

            public Builder(BootstrapContext bootstrapContext) {
                this.bootstrapContext = bootstrapContext;
            }

            public Builder withEnchantments() {
                this.enchantments = true;
                return this;
            }

            @Override
            protected Builder self() {
                return this;
            }

            @Override
            public ContextBoostrap build() {
                return new ContextBoostrap(this);
            }
        }
    }
}
