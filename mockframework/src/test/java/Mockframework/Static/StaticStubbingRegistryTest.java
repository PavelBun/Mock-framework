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

