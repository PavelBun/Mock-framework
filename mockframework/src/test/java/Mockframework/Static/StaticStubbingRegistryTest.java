package Mockframework.Static;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StaticStubbingRegistryTest {
    @Test
    void shouldSupportOverloadedStaticMethodsBySignature() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(TestStaticMethods::value).thenReturn("no-args");
            mocked.when(() -> TestStaticMethods.value("x")).thenReturn("with-arg");

            assertEquals("no-args", TestStaticMethods.value());
            assertEquals("with-arg", TestStaticMethods.value("x"));
        }
    }

    @Test
    void shouldSupportThenThrowForStaticMethod() {
        IllegalStateException expected = new IllegalStateException("boom");

        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(TestStaticMethods::number).thenThrow(expected);

            IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                TestStaticMethods::number
            );
            assertSame(expected, actual);
        }
    }

    @Test
    void shouldMatchStaticStubsByArguments() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value("x")).thenReturn("stub-x");

            assertEquals("stub-x", TestStaticMethods.value("x"));
            assertEquals("real-y", TestStaticMethods.value("y"));
        }
    }

    @Test
    void shouldSupportChainedThenReturnForStaticMethod() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(TestStaticMethods::number).thenReturn(10L).thenReturn(20L);

            assertEquals(10L, TestStaticMethods.number());
            assertEquals(20L, TestStaticMethods.number());
            assertEquals(20L, TestStaticMethods.number());
        }
    }

    @Test
    void shouldSupportChainedThenThrowForStaticMethod() {
        IllegalStateException first = new IllegalStateException("first");
        IllegalArgumentException second = new IllegalArgumentException("second");

        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(TestStaticMethods::number).thenThrow(first).thenThrow(second);

            assertSame(first, assertThrows(IllegalStateException.class, TestStaticMethods::number));
            assertSame(second, assertThrows(IllegalArgumentException.class, TestStaticMethods::number));
            assertSame(second, assertThrows(IllegalArgumentException.class, TestStaticMethods::number));
        }
    }

    @Test
    void shouldSupportChainedThenAnswerForStaticMethod() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value("x"))
                .thenAnswer(args -> "first-" + args[0])
                .thenAnswer(args -> "second-" + args[0]);

            assertEquals("first-x", TestStaticMethods.value("x"));
            assertEquals("second-x", TestStaticMethods.value("x"));
            assertEquals("second-x", TestStaticMethods.value("x"));
        }
    }

    @Test
    void shouldKeepSeparateChainsForDifferentStaticArguments() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value("x")).thenReturn("x1").thenReturn("x2");
            mocked.when(() -> TestStaticMethods.value("y")).thenReturn("y1").thenReturn("y2");

            assertEquals("x1", TestStaticMethods.value("x"));
            assertEquals("y1", TestStaticMethods.value("y"));
            assertEquals("x2", TestStaticMethods.value("x"));
            assertEquals("y2", TestStaticMethods.value("y"));
            assertEquals("x2", TestStaticMethods.value("x"));
            assertEquals("y2", TestStaticMethods.value("y"));
        }
    }

    @Test
    void shouldSupportAnyMatcherForStaticMethod() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value(Mockito.any())).thenReturn("any-match");

            assertEquals("any-match", TestStaticMethods.value("alpha"));
            assertEquals("any-match", TestStaticMethods.value("beta"));
        }
    }

    @Test
    void shouldSupportEqMatcherForStaticMethod() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value(Mockito.eq("admin"))).thenReturn("eq-match");

            assertEquals("eq-match", TestStaticMethods.value("admin"));
            assertEquals("real-user", TestStaticMethods.value("user"));
        }
    }

    @Test
    void shouldSupportContainsMatcherForStaticMethod() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value(Mockito.contains("adm"))).thenReturn("contains-match");

            assertEquals("contains-match", TestStaticMethods.value("super-admin"));
            assertEquals("real-guest", TestStaticMethods.value("guest"));
        }
    }

    @Test
    void shouldPreferExactStaticStubOverMatcherStub() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value(Mockito.any())).thenReturn("any");
            mocked.when(() -> TestStaticMethods.value("target")).thenReturn("exact");

            assertEquals("exact", TestStaticMethods.value("target"));
            assertEquals("any", TestStaticMethods.value("other"));
        }
    }

    @Test
    void shouldSupportEqNullMatcherForStaticMethod() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value(Mockito.eq(null))).thenReturn("null-match");

            assertEquals("null-match", TestStaticMethods.value(null));
            assertEquals("real-user", TestStaticMethods.value("user"));
        }
    }

    @Test
    void shouldClearMatcherStubAfterClose() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value(Mockito.contains("adm"))).thenReturn("contains-match");
            assertEquals("contains-match", TestStaticMethods.value("admin"));
        }

        assertEquals("real-admin", TestStaticMethods.value("admin"));
    }

    @Test
    void shouldKeepMatcherStaticStubInCurrentThreadOnly() throws InterruptedException {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.when(() -> TestStaticMethods.value(Mockito.contains("adm"))).thenReturn("contains-match");
            assertEquals("contains-match", TestStaticMethods.value("admin"));

            AtomicReference<String> fromOtherThread = new AtomicReference<>("contains-match");
            Thread thread = new Thread(() -> fromOtherThread.set(TestStaticMethods.value("admin")));
            thread.start();
            thread.join();

            assertNotEquals("contains-match", fromOtherThread.get());
            assertEquals("real-admin", fromOtherThread.get());
        }
    }

    @Test
    void shouldFailWhenMatcherCountDoesNotMatchStaticArguments() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> mocked.when(() -> TestStaticMethods.pair(Mockito.any(), "raw")).thenReturn("x")
            );
            assertTrue(error.getMessage().contains("expected 2 matchers but got 1"));
        }
    }

    static final class TestStaticMethods {
        private TestStaticMethods() {
        }

        static String value() {
            return "real-no-args";
        }

        static String value(String input) {
            return "real-" + input;
        }

        static long number() {
            return 1L;
        }

        static String pair(String left, String right) {
            return "real-" + left + "-" + right;
        }
    }
}
