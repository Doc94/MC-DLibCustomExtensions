package dev.mrdoc.minecraft.dlibcustomextension.recipes;

import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Resolves the {@link RecipeCategory} declared with {@link RecipeCategoryOf} in the packages of a class.
 */
public final class RecipeCategoryResolver {

    private record CacheKey(ClassLoader loader, String packageName) {
    }

    private static final Map<CacheKey, Optional<RecipeCategory>> CACHE = new ConcurrentHashMap<>();

    private RecipeCategoryResolver() {
    }

    /**
     * Finds the category declared by the package of a class or its nearest annotated parent package.
     *
     * @param type the class
     * @return the category, or {@code null} if no package declares one
     */
    public static @Nullable RecipeCategory resolve(final Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        if (loader == null) {
            return null;
        }
        return resolve(loader, type.getPackageName());
    }

    private static @Nullable RecipeCategory resolve(final ClassLoader loader, final String packageName) {
        if (packageName.isEmpty()) {
            return null;
        }
        Optional<RecipeCategory> cached = CACHE.get(new CacheKey(loader, packageName));
        if (cached != null) {
            return cached.orElse(null);
        }
        RecipeCategory category = load(loader, packageName);
        CACHE.put(new CacheKey(loader, packageName), Optional.ofNullable(category));
        return category;
    }

    private static @Nullable RecipeCategory load(final ClassLoader loader, final String packageName) {
        int dot = packageName.lastIndexOf('.');
        RecipeCategory parent = resolve(loader, dot < 0 ? "" : packageName.substring(0, dot));

        RecipeCategoryOf annotation = readAnnotation(loader, packageName);
        if (annotation == null) {
            return parent;
        }

        try {
            Key key = Key.key(annotation.key());
            Component name = annotation.name().isBlank() ? Component.empty() : Component.translatable(annotation.name());
            RecipeCategory category = new RecipeCategory(key, name, parent);
            if (!annotation.icon().isBlank()) {
                Key sprite = Key.key(annotation.icon());
                category = annotation.atlas().isBlank() ? category.withIcon(sprite) : category.withIcon(Key.key(annotation.atlas()), sprite);
            }
            return category;
        } catch (RuntimeException exception) {
            LoggerUtils.warn("Invalid @RecipeCategoryOf in package [%s]: %s".formatted(packageName, exception.getMessage()));
            return parent;
        }
    }

    private static @Nullable RecipeCategoryOf readAnnotation(final ClassLoader loader, final String packageName) {
        try {
            return Class.forName(packageName + ".package-info", false, loader).getAnnotation(RecipeCategoryOf.class);
        } catch (ClassNotFoundException | LinkageError exception) {
            return null;
        }
    }
}
