package Mockframework.examples;

import Mockframework.Dynamic.api.DynamicMockito;
import Mockframework.Static.MockedStatic;
import Mockframework.Static.Mockito;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DemoUsageTest {
    @Test
    void shouldDemonstrateDynamicMocks() {
        BillingClient client = DynamicMockito.mock(BillingClient.class);
        DynamicMockito.when(client.normalize(DynamicMockito.contains("user")))
            .thenReturn("user-1");
        DynamicMockito.when(client.charge(DynamicMockito.eq("user-1"), DynamicMockito.anyInt()))
            .thenAnswer(args -> (Integer) args[1] * 2)
            .thenThrow(new IllegalStateException("limit"));
        DynamicMockito.when(client.status()).thenReturn("OK").thenReturn("DEGRADED");

        client.ping();

        DemoProcessor processor = new DemoProcessor(client);
        assertEquals("hello-user-1:10", processor.process("super-user", 5));
        assertThrows(IllegalStateException.class, () -> processor.process("super-user", 5));

        assertEquals("OK", client.status());
        assertEquals("DEGRADED", client.status());
        assertEquals("DEGRADED", client.status());

        DynamicMockito.verify(client).ping();
        DynamicMockito.verify(client, DynamicMockito.times(2))
            .normalize(DynamicMockito.contains("user"));
        DynamicMockito.verify(client, DynamicMockito.times(2))
            .charge(DynamicMockito.eq("user-1"), DynamicMockito.anyInt());
        DynamicMockito.verify(client, DynamicMockito.atLeast(2))
            .charge(DynamicMockito.eq("user-1"), DynamicMockito.anyInt());
        DynamicMockito.verify(client, DynamicMockito.atMost(2))
            .charge(DynamicMockito.eq("user-1"), DynamicMockito.anyInt());
        DynamicMockito.verify(client, DynamicMockito.never())
            .charge(DynamicMockito.eq("user-2"), DynamicMockito.anyInt());
        DynamicMockito.verify(client, DynamicMockito.times(3)).status();

        DynamicMockito.reset();
    }


}
