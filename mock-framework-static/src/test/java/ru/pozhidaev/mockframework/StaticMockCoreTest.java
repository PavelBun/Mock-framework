package ru.pozhidaev.mockframework;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class StaticMockCoreTest {
    @Test
    void shouldReturnStubbedStaticValueAndRestoreAfterClose() {
        long fixedValue = Long.MIN_VALUE + 7;

        try (MockedStatic<TestClock> mocked = Mockito.mockStatic(TestClock.class)) {
            mocked.when(TestClock::now).thenReturn(fixedValue);
            assertEquals(fixedValue, TestClock.now());
        }

        assertNotEquals(fixedValue, TestClock.now());
    }

    @Test
    void shouldKeepStaticMockInCurrentThreadOnly() throws InterruptedException {
        long fixedValue = Long.MIN_VALUE + 8;

        try (MockedStatic<TestClock> mocked = Mockito.mockStatic(TestClock.class)) {
            mocked.when(TestClock::now).thenReturn(fixedValue);
            assertEquals(fixedValue, TestClock.now());

            AtomicLong fromOtherThread = new AtomicLong(fixedValue);
            Thread thread = new Thread(() -> fromOtherThread.set(TestClock.now()));
            thread.start();
            thread.join();

            assertNotEquals(fixedValue, fromOtherThread.get());
        }
    }

    static final class TestClock {
        private TestClock() {
        }

        static long now() {
            return System.currentTimeMillis();
        }
    }
}

