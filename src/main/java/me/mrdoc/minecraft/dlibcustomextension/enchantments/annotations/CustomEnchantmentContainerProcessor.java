package me.mrdoc.minecraft.dlibcustomextension.enchantments.annotations;

import com.google.auto.service.AutoService;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import me.mrdoc.minecraft.dlibcustomextension.enchantments.classes.AbstractBaseCustomEnchantment;
import org.jspecify.annotations.NonNull;

@AutoService(Processor.class)
@SupportedAnnotationTypes(CustomEnchantmentContainer.ANNOTATION_PATH)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class CustomEnchantmentContainerProcessor extends AbstractProcessor {

    /**
     * The file in which all enchantments container names are stored.
     */
    public static final String PATH = "META-INF/enchantments/" + CustomEnchantmentContainer.ANNOTATION_PATH;

    private Messager messager;
    private Elements elements;
    private Types types;

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
        final Set<? extends Element> elementsAnnotated = roundEnv.getElementsAnnotatedWith(CustomEnchantmentContainer.class);
        if (elementsAnnotated.isEmpty()) {
            return false; // Nothing to process...
        }
        for (Element element : elementsAnnotated) {
            if (!(element instanceof TypeElement typeElement)) {
                continue;
            }

            // Verifica que la clase extienda de AbstractCustomEnchantment
            TypeElement baseItemType = this.elements.getTypeElement(AbstractBaseCustomEnchantment.class.getCanonicalName());
            if (!this.types.isSubtype(typeElement.asType(), baseItemType.asType())) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, String.format("@CustomEnchantmentContainer-annotated class %s need extends from AbstractCustomEnchantment", element));
                continue;
            }

            this.processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    String.format(
                            "Found valid @CustomEnchantmentContainer-annotated class: %s",
                            element
                    )
            );
            listElements.add(element.asType().toString());
        }

        this.writeCommandFile(listElements);
        return false;
    }

    @SuppressWarnings({"unused", "try"})
    private void writeCommandFile(final @NonNull List<String> types) {
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
            e.printStackTrace();
        }
    }
}
