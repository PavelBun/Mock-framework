package Mockframework.Dynamic.api;

import Mockframework.Core.Answer;
import Mockframework.Core.matcher.ArgumentMatcher;
import Mockframework.Core.matcher.ArgumentMatchers;
import Mockframework.Dynamic.creator.DynamicMockCreator;
import Mockframework.Dynamic.registry.*;
import Mockframework.Dynamic.verification.*;

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
        List<ArgumentMatcher> argumentMatchers = REGISTRY.consumeMatchers();
        InvocationKey key = REGISTRY.getLastInvocationAndClear();
        if (key == null) {
            throw new IllegalStateException("when() called without a previous mock invocation");
        }
        if (argumentMatchers.isEmpty()) {
            return new OngoingStubbingImpl<>(key, null);
        }

        int argsCount = key.getArgs().length;
        if (argumentMatchers.size() != argsCount) {
            throw new IllegalStateException(
                "Invalid matcher usage: expected " + argsCount + " matchers but got " + argumentMatchers.size()
            );
        }
        return new OngoingStubbingImpl<>(key, argumentMatchers);
    }

    // Сброс всех моков
    public static void reset() {
        // Очищаем историю вызовов (если она хранится в реестре)
    // Очищаем заглушки
        REGISTRY.clearHistory();
        REGISTRY.reset();
    }

    public static <T> T any() {
        REGISTRY.registerMatcher(ArgumentMatchers.any());
        return null;
    }

    public static <T> T eq(T value) {
        REGISTRY.registerMatcher(ArgumentMatchers.eq(value));
        return value;
    }

    public static String contains(String value) {
        REGISTRY.registerMatcher(ArgumentMatchers.contains(value));
        return value;
    }

    // Реализация OngoingStubbing с поддержкой цепочек
    private static final class OngoingStubbingImpl<R> implements DynamicOngoingStubbing<R> {
        private final InvocationKey key;
        private final List<ArgumentMatcher> argumentMatchers;
        private final List<Answer> answers = new ArrayList<>();

        private OngoingStubbingImpl(InvocationKey key, List<ArgumentMatcher> argumentMatchers) {
            this.key = key;
            this.argumentMatchers = argumentMatchers != null
                ? List.copyOf(argumentMatchers)
                : null;
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
            Answer effectiveAnswer;
            if (answers.size() == 1) {
                effectiveAnswer = answers.get(0);
            } else {
                // При множественных ответах оборачиваем их в ChainedAnswer
                effectiveAnswer = new ChainedAnswer(new ArrayList<>(answers));
            }

            if (argumentMatchers == null) {
                REGISTRY.addStub(key, effectiveAnswer);
            } else {
                REGISTRY.addMatcherStub(key, argumentMatchers, effectiveAnswer);
            }
        }
    }

    public static <T> T verify(T mock) {
        return verify(mock, new Times(1));
    }

    public static <T> T verify(T mock, VerificationMode mode) {
        return VerificationProxyCreator.createVerificationProxy(mock, mode);
    }

    public static VerificationMode times(int count) {
        return new Times(count);
    }

    public static VerificationMode never() {
        return new Times(0);
    }

    public static VerificationMode atLeast(int min) {
        return new AtLeast(min);
    }

    public static VerificationMode atMost(int max) {
        return new AtMost(max);
    }
    public static int anyInt() {
        REGISTRY.registerMatcher(ArgumentMatchers.any());
        return 0;
    }

    public static String anyString() {
        REGISTRY.registerMatcher(ArgumentMatchers.any());
        return "";
    }

}
