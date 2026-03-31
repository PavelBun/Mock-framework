package Mockframework.examples;

import Mockframework.Dynamic.annotation.Mock;
import Mockframework.Dynamic.api.DynamicMockito;
import Mockframework.Dynamic.init.MockInitializer;
import Mockframework.Static.MockedStatic;
import Mockframework.Static.Mockito;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static Mockframework.Dynamic.api.DynamicMockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Демо фреймворка: dynamic + static в одном файле")
public class DemoShowcaseTest {

    @Mock
    private List<String> mockedList;

    @Mock
    private DemoTextService textService;

    @BeforeEach
    void setUp() {
        MockInitializer.initMocks(this);
    }

    @AfterEach
    void tearDown() {
        DynamicMockito.reset();
    }

    @Test
    @DisplayName("Динамика: stubbing, matchers, chaining")
    void shouldDemonstrateDynamicStubbingMatchersAndChaining() {
        when(mockedList.get(anyInt())).thenAnswer(args -> "idx-" + args[0]);
        when(mockedList.get(0)).thenReturn("first").thenReturn("second");
        when(mockedList.get(-1)).thenThrow(new IllegalArgumentException("bad index"));

        when(textService.normalize(contains("adm"))).thenReturn("admin");
        when(textService.normalize(eq("root"))).thenReturn("root");

        assertEquals("first", mockedList.get(0));
        assertEquals("second", mockedList.get(0));
        assertEquals("idx-5", mockedList.get(5));
        assertThrows(IllegalArgumentException.class, () -> mockedList.get(-1));

        assertEquals("admin", textService.normalize("super-admin"));
        assertEquals("root", textService.normalize("root"));
        assertNull(textService.normalize("guest"));
    }

    @Test
    @DisplayName("Динамика: verify и reset")
    void shouldDemonstrateDynamicVerificationAndReset() {
        mockedList.get(1);
        mockedList.get(2);
        mockedList.get(2);

        verify(mockedList).get(1);
        verify(mockedList, times(2)).get(2);
        verify(mockedList, atLeast(3)).get(anyInt());
        verify(mockedList, atMost(3)).get(anyInt());
        verify(mockedList, never()).get(99);

        DynamicMockito.reset();

        assertEquals(0, mockedList.size());
        verify(mockedList).size();
    }

    @Test
    @DisplayName("Динамика: мокинг обычного класса")
    void shouldDemonstrateDynamicClassMocking() {
        DemoCalculator calculator = DynamicMockito.mock(DemoCalculator.class);

        when(calculator.add(2, 3)).thenReturn(100);

        assertEquals(100, calculator.add(2, 3));
        assertEquals(0, calculator.add(1, 1));
        assertNull(calculator.message());
    }

    @Test
    @DisplayName("Статика: мок внутри DemoStaticApiService")
    void shouldDemonstrateStaticMockingThroughService() {
        DemoStaticApiService service = new DemoStaticApiService();

        try (MockedStatic<DemoStaticApi> mocked = Mockito.mockStatic(DemoStaticApi.class)) {
            mocked.when(DemoStaticApi::now)
                .thenReturn(100L)
                .thenReturn(200L)
                .thenReturn(300L);
            mocked.when(() -> DemoStaticApi.greet(Mockito.any())).thenReturn("hi-any");
            mocked.when(() -> DemoStaticApi.greet(Mockito.eq("admin"))).thenReturn("hi-admin");
            mocked.when(() -> DemoStaticApi.join(Mockito.any(), Mockito.any()))
                .thenAnswer(args -> args[0] + "|" + args[1]);
            mocked.when(() -> DemoStaticApi.unstable(Mockito.any()))
                .thenAnswer(args -> "ok-" + args[0]);
            mocked.when(() -> DemoStaticApi.unstable(Mockito.eq("boom")))
                .thenThrow(new IllegalStateException("boom"));

            assertEquals("hi-admin|100", service.processUser("admin"));
            assertEquals("hi-any|200", service.processUser("user"));
            assertEquals("ok-safe", service.safeProcess("safe"));
            assertEquals("fallback-boom", service.safeProcess("boom"));
            assertTrue(service.isFreshCall(3));
            assertFalse(service.isFreshCall(7));

            mocked.verify(DemoStaticApi::now, Mockito.times(4));
            mocked.verify(() -> DemoStaticApi.greet(Mockito.eq("admin")), Mockito.times(1));
            mocked.verify(() -> DemoStaticApi.greet(Mockito.any()), Mockito.times(2));
            mocked.verify(() -> DemoStaticApi.join(Mockito.any(), Mockito.any()), Mockito.times(2));
            mocked.verify(() -> DemoStaticApi.unstable(Mockito.eq("boom")), Mockito.times(1));
            mocked.verify(() -> DemoStaticApi.unstable(Mockito.any()), Mockito.times(2));
            mocked.verify(
                () -> {
                    DemoStaticApi.audit(Mockito.contains("Processed user"));
                    return null;
                },
                Mockito.times(2)
            );
            mocked.verify(
                () -> {
                    DemoStaticApi.audit(Mockito.contains("Success"));
                    return null;
                },
                Mockito.times(1)
            );
            mocked.verify(
                () -> {
                    DemoStaticApi.audit(Mockito.contains("Error"));
                    return null;
                },
                Mockito.times(1)
            );
            mocked.verify(() -> DemoStaticApi.join(Mockito.eq("unused"), Mockito.any()), Mockito.never());
        }
    }

    @Test
    @DisplayName("Совместно: динамические и статические моки")
    void shouldDemonstrateCombinedMocks() {
        DemoCalculator calculator = DynamicMockito.mock(DemoCalculator.class);
        when(calculator.add(1, 2)).thenReturn(99);

        try (MockedStatic<DemoStaticApi> mocked = Mockito.mockStatic(DemoStaticApi.class)) {
            mocked.when(DemoStaticApi::now).thenReturn(999L);

            assertEquals(99, calculator.add(1, 2));
            assertEquals(999L, DemoStaticApi.now());

            mocked.verify(DemoStaticApi::now, Mockito.times(1));
        }

        assertEquals(99, calculator.add(1, 2));
        assertNotEquals(999L, DemoStaticApi.now());
    }

    public interface DemoTextService {
        String normalize(String value);
    }

    public static class DemoCalculator {
        public int add(int left, int right) {
            return left + right;
        }

        public String message() {
            return "real";
        }
    }
}

final class DemoStaticApi {
    private DemoStaticApi() {
    }

    static long now() {
        return System.currentTimeMillis();
    }

    static String greet(String name) {
        return "hello-" + name;
    }

    static String join(String left, String right) {
        return left + ":" + right;
    }

    static void audit(String message) {
        // demo side effect placeholder
    }

    static String unstable(String input) {
        return "ok-" + input;
    }
}

final class DemoStaticApiService {

    String processUser(String name) {
        long timestamp = DemoStaticApi.now();
        String greeting = DemoStaticApi.greet(name);
        String result = DemoStaticApi.join(greeting, String.valueOf(timestamp));
        DemoStaticApi.audit("Processed user: " + name);
        return result;
    }

    String safeProcess(String input) {
        try {
            String value = DemoStaticApi.unstable(input);
            DemoStaticApi.audit("Success: " + input);
            return value;
        } catch (Exception e) {
            DemoStaticApi.audit("Error: " + input);
            return "fallback-" + input;
        }
    }

    boolean isFreshCall(long thresholdMillis) {
        long now = DemoStaticApi.now();
        return now % thresholdMillis == 0;
    }
}
