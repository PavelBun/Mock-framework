package Mockframework.Static;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    }
}
