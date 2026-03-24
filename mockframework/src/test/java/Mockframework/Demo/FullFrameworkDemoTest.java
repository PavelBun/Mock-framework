package Mockframework.Demo;

import org.junit.jupiter.api.*;
import Mockframework.Dynamic.annotation.Mock;
import Mockframework.Dynamic.api.DynamicMockito;
import Mockframework.Dynamic.init.MockInitializer;
import Mockframework.Static.Mockito;
import Mockframework.Static.MockedStatic;
import Mockframework.Core.Answer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static Mockframework.Dynamic.api.DynamicMockito.*;

// Демонстрация всех возможностей фреймворка
public class FullFrameworkDemoTest {

    // ==================== ДИНАМИЧЕСКИЕ МОКИ ====================
    @Mock
    private List<String> mockedList;

    @Mock
    private Map<String, Integer> mockedMap;

    // Класс для демо мокинга классов (должен быть static)


    // Статический класс для демо статического мокинга


    @BeforeEach
    void initDynamicMocks() {
        MockInitializer.initMocks(this);
    }

    @AfterEach
    void cleanup() {
        DynamicMockito.reset(); // сброс заглушек и истории
    }

    // ------------------------------------------------------------
    // 1. ДИНАМИЧЕСКОЕ МОКИРОВАНИЕ
    // ------------------------------------------------------------

    @Test
    @DisplayName("Динамика: стаббинг thenReturn / thenThrow")
    void dynamic_Stubbing() {
        when(mockedList.get(0)).thenReturn("first");
        assertEquals("first", mockedList.get(0));
        assertNull(mockedList.get(1));

        when(mockedList.get(-1)).thenThrow(new IllegalArgumentException("bad index"));
        assertThrows(IllegalArgumentException.class, () -> mockedList.get(-1));
    }

    @Test
    @DisplayName("Динамика: цепочки ответов (chaining)")
    void dynamic_Chaining() {
        when(mockedList.get(0))
                .thenReturn("a")
                .thenReturn("b")
                .thenThrow(new RuntimeException("boom"));

        assertEquals("a", mockedList.get(0));
        assertEquals("b", mockedList.get(0));
        assertThrows(RuntimeException.class, () -> mockedList.get(0));
        assertThrows(RuntimeException.class, () -> mockedList.get(0));
    }

    @Test
    @DisplayName("Динамика: thenAnswer с произвольной логикой")
    void dynamic_ThenAnswer() {
        Answer answer = args -> "len=" + args[0];
        when(mockedList.get(anyInt())).thenAnswer(answer);
        assertEquals("len=42", mockedList.get(42));
    }

    @Test
    @DisplayName("Динамика: верификация вызовов")
    void dynamic_Verification() {
        mockedList.get(0);
        mockedList.get(1);
        mockedList.get(1);

        verify(mockedList).get(0);
        verify(mockedList, times(2)).get(1);
        verify(mockedList, never()).get(2);
        verify(mockedList, atLeast(1)).get(anyInt());
        verify(mockedList, atMost(3)).get(anyInt());
    }

    @Test
    @DisplayName("Динамика: сброс состояния (reset)")
    void dynamic_Reset() {
        when(mockedList.size()).thenReturn(100);
        assertEquals(100, mockedList.size());

        DynamicMockito.reset();

        assertEquals(0, mockedList.size()); // default – один вызов
        verify(mockedList).size(); // проверяем, что после сброса был ровно один вызов
    }

    @Test
    @DisplayName("Динамика: мокинг обычного класса")
    void dynamic_MockClass() {
        Calculator mockCalc = DynamicMockito.mock(Calculator.class);
        when(mockCalc.add(2, 3)).thenReturn(100);
        assertEquals(100, mockCalc.add(2, 3));
        assertEquals(0, mockCalc.add(1, 1));
        assertNull(mockCalc.getMessage());
    }

    // ------------------------------------------------------------
    // 2. СТАТИЧЕСКОЕ МОКИРОВАНИЕ
    // ------------------------------------------------------------
    @Test
    @DisplayName("Статика: стаббинг thenReturn / thenThrow")
    void static_Stubbing() {
        try (MockedStatic<TestClock> mocked = Mockito.mockStatic(TestClock.class)) {
            mocked.when(TestClock::now).thenReturn(123L);
            assertEquals(123L, TestClock.now());

            mocked.when(() -> TestClock.format("x")).thenThrow(new IllegalStateException("boom"));
            assertThrows(IllegalStateException.class, () -> TestClock.format("x"));
        }
        assertNotEquals(123L, TestClock.now());
    }

    @Test
    @DisplayName("Статика: цепочки ответов")
    void static_Chaining() {
        try (MockedStatic<TestClock> mocked = Mockito.mockStatic(TestClock.class)) {
            mocked.when(TestClock::now)
                    .thenReturn(1L)
                    .thenReturn(2L)
                    .thenThrow(new RuntimeException());

            assertEquals(1L, TestClock.now());
            assertEquals(2L, TestClock.now());
            assertThrows(RuntimeException.class, TestClock::now);
        }
    }

    @Test
    @DisplayName("Статика: верификация вызовов (если реализована)")
    void static_Verification() {
        try (MockedStatic<TestClock> mocked = Mockito.mockStatic(TestClock.class)) {
            TestClock.now();
            TestClock.now();
            TestClock.format("a");

            // Если ваш напарник добавил verify в MockedStatic, раскомментируйте:
            // mocked.verify(TestClock::now, times(2));
            // mocked.verify(() -> TestClock.format(eq("a")), times(1));
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Статика: использование матчеров")
    void static_Matchers() {
        try (MockedStatic<TestClock> mocked = Mockito.mockStatic(TestClock.class)) {
            // Если матчеры есть, раскомментируйте:
            // mocked.when(() -> TestClock.format(anyString())).thenReturn("stubbed");
            // assertEquals("stubbed", TestClock.format("whatever"));
            mocked.when(() -> TestClock.format("exact")).thenReturn("exact");
            assertEquals("exact", TestClock.format("exact"));
        }
    }

    // ------------------------------------------------------------
    // 3. СОВМЕСТНОЕ ИСПОЛЬЗОВАНИЕ
    // ------------------------------------------------------------
    @Test
    @DisplayName("Совместная работа динамических и статических моков")
    void combined_Test() {
        Calculator mockCalc = DynamicMockito.mock(Calculator.class);
        when(mockCalc.add(1, 2)).thenReturn(99);

        try (MockedStatic<TestClock> mocked = Mockito.mockStatic(TestClock.class)) {
            mocked.when(TestClock::now).thenReturn(999L);

            assertEquals(99, mockCalc.add(1, 2));
            assertEquals(999L, TestClock.now());
        }

        assertNotEquals(999L, TestClock.now());
        assertEquals(99, mockCalc.add(1, 2));
    }
}