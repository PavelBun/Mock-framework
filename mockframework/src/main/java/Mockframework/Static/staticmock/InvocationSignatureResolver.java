package Mockframework.Static.staticmock;

import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Optional;

import Mockframework.Static.StaticInvocation;

final class InvocationSignatureResolver {
    private InvocationSignatureResolver() {
    }

    static <T, R> Optional<String> resolve(Class<T> clazz, StaticInvocation<R> invocation) {
        SerializedLambda lambda = extractSerializedLambda(invocation);
        if (lambda == null) {
            return Optional.empty();
        }

        String implClassName = lambda.getImplClass().replace('/', '.');
        if (!clazz.getName().equals(implClassName)) {
            return Optional.empty();
        }
        if (lambda.getImplMethodKind() != MethodHandleInfo.REF_invokeStatic) {
            return Optional.empty();
        }

        MethodType methodType;
        try {
            methodType = MethodType.fromMethodDescriptorString(
                lambda.getImplMethodSignature(),
                clazz.getClassLoader()
            );
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        StringBuilder signature = new StringBuilder();
        signature.append(lambda.getImplMethodName()).append('(');
        for (int i = 0; i < methodType.parameterCount(); i++) {
            if (i > 0) {
                signature.append(',');
            }
            signature.append(methodType.parameterType(i).getName());
        }
        signature.append(')');

        return Optional.of(signature.toString());
    }

    private static SerializedLambda extractSerializedLambda(Object lambda) {
        try {
            Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object replacement = writeReplace.invoke(lambda);
            if (replacement instanceof SerializedLambda serializedLambda) {
                return serializedLambda;
            }
            return null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}

