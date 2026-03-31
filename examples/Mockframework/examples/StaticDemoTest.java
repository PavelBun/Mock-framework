package Mockframework.examples;

import Mockframework.Static.MockedStatic;
import Mockframework.Static.Mockito;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticDemoTest {
    @Test
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
            mocked.verify(
                () -> DemoStaticApi.join(Mockito.any(), Mockito.any()),
                Mockito.times(2)
            );
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
            mocked.verify(
                () -> DemoStaticApi.join(Mockito.eq("unused"), Mockito.any()),
                Mockito.never()
            );
        }
    }
}
