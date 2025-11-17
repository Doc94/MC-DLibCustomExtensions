package dev.mrdoc.minecraft.dlibcustomextension.enchantments;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.enchantments.annotations.CustomEnchantmentContainerProcessor;
import dev.mrdoc.minecraft.dlibcustomextension.enchantments.classes.AbstractCustomEnchantment;
import dev.mrdoc.minecraft.dlibcustomextension.utils.AnnotationProcessorUtil;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * Enchantment manager
 */
@NullMarked
public class CustomEnchantmentManager {
    public static HashMap<Key, AbstractCustomEnchantment> CUSTOM_ENCHANTMENTS = new HashMap<>();

    // Config
    private static final String CONFIG_FILE_NAME = "config-custom-enchantments.yaml";
    @Nullable
    private static YamlConfigurationLoader CONFIG_LOADER;
    @Nullable
    private static CommentedConfigurationNode CONFIG_NODE;
    @Nullable
    private static CustomEnchantmentConfig CONFIG;

    /**
     * Load the manager
     *
     * @param context the context
     */
    public static void load(BootstrapContext context) {
        loadConfig(context);
        loadAllCustomEnchantments(context);
    }

    /**
     * Save the config.
     */
    @SneakyThrows
    public static void saveConfig() {
        Objects.requireNonNull(CONFIG_NODE).set(CustomEnchantmentConfig.class, CONFIG); // Update the backing node
        Objects.requireNonNull(CONFIG_LOADER).save(CONFIG_NODE); // Write to the original file
    }

    /**
     * Load the config.
     *
     * @param context the context
     */
    @SneakyThrows
    private static void loadConfig(BootstrapContext context) {
        CONFIG_LOADER = YamlConfigurationLoader.builder()
                .path(new File(context.getDataDirectory().toFile(), CONFIG_FILE_NAME).toPath())
                .build();

        CONFIG_NODE = CONFIG_LOADER.load(); // Load from file
        CONFIG = CONFIG_NODE.get(CustomEnchantmentConfig.class); // Populate object
        saveConfig(); // force a save
    }

    /**
     * Reload the config.
     */
    @SneakyThrows
    public static void reloadConfig() {
        CONFIG_LOADER = YamlConfigurationLoader.builder()
                .path(new File(DLibCustomExtensionManager.getPluginInstance().getDataFolder(), CONFIG_FILE_NAME).toPath())
                .build();

        CONFIG_NODE = CONFIG_LOADER.load(); // Load from file
        CONFIG = CONFIG_NODE.get(CustomEnchantmentConfig.class); // Populate object
    }

    /**
     * Get the custom classes
     *
     * @param classLoader a class loader
     * @return a set of classes
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static Set<Class<? extends AbstractCustomEnchantment>> getClasses(final ClassLoader classLoader) {
        final List<String> classNames = AnnotationProcessorUtil.getClassesInPath(classLoader, CustomEnchantmentContainerProcessor.PATH);

        if (classNames.isEmpty()) {
            return Set.of();
        }

        final Set<Class<? extends AbstractCustomEnchantment>> classes = new HashSet<>();
        for (final String className : classNames) {
            try {
                Class<? extends AbstractCustomEnchantment> aClass = (Class<? extends AbstractCustomEnchantment>) Class.forName(className, true, classLoader);
                classes.add(aClass);
            } catch (Throwable e) {
                LoggerUtils.warn("Cannot get the class for %s".formatted(className), e);
            }

        }

        return classes;
    }

    private static void loadAllCustomEnchantments(BootstrapContext context) {
        Set<Class<? extends AbstractCustomEnchantment>> reflectionCustomEnchantments = getClasses(DLibCustomExtensionManager.getInstance().getClassLoader());

        reflectionCustomEnchantments.forEach(aClass -> {
            try {
                AbstractCustomEnchantment abstractCustomEnchantment = aClass.getConstructor().newInstance();
                CUSTOM_ENCHANTMENTS.put(abstractCustomEnchantment.getKey(), abstractCustomEnchantment);

                // Registry
                abstractCustomEnchantment.registerEnchantment(context);

                LoggerUtils.info("Loaded " + abstractCustomEnchantment.getKey().asString() + " for Custom Enchantment.");

            } catch (Exception e) {
                LoggerUtils.warn("Cannot load [%s] for Custom Enchantment".formatted(aClass.getSimpleName()), e);
            }
        });
        LoggerUtils.info("Loaded " + CustomEnchantmentManager.CUSTOM_ENCHANTMENTS.size() + " Custom Enchantments.");
    }

    public static void onEnable(Plugin plugin) {
        CUSTOM_ENCHANTMENTS.forEach((key, abstractCustomEnchantment) -> abstractCustomEnchantment.registerListener(plugin));
    }

    public static <T extends AbstractCustomEnchantment> Optional<T> getEnchantmentInstance(Class<T> clazz) {
        return CUSTOM_ENCHANTMENTS.values().stream().filter(abstractCustomEnchantment -> abstractCustomEnchantment.getClass().equals(clazz)).map(clazz::cast).findFirst();
    }

    public static <T extends AbstractCustomEnchantment> Optional<Enchantment> getEnchantment(Class<T> clazz) {
        return CUSTOM_ENCHANTMENTS.values().stream().filter(abstractCustomEnchantment -> abstractCustomEnchantment.getClass().equals(clazz)).map(clazz::cast).map(AbstractCustomEnchantment::getEnchantment).findFirst();
    }

    public static <T extends AbstractCustomEnchantment> Optional<ItemStack> getEnchantmentBook(Class<T> clazz, int level) {
        return CUSTOM_ENCHANTMENTS.values().stream().filter(abstractCustomEnchantment -> abstractCustomEnchantment.getClass().equals(clazz)).map(clazz::cast).map(enchantmentInstance -> enchantmentInstance.generateEnchantmentBook(level)).findFirst();
    }

    public static <T extends AbstractCustomEnchantment> Optional<ItemStack> getEnchantmentBook(Class<T> clazz) {
        return CustomEnchantmentManager.getEnchantmentBook(clazz, 1);
    }

    public static int getEnchantmentLevel(final Class<? extends AbstractCustomEnchantment> clazz, @Nullable final ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return 0;
        }
        return CustomEnchantmentManager.getEnchantment(clazz).map(itemStack::getEnchantmentLevel).orElse(0);
    }

    public static boolean hasEnchantment(final Class<? extends AbstractCustomEnchantment> clazz, @Nullable final ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return CustomEnchantmentManager.getEnchantmentLevel(clazz, itemStack) > 0;
    }

    public static boolean isEnchantmentEnabled(String internalName) {
        if (CONFIG != null && !CONFIG.isEnabled()) {
            return true;
        }
        return CONFIG != null && CONFIG.getNameEnchantments().stream().anyMatch(internalName::equalsIgnoreCase);
    }

}
