package me.mrdoc.minecraft.dlibcustomextension.commands;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The command manager.
 */
@NullMarked
@Getter
public class CommandManager {

    @Nullable
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

    @SneakyThrows
    public CommandManager(BootstrapContext bootstrapContext) {
        this.commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildBootstrapped(bootstrapContext);
        this.annotationParser = new AnnotationParser<>(this.commandManager, Source.class);
        this.annotationParser.parseContainers();
    }

    /**
     * Load the manager using the Paper {@link BootstrapContext}
     *
     * @param bootstrapContext paper bootstrapContext
     */
    public static void load(BootstrapContext bootstrapContext) {
        if (instance != null) {
            return;
        }
        instance = new CommandManager(bootstrapContext);
    }

    /**
     * Load the manager using a generic bukkit {@link Plugin}
     *
     * @param plugin bukkit plugin
     */
    public static void load(Plugin plugin) {
        if (instance != null || !plugin.isEnabled()) {
            return;
        }
        instance = new CommandManager(plugin);
    }

}
