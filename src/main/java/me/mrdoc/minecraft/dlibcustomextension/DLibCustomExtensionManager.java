package me.mrdoc.minecraft.dlibcustomextension;

import com.google.common.base.Preconditions;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.mrdoc.minecraft.dlibcustomextension.enchantments.CustomEnchantmentManager;
import me.mrdoc.minecraft.dlibcustomextension.i18n.TranslatesManager;
import me.mrdoc.minecraft.dlibcustomextension.items.CustomItemsManager;
import me.mrdoc.minecraft.dlibcustomextension.potions.CustomPotionsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@Getter
public class DLibCustomExtensionManager {

    private static DLibCustomExtensionManager INSTANCE;

    public static DLibCustomExtensionManager getInstance() {
        return INSTANCE;
    }

    public static Plugin getPluginInstance() {
        return INSTANCE.getPlugin();
    }

    public static String getPluginName() {
        return INSTANCE.getInstanceName();
    }

    public static String getPluginNamespace() {
        return INSTANCE.getInstanceName().toLowerCase();
    }

    public static DLibCustomExtensionManager buildWithBoostrap(ContextBoostrap boostrapContext) {
        INSTANCE = new DLibCustomExtensionManager(boostrapContext);
        return INSTANCE;
    }

    public static DLibCustomExtensionManager buildWithEnable(ContextPlugin pluginContext) {
        INSTANCE = new DLibCustomExtensionManager(pluginContext);
        return INSTANCE;
    }

    private final Context context;

    private DLibCustomExtensionManager(Context context) {
        this.context = context;
    }

    public ClassLoader getClassLoader() {
        return INSTANCE.getClass().getClassLoader();
    }

    public Plugin getPlugin() {
        if (this.context instanceof ContextPlugin contextPlugin) {
            return contextPlugin.pluginInstance();
        } else if (this.context instanceof ContextBoostrap contextBootstrap) {
            return Bukkit.getPluginManager().getPlugin(contextBootstrap.bootstrapContext().getPluginMeta().getName());
        }
        throw new RuntimeException("No context set!");
    }

    public String getInstanceName() {
        if (this.context instanceof ContextPlugin contextPlugin) {
            return contextPlugin.pluginInstance().getPluginMeta().getName();
        } else if (this.context instanceof ContextBoostrap contextBootstrap) {
            return contextBootstrap.bootstrapContext().getPluginMeta().getName();
        }
        throw new RuntimeException("No context set!");
    }

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