package me.mrdoc.minecraft.dlibcustomextension.commands;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.processing.CommandContainer;

/**
 * Base Command with the basic register thing
 */
@Getter
public class BaseCommand {

    private final JavaPlugin plugin;

    @SneakyThrows
    public BaseCommand(JavaPlugin javaPlugin) {
        this.plugin = javaPlugin;
        CommandManager.load(javaPlugin);
        if (!this.getClass().isAnnotationPresent(CommandContainer.class)) {
            CommandManager.getInstance().getAnnotationParser().parse(this);
        }
    }

}
