package me.mrdoc.minecraft.dlibcustomextension.enchantments;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PreFlattenTagRegistrar;
import io.papermc.paper.tag.TagEntry;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import me.mrdoc.minecraft.dlibcustomextension.DLibCustomExtensionManager;
import me.mrdoc.minecraft.dlibcustomextension.enchantments.annotations.CustomEnchantmentContainerProcessor;
import me.mrdoc.minecraft.dlibcustomextension.enchantments.classes.AbstractCustomEnchantment;
import me.mrdoc.minecraft.dlibcustomextension.utils.AnnotationProcessorUtil;
import me.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class CustomEnchantmentManager {
    public static HashMap<Key, AbstractCustomEnchantment> CUSTOM_ENCHANTMENTS = new HashMap<>();

    // Config
    private static final String CONFIG_FILE_NAME = "config-custom-enchantments.yaml";
    private static YamlConfigurationLoader CONFIG_LOADER;
    private static CommentedConfigurationNode CONFIG_NODE;
    private static CustomEnchantmentConfig CONFIG;

    public static void load(BootstrapContext context) {
        loadConfig(context);
        loadAllCustomEnchantments(context);
    }

    @SneakyThrows
    public static void saveConfig() {
        CONFIG_NODE.set(CustomEnchantmentConfig.class, CONFIG); // Update the backing node
        CONFIG_LOADER.save(CONFIG_NODE); // Write to the original file
    }

    @SneakyThrows
    private static void loadConfig(BootstrapContext context) {
        CONFIG_LOADER = YamlConfigurationLoader.builder()
                .path(new File(context.getDataDirectory().toFile(), CONFIG_FILE_NAME).toPath())
                .build();

        CONFIG_NODE = CONFIG_LOADER.load(); // Load from file
        CONFIG = CONFIG_NODE.get(CustomEnchantmentConfig.class); // Populate object
        saveConfig(); // force a save
    }

    @SneakyThrows
    public static void reloadConfig() {
        CONFIG_LOADER = YamlConfigurationLoader.builder()
                .path(new File(DLibCustomExtensionManager.getPluginInstance().getDataFolder(), CONFIG_FILE_NAME).toPath())
                .build();

        CONFIG_NODE = CONFIG_LOADER.load(); // Load from file
        CONFIG = CONFIG_NODE.get(CustomEnchantmentConfig.class); // Populate object
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static Set<Class<? extends AbstractCustomEnchantment>> getClasses(final @NonNull ClassLoader classLoader) {
        final List<String> classNames = AnnotationProcessorUtil.getClassesInPath(classLoader, CustomEnchantmentContainerProcessor.PATH);

        if (classNames.isEmpty()) {
            return Set.of();
        }

        final Set<Class<? extends AbstractCustomEnchantment>> classes = new HashSet<>();
        for (final String className : classNames) {
            classes.add((Class<? extends AbstractCustomEnchantment>) Class.forName(className, true, classLoader));
        }

        return classes;
    }

    private static void loadAllCustomEnchantments(@NotNull BootstrapContext context) {
        Set<Class<? extends AbstractCustomEnchantment>> reflectionCustomEnchantments = getClasses(DLibCustomExtensionManager.getInstance().getClassLoader());

        reflectionCustomEnchantments.forEach(aClass -> {
            try {
                AbstractCustomEnchantment abstractCustomEnchantment = aClass.getConstructor().newInstance();
                if (isEnchantmentEnabled(abstractCustomEnchantment.getName())) {
                    CUSTOM_ENCHANTMENTS.put(abstractCustomEnchantment.getKey(), abstractCustomEnchantment);

                    // Registry
                    context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(registryFreezeEvent -> {
                        registryFreezeEvent.registry().register(
                                abstractCustomEnchantment.getTypedKey(),
                                abstractCustomEnchantment.generateConsumerEREB(registryFreezeEvent).andThen(builder -> {
                                    builder.supportedItems(registryFreezeEvent.getOrCreateTag(ItemTypeTagKeys.create(abstractCustomEnchantment.getEnchantableKey())));
                                    if (abstractCustomEnchantment.useSupportedItemsForPrimaryItems()) {
                                        builder.primaryItems(null);
                                    } else if(!abstractCustomEnchantment.getTagsItemPrimaryTypes().isEmpty()) {
                                        builder.primaryItems(registryFreezeEvent.getOrCreateTag(ItemTypeTagKeys.create(abstractCustomEnchantment.getEnchantablePrimaryKey())));
                                    }
                                })
                        );
                    }));

                    // Registry in Vanilla TAG
                    if (!abstractCustomEnchantment.getTagsEnchantments().isEmpty()) {
                        LoggerUtils.info("The custom enchantment " + abstractCustomEnchantment.getKey().asString() + " has the following tags to be added: " + abstractCustomEnchantment.getTagsEnchantments().stream().map(enchantmentTagKey -> enchantmentTagKey.key().asString()).collect(Collectors.joining(", ")));
                        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT), event -> {
                            final PreFlattenTagRegistrar<Enchantment> registrar = event.registrar();
                            abstractCustomEnchantment.getTagsEnchantments().forEach(enchantmentTagKey -> registrar.addToTag(enchantmentTagKey, List.of(TagEntry.valueEntry(abstractCustomEnchantment.getTypedKey()))));
                        });
                    }

                    // Registry for items valid to be enchanted with abstractCustomEnchantment
                    LoggerUtils.info("The custom enchantment " + abstractCustomEnchantment.getKey().asString() + " has the following Supported ItemType available to be added: " + abstractCustomEnchantment.getTagsItemTypes().stream().map(itemTypeTagEntry -> itemTypeTagEntry.key().asString()).collect(Collectors.joining(", ")));
                    if (abstractCustomEnchantment.useSupportedItemsForPrimaryItems()) {
                        LoggerUtils.info("The custom enchantment " + abstractCustomEnchantment.getKey().asString() + " is going to use the previous list of supported items for Primary ItemType used in natural enchantment");
                    }
                    context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM), event -> {
                        final PreFlattenTagRegistrar<ItemType> registrar = event.registrar();
                        registrar.addToTag(TagKey.create(RegistryKey.ITEM, abstractCustomEnchantment.getEnchantableKey()), abstractCustomEnchantment.getTagsItemTypes());
                    });

                    if (!abstractCustomEnchantment.getTagsItemPrimaryTypes().isEmpty()) {
                        LoggerUtils.info("The custom enchantment " + abstractCustomEnchantment.getKey().asString() + " has the following Primary ItemType available to be added: " + abstractCustomEnchantment.getTagsItemPrimaryTypes().stream().map(itemTypeTagEntry -> itemTypeTagEntry.key().asString()).collect(Collectors.joining(", ")));
                        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM), event -> {
                            final PreFlattenTagRegistrar<ItemType> registrar = event.registrar();
                            registrar.addToTag(TagKey.create(RegistryKey.ITEM, abstractCustomEnchantment.getEnchantablePrimaryKey()), abstractCustomEnchantment.getTagsItemPrimaryTypes());
                        });
                    }

                    LoggerUtils.info("Loaded " + abstractCustomEnchantment.getKey().asString() + " for Custom Enchantment.");
                } else {
                    LoggerUtils.info("Ignored " + abstractCustomEnchantment.getKey().asString() + " for Custom Enchant. [DISABLED]");
                }

            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
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

    public static int getEnchantmentLevel(final Class<? extends AbstractCustomEnchantment> clazz, final ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return 0;
        }
        return CustomEnchantmentManager.getEnchantment(clazz).map(itemStack::getEnchantmentLevel).orElse(0);
    }

    public static boolean hasEnchantment(final Class<? extends AbstractCustomEnchantment> clazz, final ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        return CustomEnchantmentManager.getEnchantmentLevel(clazz, itemStack) > 0;
    }

    public static boolean isEnchantmentEnabled(String internalName) {
        if (!CONFIG.isEnabled()) {
            return true;
        }
        return CONFIG.getNameEnchantments().stream().anyMatch(internalName::equalsIgnoreCase);
    }

}
