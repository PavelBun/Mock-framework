package Mockframework.Static;

import Mockframework.Static.verification.VerificationMode;

public interface MockedStatic<T> extends AutoCloseable {
    <R> OngoingStubbing<R> when(StaticInvocation<R> invocation);

    void verify(StaticInvocation<?> invocation);

    void verify(StaticInvocation<?> invocation, VerificationMode mode);

    @Override
    void close();
}
