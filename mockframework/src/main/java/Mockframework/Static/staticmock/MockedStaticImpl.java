package Mockframework.Static.staticmock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import Mockframework.Static.MockedStatic;
import Mockframework.Static.OngoingStubbing;
import Mockframework.Static.StaticInvocation;
import Mockframework.Core.Answer;

public final class MockedStaticImpl<T> implements MockedStatic<T> {
    private final Class<T> clazz;
    private final Map<String, Answer> registeredAnswers = new HashMap<>();
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
        if (resolvedSignature != null) {
            return new OngoingStubbingImpl<>(resolvedSignature);
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

        return new OngoingStubbingImpl<>(capturedInvocation.methodSignature());
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        for (Map.Entry<String, Answer> entry : registeredAnswers.entrySet()) {
            StaticMockManager.disableMock(clazz, entry.getKey(), entry.getValue());
        }
        registeredAnswers.clear();
        closed = true;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("MockedStatic is already closed for " + clazz.getName());
        }
    }

    private final class OngoingStubbingImpl<R> implements OngoingStubbing<R> {
        private final String methodSignature;

        private OngoingStubbingImpl(String methodSignature) {
            this.methodSignature = Objects.requireNonNull(
                methodSignature,
                "methodSignature must not be null"
            );
        }

        @Override
        public void thenReturn(R value) {
            thenAnswer(args -> value);
        }

        @Override
        public void thenThrow(Throwable throwable) {
            Objects.requireNonNull(throwable, "throwable must not be null");
            thenAnswer(args -> {
                throw throwable;
            });
        }

        @Override
        public void thenAnswer(Answer answer) {
            Objects.requireNonNull(answer, "answer must not be null");
            synchronized (MockedStaticImpl.this) {
                ensureOpen();
                StaticMockManager.enableMock(clazz, methodSignature, answer);
                registeredAnswers.put(methodSignature, answer);
            }
        }
    }
}
