package Mockframework.Static.staticmock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import Mockframework.Core.Answer;

public final class StaticMockManager {
    private static final ThreadLocal<Map<MethodKey, Answer>> MOCKS =
        ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<StubbingContext> STUBBING = new ThreadLocal<>();
    private static final String ANY_PARAMETERS_SUFFIX = "(*)";

    private StaticMockManager() {
    }

    public static void enableMock(Class<?> clazz, String methodSignature, Answer answer) {
        MethodKey key = keyOf(clazz, methodSignature);
        MOCKS.get().put(key, Objects.requireNonNull(answer, "answer must not be null"));
    }

    public static void disableMock(Class<?> clazz, String methodSignature) {
        MethodKey key = keyOf(clazz, methodSignature);
        Map<MethodKey, Answer> state = MOCKS.get();
        state.remove(key);
        cleanupIfEmpty(state);
    }

    public static void disableMock(Class<?> clazz, String methodSignature, Answer answer) {
        MethodKey key = keyOf(clazz, methodSignature);
        Map<MethodKey, Answer> state = MOCKS.get();
        state.remove(key, Objects.requireNonNull(answer, "answer must not be null"));
        cleanupIfEmpty(state);
    }

    public static Optional<Answer> findMock(Class<?> clazz, String methodSignature) {
        MethodKey key = keyOf(clazz, methodSignature);
        Answer exact = MOCKS.get().get(key);
        if (exact != null) {
            return Optional.of(exact);
        }

        return Optional.ofNullable(MOCKS.get().get(keyOf(clazz, wildcardSignature(methodSignature))));
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

        context.capture(clazz, methodSignature);
    }

    private static MethodKey keyOf(Class<?> clazz, String methodSignature) {
        return new MethodKey(
            Objects.requireNonNull(clazz, "clazz must not be null"),
            normalizeSignature(Objects.requireNonNull(methodSignature, "methodSignature must not be null"))
        );
    }

    private static String wildcardSignature(String methodSignature) {
        String normalized = normalizeSignature(methodSignature);
        if (normalized.endsWith(ANY_PARAMETERS_SUFFIX)) {
            return normalized;
        }
        int index = normalized.indexOf('(');
        if (index < 0) {
            return normalized;
        }
        return normalized.substring(0, index) + ANY_PARAMETERS_SUFFIX;
    }

    private static String normalizeSignature(String methodSignature) {
        if (methodSignature.indexOf('(') >= 0) {
            return methodSignature;
        }
        return methodSignature + ANY_PARAMETERS_SUFFIX;
    }

    private static void cleanupIfEmpty(Map<MethodKey, Answer> state) {
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

        private void capture(Class<?> clazz, String methodSignature) {
            if (invocation != null) {
                throw new IllegalStateException("Only one static invocation can be configured in when(...)");
            }
            invocation = new CapturedInvocation(clazz, methodSignature);
        }
    }

    record CapturedInvocation(Class<?> clazz, String methodSignature) {
        CapturedInvocation {
            Objects.requireNonNull(clazz, "clazz must not be null");
            Objects.requireNonNull(methodSignature, "methodSignature must not be null");
        }
    }

    private record MethodKey(Class<?> clazz, String methodSignature) {
    }
}
