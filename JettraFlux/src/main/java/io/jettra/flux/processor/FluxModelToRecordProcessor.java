package io.jettra.flux.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import io.jettra.core.flux.FluxModelToRecordConversor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;

@AutoService(Processor.class)
@SupportedAnnotationTypes("io.jettra.core.flux.FluxModelToRecordConversor")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class FluxModelToRecordProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(FluxModelToRecordConversor.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                generateConversor((TypeElement) element);
            }
        }
        return true;
    }

    private void generateConversor(TypeElement modelElement) {
        FluxModelToRecordConversor annotation = modelElement.getAnnotation(FluxModelToRecordConversor.class);
        
        String packageName = processingEnv.getElementUtils().getPackageOf(modelElement).getQualifiedName().toString();
        String modelClassName = modelElement.getSimpleName().toString();
        
        TypeMirror goalType = getGoalType(annotation);
        TypeName recordTypeName;
        String recordClassName;
        
        if (goalType != null && !goalType.toString().equals("void")) {
            recordTypeName = TypeName.get(goalType);
            Element goalElement = processingEnv.getTypeUtils().asElement(goalType);
            if (goalElement != null) {
                recordClassName = goalElement.getSimpleName().toString();
            } else {
                String typeStr = goalType.toString();
                int lastDot = typeStr.lastIndexOf('.');
                recordClassName = lastDot != -1 ? typeStr.substring(lastDot + 1) : typeStr;
            }
        } else {
            recordClassName = modelClassName.endsWith("Model") ? modelClassName.substring(0, modelClassName.length() - 5) : modelClassName + "Record";
            recordTypeName = ClassName.get(packageName, recordClassName);
        }

        String conversorClassName = recordClassName + "ModelConverter";

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(conversorClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ClassName.get("io.jettra.scoped", "ApplicationScoped"));

        // toModel
        MethodSpec.Builder toModelBuilder = MethodSpec.methodBuilder("toModel")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(modelElement))
                .addParameter(recordTypeName, "record")
                .beginControlFlow("if (record == null)")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("$T model = new $T()", ClassName.get(modelElement), ClassName.get(modelElement));

        // toRecord
        MethodSpec.Builder toRecordBuilder = MethodSpec.methodBuilder("toRecord")
                .addModifiers(Modifier.PUBLIC)
                .returns(recordTypeName)
                .addParameter(ClassName.get(modelElement), "model")
                .beginControlFlow("if (model == null)")
                .addStatement("return null")
                .endControlFlow();

        List<String> recordParams = new ArrayList<>();

        // Find existing methods to detect getters/setters
        Set<String> methodNames = new HashSet<>();
        for (Element enclosed : modelElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD) {
                methodNames.add(enclosed.getSimpleName().toString());
            }
        }

        for (Element enclosed : modelElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement fieldElement = (VariableElement) enclosed;
                Set<Modifier> modifiers = fieldElement.getModifiers();
                
                if (modifiers.contains(Modifier.STATIC)) {
                    continue;
                }

                String fieldName = fieldElement.getSimpleName().toString();
                String capName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                
                String setterName = "set" + capName;
                String getterName = "get" + capName;
                if (fieldElement.asType().toString().equals("boolean") || fieldElement.asType().toString().equals("java.lang.Boolean")) {
                    getterName = "is" + capName;
                }
                
                // For toModel
                if (methodNames.contains(setterName)) {
                    toModelBuilder.addStatement("model.$L(record.$L())", setterName, fieldName);
                } else if (modifiers.contains(Modifier.PUBLIC)) {
                    toModelBuilder.addStatement("model.$L = record.$L()", fieldName, fieldName);
                } else {
                    // Fallback to setter anyway to show compile error or assuming it exists in superclass
                    toModelBuilder.addStatement("model.$L(record.$L())", setterName, fieldName);
                }
                
                // For toRecord
                if (methodNames.contains(getterName) || methodNames.contains("get" + capName)) {
                    String actualGetter = methodNames.contains(getterName) ? getterName : "get" + capName;
                    recordParams.add("model." + actualGetter + "()");
                } else if (modifiers.contains(Modifier.PUBLIC)) {
                    recordParams.add("model." + fieldName);
                } else {
                    recordParams.add("model." + getterName + "()");
                }
            }
        }

        toModelBuilder.addStatement("return model");
        
        toRecordBuilder.addStatement("return new $T(\n  $L\n)", recordTypeName, String.join(",\n  ", recordParams));

        classBuilder.addMethod(toModelBuilder.build());
        classBuilder.addMethod(toRecordBuilder.build());

        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeMirror getGoalType(FluxModelToRecordConversor annotation) {
        try {
            annotation.goal();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null;
    }
}
