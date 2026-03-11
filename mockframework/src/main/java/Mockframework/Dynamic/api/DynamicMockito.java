package Mockframework.Dynamic.api;

import Mockframework.Core.Answer;
import Mockframework.Dynamic.creator.DynamicMockCreator;
import Mockframework.Dynamic.registry.DynamicStubbingRegistry;
import Mockframework.Dynamic.registry.InvocationKey;

public final class DynamicMockito {
    private static final DynamicStubbingRegistry REGISTRY = DynamicStubbingRegistry.getInstance();

    private DynamicMockito() {}

    // Создание мока
    public static <T> T mock(Class<T> typeToMock) {
        return DynamicMockCreator.createMock(typeToMock);
    }

    // Начало стаббинга
    public static <T> DynamicOngoingStubbing<T> when(T call) {
        InvocationKey key = REGISTRY.getLastInvocationAndClear();
        if (key == null) {
            throw new IllegalStateException("when() called without a previous mock invocation");
        }
        return new OngoingStubbingImpl<>(key);
    }

    // Сброс всех моков (очистка реестра)
    public static void reset() {
        REGISTRY.reset();
    }

    // Реализация OngoingStubbing
    private static final class OngoingStubbingImpl<R> implements DynamicOngoingStubbing<R> {
        private final InvocationKey key;

        private OngoingStubbingImpl(InvocationKey key) {
            this.key = key;
        }

        @Override
        public DynamicOngoingStubbing<R> thenReturn(R value) {
            REGISTRY.addStub(key, args -> value);
            return this;
        }

        @Override
        public DynamicOngoingStubbing<R> thenThrow(Throwable throwable) {
            REGISTRY.addStub(key, args -> { throw throwable; });
            return this;
        }

        @Override
        public DynamicOngoingStubbing<R> thenAnswer(Answer answer) {
            REGISTRY.addStub(key, answer);
            return this;
        }
    }
}