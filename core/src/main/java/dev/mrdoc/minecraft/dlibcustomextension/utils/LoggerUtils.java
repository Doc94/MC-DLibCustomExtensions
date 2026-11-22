package dev.mrdoc.minecraft.dlibcustomextension.utils;

import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiStatus.Internal
public class LoggerUtils {

    private static boolean DEBUG = false;

    public static Logger getLogger() {
        return LoggerFactory.getLogger("DLibCustomExtensionLogger");
    }

    public static void setDebug(final boolean status) {
        DEBUG = status;
        debug("Set debug mode to %s".formatted(status));
    }

    public static boolean isDebugEnabled() {
        return DEBUG;
    }

    public static void debug(final String message) {
        if (!DEBUG) {
            return;
        }
        getLogger().info("[DEBUG] [{}] {}", getCallerClassSimpleName(), message);
    }

    public static void info(final JavaPlugin javaPlugin, final Component message) {
        javaPlugin.getComponentLogger().info(Component.text("[{}] {}"), getCallerClassSimpleName(), message);
    }

    public static void info(final String message) {
        getLogger().info("[{}] {}", getCallerClassSimpleName(), message);
    }

    public static void info(final String message, final Throwable throwable) {
        getLogger().info("[%s] %s".formatted(getCallerClassSimpleName(), message), throwable);
    }

    public static void warn(final String message) {
        getLogger().warn("[{}] {}", getCallerClassSimpleName(), message);
    }

    public static void warn(final String message, final Throwable throwable) {
        getLogger().warn("[%s] %s".formatted(getCallerClassSimpleName(), message), throwable);
    }

    public static void error(final String message) {
        getLogger().error("[{}] {}", getCallerClassSimpleName(), message);
    }

    public static void error(final String message, final Throwable throwable) {
        getLogger().error("[%s] %s".formatted(getCallerClassSimpleName(), message), throwable);
    }

    private static String getCallerClassSimpleName() {
        Class<?> callerClass = getCallerClass();
        return callerClass == null ? "Unknown" : callerClass.getSimpleName();
    }

    private static @Nullable Class<?> getCallerClass() {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        Optional<Class<?>> callerClass = walker.walk(stream ->
                stream
                        .skip(2)
                        .findFirst()
                        .map(StackWalker.StackFrame::getDeclaringClass)
        );
        return callerClass.orElse(null);
    }


}
