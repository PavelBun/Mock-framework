package Mockframework.Static.staticmock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Mockframework.Static.MockedStatic;
import Mockframework.Static.OngoingStubbing;
import Mockframework.Static.StaticInvocation;
import Mockframework.Core.Answer;

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
            return new OngoingStubbingImpl<>(resolvedSignature, new Object[0]);
        }

        StaticMockManager.beginStubbing(clazz);
        Throwable invocationFailure = null;
        try {
            invocation.invoke();
        } catch (Throwable throwable) {
            invocationFailure = throwable;
        }

        StaticMockManager.CapturedInvocation capturedInvocation = StaticMockManager.finishStubbing();
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

        return new OngoingStubbingImpl<>(capturedInvocation.methodSignature(), capturedInvocation.args());
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        for (RegisteredAnswer entry : registeredAnswers) {
            StaticMockManager.disableMock(
                clazz,
                entry.methodSignature,
                entry.args,
                entry.answer
            );
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
        private final List<Answer> answers = new ArrayList<>();

        private OngoingStubbingImpl(String methodSignature, Object[] invocationArgs) {
            this.methodSignature = Objects.requireNonNull(
                methodSignature,
                "methodSignature must not be null"
            );
            this.invocationArgs = invocationArgs != null ? invocationArgs.clone() : new Object[0];
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

                StaticMockManager.enableMock(clazz, methodSignature, invocationArgs, effectiveAnswer);
                registeredAnswers.add(new RegisteredAnswer(methodSignature, invocationArgs, effectiveAnswer));
            }
            return this;
        }
    }

    private static final class RegisteredAnswer {
        private final String methodSignature;
        private final Object[] args;
        private final Answer answer;

        private RegisteredAnswer(String methodSignature, Object[] args, Answer answer) {
            this.methodSignature = methodSignature;
            this.args = args != null ? args.clone() : new Object[0];
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
