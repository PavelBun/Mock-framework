package Mockframework.Static.staticmock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import Mockframework.Core.Answer;
import Mockframework.Core.matcher.ArgumentMatcher;

public final class StaticMockManager {
    private static final ThreadLocal<Map<MethodInvocationKey, Answer>> MOCKS =
        ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<List<MatcherMethodStub>> MATCHER_MOCKS =
        ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<ArgumentMatcher>> PENDING_MATCHERS =
        ThreadLocal.withInitial(ArrayList::new);
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

    public static void enableMatcherMock(
        Class<?> clazz,
        String methodSignature,
        List<ArgumentMatcher> argumentMatchers,
        Answer answer
    ) {
        MatcherMethodStub newStub = MatcherMethodStub.create(clazz, methodSignature, argumentMatchers, answer);
        List<MatcherMethodStub> state = MATCHER_MOCKS.get();
        state.removeIf(existing -> existing.hasSamePattern(newStub));
        state.add(newStub);
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

    public static void disableMatcherMock(
        Class<?> clazz,
        String methodSignature,
        List<ArgumentMatcher> argumentMatchers,
        Answer answer
    ) {
        List<MatcherMethodStub> state = MATCHER_MOCKS.get();
        state.removeIf(stub ->
            stub.hasSamePattern(clazz, methodSignature, argumentMatchers)
                && stub.hasAnswer(answer)
        );
        cleanupMatcherStateIfEmpty(state);
    }

    public static Optional<Answer> findMock(Class<?> clazz, String methodSignature) {
        return findMock(clazz, methodSignature, new Object[0]);
    }

    public static Optional<Answer> findMock(Class<?> clazz, String methodSignature, Object[] args) {
        MethodInvocationKey key = keyOf(clazz, methodSignature, args);
        Answer exact = MOCKS.get().get(key);
        if (exact != null) {
            return Optional.of(exact);
        }

        List<MatcherMethodStub> matcherStubs = MATCHER_MOCKS.get();
        for (int i = matcherStubs.size() - 1; i >= 0; i--) {
            MatcherMethodStub stub = matcherStubs.get(i);
            if (stub.matches(clazz, methodSignature, args)) {
                return Optional.of(stub.answer);
            }
        }
        return Optional.empty();
    }

    public static void clear() {
        MOCKS.remove();
        MATCHER_MOCKS.remove();
        PENDING_MATCHERS.remove();
        STUBBING.remove();
    }

    public static void registerMatcher(ArgumentMatcher matcher) {
        PENDING_MATCHERS.get().add(Objects.requireNonNull(matcher, "matcher must not be null"));
    }

    public static List<ArgumentMatcher> consumeMatchers() {
        List<ArgumentMatcher> matchers = PENDING_MATCHERS.get();
        if (matchers.isEmpty()) {
            PENDING_MATCHERS.remove();
            return List.of();
        }
        List<ArgumentMatcher> copy = List.copyOf(matchers);
        PENDING_MATCHERS.remove();
        return copy;
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

    private static void cleanupMatcherStateIfEmpty(List<MatcherMethodStub> state) {
        if (state.isEmpty()) {
            MATCHER_MOCKS.remove();
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

    private static final class MatcherMethodStub {
        private final Class<?> clazz;
        private final String methodSignature;
        private final List<ArgumentMatcher> argumentMatchers;
        private final Answer answer;

        private MatcherMethodStub(
            Class<?> clazz,
            String methodSignature,
            List<ArgumentMatcher> argumentMatchers,
            Answer answer
        ) {
            this.clazz = clazz;
            this.methodSignature = methodSignature;
            this.argumentMatchers = argumentMatchers;
            this.answer = answer;
        }

        private static MatcherMethodStub create(
            Class<?> clazz,
            String methodSignature,
            List<ArgumentMatcher> argumentMatchers,
            Answer answer
        ) {
            return new MatcherMethodStub(
                Objects.requireNonNull(clazz, "clazz must not be null"),
                Objects.requireNonNull(methodSignature, "methodSignature must not be null"),
                List.copyOf(Objects.requireNonNull(argumentMatchers, "argumentMatchers must not be null")),
                Objects.requireNonNull(answer, "answer must not be null")
            );
        }

        private boolean hasSamePattern(MatcherMethodStub other) {
            return clazz.equals(other.clazz)
                && methodSignature.equals(other.methodSignature)
                && argumentMatchers.equals(other.argumentMatchers);
        }

        private boolean hasSamePattern(
            Class<?> expectedClass,
            String expectedMethodSignature,
            List<ArgumentMatcher> expectedMatchers
        ) {
            return clazz.equals(expectedClass)
                && methodSignature.equals(expectedMethodSignature)
                && argumentMatchers.equals(expectedMatchers);
        }

        private boolean hasAnswer(Answer expectedAnswer) {
            return answer == expectedAnswer;
        }

        private boolean matches(Class<?> invocationClass, String invocationSignature, Object[] invocationArgs) {
            if (!clazz.equals(invocationClass) || !methodSignature.equals(invocationSignature)) {
                return false;
            }
            if (invocationArgs.length != argumentMatchers.size()) {
                return false;
            }

            for (int i = 0; i < invocationArgs.length; i++) {
                if (!argumentMatchers.get(i).matches(invocationArgs[i])) {
                    return false;
                }
            }
            return true;
        }
    }
}
