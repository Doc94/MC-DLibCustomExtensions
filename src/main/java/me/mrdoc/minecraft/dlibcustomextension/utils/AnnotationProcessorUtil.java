package me.mrdoc.minecraft.dlibcustomextension.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.Internal
@NullMarked
public class AnnotationProcessorUtil {

    public static List<String> getClassesInPath(ClassLoader classLoader, String path) {
        final List<String> classNames;
        try (InputStream stream = classLoader.getResourceAsStream(path)) {
            if (stream == null) {
                return List.of();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                classNames = reader.lines().distinct().toList();
            }
        } catch (Throwable t) {
            return List.of();
        }

        return classNames;
    }

}
