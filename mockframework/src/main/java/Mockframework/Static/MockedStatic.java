package Mockframework.Static;

public interface MockedStatic<T> extends AutoCloseable {
    <R> OngoingStubbing<R> when(StaticInvocation<R> invocation);

    @Override
    void close();
}

