package me.mrdoc.minecraft.dlibcustomextension.commands;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;

@Getter
public class CommandManager {

    @Getter
    public static CommandManager instance;
    private final AnnotationParser<Source> annotationParser;
    private final PaperCommandManager<Source> commandManager;

    @SneakyThrows
    public CommandManager(Plugin plugin) {
        this.commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);
        this.annotationParser = new AnnotationParser<>(this.commandManager, Source.class);
        this.annotationParser.parseContainers();
    }

    public static void load(Plugin plugin) {
        if (instance != null) {
            return;
        }
        instance = new CommandManager(plugin);
    }

}
