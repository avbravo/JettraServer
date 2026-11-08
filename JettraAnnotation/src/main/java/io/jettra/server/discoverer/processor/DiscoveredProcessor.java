package io.jettra.server.discoverer.processor;

import com.google.auto.service.AutoService;
import io.jettra.server.discoverer.Discovered;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("io.jettra.server.discoverer.Discovered")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
@AutoService(Processor.class)
public class DiscoveredProcessor extends AbstractProcessor {

    private final Map<String, Boolean> discoveredClasses = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("[DiscoveredProcessor] Processing round...");
        if (roundEnv.processingOver()) {
            System.out.println("[DiscoveredProcessor] Round over, writing classes: " + discoveredClasses.size());
            writeDiscoveredClasses();
            return true;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Discovered.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                Discovered annotation = typeElement.getAnnotation(Discovered.class);
                discoveredClasses.put(typeElement.getQualifiedName().toString(), annotation.automatic());
                System.out.println("[DiscoveredProcessor] Found: " + typeElement.getQualifiedName().toString());
            }
        }

        return true;
    }

    private void writeDiscoveredClasses() {
        if (discoveredClasses.isEmpty()) {
            return;
        }
        try {
            FileObject file = processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT, "", "META-INF/jettra/discovered.classes");
            try (Writer writer = file.openWriter()) {
                for (Map.Entry<String, Boolean> entry : discoveredClasses.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
