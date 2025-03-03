package me.mrdoc.minecraft.dlibcustomextension.potions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtension;
import me.mrdoc.minecraft.dlibcustomextension.potions.commands.GivePotionCustomCommand;
import me.mrdoc.minecraft.dlibcustomextension.potions.potion.CustomPotionConfig;
import me.mrdoc.minecraft.dlibcustomextension.potions.potion.annotations.CustomPotionContainerProcessor;
import me.mrdoc.minecraft.dlibcustomextension.potions.potion.classes.AbstractBaseCustomPotion;
import me.mrdoc.minecraft.dlibcustomextension.potions.potion.classes.AbstractCustomPotion;
import me.mrdoc.minecraft.dlibcustomextension.utils.AnnotationProcessorUtil;
import me.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class CustomPotionsManager {

    private static JavaPlugin PLUGIN_INSTANCE;
    private static NamespacedKey NAMESPACED_CUSTOM_POTION;

    private static final HashSet<AbstractCustomPotion> CUSTOM_POTIONS = new HashSet<>();

    // Config
    private static final String CONFIG_FILE_NAME = "config-custom-potions.yaml";
    private static YamlConfigurationLoader CONFIG_LOADER;
    private static CommentedConfigurationNode CONFIG_NODE;
    private static CustomPotionConfig CONFIG;

    public static void load() {
        PLUGIN_INSTANCE = DLibCustomExtension.getPluginInstance();
        NAMESPACED_CUSTOM_POTION = new NamespacedKey(PLUGIN_INSTANCE, "custom_potion");
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
                .path(new File(DLibCustomExtension.getPluginInstance().getDataFolder(), CONFIG_FILE_NAME).toPath())
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
            classes.add((Class<? extends AbstractCustomPotion>) Class.forName(className, true, classLoader));
        }

        return classes;
    }

    public static void loadAllCustomPotions() {
        Set<Class<? extends AbstractCustomPotion>> reflectionCustomItems = getClasses(DLibCustomExtension.getClassLoader());

        reflectionCustomItems.forEach(aClass -> {
            try {
                AbstractCustomPotion baseItem = aClass.getConstructor().newInstance();
                CUSTOM_POTIONS.add(baseItem);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                LoggerUtils.warn("No se pudo cargar " + aClass.getSimpleName() + " como pocion custom. Detalles: " + e.getMessage(), e);
            }
        });
        LoggerUtils.info("Se cargaron " + CustomPotionsManager.CUSTOM_POTIONS.size() + " pociones custom.");
    }

    public static void registerAllRecipes() {
        for (AbstractCustomPotion basePotion : CUSTOM_POTIONS) {
            try {
                Bukkit.getPotionBrewer().addPotionMix(basePotion.getPotionMix());
            } catch (IllegalStateException | IllegalArgumentException ex) {
                LoggerUtils.warn("Error creando receta de pocion [" + basePotion.getPotionNamespace().getKey() + "]: " + ex.getMessage());
            }
        }
    }

    public static void unregisterAllRecipes() {
        for (AbstractBaseCustomPotion basePotion : CUSTOM_POTIONS) {
            Bukkit.getPotionBrewer().removePotionMix(basePotion.getPotionNamespace());
        }
    }

    public static void registerCommands() {
        new GivePotionCustomCommand();
    }

    /**
     * Obtiene la clase que genera el potion custom
     *
     * @param internalName Nombre interno
     * @return Optional del potion custom (puede ser casteado)
     */
    public static Optional<AbstractCustomPotion> getCustomItem(String internalName) {
        return CUSTOM_POTIONS.stream().filter(basePotion -> basePotion.getPotionNamespace().getKey().equalsIgnoreCase(internalName)).findFirst();
    }

    public static boolean isItemEnable(String internalName) {
        if (!CONFIG.isEnabled()) {
            return true;
        }
        return CONFIG.getNamePotions().stream().anyMatch(internalName::equalsIgnoreCase);
    }

    public static boolean isCustomItem(ItemStack item) {
        return !getInternalName(item).isEmpty();
    }

    /**
     * Obtiene el nombre interno de un item custom si es valido
     *
     * @param item ItemStack
     * @return Nombre interno del item custom o vacio si no es valido.
     */
    public static String getInternalName(ItemStack item) {
        if (item == null) {
            return "";
        }
        return item.getPersistentDataContainer().getOrDefault(NAMESPACED_CUSTOM_POTION, PersistentDataType.STRING, "");
    }

    public static HashSet<NamespacedKey> getNamespacedKeys() {
        return CUSTOM_POTIONS.stream().map(AbstractBaseCustomPotion::getPotionNamespace).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Obtiene un item para entregar a jugadores
     *
     * @param internalName Nombre interno del item custom
     * @return ItemStack para el jugador si el item existe.
     */
    public static Optional<ItemStack> getItem(String internalName) {
        return CUSTOM_POTIONS.stream().filter(baseItem -> baseItem.getPotionNamespace().getKey().equalsIgnoreCase(internalName)).map(AbstractBaseCustomPotion::getItemForPlayer).map(ItemStack::clone).findAny();
    }

}
