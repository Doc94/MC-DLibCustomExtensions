package me.mrdoc.minecraft.dlibcustomextension.items;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import me.mrdoc.minecraft.dlibcustomextension.items.annotations.CustomItemContainer;
import me.mrdoc.minecraft.dlibcustomextension.items.annotations.CustomItemContainerProcessor;
import me.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractCustomItem;
import me.mrdoc.minecraft.dlibcustomextension.items.commands.DisplayItemCustomCommand;
import me.mrdoc.minecraft.dlibcustomextension.items.commands.GiveItemCustomCommand;
import me.mrdoc.minecraft.dlibcustomextension.utils.AnnotationProcessorUtil;
import me.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class CustomItemsManager {

    private static Plugin PLUGIN_INSTANCE;
    private static NamespacedKey NAMESPACED_CUSTOM_ITEM;
    private static final HashSet<AbstractCustomItem> CUSTOM_ITEMS = new HashSet<>();

    // Config
    private static final String CONFIG_FILE_NAME = "config-custom-items.yaml";
    private static YamlConfigurationLoader CONFIG_LOADER;
    private static CommentedConfigurationNode CONFIG_NODE;
    private static CustomItemConfig CONFIG;


    public static void load() {
        PLUGIN_INSTANCE = DLibCustomExtensionManager.getPluginInstance();
        NAMESPACED_CUSTOM_ITEM = new NamespacedKey(PLUGIN_INSTANCE, "custom_item");
        loadConfig();
        loadAllCustomItems();
        registerAllRecipes();
        registerAllCommands();
        Bukkit.getPluginManager().registerEvents(new CustomItemsListener(), PLUGIN_INSTANCE);
    }

    public static NamespacedKey getNamespacedKey() {
        return NAMESPACED_CUSTOM_ITEM;
    }

    @SneakyThrows
    public static void saveConfig() {
        CONFIG_NODE.set(CustomItemConfig.class, CONFIG); // Update the backing node
        CONFIG_LOADER.save(CONFIG_NODE); // Write to the original file
    }

    @SneakyThrows
    private static void loadConfig() {
        CONFIG_LOADER = YamlConfigurationLoader.builder()
                .path(new File(PLUGIN_INSTANCE.getDataFolder(), CONFIG_FILE_NAME).toPath())
                .build();

        CONFIG_NODE = CONFIG_LOADER.load(); // Load from file
        CONFIG = CONFIG_NODE.get(CustomItemConfig.class); // Populate object
        saveConfig(); // force a save
    }

    @SneakyThrows
    public static void reloadConfig() {
        CONFIG_LOADER = YamlConfigurationLoader.builder()
                .path(new File(DLibCustomExtensionManager.getPluginInstance().getDataFolder(), CONFIG_FILE_NAME).toPath())
                .build();

        CONFIG_NODE = CONFIG_LOADER.load(); // Load from file
        CONFIG = CONFIG_NODE.get(CustomItemConfig.class); // Populate object
    }

    /**
     * Get the custom classes
     *
     * @param classLoader a class loader
     * @return a set of classes
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static Set<Class<? extends AbstractCustomItem>> getClasses(final @NonNull ClassLoader classLoader) {
        final List<String> classNames = AnnotationProcessorUtil.getClassesInPath(classLoader, CustomItemContainerProcessor.PATH);

        if (classNames.isEmpty()) {
            return Set.of();
        }

        final Set<Class<? extends AbstractCustomItem>> classes = new HashSet<>();
        for (final String className : classNames) {
            classes.add((Class<? extends AbstractCustomItem>) Class.forName(className, true, classLoader));
        }

        return classes;
    }

    public static void loadAllCustomItems() {
        Set<Class<? extends AbstractCustomItem>> reflectionCustomItems = getClasses(DLibCustomExtensionManager.getInstance().getClassLoader());

        Set<Class<? extends AbstractCustomItem>> loadedClasses = new HashSet<>();
        Set<Class<? extends AbstractCustomItem>> failedClasses = new HashSet<>();

        while (!reflectionCustomItems.isEmpty()) {
            Iterator<Class<? extends AbstractCustomItem>> iterator = reflectionCustomItems.iterator();

            while (iterator.hasNext()) {
                Class<? extends AbstractCustomItem> itemClass = iterator.next();
                CustomItemContainer annotation = itemClass.getAnnotation(CustomItemContainer.class);
                Class<? extends AbstractCustomItem>[] dependencies = annotation.depends();

                boolean dependenciesLoaded = Arrays.stream(dependencies)
                        .allMatch(dep -> loadedClasses.contains(dep) || (!annotation.strongDependency() && failedClasses.contains(dep)));

                if (dependenciesLoaded) {
                    try {
                        AbstractCustomItem customItem = itemClass.getConstructor().newInstance();
                        CUSTOM_ITEMS.add(customItem);  // Añadir directamente a CUSTOM_ITEMS
                        loadedClasses.add(itemClass);
                        iterator.remove();
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                             InvocationTargetException e) {
                        LoggerUtils.warn("Cannot load [%s] item custom".formatted(itemClass.getSimpleName()), e);
                        failedClasses.add(itemClass);
                        iterator.remove();
                    }
                }
            }
        }

        LoggerUtils.info("Loaded " + CustomItemsManager.CUSTOM_ITEMS.size() + " custom items.");
    }

    /**
     * Gets the class for a custom item.
     *
     * @param internalName custom item name
     * @return an Optional
     */
    public static Optional<AbstractCustomItem> getCustomItem(String internalName) {
        if (internalName == null || internalName.isEmpty()) {
            return Optional.empty();
        }
        return CUSTOM_ITEMS.stream().filter(baseItem -> baseItem.getRecipeNamespace().getKey().equalsIgnoreCase(internalName) || baseItem.getRecipeNamespace().toString().equalsIgnoreCase(internalName)).findFirst();
    }

    public static <T extends AbstractCustomItem> Optional<T> getCustomItem(Class<T> baseItemClass) {
        return CUSTOM_ITEMS.stream().filter(baseItem -> baseItem.getClass().equals(baseItemClass)).map(baseItemClass::cast).findFirst();
    }

    public static Optional<AbstractCustomItem> getCustomItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return Optional.empty();
        }

        return CustomItemsManager.getCustomItem(getInternalName(itemStack));
    }

    public static boolean isItemEnable(String internalName) {
        if (!CONFIG.isEnabled()) {
            return true;
        }
        return CONFIG.getNameItems().stream().anyMatch(internalName::equalsIgnoreCase);
    }

    public static boolean isCustomItem(ItemStack item) {
        return !CustomItemsManager.getInternalName(item).isEmpty();
    }

    /**
     * Gets the internal name if is valid.
     *
     * @param item ItemStack
     * @return the internal name or empty
     */
    public static String getInternalName(ItemStack item) {
        if (item == null) {
            return "";
        }
        return item.getPersistentDataContainer().getOrDefault(CustomItemsManager.NAMESPACED_CUSTOM_ITEM, PersistentDataType.STRING, "");
    }

    public static HashSet<NamespacedKey> getNamespacedKeys() {
        return CUSTOM_ITEMS.stream().map(AbstractCustomItem::getRecipeNamespace).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Gets the item to give for players.
     *
     * @param internalName custom item name
     * @return an Optional
     */
    public static Optional<ItemStack> getItem(String internalName) {
        return CUSTOM_ITEMS.stream().filter(baseItem -> baseItem.getRecipeNamespace().getKey().equalsIgnoreCase(internalName)).map(AbstractCustomItem::getItemForPlayer).map(ItemStack::clone).findAny();
    }

    /**
     * Gets the item to give for players.
     *
     * @param baseItemClass custom item instance
     * @return an Optional
     */
    public static <T extends AbstractCustomItem> Optional<ItemStack> getItem(Class<T> baseItemClass) {
        return CustomItemsManager.getCustomItem(baseItemClass).map(AbstractCustomItem::getItemForPlayer).map(ItemStack::clone);
    }

    public static boolean isRegisterRecipe(Recipe recipe) {
        return CUSTOM_ITEMS.stream().anyMatch(baseRecipe -> baseRecipe.getRecipe().equals(recipe));
    }

    public static void handleAvailableRecipes(Player player) {
        for (AbstractCustomItem customItem : CUSTOM_ITEMS) {
            if (customItem.isEnabled()) {
                customItem.discoverRecipe(player);
            } else {
                customItem.undiscoverRecipe(player);
            }
        }
    }

    public static List<Recipe> getRecipes() {
        return CUSTOM_ITEMS.stream().map(AbstractCustomItem::getRecipe).collect(Collectors.toList());
    }

    public static void registerAllCommands() {
        new GiveItemCustomCommand();
        new DisplayItemCustomCommand();
    }

    public static void registerAllRecipes() {
        for (AbstractCustomItem customItem : CUSTOM_ITEMS) {
            try {
                customItem.registerRecipe();
            } catch (IllegalStateException ex) {
                LoggerUtils.warn("Cannot register the recipe for [%s]".formatted(customItem.getRecipeNamespace().getKey()), ex);
            }
        }
    }

    public static void unregisterAllRecipes() {
        for (AbstractCustomItem customItem : CUSTOM_ITEMS) {
            customItem.unRegisterRecipe();
        }
    }

}
