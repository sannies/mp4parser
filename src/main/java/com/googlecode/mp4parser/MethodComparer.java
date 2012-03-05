package com.googlecode.mp4parser;

import org.aspectj.lang.Signature;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodComparer {

    private final Method method;
    private String methodName = null;
    private List<String> parameterTypes = new ArrayList<String>();

    public MethodComparer(Method method) {
        this.method = method;

        parseMethodNameAndParameters();
    }

    private void parseMethodNameAndParameters() {
        methodName = method.getName();

        for (Class<?> parameterClass : method.getParameterTypes()) {
            parameterTypes.add(parameterClass.getSimpleName());
        }
    }

    public boolean hasSignature(Signature signature) {

        String methodNameOfSignature = parseMethodName(signature.toString());
        List<String> parameterTypesOfSignature = parseParameterTypes(signature.toString());

        return compareMethod(methodNameOfSignature, parameterTypesOfSignature);
    }

    private String parseMethodName(String signature) {

        String methodBeforeArguments = signature.substring(0, signature.indexOf("("));
        int positionOfMethodNameStart = methodBeforeArguments.lastIndexOf(".");

        return methodBeforeArguments.substring(positionOfMethodNameStart + 1);
    }

    private List<String> parseParameterTypes(String signature) {

        int positionOfParameterStart = signature.indexOf("(");
        int positionOfParameterEnd = signature.indexOf(")");

        String allParams = signature.substring(positionOfParameterStart + 1,
                positionOfParameterEnd);

        String[] paramsInArray = allParams.split(",");
        List<String> parameters = new ArrayList<String>();

        for (String parameter : paramsInArray) {
            parameters.add(parameter.trim());
        }

        return parameters;
    }

    private boolean compareMethod(String methodNameOfSignature, List<String> parameterTypesOfSignature) {

        if (!methodNameOfSignature.equalsIgnoreCase(this.methodName)) return false;

        if (parameterTypesOfSignature.size() != this.parameterTypes.size()) return false;

        for (String paramInSignature : parameterTypesOfSignature) {
            if (!this.parameterTypes.contains(paramInSignature)) {
                return false;
            }
        }

        return true;
    }
}