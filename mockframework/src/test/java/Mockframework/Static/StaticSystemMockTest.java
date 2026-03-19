package Mockframework.Static;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticSystemMockTest {
    @Test
    void shouldRejectMockingCoreJavaClass() {
        UnsupportedOperationException error = assertThrows(
            UnsupportedOperationException.class,
            () -> Mockito.mockStatic(System.class)
        );
        assertTrue(error.getMessage().contains("java.lang.System"));
    }

    @Test
    void shouldRejectSystemCurrentTimeMillisStubbing() {
        UnsupportedOperationException error = assertThrows(
            UnsupportedOperationException.class,
            () -> {
                try (MockedStatic<System> ignored = Mockito.mockStatic(System.class)) {
                    // no-op
                }
            }
        );
        assertTrue(error.getMessage().contains("core Java classes"));
    }
}
