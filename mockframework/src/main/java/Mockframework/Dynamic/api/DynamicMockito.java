package Mockframework.Dynamic.api;

import Mockframework.Core.Answer;
import Mockframework.Dynamic.creator.DynamicMockCreator;
import Mockframework.Dynamic.registry.DynamicStubbingRegistry;
import Mockframework.Dynamic.registry.InvocationKey;

import java.util.ArrayList;
import java.util.List;

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

    // Сброс всех моков
    public static void reset() {
        REGISTRY.reset();
    }

    // Реализация OngoingStubbing с поддержкой цепочек
    private static final class OngoingStubbingImpl<R> implements DynamicOngoingStubbing<R> {
        private final InvocationKey key;
        private final List<Answer> answers = new ArrayList<>();

        private OngoingStubbingImpl(InvocationKey key) {
            this.key = key;
        }

        @Override
        public DynamicOngoingStubbing<R> thenReturn(R value) {
            answers.add(args -> value);
            updateStub();
            return this;
        }

        @Override
        public DynamicOngoingStubbing<R> thenThrow(Throwable throwable) {
            answers.add(args -> { throw throwable; });
            updateStub();
            return this;
        }

        @Override
        public DynamicOngoingStubbing<R> thenAnswer(Answer answer) {
            answers.add(answer);
            updateStub();
            return this;
        }

        private void updateStub() {
            if (answers.size() == 1) {
                REGISTRY.addStub(key, answers.get(0));
            } else {
                // При множественных ответах оборачиваем их в ChainedAnswer
                REGISTRY.addStub(key, new ChainedAnswer(new ArrayList<>(answers)));
            }
        }
    }
}