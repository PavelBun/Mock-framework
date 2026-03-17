package Mockframework.Dynamic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import Mockframework.Dynamic.annotation.Mock;
import Mockframework.Dynamic.api.DynamicMockito;
import Mockframework.Dynamic.init.MockInitializer;
import org.junit.jupiter.api.*;
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
        DynamicMockito.when(mockedList.get(0)).thenReturn("first");
        assertEquals("first", mockedList.get(0));
        assertNull(mockedList.get(1)); // default value
    }

    @Test
    void shouldStubMultipleCalls() {
        DynamicMockito.when(mockedList.size()).thenReturn(10);
        assertEquals(10, mockedList.size());
    }

    @Test
    void shouldThrowException() {
        IllegalArgumentException exception = new IllegalArgumentException("bad index");
        DynamicMockito.when(mockedList.get(-1)).thenThrow(exception);
        assertThrows(IllegalArgumentException.class, () -> mockedList.get(-1));
    }

    @Test
    void shouldWorkWithDifferentArguments() {
        DynamicMockito.when(mockedMap.get("key1")).thenReturn(100);
        DynamicMockito.when(mockedMap.get("key2")).thenReturn(200);
        assertEquals(100, mockedMap.get("key1"));
        assertEquals(200, mockedMap.get("key2"));
        assertNull(mockedMap.get("key3"));
    }

    @Test
    void shouldStubMethodWithNoArgs() {
        DynamicMockito.when(mockedList.isEmpty()).thenReturn(true);
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

    @Test
    void shouldMockClass() {
        Calculator mockCalc = DynamicMockito.mock(Calculator.class);
        DynamicMockito.when(mockCalc.add(2, 3)).thenReturn(100);
        assertEquals(100, mockCalc.add(2, 3));
        assertEquals(0, mockCalc.add(1, 1)); // default
        assertNull(mockCalc.getMessage()); // default null
    }
    @Test
    void shouldChainThenReturn() {
        DynamicMockito.when(mockedList.get(0)).thenReturn("first").thenReturn("second");
        assertEquals("first", mockedList.get(0));
        assertEquals("second", mockedList.get(0));
        assertEquals("second", mockedList.get(0)); // повтор последнего
    }

    @Test
    void shouldChainThenThrowAfterReturn() {
        IllegalArgumentException exception = new IllegalArgumentException("error");
        DynamicMockito.when(mockedList.get(0)).thenReturn("ok").thenThrow(exception);
        assertEquals("ok", mockedList.get(0));
        assertThrows(IllegalArgumentException.class, () -> mockedList.get(0));
        assertThrows(IllegalArgumentException.class, () -> mockedList.get(0)); // после тоже исключение
    }

    @Test
    void shouldChainThenAnswer() throws Throwable {
        Answer answer1 = args -> "answer1";
        Answer answer2 = args -> "answer2";
        DynamicMockito.when(mockedList.get(0)).thenAnswer(answer1).thenAnswer(answer2);
        assertEquals("answer1", mockedList.get(0));
        assertEquals("answer2", mockedList.get(0));
        assertEquals("answer2", mockedList.get(0));
    }

    @Test
    void shouldChainMixedThenReturnAndThenThrow() {
        DynamicMockito.when(mockedList.get(0)).thenReturn("a").thenThrow(new RuntimeException("boom")).thenReturn("c");
        assertEquals("a", mockedList.get(0));
        assertThrows(RuntimeException.class, () -> mockedList.get(0));
        assertEquals("c", mockedList.get(0)); // после исключения – следующий ответ
        assertEquals("c", mockedList.get(0)); // повтор последнего
    }

    @Test
    void shouldResetChainedStubs() {
        DynamicMockito.when(mockedList.size()).thenReturn(1).thenReturn(2);
        assertEquals(1, mockedList.size());
        DynamicMockito.reset();
        assertEquals(0, mockedList.size()); // после сброса - значение по умолчанию
    }
    @Test
    void shouldChainWithThenAnswerManipulatingArgs() throws Throwable {
        // Answer, который возвращает строку, зависящую от аргумента
        Answer answer = args -> "arg: " + args[0];
        DynamicMockito.when(mockedList.get(0)).thenAnswer(answer);
        assertEquals("arg: 0", mockedList.get(0));
    }

    @Test
    void shouldThrowCheckedException() {
        // Проверяем, что можно выбросить checked исключение
        Exception checkedException = new Exception("checked exception");
        DynamicMockito.when(mockedList.get(0)).thenThrow(checkedException);
        assertThrows(Exception.class, () -> mockedList.get(0));
    }

    @Test
    void shouldReturnDefaultForNonStubbedArgsAfterChaining() {
        // Стаб для конкретного аргумента с цепочкой
        DynamicMockito.when(mockedList.get(0)).thenReturn("first").thenReturn("second");
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
        DynamicMockito.when(mockedList.get(0)).thenThrow(firstEx).thenThrow(secondEx);
        assertThrows(RuntimeException.class, () -> mockedList.get(0));
        assertThrows(IllegalStateException.class, () -> mockedList.get(0));
        assertThrows(IllegalStateException.class, () -> mockedList.get(0)); // повтор последнего
    }

    @Test
    void shouldChainThenReturnAndThenAnswer() throws Throwable {
        Answer answer = args -> "answer";
        DynamicMockito.when(mockedList.get(0)).thenReturn("return").thenAnswer(answer);
        assertEquals("return", mockedList.get(0));
        assertEquals("answer", mockedList.get(0));
        assertEquals("answer", mockedList.get(0));
    }
}