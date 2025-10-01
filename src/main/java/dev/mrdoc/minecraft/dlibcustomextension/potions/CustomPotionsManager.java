package dev.mrdoc.minecraft.dlibcustomextension.potions;

import dev.mrdoc.minecraft.dlibcustomextension.potions.commands.DisplayPotionCustomCommand;
import dev.mrdoc.minecraft.dlibcustomextension.utils.persistence.PersistentDataKey;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import dev.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import dev.mrdoc.minecraft.dlibcustomextension.potions.commands.GivePotionCustomCommand;
import dev.mrdoc.minecraft.dlibcustomextension.potions.annotations.CustomPotionContainerProcessor;
import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.AbstractBaseCustomPotion;
import dev.mrdoc.minecraft.dlibcustomextension.potions.classes.AbstractCustomPotion;
import dev.mrdoc.minecraft.dlibcustomextension.utils.AnnotationProcessorUtil;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * Manages custom potions within the plugin, overseeing their loading, configuration, and interaction.
 * Handles registration, configuration management, and access for custom potions.
 */
@NullMarked
public class CustomPotionsManager {

    private static Plugin PLUGIN_INSTANCE;
    private static NamespacedKey NAMESPACED_CUSTOM_POTION;

    private static final HashSet<AbstractCustomPotion> CUSTOM_POTIONS = new HashSet<>();

    // Config
    private static final String CONFIG_FILE_NAME = "config-custom-potions.yaml";
    private static YamlConfigurationLoader CONFIG_LOADER;
    private static CommentedConfigurationNode CONFIG_NODE;
    private static CustomPotionConfig CONFIG;

    public static void load() {
        PLUGIN_INSTANCE = DLibCustomExtensionManager.getPluginInstance();
        NAMESPACED_CUSTOM_POTION = new NamespacedKey(PLUGIN_INSTANCE, "custom_potion");
        loadConfig();
        loadAllCustomPotions();
        registerAllRecipes();
        registerCommands();
    }

    public static NamespacedKey getNamespacedKey() {
        return NAMESPACED_CUSTOM_POTION;
    }

    @SneakyThrows
    public static void saveConfig() {
        CONFIG_NODE.set(CustomPotionConfig.class, CONFIG); // Update the backing node
        CONFIG_LOADER.save(CONFIG_NODE); // Write to the original file
    }

    @SneakyThrows
    private static void loadConfig() {
        CONFIG_LOADER = YamlConfigurationLoader.builder()
                .path(new File(PLUGIN_INSTANCE.getDataFolder(), CONFIG_FILE_NAME).toPath())
                .build();

        CONFIG_NODE = CONFIG_LOADER.load(); // Load from file
        CONFIG = CONFIG_NODE.get(CustomPotionConfig.class); // Populate object
        saveConfig(); // force a save
    }

    @SneakyThrows
    public static void reloadConfig() {
        CONFIG_LOADER = YamlConfigurationLoader.builder()
                .path(new File(DLibCustomExtensionManager.getPluginInstance().getDataFolder(), CONFIG_FILE_NAME).toPath())
                .build();

        CONFIG_NODE = CONFIG_LOADER.load(); // Load from file
        CONFIG = CONFIG_NODE.get(CustomPotionConfig.class); // Populate object
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static Set<Class<? extends AbstractCustomPotion>> getClasses(final @NonNull ClassLoader classLoader) {
        final List<String> classNames = AnnotationProcessorUtil.getClassesInPath(classLoader, CustomPotionContainerProcessor.PATH);

        if (classNames.isEmpty()) {
            return Set.of();
        }

        final Set<Class<? extends AbstractCustomPotion>> classes = new HashSet<>();
        for (final String className : classNames) {
            try {
                Class<? extends AbstractCustomPotion> aClass = (Class<? extends AbstractCustomPotion>) Class.forName(className, true, classLoader);
                classes.add(aClass);
            } catch (Throwable e) {
                LoggerUtils.warn("Cannot get the class for %s".formatted(className), e);
            }
        }

        return classes;
    }

    public static void loadAllCustomPotions() {
        Set<Class<? extends AbstractCustomPotion>> reflectionCustomItems = getClasses(DLibCustomExtensionManager.getInstance().getClassLoader());

        reflectionCustomItems.forEach(aClass -> {
            try {
                AbstractCustomPotion baseItem = aClass.getConstructor().newInstance();
                CUSTOM_POTIONS.add(baseItem);
            } catch (Exception e) {
                LoggerUtils.warn("Cannot load the custom potion " + aClass.getSimpleName() + ". Details: " + e.getMessage(), e);
            }
        });
        LoggerUtils.info("Loaded " + CustomPotionsManager.CUSTOM_POTIONS.size() + " custom potions.");
    }

    public static void registerAllRecipes() {
        for (AbstractCustomPotion basePotion : CUSTOM_POTIONS) {
            try {
                basePotion.registerPotionMix();
            } catch (IllegalStateException | IllegalArgumentException ex) {
                LoggerUtils.warn("Cannot create potion recipe [" + basePotion.getKey().asString() + "]: " + ex.getMessage());
            }
        }
    }

    public static void unregisterAllRecipes() {
        for (AbstractBaseCustomPotion basePotion : CUSTOM_POTIONS) {
            basePotion.unRegisterPotionMix();
        }
    }

    public static void registerCommands() {
        new GivePotionCustomCommand();
        new DisplayPotionCustomCommand();
    }

    /**
     * Gets the instance for the custom potion.
     *
     * @param internalName Internal name
     * @return an Optional
     */
    public static Optional<AbstractCustomPotion> getCustomPotion(String internalName) {
        return CUSTOM_POTIONS.stream().filter(basePotion -> basePotion.getKey().value().equalsIgnoreCase(internalName) || basePotion.getKey().toString().equalsIgnoreCase(internalName)).findFirst();
    }

    /**
     * Gets the instance for the custom potion.
     *
     * @param baseItemClass the class of custom potion
     * @return an Optional
     */
    public static <T extends AbstractCustomPotion> Optional<T> getCustomPotion(Class<T> baseItemClass) {
        return CUSTOM_POTIONS.stream().filter(baseItem -> baseItem.getClass().equals(baseItemClass)).map(baseItemClass::cast).findFirst();
    }

    public static boolean isItemEnable(String internalName) {
        if (!CONFIG.isEnabled()) {
            return true;
        }
        return CONFIG.getNamePotions().stream().anyMatch(internalName::equalsIgnoreCase);
    }

    public static boolean isCustomItem(ItemStack item) {
        return getInternalKey(item) != null;
    }

    /**
     * Gets the internal potion name of this item if is valid.
     *
     * @param item ItemStack
     * @return the internal key or null
     */
    @Nullable
    public static Key getInternalKey(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }
        if (!item.getPersistentDataContainer().has(NAMESPACED_CUSTOM_POTION)) {
            return null;
        }
        return item.getPersistentDataContainer().get(NAMESPACED_CUSTOM_POTION, PersistentDataKey.KEY_CONTAINER);
    }

    public static HashSet<NamespacedKey> getNamespacedKeys() {
        return CUSTOM_POTIONS.stream().map(AbstractBaseCustomPotion::getNamespaceKey).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Gets the item to give for players.
     *
     * @param baseItemClass custom potion instance
     * @return an Optional
     */
    public static <T extends AbstractCustomPotion> Optional<ItemStack> getItem(Class<T> baseItemClass) {
        return CustomPotionsManager.getCustomPotion(baseItemClass).map(AbstractBaseCustomPotion::getItemForPlayer).map(ItemStack::clone);
    }

    /**
     * Gets the item to give for players.
     *
     * @param internalName custom potion internal name
     * @return an Optional
     */
    public static Optional<ItemStack> getItem(String internalName) {
        return CUSTOM_POTIONS.stream().filter(basePotion -> basePotion.getKey().value().equalsIgnoreCase(internalName) || basePotion.getKey().toString().equalsIgnoreCase(internalName)).map(AbstractBaseCustomPotion::getItemForPlayer).map(ItemStack::clone).findAny();
    }

}
