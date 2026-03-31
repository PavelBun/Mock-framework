package Mockframework.Dynamic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import Mockframework.Dynamic.annotation.Mock;
import Mockframework.Dynamic.api.DynamicMockito;
import Mockframework.Dynamic.init.MockInitializer;
import org.junit.jupiter.api.*;

import static Mockframework.Dynamic.api.DynamicMockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import Mockframework.Core.Answer;

import static org.junit.jupiter.api.Assertions.*;

class DynamicMockTest {

    @Mock
    private List<String> mockedList;

    @Mock
    private Map<String, Integer> mockedMap;

    @Mock
    private TextService textService;

    @Mock
    private MultiArgService multiArgService;

    @BeforeEach
    void setUp() {
        MockInitializer.initMocks(this);
    }

    @AfterEach
    void tearDown() {
        DynamicMockito.reset();
    }

    @Test
    void shouldStubInterfaceMethod() {
        when(mockedList.get(0)).thenReturn("first");
        assertEquals("first", mockedList.get(0));
        assertNull(mockedList.get(1)); // default value
    }

    @Test
    void shouldStubMultipleCalls() {
        when(mockedList.size()).thenReturn(10);
        assertEquals(10, mockedList.size());
    }

    @Test
    void shouldThrowException() {
        IllegalArgumentException exception = new IllegalArgumentException("bad index");
        when(mockedList.get(-1)).thenThrow(exception);
        assertThrows(IllegalArgumentException.class, () -> mockedList.get(-1));
    }

    @Test
    void shouldWorkWithDifferentArguments() {
        when(mockedMap.get("key1")).thenReturn(100);
        when(mockedMap.get("key2")).thenReturn(200);
        assertEquals(100, mockedMap.get("key1"));
        assertEquals(200, mockedMap.get("key2"));
        assertNull(mockedMap.get("key3"));
    }

    @Test
    void shouldStubMethodWithNoArgs() {
        when(mockedList.isEmpty()).thenReturn(true);
        assertTrue(mockedList.isEmpty());
    }

    // Класс для тестирования моков классов
    public static class Calculator {
        public int add(int a, int b) {
            return a + b;
        }
        public String getMessage() {
            return "real";
        }
    }

    interface TextService {
        String normalize(String value);
    }

    interface MultiArgService {
        String join(String left, String right);
    }

    @Test
    void shouldMockClass() {
        Calculator mockCalc = DynamicMockito.mock(Calculator.class);
        when(mockCalc.add(2, 3)).thenReturn(100);
        assertEquals(100, mockCalc.add(2, 3));
        assertEquals(0, mockCalc.add(1, 1)); // default
        assertNull(mockCalc.getMessage()); // default null
    }
    @Test
    void shouldChainThenReturn() {
        when(mockedList.get(0)).thenReturn("first").thenReturn("second");
        assertEquals("first", mockedList.get(0));
        assertEquals("second", mockedList.get(0));
        assertEquals("second", mockedList.get(0)); // повтор последнего
    }

    @Test
    void shouldChainThenThrowAfterReturn() {
        IllegalArgumentException exception = new IllegalArgumentException("error");
        when(mockedList.get(0)).thenReturn("ok").thenThrow(exception);
        assertEquals("ok", mockedList.get(0));
        assertThrows(IllegalArgumentException.class, () -> mockedList.get(0));
        assertThrows(IllegalArgumentException.class, () -> mockedList.get(0)); // после тоже исключение
    }

    @Test
    void shouldChainThenAnswer() throws Throwable {
        Answer answer1 = args -> "answer1";
        Answer answer2 = args -> "answer2";
        when(mockedList.get(0)).thenAnswer(answer1).thenAnswer(answer2);
        assertEquals("answer1", mockedList.get(0));
        assertEquals("answer2", mockedList.get(0));
        assertEquals("answer2", mockedList.get(0));
    }

    @Test
    void shouldChainMixedThenReturnAndThenThrow() {
        when(mockedList.get(0)).thenReturn("a").thenThrow(new RuntimeException("boom")).thenReturn("c");
        assertEquals("a", mockedList.get(0));
        assertThrows(RuntimeException.class, () -> mockedList.get(0));
        assertEquals("c", mockedList.get(0)); // после исключения – следующий ответ
        assertEquals("c", mockedList.get(0)); // повтор последнего
    }

    @Test
    void shouldResetChainedStubs() {
        when(mockedList.size()).thenReturn(1).thenReturn(2);
        assertEquals(1, mockedList.size());
        DynamicMockito.reset();
        assertEquals(0, mockedList.size()); // после сброса - значение по умолчанию
    }
    @Test
    void shouldChainWithThenAnswerManipulatingArgs() throws Throwable {
        // Answer, который возвращает строку, зависящую от аргумента
        Answer answer = args -> "arg: " + args[0];
        when(mockedList.get(0)).thenAnswer(answer);
        assertEquals("arg: 0", mockedList.get(0));
    }

    @Test
    void shouldThrowCheckedException() {
        // Проверяем, что можно выбросить checked исключение
        Exception checkedException = new Exception("checked exception");
        when(mockedList.get(0)).thenThrow(checkedException);
        assertThrows(Exception.class, () -> mockedList.get(0));
    }

    @Test
    void shouldReturnDefaultForNonStubbedArgsAfterChaining() {
        // Стаб для конкретного аргумента с цепочкой
        when(mockedList.get(0)).thenReturn("first").thenReturn("second");
        // Для другого аргумента стаба нет – должно вернуться default
        assertNull(mockedList.get(1));
        // Для стабленного аргумента цепочка работает
        assertEquals("first", mockedList.get(0));
        assertEquals("second", mockedList.get(0));
    }

    @Test
    void shouldChainMultipleThenThrow() {
        RuntimeException firstEx = new RuntimeException("first");
        IllegalStateException secondEx = new IllegalStateException("second");
        when(mockedList.get(0)).thenThrow(firstEx).thenThrow(secondEx);
        assertThrows(RuntimeException.class, () -> mockedList.get(0));
        assertThrows(IllegalStateException.class, () -> mockedList.get(0));
        assertThrows(IllegalStateException.class, () -> mockedList.get(0)); // повтор последнего
    }

    @Test
    void shouldChainThenReturnAndThenAnswer() throws Throwable {
        Answer answer = args -> "answer";
        when(mockedList.get(0)).thenReturn("return").thenAnswer(answer);
        assertEquals("return", mockedList.get(0));
        assertEquals("answer", mockedList.get(0));
        assertEquals("answer", mockedList.get(0));
    }

    @Test
    void shouldSupportAnyMatcher() {
        when(mockedMap.get(DynamicMockito.any())).thenReturn(111);
        assertEquals(111, mockedMap.get("alpha"));
        assertEquals(111, mockedMap.get("beta"));
    }

    @Test
    void shouldSupportEqMatcher() {
        when(mockedMap.get(DynamicMockito.eq("target"))).thenReturn(222);
        assertEquals(222, mockedMap.get("target"));
        assertNull(mockedMap.get("other"));
    }

    @Test
    void shouldSupportContainsMatcher() {
        when(textService.normalize(DynamicMockito.contains("adm"))).thenReturn("admin");
        assertEquals("admin", textService.normalize("super-admin"));
        assertNull(textService.normalize("guest"));
    }

    @Test
    void shouldPreferExactStubOverMatcher() {
        when(mockedMap.get(DynamicMockito.any())).thenReturn(111);
        when(mockedMap.get("target")).thenReturn(222);

        assertEquals(222, mockedMap.get("target"));
        assertEquals(111, mockedMap.get("other"));
    }

    @Test
    void shouldSupportEqNullMatcher() {
        when(textService.normalize(DynamicMockito.eq(null))).thenReturn("null-value");

        assertEquals("null-value", textService.normalize(null));
        assertNull(textService.normalize("something"));
    }

    @Test
    void shouldClearMatcherStubAfterReset() {
        when(mockedMap.get(DynamicMockito.any())).thenReturn(777);
        assertEquals(777, mockedMap.get("before-reset"));

        DynamicMockito.reset();

        assertNull(mockedMap.get("after-reset"));
    }

    @Test
    void shouldFailWhenMatchersCountDoesNotMatchArguments() {
        IllegalStateException error = assertThrows(
            IllegalStateException.class,
            () -> when(multiArgService.join(DynamicMockito.any(), "raw")).thenReturn("x")
        );
        assertTrue(error.getMessage().contains("expected 2 matchers but got 1"));
    }

    @Test
    void shouldNotLeakPendingMatchersAfterInvalidUsage() {
        DynamicMockito.any();
        assertThrows(
            IllegalStateException.class,
            () -> when(mockedList.isEmpty()).thenReturn(true)
        );

        when(mockedList.isEmpty()).thenReturn(false);
        assertFalse(mockedList.isEmpty());
    }

    @Test
    void shouldVerifySingleCall() {
        mockedList.get(0);
        verify(mockedList).get(0);
    }

    @Test
    void shouldVerifyWithTimes() {
        mockedList.get(0);
        mockedList.get(0);
        verify(mockedList, times(2)).get(0);
    }

    @Test
    void shouldVerifyNever() {
        verify(mockedList, DynamicMockito.never()).get(0);
    }

    @Test
    void shouldVerifyAtLeast() {
        mockedList.get(0);
        mockedList.get(0);
        verify(mockedList, DynamicMockito.atLeast(1)).get(0);
        verify(mockedList, DynamicMockito.atLeast(2)).get(0);
    }

    @Test
    void shouldVerifyAtMost() {
        mockedList.get(0);
        verify(mockedList, DynamicMockito.atMost(1)).get(0);
    }
    @Test
    void shouldVerifyWithAnyMatcher() {
        mockedList.get(10);
        mockedList.get(20);
        verify(mockedList, times(2)).get(DynamicMockito.anyInt());
    }

    @Test
    void shouldVerifyWithEqMatcher() {
        mockedList.get(42);
        verify(mockedList).get(DynamicMockito.eq(42));
    }
    @Test
    void shouldVerifyWithContainsMatcher() {
        textService.normalize("admin");
        verify(textService).normalize(DynamicMockito.contains("adm"));
    }

    @Test
    void shouldFailWhenWrongCallCount() {
        mockedList.get(0);
        Assertions.assertThrows(AssertionError.class,
                () -> verify(mockedList, times(2)).get(0));
    }

    @Test
    void shouldFailWhenNeverButCalled() {
        mockedList.get(0);
        Assertions.assertThrows(AssertionError.class,
                () -> verify(mockedList, DynamicMockito.never()).get(0));
    }

}
