package Mockframework.Static.verification;

import java.util.Arrays;
import java.util.Objects;

public final class StaticInvocationKey {
    private final Class<?> clazz;
    private final String methodSignature;
    private final Object[] args;

    public StaticInvocationKey(Class<?> clazz, String methodSignature, Object[] args) {
        this.clazz = Objects.requireNonNull(clazz, "clazz must not be null");
        this.methodSignature = Objects.requireNonNull(methodSignature, "methodSignature must not be null");
        this.args = args != null ? args.clone() : new Object[0];
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public Object[] getArgs() {
        return args.clone();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StaticInvocationKey that)) {
            return false;
        }
        return clazz.equals(that.clazz)
            && methodSignature.equals(that.methodSignature)
            && Arrays.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = clazz.hashCode();
        result = 31 * result + methodSignature.hashCode();
        result = 31 * result + Arrays.deepHashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "StaticInvocationKey{" +
            "clazz=" + clazz +
            ", methodSignature='" + methodSignature + '\'' +
            ", args=" + Arrays.deepToString(args) +
            '}';
    }
}
