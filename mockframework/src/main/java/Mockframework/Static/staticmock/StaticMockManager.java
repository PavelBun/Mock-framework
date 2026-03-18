package Mockframework.Static.staticmock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import Mockframework.Core.Answer;

public final class StaticMockManager {
    private static final ThreadLocal<Map<MethodInvocationKey, Answer>> MOCKS =
        ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<StubbingContext> STUBBING = new ThreadLocal<>();

    private StaticMockManager() {
    }

    public static void enableMock(Class<?> clazz, String methodSignature, Answer answer) {
        enableMock(clazz, methodSignature, new Object[0], answer);
    }

    public static void enableMock(Class<?> clazz, String methodSignature, Object[] args, Answer answer) {
        MethodInvocationKey key = keyOf(clazz, methodSignature, args);
        MOCKS.get().put(key, Objects.requireNonNull(answer, "answer must not be null"));
    }

    public static void disableMock(Class<?> clazz, String methodSignature) {
        disableMock(clazz, methodSignature, new Object[0]);
    }

    public static void disableMock(Class<?> clazz, String methodSignature, Object[] args) {
        MethodInvocationKey key = keyOf(clazz, methodSignature, args);
        Map<MethodInvocationKey, Answer> state = MOCKS.get();
        state.remove(key);
        cleanupIfEmpty(state);
    }

    public static void disableMock(Class<?> clazz, String methodSignature, Answer answer) {
        disableMock(clazz, methodSignature, new Object[0], answer);
    }

    public static void disableMock(Class<?> clazz, String methodSignature, Object[] args, Answer answer) {
        MethodInvocationKey key = keyOf(clazz, methodSignature, args);
        Map<MethodInvocationKey, Answer> state = MOCKS.get();
        state.remove(key, Objects.requireNonNull(answer, "answer must not be null"));
        cleanupIfEmpty(state);
    }

    public static Optional<Answer> findMock(Class<?> clazz, String methodSignature) {
        return findMock(clazz, methodSignature, new Object[0]);
    }

    public static Optional<Answer> findMock(Class<?> clazz, String methodSignature, Object[] args) {
        MethodInvocationKey key = keyOf(clazz, methodSignature, args);
        return Optional.ofNullable(MOCKS.get().get(key));
    }

    public static void clear() {
        MOCKS.remove();
        STUBBING.remove();
    }

    static void beginStubbing(Class<?> expectedClass) {
        Objects.requireNonNull(expectedClass, "expectedClass must not be null");
        if (STUBBING.get() != null) {
            throw new IllegalStateException("Nested static stubbing is not supported");
        }
        STUBBING.set(new StubbingContext(expectedClass));
    }

    static CapturedInvocation finishStubbing() {
        StubbingContext context = STUBBING.get();
        if (context == null) {
            throw new IllegalStateException("Static stubbing was not started");
        }
        STUBBING.remove();
        return context.invocation();
    }

    public static boolean isStubbingInProgress() {
        return STUBBING.get() != null;
    }

    public static void captureInvocation(Class<?> clazz, String methodSignature) {
        captureInvocation(clazz, methodSignature, new Object[0]);
    }

    public static void captureInvocation(Class<?> clazz, String methodSignature, Object[] args) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        Objects.requireNonNull(methodSignature, "methodSignature must not be null");

        StubbingContext context = STUBBING.get();
        if (context == null) {
            return;
        }

        if (!context.expectedClass().equals(clazz)) {
            throw new IllegalStateException(
                "Expected static invocation on " + context.expectedClass().getName() +
                " but captured " + clazz.getName() + "." + methodSignature
            );
        }

        context.capture(clazz, methodSignature, args);
    }

    private static MethodInvocationKey keyOf(Class<?> clazz, String methodSignature, Object[] args) {
        return new MethodInvocationKey(
            Objects.requireNonNull(clazz, "clazz must not be null"),
            Objects.requireNonNull(methodSignature, "methodSignature must not be null"),
            cloneArgs(args)
        );
    }

    private static Object[] cloneArgs(Object[] args) {
        return args != null ? args.clone() : new Object[0];
    }

    private static void cleanupIfEmpty(Map<MethodInvocationKey, Answer> state) {
        if (state.isEmpty()) {
            MOCKS.remove();
        }
    }

    static final class StubbingContext {
        private final Class<?> expectedClass;
        private CapturedInvocation invocation;

        private StubbingContext(Class<?> expectedClass) {
            this.expectedClass = expectedClass;
        }

        private Class<?> expectedClass() {
            return expectedClass;
        }

        private CapturedInvocation invocation() {
            return invocation;
        }

        private void capture(Class<?> clazz, String methodSignature, Object[] args) {
            if (invocation != null) {
                throw new IllegalStateException("Only one static invocation can be configured in when(...)");
            }
            invocation = new CapturedInvocation(clazz, methodSignature, args);
        }
    }

    static final class CapturedInvocation {
        private final Class<?> clazz;
        private final String methodSignature;
        private final Object[] args;

        CapturedInvocation(Class<?> clazz, String methodSignature, Object[] args) {
            this.clazz = Objects.requireNonNull(clazz, "clazz must not be null");
            this.methodSignature = Objects.requireNonNull(methodSignature, "methodSignature must not be null");
            this.args = cloneArgs(args);
        }

        Class<?> clazz() {
            return clazz;
        }

        String methodSignature() {
            return methodSignature;
        }

        Object[] args() {
            return cloneArgs(args);
        }
    }

    private static final class MethodInvocationKey {
        private final Class<?> clazz;
        private final String methodSignature;
        private final Object[] args;

        private MethodInvocationKey(Class<?> clazz, String methodSignature, Object[] args) {
            this.clazz = clazz;
            this.methodSignature = methodSignature;
            this.args = args;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MethodInvocationKey that)) {
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
    }
}
