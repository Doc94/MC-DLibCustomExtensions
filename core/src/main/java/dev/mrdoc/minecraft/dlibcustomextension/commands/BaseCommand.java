package dev.mrdoc.minecraft.dlibcustomextension.commands;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base Command with the basic register thing
 */
@ApiStatus.Internal
@Getter
public class BaseCommand {

    private final Plugin plugin;

    @SneakyThrows
    public BaseCommand(Plugin plugin) {
        this.plugin = plugin;
        CommandManager.load(plugin);
        if (!this.getClass().isAnnotationPresent(CommandContainer.class) && CommandManager.getInstance() != null) {
            CommandManager.getInstance().getAnnotationParser().parse(this);
        }
    }

}
