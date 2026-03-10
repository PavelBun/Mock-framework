package ru.pozhidaev.mockframework.staticmock;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import ru.pozhidaev.mockframework.core.Answer;

public final class StaticMethodStub {
    private StaticMethodStub() {
    }

    @RuntimeType
    public static Object intercept(
        @Origin Method method,
        @AllArguments Object[] args,
        @SuperCall(nullIfImpossible = true) Callable<?> superCall
    ) throws Throwable {
        String methodSignature = methodSignature(method);
        if (StaticMockManager.isStubbingInProgress()) {
            StaticMockManager.captureInvocation(method.getDeclaringClass(), methodSignature);
            return defaultValue(method.getReturnType());
        }

        Answer answer = StaticMockManager
            .findMock(method.getDeclaringClass(), methodSignature)
            .orElse(null);
        if (answer != null) {
            return answer.answer(args);
        }

        if (superCall != null) {
            return superCall.call();
        }

        throw new IllegalStateException("Original static method is not callable: " + method);
    }

    private static String methodSignature(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getName()).append('(');
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(parameterTypes[i].getName());
        }
        builder.append(')');
        return builder.toString();
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == void.class) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0.0f;
        }
        if (returnType == double.class) {
            return 0.0d;
        }
        throw new IllegalStateException("Unsupported primitive return type: " + returnType.getName());
    }
}
