package Mockframework.Dynamic.handler;

import Mockframework.Core.matcher.ArgumentMatcher;
import Mockframework.Dynamic.registry.DynamicStubbingRegistry;
import Mockframework.Dynamic.registry.InvocationKey;
import Mockframework.Dynamic.verification.VerificationMode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VerificationInvocationHandler implements InvocationHandler {
    private final Object mock;
    private final VerificationMode mode;

    public VerificationInvocationHandler(Object mock, VerificationMode mode) {
        this.mock = mock;
        this.mode = mode;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Verifying " + method.getName() + " with args " + Arrays.toString(args));

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
        System.out.println("History size: " + history.size());

        try {
            mode.verify(history, expectedKey, matchers);
        } catch (Throwable t) {
            System.err.println("Error in verification: " + t);
            t.printStackTrace();
            throw t;
        }
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