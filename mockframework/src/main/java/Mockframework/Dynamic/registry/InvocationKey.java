package Mockframework.Dynamic.registry;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public final class InvocationKey {
    private final Object mock;
    private final Method method;
    private final Object[] args;

    public InvocationKey(Object mock, Method method, Object[] args) {
        this.mock = Objects.requireNonNull(mock, "mock");
        this.method = Objects.requireNonNull(method, "method");
        this.args = args != null ? args.clone() : new Object[0];
    }

    public Object getMock() {
        return mock;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvocationKey that = (InvocationKey) o;
        return mock == that.mock &&
                method.equals(that.method) &&
                Arrays.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = System.identityHashCode(mock);
        result = 31 * result + method.hashCode();
        result = 31 * result + Arrays.deepHashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "InvocationKey{" +
                "mock=" + mock +
                ", method=" + method +
                ", args=" + Arrays.deepToString(args) +
                '}';
    }
}