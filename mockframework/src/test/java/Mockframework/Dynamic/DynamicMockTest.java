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
}