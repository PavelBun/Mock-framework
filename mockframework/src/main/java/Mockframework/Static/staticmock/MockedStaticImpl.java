package Mockframework.Static.staticmock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Mockframework.Static.MockedStatic;
import Mockframework.Static.OngoingStubbing;
import Mockframework.Static.StaticInvocation;
import Mockframework.Core.Answer;
import Mockframework.Core.matcher.ArgumentMatcher;

public final class MockedStaticImpl<T> implements MockedStatic<T> {
    private final Class<T> clazz;
    private final List<RegisteredAnswer> registeredAnswers = new ArrayList<>();
    private boolean closed;

    public MockedStaticImpl(Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz, "clazz must not be null");
    }

    @Override
    public synchronized <R> OngoingStubbing<R> when(StaticInvocation<R> invocation) {
        ensureOpen();
        Objects.requireNonNull(invocation, "invocation must not be null");

        String resolvedSignature = InvocationSignatureResolver
            .resolve(clazz, invocation)
            .orElse(null);
        if (resolvedSignature != null && isNoArgsSignature(resolvedSignature)) {
            List<ArgumentMatcher> argumentMatchers = StaticMockManager.consumeMatchers();
            if (!argumentMatchers.isEmpty()) {
                throw new IllegalStateException(
                    "Invalid matcher usage: no-arg static method cannot use argument matchers"
                );
            }
            return new OngoingStubbingImpl<>(resolvedSignature, new Object[0], null);
        }

        StaticMockManager.beginStubbing(clazz);
        Throwable invocationFailure = null;
        try {
            invocation.invoke();
        } catch (Throwable throwable) {
            invocationFailure = throwable;
        }

        StaticMockManager.CapturedInvocation capturedInvocation = StaticMockManager.finishStubbing();
        List<ArgumentMatcher> argumentMatchers = StaticMockManager.consumeMatchers();
        if (capturedInvocation == null) {
            String message = "No static invocation was captured in when(...) for " + clazz.getName();
            if (invocationFailure != null) {
                throw new IllegalStateException(message, invocationFailure);
            }
            throw new IllegalStateException(message);
        }

        if (invocationFailure != null) {
            throw new IllegalStateException(
                "Failed to capture static invocation for " + clazz.getName(),
                invocationFailure
            );
        }

        if (!argumentMatchers.isEmpty() && argumentMatchers.size() != capturedInvocation.args().length) {
            throw new IllegalStateException(
                "Invalid matcher usage: expected " + capturedInvocation.args().length +
                    " matchers but got " + argumentMatchers.size()
            );
        }

        return new OngoingStubbingImpl<>(
            capturedInvocation.methodSignature(),
            capturedInvocation.args(),
            argumentMatchers.isEmpty() ? null : argumentMatchers
        );
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        for (RegisteredAnswer entry : registeredAnswers) {
            if (entry.argumentMatchers == null) {
                StaticMockManager.disableMock(
                    clazz,
                    entry.methodSignature,
                    entry.args,
                    entry.answer
                );
            } else {
                StaticMockManager.disableMatcherMock(
                    clazz,
                    entry.methodSignature,
                    entry.argumentMatchers,
                    entry.answer
                );
            }
        }
        registeredAnswers.clear();
        closed = true;
    }

    private boolean isNoArgsSignature(String methodSignature) {
        return methodSignature.endsWith("()");
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("MockedStatic is already closed for " + clazz.getName());
        }
    }

    private final class OngoingStubbingImpl<R> implements OngoingStubbing<R> {
        private final String methodSignature;
        private final Object[] invocationArgs;
        private final List<ArgumentMatcher> argumentMatchers;
        private final List<Answer> answers = new ArrayList<>();

        private OngoingStubbingImpl(
            String methodSignature,
            Object[] invocationArgs,
            List<ArgumentMatcher> argumentMatchers
        ) {
            this.methodSignature = Objects.requireNonNull(
                methodSignature,
                "methodSignature must not be null"
            );
            this.invocationArgs = invocationArgs != null ? invocationArgs.clone() : new Object[0];
            this.argumentMatchers = argumentMatchers != null ? List.copyOf(argumentMatchers) : null;
        }

        @Override
        public OngoingStubbing<R> thenReturn(R value) {
            return thenAnswer(args -> value);
        }

        @Override
        public OngoingStubbing<R> thenThrow(Throwable throwable) {
            Objects.requireNonNull(throwable, "throwable must not be null");
            return thenAnswer(args -> {
                throw throwable;
            });
        }

        @Override
        public OngoingStubbing<R> thenAnswer(Answer answer) {
            Objects.requireNonNull(answer, "answer must not be null");
            synchronized (MockedStaticImpl.this) {
                ensureOpen();
                answers.add(answer);
                Answer effectiveAnswer = answers.size() == 1
                    ? answers.get(0)
                    : new ChainedStaticAnswer(new ArrayList<>(answers));

                if (argumentMatchers == null) {
                    StaticMockManager.enableMock(clazz, methodSignature, invocationArgs, effectiveAnswer);
                } else {
                    StaticMockManager.enableMatcherMock(clazz, methodSignature, argumentMatchers, effectiveAnswer);
                }
                registeredAnswers.add(
                    new RegisteredAnswer(methodSignature, invocationArgs, argumentMatchers, effectiveAnswer)
                );
            }
            return this;
        }
    }

    private static final class RegisteredAnswer {
        private final String methodSignature;
        private final Object[] args;
        private final List<ArgumentMatcher> argumentMatchers;
        private final Answer answer;

        private RegisteredAnswer(
            String methodSignature,
            Object[] args,
            List<ArgumentMatcher> argumentMatchers,
            Answer answer
        ) {
            this.methodSignature = methodSignature;
            this.args = args != null ? args.clone() : new Object[0];
            this.argumentMatchers = argumentMatchers != null ? List.copyOf(argumentMatchers) : null;
            this.answer = answer;
        }
    }

    private static final class ChainedStaticAnswer implements Answer {
        private final List<Answer> answers;
        private int invocationCount;

        private ChainedStaticAnswer(List<Answer> answers) {
            this.answers = answers;
        }

        @Override
        public Object answer(Object[] args) throws Throwable {
            int index = invocationCount;
            invocationCount++;
            if (index >= answers.size()) {
                index = answers.size() - 1;
            }
            return answers.get(index).answer(args);
        }
    }
}
