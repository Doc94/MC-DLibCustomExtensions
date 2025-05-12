package dev.mrdoc.minecraft.dlibcustomextension.utils;

import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
@ApiStatus.Internal
public class LoggerUtils {

    private static boolean DEBUG = false;

    public static Logger getLogger() {
        return LoggerFactory.getLogger("DLibCustomExtensionLogger");
    }

    public static void setDebug(boolean status) {
        DEBUG = status;
        debug("Set debug mode to %s".formatted(status));
    }

    public static boolean isDebugEnabled() {
        return DEBUG;
    }

    public static void debug(String message) {
        if (!DEBUG) {
            return;
        }
        getLogger().info("[DEBUG] [{}] {}", getCallerClass().getSimpleName(), message);
    }

    public static void info(JavaPlugin javaPlugin, Component message) {
        javaPlugin.getComponentLogger().info(Component.text("[{}] {}"), getCallerClass().getSimpleName(), message);
    }

    public static void info(String message) {
        getLogger().info("[{}] {}", getCallerClass().getSimpleName(), message);
    }

    public static void info(String message, Throwable throwable) {
        getLogger().info("[%s] %s".formatted(getCallerClass().getSimpleName(), message), throwable);
    }

    public static void warn(String message) {
        getLogger().warn("[{}] {}", getCallerClass().getSimpleName(), message);
    }

    public static void warn(String message, Throwable throwable) {
        getLogger().warn("[%s] %s".formatted(getCallerClass().getSimpleName(), message), throwable);
    }

    public static void error(String message) {
        getLogger().error("[{}] {}", getCallerClass().getSimpleName(), message);
    }

    public static void error(String message, Throwable throwable) {
        getLogger().error("[%s] %s".formatted(getCallerClass().getSimpleName(), message), throwable);
    }

    @Nullable
    public static Class<?> getCallerClass() {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        Optional<Class<?>> callerClass = walker.walk(stream ->
                stream
                        .skip(2) // Salta los dos primeros frames para obtener la clase que lo llamo
                        .findFirst()
                        .map(StackWalker.StackFrame::getDeclaringClass)
        );
        return callerClass.orElse(null); // Retorna null si no se encuentra ningún frame
    }


}
