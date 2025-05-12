package dev.mrdoc.minecraft.dlibcustomextension.commands;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.jspecify.annotations.NullMarked;

/**
 * Base Command with the basic register thing
 */
@NullMarked
@Getter
public class BaseCommand {

    private final Plugin plugin;

    @SneakyThrows
    public BaseCommand(Plugin Plugin) {
        this.plugin = Plugin;
        CommandManager.load(Plugin);
        if (!this.getClass().isAnnotationPresent(CommandContainer.class)) {
            CommandManager.getInstance().getAnnotationParser().parse(this);
        }
    }

}
