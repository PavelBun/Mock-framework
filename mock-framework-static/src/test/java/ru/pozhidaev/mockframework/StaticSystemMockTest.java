package ru.pozhidaev.mockframework;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class StaticSystemMockTest {
    @Disabled("System.currentTimeMillis() is JVM intrinsic/native and not interceptable with current agent approach.")
    @Test
    void shouldReturnStubbedCurrentTimeMillis() {
        long fixedValue = Long.MIN_VALUE + 42;

        try (MockedStatic<System> mocked = Mockito.mockStatic(System.class)) {
            mocked.when(System::currentTimeMillis).thenReturn(fixedValue);

            long actual = System.currentTimeMillis();

            assertEquals(fixedValue, actual);
        }

        assertNotEquals(fixedValue, System.currentTimeMillis());
    }

    @Disabled("System.currentTimeMillis() is JVM intrinsic/native and not interceptable with current agent approach.")
    @Test
    void shouldKeepStaticMockInCurrentThreadOnly() throws InterruptedException {
        long fixedValue = Long.MIN_VALUE + 100;

        try (MockedStatic<System> mocked = Mockito.mockStatic(System.class)) {
            mocked.when(System::currentTimeMillis).thenReturn(fixedValue);

            assertEquals(fixedValue, System.currentTimeMillis());

            AtomicLong fromOtherThread = new AtomicLong(fixedValue);
            Thread thread = new Thread(() -> fromOtherThread.set(System.currentTimeMillis()));
            thread.start();
            thread.join();

            assertNotEquals(fixedValue, fromOtherThread.get());
        }
    }
}
