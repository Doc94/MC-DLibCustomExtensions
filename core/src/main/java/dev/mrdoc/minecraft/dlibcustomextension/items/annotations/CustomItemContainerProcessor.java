package dev.mrdoc.minecraft.dlibcustomextension.items.annotations;

import com.google.auto.service.AutoService;
import dev.mrdoc.minecraft.dlibcustomextension.items.classes.AbstractBaseCustomItem;
import dev.mrdoc.minecraft.dlibcustomextension.utils.LoggerUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * An abstract annotation processor
 */
@ApiStatus.Internal
@AutoService(Processor.class)
@SupportedAnnotationTypes(CustomItemContainer.ANNOTATION_PATH)
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class CustomItemContainerProcessor extends AbstractProcessor {

    /**
     * The file in which all items container names are stored.
     */
    public static final String PATH = "META-INF/items/" + CustomItemContainer.ANNOTATION_PATH;

    private @Nullable Messager messager;
    private @Nullable Elements elements;
    private @Nullable Types types;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<String> listElements = new ArrayList<>();
        final Set<? extends Element> elementsAnnotated = roundEnv.getElementsAnnotatedWith(CustomItemContainer.class);
        if (elementsAnnotated.isEmpty()) {
            return false; // Nothing to process...
        }
        for (Element element : elementsAnnotated) {
            if (!(element instanceof TypeElement typeElement)) {
                continue;
            }

            // Verify class requirement
            TypeElement baseItemType = Objects.requireNonNull(this.elements).getTypeElement(AbstractBaseCustomItem.class.getCanonicalName());
            if (!Objects.requireNonNull(this.types).isSubtype(typeElement.asType(), baseItemType.asType())) {
                Objects.requireNonNull(this.messager).printMessage(Diagnostic.Kind.ERROR, String.format("@CustomItemContainer-annotated class %s need extends from BaseItem", element));
                continue;
            }

            this.processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    String.format(
                            "Found valid @CustomItemContainer-annotated class: %s",
                            element
                    )
            );
            listElements.add(element.asType().toString());
        }

        this.writeCommandFile(listElements);
        return false;
    }

    @SuppressWarnings({"unused", "try"})
    private void writeCommandFile(final List<String> types) {
        try (BufferedWriter writer = new BufferedWriter(this.processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                PATH
        ).openWriter())) {
            for (final String t : types) {
                writer.write(t);
                writer.newLine();
            }
            writer.flush();
        } catch (final IOException e) {
            LoggerUtils.error("Cannot write the file %s".formatted(PATH), e);
        }
    }
}
