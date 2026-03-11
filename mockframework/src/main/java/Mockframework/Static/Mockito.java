package Mockframework.Static;

import java.util.Objects;

import Mockframework.Static.staticmock.MockedStaticImpl;

public final class Mockito {
    private Mockito() {
    }

    public static <T> MockedStatic<T> mockStatic(Class<T> clazz) {
        return new MockedStaticImpl<>(Objects.requireNonNull(clazz, "clazz must not be null"));
    }
}

