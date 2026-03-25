package Mockframework.examples;

import Mockframework.Static.MockedStatic;
import Mockframework.Static.Mockito;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StaticDemoTest {
    @Test
    void shouldDemonstrateStaticMocking() {
        try (MockedStatic<DemoStaticApi> mocked = Mockito.mockStatic(DemoStaticApi.class)) {
            mocked.when(DemoStaticApi::now).thenReturn(100L).thenReturn(200L);
            mocked.when(() -> DemoStaticApi.greet(Mockito.any())).thenReturn("hi-any");
            mocked.when(() -> DemoStaticApi.greet(Mockito.eq("admin"))).thenReturn("hi-admin");
            mocked.when(() -> DemoStaticApi.greet(Mockito.eq("answer")))
                    .thenAnswer(args -> "hi-" + args[0]);
            mocked.when(() -> DemoStaticApi.join(Mockito.contains("left"), Mockito.any()))
                    .thenReturn("joined");
            mocked.when(() -> DemoStaticApi.unstable(Mockito.contains("boom")))
                    .thenThrow(new IllegalStateException("boom"));

            assertEquals(100L, DemoStaticApi.now());
            assertEquals(200L, DemoStaticApi.now());
            assertEquals("hi-admin", DemoStaticApi.greet("admin"));
            assertEquals("hi-answer", DemoStaticApi.greet("answer"));
            assertEquals("hi-any", DemoStaticApi.greet("user"));
            assertEquals("joined", DemoStaticApi.join("left-part", "right"));
            assertThrows(IllegalStateException.class, () -> DemoStaticApi.unstable("boom"));

            DemoStaticApi.audit("audit-1");
            DemoStaticApi.audit("audit-2");

            mocked.verify(DemoStaticApi::now, Mockito.times(2));
            mocked.verify(() -> DemoStaticApi.greet(Mockito.eq("admin")), Mockito.times(1));
            mocked.verify(() -> DemoStaticApi.greet(Mockito.eq("answer")), Mockito.times(1));
            mocked.verify(() -> DemoStaticApi.greet(Mockito.any()), Mockito.atLeast(2));
            mocked.verify(
                    () -> DemoStaticApi.join(Mockito.contains("left"), Mockito.any()),
                    Mockito.atMost(1)
            );
            mocked.verify(() -> DemoStaticApi.unstable(Mockito.contains("boom")), Mockito.times(1));
            mocked.verify(
                    () -> {
                        DemoStaticApi.audit(Mockito.contains("audit"));
                        return null;
                    },
                    Mockito.times(2)
            );
            mocked.verify(
                    () -> DemoStaticApi.join(Mockito.eq("unused"), Mockito.any()),
                    Mockito.never()
            );
        }
    }
}
