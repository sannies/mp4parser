package com.googlecode.mp4parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MethodAnnotationChecker {

    private final Method methodToCheck;

    public MethodAnnotationChecker(Method method) {
        this.methodToCheck = method;
    }

    public boolean containsAnnotation(String annotation) {
        Annotation[] annotations = methodToCheck.getAnnotations();

        for (Annotation ant : annotations) {
            if (annotationMatches(ant, annotation)) return true;
        }

        return false;
    }

    private boolean annotationMatches(Annotation antFromMethod, String antFromUser) {

        String justAnnotationNameFromUserInput = extractAnnotationNameFromUserInput(antFromUser);
        String justAnnotationNameFromMethod = extractAnnotationNameFromMethod(antFromMethod);

        return justAnnotationNameFromMethod.equalsIgnoreCase(justAnnotationNameFromUserInput);
    }

    private String extractAnnotationNameFromMethod(Annotation antFromMethod) {

        int endOfAnnotationName = antFromMethod.toString().indexOf("(");
        String upToAnnotationName = antFromMethod.toString().substring(0, endOfAnnotationName);
        return upToAnnotationName.substring(upToAnnotationName.lastIndexOf(".") + 1);
    }

    private String extractAnnotationNameFromUserInput(String antFromUser) {

        int endOfAnnotationName = antFromUser.toString().indexOf("(");
        return antFromUser.substring(1, endOfAnnotationName);
    }
}