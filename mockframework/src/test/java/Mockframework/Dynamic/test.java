package Mockframework.Dynamic;

import Mockframework.Dynamic.annotation.Mock;
import Mockframework.Dynamic.init.MockInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static Mockframework.Dynamic.api.DynamicMockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class test {
    @Mock
    private List list;

    @BeforeEach
    void setup() {
        MockInitializer.initMocks(this);
    }

    @Test
    void test() {
        when(list.get(0)).thenReturn("first").thenThrow(new RuntimeException());

        assertEquals("first", list.get(0));
        assertThrows(RuntimeException.class, () -> list.get(0));

        verify(list, times(2)).get(0);
    }
}
