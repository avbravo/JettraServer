package io.jettra.core.server.processor;

import com.google.auto.service.AutoService;
import io.jettra.core.server.Page;

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

@SupportedAnnotationTypes("io.jettra.core.server.Page")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
@AutoService(Processor.class)
public class PageProcessor extends AbstractProcessor {

    private final Map<String, String> pageClasses = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            writePageClasses();
            return true;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Page.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                Page annotation = typeElement.getAnnotation(Page.class);
                pageClasses.put(typeElement.getQualifiedName().toString(), annotation.path());
            }
        }

        return true;
    }

    private void writePageClasses() {
        if (pageClasses.isEmpty()) {
            return;
        }
        try {
            FileObject file = processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT, "", "META-INF/jettra/page.classes");
            try (Writer writer = file.openWriter()) {
                for (Map.Entry<String, String> entry : pageClasses.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
