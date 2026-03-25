package Mockframework.Static;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticVerificationTest {
    @Test
    void shouldVerifySingleCall() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("x");
            mocked.verify(() -> TestStaticMethods.value("x"));
        }
    }

    @Test
    void shouldVerifyWithTimes() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("x");
            TestStaticMethods.value("x");
            mocked.verify(() -> TestStaticMethods.value("x"), Mockito.times(2));
        }
    }

    @Test
    void shouldVerifyNever() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.verify(() -> TestStaticMethods.value("x"), Mockito.never());
        }
    }

    @Test
    void shouldVerifyAtLeast() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("x");
            TestStaticMethods.value("x");
            mocked.verify(() -> TestStaticMethods.value("x"), Mockito.atLeast(1));
            mocked.verify(() -> TestStaticMethods.value("x"), Mockito.atLeast(2));
        }
    }

    @Test
    void shouldVerifyAtMost() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("x");
            mocked.verify(() -> TestStaticMethods.value("x"), Mockito.atMost(1));
        }
    }

    @Test
    void shouldVerifyWithAnyMatcher() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("alpha");
            TestStaticMethods.value("beta");
            mocked.verify(() -> TestStaticMethods.value(Mockito.any()), Mockito.times(2));
        }
    }

    @Test
    void shouldVerifyWithEqMatcher() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("target");
            mocked.verify(() -> TestStaticMethods.value(Mockito.eq("target")));
        }
    }

    @Test
    void shouldVerifyWithContainsMatcher() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("super-admin");
            mocked.verify(() -> TestStaticMethods.value(Mockito.contains("adm")));
        }
    }

    @Test
    void shouldVerifyNoArgsCall() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.ping();
            mocked.verify(TestStaticMethods::ping);
        }
    }

    @Test
    void shouldNotCountCallsOutsideMockScope() {
        TestStaticMethods.value("outside");

        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.verify(() -> TestStaticMethods.value("outside"), Mockito.never());
        }
    }

    @Test
    void shouldClearHistoryOnClose() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("x");
            mocked.verify(() -> TestStaticMethods.value("x"));
        }

        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            mocked.verify(() -> TestStaticMethods.value("x"), Mockito.never());
        }
    }

    @Test
    void shouldFailWhenMatcherCountDoesNotMatchVerifyArgs() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> mocked.verify(() -> TestStaticMethods.pair(Mockito.any(), "raw"))
            );
            assertTrue(error.getMessage().contains("expected 2 matchers but got 1"));
        }
    }

    @Test
    void shouldNotLeakMatchersAfterInvalidNoArgVerify() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.ping();
            Mockito.any();
            assertThrows(
                IllegalStateException.class,
                () -> mocked.verify(TestStaticMethods::ping)
            );

            mocked.verify(TestStaticMethods::ping);
        }
    }

    @Test
    void shouldFailWhenWrongCallCount() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("x");
            assertThrows(
                AssertionError.class,
                () -> mocked.verify(() -> TestStaticMethods.value("x"), Mockito.times(2))
            );
        }
    }

    @Test
    void shouldFailWhenNeverButCalled() {
        try (MockedStatic<TestStaticMethods> mocked = Mockito.mockStatic(TestStaticMethods.class)) {
            TestStaticMethods.value("x");
            assertThrows(
                AssertionError.class,
                () -> mocked.verify(() -> TestStaticMethods.value("x"), Mockito.never())
            );
        }
    }

    static final class TestStaticMethods {
        private TestStaticMethods() {
        }

        static String value(String input) {
            return "real-" + input;
        }

        static String pair(String left, String right) {
            return "real-" + left + "-" + right;
        }

        static String ping() {
            return "pong";
        }
    }
}
