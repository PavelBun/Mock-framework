package Mockframework.Dynamic.handler;

import Mockframework.Core.matcher.ArgumentMatcher;
import Mockframework.Dynamic.registry.DynamicStubbingRegistry;
import Mockframework.Dynamic.registry.InvocationKey;
import Mockframework.Dynamic.verification.VerificationMode;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class VerificationInterceptor {
    private final Object mock;
    private final VerificationMode mode;

    public VerificationInterceptor(Object mock, VerificationMode mode) {
        this.mock = mock;
        this.mode = mode;
    }

    @RuntimeType
    public Object intercept(@This Object proxy, @Origin Method method, @AllArguments Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            String name = method.getName();
            if ("toString".equals(name)) {
                return "Verification proxy for " + mock;
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(mock);
            } else if ("equals".equals(name)) {
                return mock == args[0];
            }
            return null;
        }

        // List<ArgumentMatcher> matchers = DynamicStubbingRegistry.getInstance().consumeMatchersForVerification();
        List<ArgumentMatcher> matchers = Collections.emptyList();        InvocationKey expectedKey = new InvocationKey(mock, method, args);
        List<InvocationKey> history = DynamicStubbingRegistry.getInstance().getHistory(mock);
        mode.verify(history, expectedKey, matchers);
        return defaultValue(method.getReturnType());
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        return null;
    }
}